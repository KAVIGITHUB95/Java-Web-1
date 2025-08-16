
package controller;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import hibernate.HibernateUtil;
import hibernate.OrderItem;
import hibernate.Orders;
import hibernate.Stock;
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
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.criterion.Restrictions;

@WebServlet(name = "CancelOrder", urlPatterns = {"/CancelOrder"})
public class CancelOrder extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        Gson gson = new Gson();
        JsonObject orderIdObject = gson.fromJson(request.getReader(), JsonObject.class);
        String orderIdStr = orderIdObject.get("orderId").getAsString();
        JsonObject responseJson = new JsonObject();

        
        if (orderIdStr == null || orderIdStr.equals("undefined")) {
            responseJson.addProperty("status", false);
            
            responseJson.addProperty("message", "Order ID missing");
            
            writeJsonResponse(response, responseJson);
            System.out.println(orderIdStr);
            return;
        }

        SessionFactory sf = HibernateUtil.getSessionFactory();
        Session s = sf.openSession();
        Transaction tr = s.beginTransaction();

        
        try {
            int orderId = Integer.parseInt(orderIdStr);
            Orders order = (Orders) s.get(Orders.class, orderId);

            if (order == null) {
                responseJson.addProperty("status", false);
                responseJson.addProperty("message", "Order not found");
                writeJsonResponse(response, responseJson);
                
                return;
            }

            
            // Fetch order items
            Criteria c = s.createCriteria(OrderItem.class);
            
            c.add(Restrictions.eq("orders", order));
            
            List<OrderItem> orderItems = c.list();

            // Rollback stock quantities
            for (OrderItem item : orderItems) {
                Stock stock = item.getStock();
                stock.setQty(stock.getQty() + item.getQty());
                s.update(stock);
                s.delete(item);
            }

            // Delete the order
            s.delete(order);

            tr.commit();

            responseJson.addProperty("status", true);
            responseJson.addProperty("message", "Order cancelled and stock restored.");
        } catch (Exception e) {
            tr.rollback();
            e.printStackTrace();
            responseJson.addProperty("status", false);
            responseJson.addProperty("message", "Internal error cancelling order.");
        } finally {
            s.close();
            writeJsonResponse(response, responseJson);
        }
    }

    
    private void writeJsonResponse(HttpServletResponse response, JsonObject json) throws IOException {
        response.setContentType("application/json");
        response.getWriter().write(json.toString());
    }
}
