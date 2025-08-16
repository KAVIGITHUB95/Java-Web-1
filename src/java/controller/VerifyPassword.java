package controller;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import hibernate.HibernateUtil;
import hibernate.User;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import model.Mail;
import model.Util;
import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Restrictions;

@WebServlet(name = "VerifyPassword", urlPatterns = {"/VerifyPassword"})
public class VerifyPassword extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        Gson gson = new Gson();
        JsonObject signIn = gson.fromJson(request.getReader(), JsonObject.class);

        JsonObject responseObject = new JsonObject();
        responseObject.addProperty("status", false);

        final String email = signIn.get("email").getAsString();

        String verificationCode = signIn.get("verificationCode").getAsString();
        String newPassword = signIn.get("newPassword").getAsString();
        String confirmPassword = signIn.get("confirmPassword").getAsString();

        System.out.println(email);
        System.out.println(newPassword);
        System.out.println(confirmPassword);

        if (email.isEmpty()) {
            responseObject.addProperty("message", "Email cannot be empty!");
        } else if (verificationCode.isEmpty()) {
            responseObject.addProperty("message", "Verification code cannot be empty!");
        } else if (newPassword.isEmpty() || confirmPassword.isEmpty()) {
            responseObject.addProperty("message", "Password fields cannot be empty!");
        } else if (!newPassword.isEmpty() && !Util.isPasswordValid(newPassword)) {
            responseObject.addProperty("message", "The password must contains at least uppercase, lowercase,"
                    + " number, special character and to be minimum eight characters long!");
        } else if (!confirmPassword.isEmpty() && !Util.isPasswordValid(confirmPassword)) {
            responseObject.addProperty("message", "The password must contains at least uppercase, lowercase,"
                    + " number, special character and to be minimum eight characters long!");
        } else if (!confirmPassword.equals(newPassword)) {
            responseObject.addProperty("message", "Confirmed password does not match with the new entered password");
        } else {
            SessionFactory sf = HibernateUtil.getSessionFactory();

            Session s = sf.openSession();

            Criteria c = s.createCriteria(User.class);

            Criterion crt1 = Restrictions.eq("email", email);
            Criterion crt2 = Restrictions.eq("verification", verificationCode);
            c.add(crt1);
            c.add(crt2);

            if (c.list().isEmpty()) {
                responseObject.addProperty("message", "Invalid credentails!");
            } else {

                User u = (User) c.list().get(0);
                HttpSession ses = request.getSession();
                if (!u.getVerification().equals("Verified")) { //not verfied
                    ses.setAttribute("email", email);

                    if (!confirmPassword.isEmpty()) {
                        u.setPassword(confirmPassword);
                    } else {
                        u.setPassword(newPassword);
                    }

                    u.setVerification("Verified");

                    ses.setAttribute("user", u);

                    s.merge(u);
                    s.beginTransaction().commit();

                    responseObject.addProperty("status", true);
                    responseObject.addProperty("message", "Password changed successfully!");

                }

            }

            s.close();
        }

        String responseText = gson.toJson(responseObject);
        response.setContentType("application/json");
        response.getWriter().write(responseText);
    }

}
