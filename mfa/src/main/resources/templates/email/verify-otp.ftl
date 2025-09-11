<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <title>OTP Verification</title>
    <style>
        body {
            margin: 0;
            padding: 0;
            font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif;
            background-color: #f4f6f8;
        }
        .container {
            max-width: 600px;
            margin: 40px auto;
            background: #ffffff;
            border-radius: 12px;
            box-shadow: 0 4px 20px rgba(0,0,0,0.08);
            overflow: hidden;
        }
        .header {
            background: linear-gradient(135deg, #007bff, #00c6ff);
            color: white;
            text-align: center;
            padding: 25px;
        }
        .header h1 {
            margin: 0;
            font-size: 22px;
        }
        .content {
            padding: 30px 25px;
            text-align: center;
            color: #333;
        }
        .otp-box {
            font-size: 28px;
            font-weight: bold;
            color: #007bff;
            background: #f1f6ff;
            border: 2px dashed #007bff;
            border-radius: 8px;
            padding: 15px;
            margin: 25px 0;
            display: inline-block;
            letter-spacing: 5px;
        }
        .note {
            font-size: 14px;
            color: #666;
            margin-top: 10px;
        }
        .footer {
            background: #f9f9f9;
            text-align: center;
            padding: 20px;
            font-size: 12px;
            color: #999;
            border-top: 1px solid #eee;
        }
        .footer a {
            color: #007bff;
            text-decoration: none;
        }
    </style>
</head>
<body>
<div class="container">
    <!-- Header -->
    <div class="header">
        <h1>🔐 OTP Verification</h1>
    </div>

    <!-- Content -->
    <div class="content">
        <p>Hello <b>${userName}</b>,</p>
        <p>Use the following OTP to complete your verification process:</p>

        <!-- OTP -->
        <div class="otp-box">
            ${otp}
        </div>

        <p class="note">⚠️ This OTP is valid for <b>5 minutes</b>. Do not share it with anyone.</p>
    </div>

    <!-- Footer -->
    <div class="footer">
        <p>If you didn’t request this, please ignore this email.</p>
        <p>Powered by <a href="https://your-company.com">Your Company</a></p>
    </div>
</div>
</body>
</html>
