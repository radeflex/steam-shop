<!DOCTYPE html>
<html lang="ru">
<head>
    <meta charset="UTF-8">
    <title>steamshop812</title>
    <style>
        body {
            font-family: Arial, sans-serif;
            background-color: #f4f4f4;
            margin: 0;
            padding: 0;
        }
        .container {
            max-width: 600px;
            margin: 50px auto;
            background-color: #ffffff;
            padding: 30px;
            border-radius: 10px;
            box-shadow: 0 2px 10px rgba(0,0,0,0.1);
        }
        .btn {
            display: inline-block;
            padding: 12px 25px;
            font-size: 16px;
            color: #ffffff;
            background-color: #007BFF;
            text-decoration: none;
            border-radius: 5px;
            margin-top: 20px;
        }
        .btn:hover {
            background-color: #0056b3;
        }
        .footer {
            margin-top: 30px;
            font-size: 12px;
            color: #777777;
        }
    </style>
</head>
<body>
<div class="container">
    <h2>Привет, ${username}!</h2>
    <p>Спасибо за регистрацию на нашем сервисе.</p>
    <p>Для подтверждения вашего email, пожалуйста, нажмите на кнопку ниже:</p>
    <a class="btn" href="http://localhost:3000/confirm-email?token=${token}">Подтвердить Email</a>
    <p class="footer">Если вы не регистрировались на нашем сервисе, просто проигнорируйте это письмо.</p>
</div>
</body>
</html>
