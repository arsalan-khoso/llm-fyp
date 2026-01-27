from fastapi import FastAPI, HTTPException, Depends, status
from fastapi.middleware.cors import CORSMiddleware
import uvicorn
from fastapi.security import HTTPBearer, HTTPAuthorizationCredentials
from pydantic import BaseModel, EmailStr
from openai import OpenAI
import os
from typing import Optional
import json
import hashlib
import secrets
from datetime import datetime, timedelta
import jwt
from dotenv import load_dotenv
from pathlib import Path

# Load environment variables
# For production (Render), env vars are set in the dashboard, so we don't strictly need .env
# but we keeping this for local dev consistency.
env_path = Path(__file__).parent / '.env'
load_dotenv(dotenv_path=env_path, override=True)

# Also try loading from current directory as fallback
if not os.getenv("OPENAI_API_KEY"):
    load_dotenv(override=True)

app = FastAPI(title="UoS Sindh Bot API")

# Simple in-memory database (replace with real database in production)
# WARNING: DATA WILL BE LOST ON RESTART
users_db = {}
tokens_db = {}

# JWT Secret Key
# In production, this MUST be set via environment variable
JWT_SECRET = os.getenv("JWT_SECRET", "your-secret-key-change-in-production")
JWT_ALGORITHM = "HS256"
JWT_EXPIRATION_HOURS = 24

security = HTTPBearer()

# CORS middleware to allow Android app to connect
app.add_middleware(
    CORSMiddleware,
    # Allow all origins for testing/production simplified setup
    # You might want to restrict this to your specific domain in a strict production env
    allow_origins=["*"], 
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

# OpenAI API Key
OPENAI_API_KEY = os.getenv("OPENAI_API_KEY")
if not OPENAI_API_KEY:
    # We print a warning instead of raising error immediately to allow app to start
    # but actual AI features will fail.
    print("WARNING: OPENAI_API_KEY not found! Set it in environment variables.")

client = None
if OPENAI_API_KEY:
    client = OpenAI(api_key=OPENAI_API_KEY)
    print("✅ OpenAI API client initialized successfully")

# ... rest of the code ...
# For brevity, I am not rewriting the entire content, 
# just noting that the user needs to ensure requirements.txt is up to date 
# and Procfile is created.

# Load FAQs from JSON file
def load_faqs_knowledge_base():
    try:
        faq_path = Path(__file__).parent / 'faqs.json'
        if faq_path.exists():
            with open(faq_path, 'r', encoding='utf-8') as f:
                data = json.load(f)
                return data.get('faqs', [])
    except Exception as e:
        print(f"Error loading FAQs: {e}")
    return []

# Formatted FAQ text
faqs_list = load_faqs_knowledge_base()
formatted_faqs = "\n\nFREQUENTLY ASKED QUESTIONS AND ANSWERS:\nThis section contains specific questions and their official answers. Use these EXACT answers if the user asks a matching question.\n"
for item in faqs_list:
    formatted_faqs += f"\nQ: {item.get('question')}\nA: {item.get('answer')}\n"

UOS_KNOWLEDGE_BASE = """
University of Sindh (UoS) - Comprehensive Information:

ABOUT UNIVERSITY OF SINDH:
- Established in 1947, University of Sindh is one of the oldest universities in Pakistan
- Located in Jamshoro, Sindh, Pakistan
- Public sector university offering quality education
- Multiple campuses: Jamshoro (Main), Hyderabad, Mirpurkhas

ADMISSION REQUIREMENTS:
- Minimum 45% marks in intermediate/HSC for admission to most programs
- Entry test is required for most undergraduate and graduate programs
- Original documents required: Matric certificate, Intermediate certificate, CNIC, Domicile certificate, Passport size photos
- Admission forms available online at www.usindh.edu.pk
- Admission deadline: Usually in August-September each year for fall semester
- Merit-based admission system
- Some programs may have specific subject requirements

FEE STRUCTURE (Approximate):
- Admission Fee: PKR 5,000 (one-time, non-refundable)
- Tuition Fee: PKR 15,000 - 25,000 per semester (varies by program and faculty)
- Examination Fee: PKR 2,000 per semester
- Library Fee: PKR 1,000 per semester
- Sports Fee: PKR 500 per semester
- Student Activity Fee: PKR 500 per semester
- Total per semester: Approximately PKR 20,000 - 30,000 depending on program
- Hostel fee (if applicable): PKR 3,000 - 5,000 per month
- Fees may vary by program and are subject to change

PROGRAMS OFFERED:
- Bachelor's Degree Programs: B.A, B.Sc, B.Com, B.Eng, BBA, BS in various disciplines
- Master's Degree Programs: M.A, M.Sc, M.Com, MBA, MS in various fields
- PhD Programs: Available in multiple disciplines
- Professional Programs: Law, Education, Social Work, etc.
- Faculties include: Arts, Science, Commerce, Engineering, Education, Law, Islamic Studies, Social Sciences

ACADEMIC CALENDAR:
- Fall Semester: Usually starts in September/October
- Spring Semester: Usually starts in February/March
- Summer Semester: Optional, for some programs
- Exams: Usually held at the end of each semester

CAMPUS LOCATIONS:
- Main Campus: Jamshoro (Allama I.I. Kazi Campus)
- Hyderabad Campus: Located in Hyderabad city
- Mirpurkhas Campus: Located in Mirpurkhas district
- Each campus offers different programs

FACILITIES:
- Central Library with extensive collection
- Computer Labs with modern equipment
- Sports facilities including gymnasium and sports grounds
- Hostels for male and female students
- Cafeteria and dining facilities
- Medical facilities
- Transportation services

CONTACT INFORMATION:
- Official Website: www.usindh.edu.pk
- Email: info@usindh.edu.pk
- Phone: +92-22-9213161-67
- Address: Allama I.I. Kazi Campus, University of Sindh, Jamshoro, Sindh, Pakistan
- Postal Code: 76080

STUDENT SERVICES:
- Student Affairs Office
- Career Counseling Center
- Financial Aid Office
- International Students Office
- Alumni Association

IMPORTANT NOTES:
- All fees and requirements are subject to change
- For most accurate and up-to-date information, always check the official website
- Admission process and requirements may vary by program
- Scholarships and financial aid may be available for deserving students
""" + formatted_faqs

# Authentication Models
class LoginRequest(BaseModel):
    email: EmailStr
    password: str

class SignupRequest(BaseModel):
    full_name: str
    email: EmailStr
    password: str

class ForgotPasswordRequest(BaseModel):
    email: EmailStr

class ResetPasswordRequest(BaseModel):
    email: EmailStr
    reset_token: str
    new_password: str

class AuthResponse(BaseModel):
    success: bool
    message: str
    token: Optional[str] = None
    user: Optional[dict] = None

# Chat Models
class QuestionRequest(BaseModel):
    question: str
    language: str = "en"

class QuestionResponse(BaseModel):
    answer: str
    source: Optional[str] = None
    language: str = "en"
    success: bool = True
    error: Optional[str] = None

# Helper Functions
def hash_password(password: str) -> str:
    """Hash password using SHA-256 (use bcrypt in production)"""
    return hashlib.sha256(password.encode()).hexdigest()

def verify_password(password: str, hashed: str) -> bool:
    """Verify password"""
    return hash_password(password) == hashed

def create_token(user_id: str, email: str) -> str:
    """Create JWT token"""
    expiration = datetime.utcnow() + timedelta(hours=JWT_EXPIRATION_HOURS)
    payload = {
        "user_id": user_id,
        "email": email,
        "exp": expiration
    }
    return jwt.encode(payload, JWT_SECRET, algorithm=JWT_ALGORITHM)

def verify_token(token: str) -> Optional[dict]:
    """Verify JWT token"""
    try:
        payload = jwt.decode(token, JWT_SECRET, algorithms=[JWT_ALGORITHM])
        return payload
    except jwt.ExpiredSignatureError:
        return None
    except jwt.InvalidTokenError:
        return None

def translate_text(text: str, target_lang: str) -> str:
    """Translate text using OpenAI"""
    if target_lang == "en":
        return text
    
    if not client:
        return text # Fallback if no client

    try:
        response = client.chat.completions.create(
            model="gpt-3.5-turbo",
            messages=[
                {"role": "system", "content": f"Translate the following text to {target_lang}. Return only the translation, no explanations."},
                {"role": "user", "content": text}
            ],
            max_tokens=500,
            temperature=0.3
        )
        return response.choices[0].message.content.strip()
    except Exception as e:
        print(f"Translation error: {e}")
        return text

def get_uos_answer(question: str, user_language: str) -> dict:
    """Get answer from OpenAI with UoS context restriction"""
    
    if not client:
        return {
            "answer": "OpenAI API Key is missing. Please configure the backend environment.",
            "source": None,
            "language": user_language,
            "success": False,
            "error": "Missing API Key"
        }

    # System prompt that provides helpful answers about UoS
    system_prompt = f"""You are a helpful and knowledgeable assistant for University of Sindh (UoS). Your role is to provide accurate, helpful information about the University of Sindh including admissions, fees, programs, facilities, and general university information.

KNOWLEDGE BASE ABOUT UNIVERSITY OF SINDH:
{UOS_KNOWLEDGE_BASE}

INSTRUCTIONS:
1. Answer questions about University of Sindh using the knowledge base provided above
2. Be helpful, friendly, and provide detailed answers when possible
3. If the question is about UoS but specific information is not in the knowledge base, provide a general helpful answer based on common university practices, but mention that for specific details, they should contact the university
4. If the question is completely unrelated to University of Sindh, politely redirect to UoS-related topics
5. Always be accurate and honest - if you don't know something specific, say so
6. Include relevant contact information (email, website, phone) when helpful
7. Format your answers clearly with proper structure
8. For fee-related questions, mention that fees may vary and are subject to change
9. For admission questions, mention that requirements may vary by program

IMPORTANT: 
- Provide actual helpful answers, not just "visit the website"
- Use the knowledge base to give specific information
- Be conversational and helpful
- Only suggest visiting the website if the question requires very specific or up-to-date information not in the knowledge base"""
    
    try:
        # Translate question to English if needed
        question_english = question
        if user_language != "en":
            question_english = translate_text(question, "en")
        
        # Get answer from OpenAI
        response = client.chat.completions.create(
            model="gpt-3.5-turbo",
            messages=[
                {"role": "system", "content": system_prompt},
                {"role": "user", "content": question_english}
            ],
            max_tokens=800,
            temperature=0.7
        )
        
        answer_english = response.choices[0].message.content.strip()
        
        # Translate answer back to user's language if needed
        if user_language != "en":
            answer = translate_text(answer_english, user_language)
        else:
            answer = answer_english
        
        # Extract source if mentioned
        source = "www.usindh.edu.pk" if "usindh" in answer.lower() else None
        
        return {
            "answer": answer,
            "source": source,
            "language": user_language,
            "success": True
        }
        
    except Exception as e:
        return {
            "answer": "",
            "source": None,
            "language": user_language,
            "success": False,
            "error": str(e)
        }

@app.get("/")
def read_root():
    return {"message": "UoS Sindh Bot API is running", "status": "active"}

# Authentication Endpoints
@app.post("/api/auth/signup", response_model=AuthResponse)
def signup(request: SignupRequest):
    """User registration"""
    try:
        # Check if user already exists
        if request.email in users_db:
            raise HTTPException(
                status_code=status.HTTP_400_BAD_REQUEST,
                detail="Email already registered"
            )
        
        # Validate password
        if len(request.password) < 6:
            raise HTTPException(
                status_code=status.HTTP_400_BAD_REQUEST,
                detail="Password must be at least 6 characters"
            )
        
        # Create user
        user_id = secrets.token_urlsafe(16)
        hashed_password = hash_password(request.password)
        
        users_db[request.email] = {
            "user_id": user_id,
            "full_name": request.full_name,
            "email": request.email,
            "password": hashed_password,
            "created_at": datetime.utcnow().isoformat()
        }
        
        # Create token
        token = create_token(user_id, request.email)
        tokens_db[token] = {"user_id": user_id, "email": request.email}
        
        return AuthResponse(
            success=True,
            message="Account created successfully",
            token=token,
            user={
                "user_id": user_id,
                "full_name": request.full_name,
                "email": request.email
            }
        )
    except HTTPException:
        raise
    except Exception as e:
        raise HTTPException(
            status_code=status.HTTP_500_INTERNAL_SERVER_ERROR,
            detail=str(e)
        )

@app.post("/api/auth/login", response_model=AuthResponse)
def login(request: LoginRequest):
    """User login"""
    try:
        # Check if user exists
        if request.email not in users_db:
            raise HTTPException(
                status_code=status.HTTP_401_UNAUTHORIZED,
                detail="Invalid email or password"
            )
        
        user = users_db[request.email]
        
        # Verify password
        if not verify_password(request.password, user["password"]):
            raise HTTPException(
                status_code=status.HTTP_401_UNAUTHORIZED,
                detail="Invalid email or password"
            )
        
        # Create token
        token = create_token(user["user_id"], request.email)
        tokens_db[token] = {"user_id": user["user_id"], "email": request.email}
        
        return AuthResponse(
            success=True,
            message="Login successful",
            token=token,
            user={
                "user_id": user["user_id"],
                "full_name": user["full_name"],
                "email": user["email"]
            }
        )
    except HTTPException:
        raise
    except Exception as e:
        raise HTTPException(
            status_code=status.HTTP_500_INTERNAL_SERVER_ERROR,
            detail=str(e)
        )

@app.post("/api/auth/forgot-password", response_model=AuthResponse)
def forgot_password(request: ForgotPasswordRequest):
    """Request password reset"""
    try:
        # Check if user exists
        if request.email not in users_db:
            # Don't reveal if email exists for security
            return AuthResponse(
                success=True,
                message="If the email exists, a reset link has been sent"
            )
        
        # Generate reset token (in production, send via email)
        reset_token = secrets.token_urlsafe(32)
        # Store reset token (in production, store in database with expiration)
        
        return AuthResponse(
            success=True,
            message=f"Reset token generated: {reset_token} (In production, this would be sent via email)"
        )
    except Exception as e:
        raise HTTPException(
            status_code=status.HTTP_500_INTERNAL_SERVER_ERROR,
            detail=str(e)
        )

@app.post("/api/auth/reset-password", response_model=AuthResponse)
def reset_password(request: ResetPasswordRequest):
    """Reset password with token"""
    try:
        # Check if user exists
        if request.email not in users_db:
            raise HTTPException(
                status_code=status.HTTP_404_NOT_FOUND,
                detail="User not found"
            )
        
        # Validate password
        if len(request.new_password) < 6:
            raise HTTPException(
                status_code=status.HTTP_400_BAD_REQUEST,
                detail="Password must be at least 6 characters"
            )
        
        # In production, verify reset token from database
        # For now, accept any token if email matches
        
        # Update password
        user = users_db[request.email]
        user["password"] = hash_password(request.new_password)
        
        return AuthResponse(
            success=True,
            message="Password reset successfully"
        )
    except HTTPException:
        raise
    except Exception as e:
        raise HTTPException(
            status_code=status.HTTP_500_INTERNAL_SERVER_ERROR,
            detail=str(e)
        )

@app.get("/api/auth/verify")
def verify_auth(credentials: HTTPAuthorizationCredentials = Depends(security)):
    """Verify authentication token"""
    token = credentials.credentials
    payload = verify_token(token)
    
    if not payload:
        raise HTTPException(
            status_code=status.HTTP_401_UNAUTHORIZED,
            detail="Invalid or expired token"
        )
    
    return {"success": True, "user_id": payload["user_id"], "email": payload["email"]}

@app.post("/api/ask", response_model=QuestionResponse)
def ask_question(request: QuestionRequest):
    """Handle question from Android app"""
    try:
        result = get_uos_answer(request.question, request.language)
        return QuestionResponse(**result)
    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))

if __name__ == "__main__":
    import uvicorn
    import sys
    
    # Check if port 8000 is available, if not use 8001
    port = 8000
    try:
        import socket
        sock = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
        sock.settimeout(1)
        result = sock.connect_ex(('localhost', port))
        sock.close()
        if result == 0:
            print(f"⚠️  Port {port} is in use, trying port 8001...")
            port = 8001
    except:
        pass
    
    print(f"🚀 Starting server on port {port}...")
    uvicorn.run(app, host="0.0.0.0", port=port)
