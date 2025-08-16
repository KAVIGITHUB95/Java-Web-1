package controller;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import hibernate.HibernateUtil;
import hibernate.User;
import hibernate.UserStatus;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.criterion.Restrictions;

@WebServlet(name = "LoadAllUsers", urlPatterns = {"/LoadAllUsers"})
public class LoadAllUsers extends HttpServlet {

    private static final int ACTIVE_USER = 1;

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        Gson gson = new Gson();

        Session s = null;
        Transaction tx = null;

        JsonObject responseObject = new JsonObject();
        responseObject.addProperty("status", Boolean.FALSE);

        try {
            s = HibernateUtil.getSessionFactory().openSession();
            tx = s.beginTransaction();

            Criteria c1 = s.createCriteria(User.class);
            UserStatus userStatus = (UserStatus) s.get(UserStatus.class, LoadAllUsers.ACTIVE_USER);
            c1.add(Restrictions.eq("userstatus", userStatus));
            tx.commit();

            if (c1.list() == null || c1.list().isEmpty()) {
                responseObject.addProperty("message", "No users found in the system.");
            } else {
                List<User> userList = c1.list();
                responseObject.addProperty("status", true);
                responseObject.addProperty("message", "Users loaded successfully.");
                responseObject.add("userList", gson.toJsonTree(userList));
            }

        } catch (Exception e) {
            if (tx != null && tx.isActive()) {
                tx.rollback();
            }
            System.err.println("Exception in GetAllUsers servlet: " + e.getMessage());
            e.printStackTrace();

            responseObject.addProperty("status", Boolean.FALSE);
            responseObject.addProperty("message", "Failed to retrieve users: " + e.getMessage());
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        } finally {
            if (s != null && s.isOpen()) {
                s.close();
            }
            String responseText = gson.toJson(responseObject);
            response.setContentType("application/json");
            response.getWriter().write(responseText);

        }

    }
}
