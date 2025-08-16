
package controller;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import hibernate.Gender;
import hibernate.HibernateUtil;
import hibernate.User;
import hibernate.UserStatus;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Date;
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

@WebServlet(name = "SignUp", urlPatterns = {"/SignUp"})
public class SignUp extends HttpServlet {
    
    private static final int USER_ACTIVE_STATUS = 1;

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        
        Gson gson = new Gson();
        JsonObject user = gson.fromJson(request.getReader(), JsonObject.class);
        final String firstName = user.get("firstName").getAsString();
        
        final String lastName = user.get("lastName").getAsString();
        String mobile = user.get("mobile").getAsString();
        final String email = user.get("email").getAsString();
        
        int genderId = user.get("gender").getAsInt();
        System.out.println("SignUp "+genderId);
        String password = user.get("password").getAsString();

        JsonObject responseObject = new JsonObject();
        responseObject.addProperty("status", false);

        
        try {
        if (firstName.isEmpty()) {
            responseObject.addProperty("message", "First Name can not be empty!");
        } else if (lastName.isEmpty()) {
            responseObject.addProperty("message", "Last Name can not be empty!");
        } else if (email.isEmpty()) {
            responseObject.addProperty("message", "Email can not be empty!");
        } else if (!Util.isEmailValidation(email)) {
            responseObject.addProperty("message", "Please enter a valid email!");
        } else if (password.isEmpty()) {

            responseObject.addProperty("message", "Password can not be empty!");

        } else if (!Util.isPasswordValid(password)) {
            responseObject.addProperty("message", "The password must contains at least uppercase, lowercase,"
                    + " number, special character and to be minimum eight characters long!");

        } else {
            //hibernate save

            SessionFactory sf = HibernateUtil.getSessionFactory();
            
            Session s = sf.openSession();
            Criteria criteria = s.createCriteria(User.class);
            criteria.add(Restrictions.eq("email", email));

            if (!criteria.list().isEmpty()) {
                responseObject.addProperty("message", "User with this Email already exists!");
            } else {
                User u = new User();

                u.setFirst_name(firstName);
                u.setLast_name(lastName);
                u.setEmail(email);
                u.setMobile(mobile);
                
                Gender gender = (Gender) s.load(Gender.class, genderId);
                UserStatus ustatus = (UserStatus) s.get(UserStatus.class, SignUp.USER_ACTIVE_STATUS);
                u.setGender(gender);
                u.setPassword(password);
                u.setUserstatus(ustatus);

                //generate verification code
                final String verificationCode = Util.generateCode();
                u.setVerification(verificationCode);
                //generate verification code
                
                u.setCreated_at(new Date());
                System.out.println(u);
                s.save(u);
                s.beginTransaction().commit();

                    new Thread(new Runnable() {
                        @Override

                        public void run() {
                            Mail.sendMail(email, "KickCart - Verification", Util.generateVerificationEmail(firstName + " " + lastName, verificationCode));
                        }

                    }).start();
                
                //session management
                HttpSession ses = request.getSession();
                ses.setAttribute("email", email);
                
                //session management
               
                responseObject.addProperty("status", true);
                responseObject.addProperty("message", "Registration success. Please check your email for the verification code");
            }
            s.close();
        }

        String responseText = gson.toJson(responseObject);
        response.setContentType("application/json");
        response.getWriter().write(responseText);
        } catch (Exception e) {
            e.printStackTrace();
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            responseObject.addProperty("status", "error");
            responseObject.addProperty("message", "Failed to update user.");
        }
//        User user = gson.fromJson(request.getReader(), User.class);
//        String firstName = user.getFirst_name();
//        String lastName = user.getLast_name();
//        String email = user.getEmail();
//        String password = user.getPassword();

//        dto.User user = gson.fromJson(request.getReader(), dto.User.class);
//        String firstName = user.getFirstName();
//        String lastName = user.getLastName();
//        String email = user.getEmail();
//        String password = user.getPassword();
    }

}
