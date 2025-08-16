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
import org.hibernate.criterion.Restrictions;

@WebServlet(name = "ResendCode", urlPatterns = {"/ResendCode"})
public class ResendCode extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        Gson gson = new Gson();
        JsonObject responseObject = new JsonObject();

        responseObject.addProperty("status", false);
        HttpSession ses = request.getSession();
        if (ses.getAttribute("email") == null) {
            System.out.println(ses.getAttribute("email") == null);

            responseObject.addProperty("message", "Please sign in!");
        } else {

            final String email = ses.getAttribute("email").toString();

            SessionFactory sf = HibernateUtil.getSessionFactory();
            Session s = sf.openSession();

            Criteria c = s.createCriteria(User.class);
            c.add(Restrictions.eq("email", email));

            if (c.list().isEmpty()) {
                responseObject.addProperty("message", "2");
            } else {
                //generate verification code
//                final String verificationCode = Util.generateCode();

                List<User> userList = c.list();

                for (User u : userList) {
                    u.setVerification(Util.generateCode());
                    s.update(u);

                    s.beginTransaction().commit();
                    
                    final String verificationCode = u.getVerification();
                    final String firstName = u.getFirst_name();
                    final String lastName = u.getLast_name();

                    //send email
                    new Thread(new Runnable() {
                        @Override

                        public void run() {
                            Mail.sendMail(email, "KickCart - Verification", Util.generateVerificationEmail(firstName + " " + lastName, verificationCode));
                        }

                    }).start();
                }
            }
            s.close();

            responseObject.addProperty("status", true);
            responseObject.addProperty("message", "Verfication code sent!");
        }

        String responseText = gson.toJson(responseObject);
        System.out.println(responseText);
        response.setContentType("application/json");
        response.getWriter().write(responseText);
    }

}
