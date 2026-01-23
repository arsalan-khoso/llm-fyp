# How to Prevent Render API from Going to Sleep (Free Tier)

Render's Free Tier spins down web services after **15 minutes of inactivity**. When a new request comes in, it takes about **30–50 seconds** to "wake up" (Cold Start), which delays your app.

## Solution 1: Use a Free Uptime Monitor (Recommended)
You can use a free service to "ping" your API every 10 minutes to keep it active.

1.  **Go to [UptimeRobot.com](https://uptimerobot.com/)** and create a free account.
2.  Click **"Add New Monitor"**.
3.  **Monitor Type**: Select `HTTP(s)`.
4.  **Friendly Name**: `LLM FYP Backend`.
5.  **URL (or IP)**: `https://llm-fyp.onrender.com/`
6.  **Monitoring Interval**: Set to `5 minutes` or `10 minutes` (anything less than 15).
7.  **Create Monitor**.

**Result:** UptimeRobot will visit your site every few minutes, fooling Render into thinking it's being used, so it won't go to sleep.

## Solution 2: Upgrade Render Plan
If you want guaranteed performance without "hacks", upgrading to the **Starter Plan ($7/month)** eliminates this behavior entirely.

## Why is it shutting down in the logs?
Your logs show:
```
INFO: Shutting down
INFO: Waiting for application shutdown.
```
This confirms Render is automatically stopping the service because no requests were received for 15 minutes.
