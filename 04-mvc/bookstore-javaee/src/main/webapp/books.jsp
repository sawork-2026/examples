<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<!DOCTYPE html>
<html>
<head>
    <title>在线书店</title>
    <style>
        table { border-collapse: collapse; width: 80%; margin: 20px auto; }
        th, td { border: 1px solid #ddd; padding: 8px; text-align: left; }
        th { background-color: #4CAF50; color: white; }
        h1 { text-align: center; }
    </style>
</head>
<body>
    <h1>在线书店 - 图书列表</h1>
    <table>
        <thead>
            <tr>
                <th>书名</th>
                <th>作者</th>
                <th>价格</th>
                <th>ISBN</th>
            </tr>
        </thead>
        <tbody>
            <c:forEach var="book" items="${books}">
                <tr>
                    <td>${book.title}</td>
                    <td>${book.author}</td>
                    <td>¥${book.price}</td>
                    <td>${book.isbn}</td>
                </tr>
            </c:forEach>
        </tbody>
    </table>
</body>
</html>
