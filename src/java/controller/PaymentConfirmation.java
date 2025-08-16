package controller;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import hibernate.HibernateUtil;
import hibernate.OrderItem;
import hibernate.OrderStatus;
import hibernate.Orders;
import hibernate.User;
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

@WebServlet(name = "PaymentConfirmation", urlPatterns = {"/PaymentConfirmation"})
public class PaymentConfirmation extends HttpServlet {

    private static final int ORDER_PAID = 1;

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        Gson gson = new Gson();
        JsonObject verifiedPayments = gson.fromJson(request.getReader(), JsonObject.class);
        String orderIdStr = verifiedPayments.get("orderId").getAsString();

        JsonObject responseObject = new JsonObject();
        responseObject.addProperty("status", false);

        SessionFactory sf = HibernateUtil.getSessionFactory();
        Session s = sf.openSession();
        Transaction tr = s.beginTransaction();

        try {
            User user = (User) request.getSession().getAttribute("user");

            if (user == null) {
                responseObject.addProperty("message", "Session expired! Please log in again");
            } else {
                int orderId = Integer.parseInt(orderIdStr);
                OrderStatus paidStatus = (OrderStatus) s.get(OrderStatus.class, PaymentConfirmation.ORDER_PAID);
                Orders orders = (Orders) s.get(Orders.class, orderId);
                
                Criteria c1 = s.createCriteria(OrderItem.class);
                c1.add(Restrictions.eq("orders", orders));
                List<OrderItem> orderItemsList = c1.list();
                System.out.println("");

                if (orders == null) {
                    responseObject.addProperty("message", "Order not found");
                } else if (orders.getOrder_status().equals(paidStatus)) {
                    JsonObject orderJSONObject = new JsonObject();
                    orderJSONObject.addProperty("status", true);
                    orderJSONObject.addProperty("orderId", orders.getId());

                    orderJSONObject.addProperty("firstName", orders.getUser().getFirst_name());
                    
                    orderJSONObject.addProperty("lastName", orders.getUser().getLast_name());
                    if (orders.getAddress() != null) {
                        orderJSONObject.addProperty("lineOne", orders.getAddress().getLineOne());
                        orderJSONObject.addProperty("lineTwo", orders.getAddress().getLineTwo());
                        orderJSONObject.addProperty("postalCode", orders.getAddress().getPostalCode());
                        if (orders.getAddress().getCity() != null) {
                            orderJSONObject.addProperty("city", orders.getAddress().getCity().getName());
                        }
                    }

                    orderJSONObject.addProperty("created_at", String.valueOf(orders.getCreated_at()));
                    orderJSONObject.addProperty("total", orders.getTotal());
                    orderJSONObject.addProperty("orderStatus", orders.getOrder_status().getValue());

                    if (orders.getDelivery_type() != null) {
                        orderJSONObject.addProperty("deliveryType", orders.getDelivery_type().getName());
                    }
                    
                    responseObject.add("orderList", gson.toJsonTree(orderJSONObject));
                    responseObject.add("orderItemsList", gson.toJsonTree(orderItemsList));
                    responseObject.addProperty("status", true);

                } else {
                    responseObject.addProperty("message", "Order status does not match");
                }
            }

            tr.commit();
        } catch (Exception e) {
            tr.rollback();
            e.printStackTrace();
            responseObject.addProperty("message", "Error: " + e.getMessage());
        } finally {
            s.close();
        }

        response.setContentType("application/json");
        response.getWriter().write(responseObject.toString());

    }

}
