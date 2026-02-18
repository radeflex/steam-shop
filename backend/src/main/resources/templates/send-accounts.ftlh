<!DOCTYPE html>
<html lang="ru">
<head>
    <meta charset="UTF-8">
    <title>Ваши товары</title>
    <style>
        body {
            font-family: Arial, sans-serif;
            color: #333;
        }
        h2 {
            margin-top: 30px;
        }
        table {
            width: 100%;
            border-collapse: collapse;
            margin-bottom: 25px;
        }
        th, td {
            border: 1px solid #ccc;
            padding: 8px 10px;
            text-align: left;
        }
        th {
            background-color: #f5f5f5;
        }
    </style>
</head>
<body>

<p>Здравствуйте, <strong>${username}</strong>!</p>
<p>Ниже представлены товары, выданные вам:</p>

<#-- Перебор товаров -->
<#list accounts?keys as productName>
    <h2>${productName}</h2>

    <table>
        <thead>
        <tr>
            <th>Логин</th>
            <th>Пароль</th>
            <th>Email</th>
            <th>Пароль от Email</th>
        </tr>
        </thead>
        <tbody>
        <#list accounts[productName] as account>
            <tr>
                <td>${account.username}</td>
                <td>${account.password}</td>
                <td>${account.email}</td>
                <td>${account.emailPassword}</td>
            </tr>
        </#list>
        </tbody>
    </table>
</#list>

<p>Если возникнут вопросы — просто ответьте на это письмо.</p>

</body>
</html>
