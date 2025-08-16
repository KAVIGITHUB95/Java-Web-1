package controller;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import hibernate.HibernateUtil;
import hibernate.OrderItem;
import hibernate.Orders;
import hibernate.User;
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
import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.Restrictions;

@WebServlet(name = "LoadOrder", urlPatterns = {"/LoadOrder"})

public class LoadOrder extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        Gson gson = new Gson();
        JsonObject responseObject = new JsonObject();
        responseObject.addProperty("status", false);

        SessionFactory sf = HibernateUtil.getSessionFactory();
        Session s = sf.openSession();

        try {
            HttpSession ses = request.getSession(false);
            if (ses != null && ses.getAttribute("user") != null) {
                User user = (User) ses.getAttribute("user");

                Criteria c1 = s.createCriteria(Orders.class);
                c1.add(Restrictions.eq("user", user));
                List<Orders> orders = c1.list();

                if (orders.isEmpty()) {
                    responseObject.addProperty("message", "No orders found.");
                } else {
                    
                    // Will store orders with their items
                    List<JsonObject> ordersWithItems = new ArrayList<>();

                    for (Orders o : orders) {
                        // Fetch items for this order
                        Criteria c2 = s.createCriteria(OrderItem.class);
                        c2.add(Restrictions.eq("orders", o));
                        List<OrderItem> orderItems = c2.list();

                        // Convert order to JSON
                        JsonObject orderJson = gson.toJsonTree(o).getAsJsonObject();
                        orderJson.add("orderItems", gson.toJsonTree(orderItems));

                        ordersWithItems.add(orderJson);
                    }

                    responseObject.add("orders", gson.toJsonTree(ordersWithItems));
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
