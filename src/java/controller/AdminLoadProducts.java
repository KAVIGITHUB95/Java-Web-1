
package controller;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import hibernate.Brand;
import hibernate.HibernateUtil;
import hibernate.Model;
import hibernate.OrderItem;
import hibernate.Orders;
import hibernate.Product;
import hibernate.Stock;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import model.Util;
import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.Restrictions;

@WebServlet(name = "AdminLoadProducts", urlPatterns = {"/AdminLoadProducts"})
public class AdminLoadProducts extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        Gson gson = new Gson();
        JsonObject responseObject = new JsonObject();
        responseObject.addProperty("status", false);

        SessionFactory sf = HibernateUtil.getSessionFactory();
        Session s = sf.openSession();

        try {
            HttpSession ses = request.getSession(false);
            if (ses != null && ses.getAttribute("admin") != null) {

                Criteria c1 = s.createCriteria(Stock.class);
                
                List<Stock> stock = c1.list();

                if (stock.isEmpty()) {
                    responseObject.addProperty("message", "No products found.");
                } else {
                    responseObject.add("products", gson.toJsonTree(stock));
                    responseObject.addProperty("status", true);
                
                }
            } else {
                responseObject.addProperty("message", "User not logged in.");
            
            }
        } catch (Exception e) {
            e.printStackTrace();
            responseObject.addProperty("message", "Something went wrong.");
        } finally {
            s.close();
        }

        response.setContentType("application/json");
        response.getWriter().write(gson.toJson(responseObject));
    }

}
