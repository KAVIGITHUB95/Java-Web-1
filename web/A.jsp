<%-- 
    Document   : A
    Created on : Aug 14, 2025, 11:51:09 PM
    Author     : Kavishka Jayawardana
--%>

<%@page contentType="text/html" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <title>JSP Page</title>
    </head>
    <body>
        <form action="Login" method="POST">
            <input name="email" type="text"/>
            <button type="submit">Submit</button>
        </form>
        
        <form action="Login" method="GET">
            <button type="submit">Get</button>
        </form>
    </body>
</html>
