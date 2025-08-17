package controller;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import java.io.IOException;
import java.io.PrintWriter;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

@WebServlet(name = "CheckUser", urlPatterns = {"/CheckUser"})
public class CheckUser extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        Gson gson = new Gson();
        
        JsonObject responseObject = new JsonObject();
        responseObject.addProperty("status", false);
        HttpSession ses = request.getSession();
        
        if (ses != null && ses.getAttribute("user") != null) {

            responseObject.addProperty("status", true);

            responseObject.addProperty("message", "User exists!");

        } else {
            responseObject.addProperty("message", "User does not exists!");
        }

        response.setContentType("application/json");
        response.getWriter().write(gson.toJson(responseObject));
    }

    public boolean isCookiePresent(HttpServletRequest request, String cookieName) {
        // Get all cookies from the request
        Cookie[] cookies = request.getCookies();

        // If no cookies, return false
        if (cookies == null) {
            return false;
        }

        // Loop through cookies to find the one with the given name
        for (Cookie cookie : cookies) {
            if (cookie.getName().equals(cookieName)) {
                return true; // Cookie exists
            }
        }

        return false; // Cookie not found
    }

}
