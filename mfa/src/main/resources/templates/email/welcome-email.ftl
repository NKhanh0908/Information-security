<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>Welcome Email</title>
    <style>
        body {
            font-family: "Segoe UI", Arial, sans-serif;
            background-color: #f3f4f6;
            margin: 0;
            padding: 30px;
        }
        .container {
            background: #ffffff;
            border-radius: 14px;
            max-width: 650px;
            margin: auto;
            overflow: hidden;
            box-shadow: 0 6px 18px rgba(0,0,0,0.08);
        }
        .header {
            background: linear-gradient(135deg, #2563eb, #1d4ed8);
            color: #ffffff;
            padding: 25px;
            text-align: center;
        }
        .header h1 {
            margin: 0;
            font-size: 28px;
            font-weight: 600;
        }
        .content {
            padding: 30px;
            color: #374151;
        }
        .content h2 {
            margin-top: 0;
            font-size: 24px;
            color: #111827;
        }
        .device-box {
            background: #f9fafb;
            border: 1px solid #e5e7eb;
            border-radius: 10px;
            padding: 20px;
            margin: 25px 0;
            text-align: center;
        }
        .device-box p {
            margin: 6px 0;
            font-size: 16px;
        }
        .highlight {
            font-weight: bold;
            color: #2563eb;
        }
        .footer {
            background: #f9fafb;
            color: #6b7280;
            font-size: 13px;
            text-align: center;
            padding: 20px;
        }
    </style>
</head>
<body>
<div class="container">

    <!-- Header -->
    <div class="header">
        <h1>🎉 Chào mừng ${userName}!</h1>
    </div>

    <!-- Content -->
    <div class="content">
        <h2>Xin chúc mừng 🎊</h2>
        <p>Bạn đã xác thực thành công và đăng nhập vào hệ thống <b>MFA Security</b>.</p>

        <div class="device-box">
            <p>✅ Thiết bị tin cậy đã được thêm:</p>
            <p class="highlight">📱 ${deviceInfo}</p>
        </div>

        <p>⏰ Thời gian xác thực: <b>${currentDateTime}</b></p>

        <p>Cảm ơn bạn đã tin tưởng sử dụng hệ thống của chúng tôi.
            Từ nay, thiết bị này sẽ được ghi nhớ để bạn đăng nhập <b>nhanh hơn</b> và <b>an toàn hơn</b>.</p>
    </div>

    <!-- Footer -->
    <div class="footer">
        &copy; 2025 MFA Security System · All rights reserved.
    </div>

</div>
</body>
</html>
