
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
import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Restrictions;

@WebServlet(name = "VerifyAccount", urlPatterns = {"/VerifyAccount"})
public class VerifyAccount extends HttpServlet {

    @Override
    protected void doPut(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        
        Gson gson = new Gson();
        JsonObject responseObject = new JsonObject();
        responseObject.addProperty("status", false);
        
        HttpSession ses = request.getSession();
        if (ses.getAttribute("email")==null) {
            
//            responseObject.addProperty("message", "Email not found");
            
            responseObject.addProperty("message", "1");
        }else{
            String email = ses.getAttribute("email").toString();
            JsonObject verification = gson.fromJson(request.getReader(), JsonObject.class);
            String verificationCode = verification.get("verificationCode").getAsString();
            System.out.println(verificationCode);
            
            SessionFactory sf = HibernateUtil.getSessionFactory();
            Session s = sf.openSession();
            Criteria c1 = s.createCriteria(User.class);
            Criterion crt1 = Restrictions.eq("email", email);
            Criterion crt2 = Restrictions.eq("verification", verificationCode);
            
            c1.add(crt1);
            c1.add(crt2);
            List<User> l1 = c1.list();
            for(User user: l1) {
                System.out.println(user.getEmail());
            }
            System.out.println(c1.list());
            
            
            if (c1.list().isEmpty()) {
                responseObject.addProperty("message", "Invalid Verification Code");
            
            } else {
                
                User user = (User) c1.list().get(0);
                user.setVerification("Verified");
                s.update(user);
                
                s.beginTransaction().commit();
                s.close();
                
                //store user in the session
                ses.setAttribute("user", user);
                //store user in the session
                
                responseObject.addProperty("status", true);
                responseObject.addProperty("message", "Verification successful!");
            }
            
        }
        
        String responseText = gson.toJson(responseObject);
        System.out.println(responseText);
        response.setContentType("application/json");
        response.getWriter().write(responseText);
    }

}
