# OpenAI API Key Setup

## ✅ API Key Configured

Your OpenAI API key has been set up in the backend. The key is stored in `backend/.env` file.

## 🔒 Security Important Notes

⚠️ **CRITICAL SECURITY WARNINGS:**

1. **Never commit `.env` file to Git**
   - The `.env` file is already in `.gitignore`
   - Never share your API key publicly
   - If you accidentally commit it, regenerate the key immediately

2. **API Key Location:**
   - Stored in: `backend/.env`
   - This file is git-ignored for security

3. **If Key is Compromised:**
   - Go to: https://platform.openai.com/api-keys
   - Delete the compromised key
   - Create a new one
   - Update `backend/.env` with the new key

## 🚀 How to Use

The backend will automatically load the API key from the `.env` file when you run:

```bash
cd backend
python main.py
```

## 🔄 Alternative: Environment Variable

You can also set it as an environment variable:

**Windows (PowerShell):**
```powershell
$env:OPENAI_API_KEY="sk-proj-..."
```

**Windows (CMD):**
```cmd
set OPENAI_API_KEY=sk-proj-...
```

**Mac/Linux:**
```bash
export OPENAI_API_KEY="sk-proj-..."
```

## ✅ Verification

When you start the backend, you should see:
```
✅ OpenAI API client initialized successfully
```

If you see an error, check:
1. The `.env` file exists in `backend/` directory
2. The API key is correct
3. You have internet connection
4. Your OpenAI account has credits

## 📝 Testing

To test if the API key works:

1. Start the backend:
   ```bash
   cd backend
   python main.py
   ```

2. The server should start without errors

3. Test the chat endpoint from the Android app

## 💰 API Usage

- OpenAI charges per token used
- Monitor usage at: https://platform.openai.com/usage
- Set usage limits to avoid unexpected charges

## 🔐 Best Practices

1. ✅ Use `.env` file (already done)
2. ✅ Keep `.env` in `.gitignore` (already done)
3. ✅ Don't share API key
4. ✅ Rotate keys periodically
5. ✅ Set usage limits in OpenAI dashboard
6. ✅ Monitor API usage regularly

