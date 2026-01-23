# UoS Sindh Bot Backend API

FastAPI backend that integrates with OpenAI to provide UoS-specific information.

## Setup

1. Install dependencies:
```bash
pip install -r requirements.txt
```

2. Set your OpenAI API key:

**Option 1: Use .env file (Recommended)**
- Copy `.env.example` to `.env`:
  ```bash
  cp .env.example .env
  ```
- Edit `.env` and add your API key:
  ```
  OPENAI_API_KEY=your-api-key-here
  ```

**Option 2: Environment Variable**
```bash
export OPENAI_API_KEY="your-api-key-here"
```

3. Run the server:
```bash
python main.py
```

The server will automatically load the API key from `.env` file.

Or using uvicorn:
```bash
uvicorn main:app --reload --host 0.0.0.0 --port 8000
```

## API Endpoints

### POST /api/ask
Ask a question about UoS admissions or fees.

Request body:
```json
{
  "question": "What are the admission requirements?",
  "language": "en"
}
```

Response:
```json
{
  "answer": "Minimum 45% marks in intermediate...",
  "source": "www.usindh.edu.pk",
  "language": "en",
  "success": true
}
```

## Notes

- The API restricts responses to UoS website content only
- Supports English, Urdu, and Sindhi languages
- For Android emulator, use `http://10.0.2.2:8000/`
- For real device, use your computer's IP address

