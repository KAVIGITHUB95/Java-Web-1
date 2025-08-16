package controller;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import hibernate.HibernateUtil;
import hibernate.OrderItem;
import hibernate.Orders;
import java.util.List;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashSet;
import java.util.Set;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;

@WebServlet(name = "AdminDashboardData", urlPatterns = {"/AdminDashboardData"})
public class AdminDashboardData extends HttpServlet {

    private static final int ORDER_STATUS_PAID = 1;
    private static final int ORDER_STATUS_PENDING = 5; // Example, adjust accordingly

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

                Criteria orderCriteria = s.createCriteria(Orders.class);
                orderCriteria.addOrder(Order.desc("created_at"));
                List<Orders> orders = orderCriteria.list();

                JsonArray ordersArray = new JsonArray();

                double totalSales = 0;
                Set<Integer> uniqueCustomerIds = new HashSet<>();
                int pendingDeliveriesCount = 0;

                for (Orders order : orders) {
                    double total = 0;
                    JsonObject orderJson = new JsonObject();
                    orderJson.addProperty("orderId", order.getId());
                    orderJson.addProperty("date", order.getCreated_at().toString()); // Format as needed

                    // -------- Fetch Items for Each Order --------
                    Criteria itemCriteria = s.createCriteria(OrderItem.class);
                    itemCriteria.add(Restrictions.eq("orders", order));
                    List<OrderItem> items = itemCriteria.list();

                    JsonArray itemsArray = new JsonArray();

                    if (!items.isEmpty()) {
                        OrderItem firstItem = items.get(0);
                        orderJson.addProperty("status", firstItem.getOrders().getOrder_status().getValue());
                        orderJson.addProperty("statusId", firstItem.getOrders().getOrder_status().getId());
                        orderJson.addProperty("delivery", firstItem.getDelivery_fee());
                    } else {
                        orderJson.addProperty("status", "N/A");
                        orderJson.addProperty("statusId", 0);
                        orderJson.addProperty("delivery", 0.0);
                    }

                    // Add address and user without sensitive data
                    order.getAddress().setUser(null);
                    orderJson.add("address", gson.toJsonTree(order.getAddress()));
                    order.getUser().setPassword(null);
                    orderJson.add("user", gson.toJsonTree(order.getUser()));

                    // Add order items & calculate totals
                    for (OrderItem item : items) {
                        JsonObject itemJson = new JsonObject();
                        itemJson.addProperty("productId", item.getStock().getProduct().getId());
                        itemJson.addProperty("productName", item.getStock().getProduct().getTitle());
                        itemJson.addProperty("brand", item.getStock().getProduct().getModel().getBrand().getName());
                        itemJson.addProperty("price", item.getStock().getPrice());
                        itemJson.addProperty("qty", item.getQty());
                        itemJson.addProperty("size", item.getStock().getSize().getValue());
                        itemJson.addProperty("color", item.getStock().getColor().getValue());

                        itemsArray.add(itemJson);

                        total += item.getStock().getPrice() * item.getQty();

                        // Count pending deliveries if order_status is pending (adjust id)
                        if (item.getOrders().getOrder_status().getId() == ORDER_STATUS_PENDING) {
                            pendingDeliveriesCount++;
                        }
                    }

                    orderJson.addProperty("total", total);
                    orderJson.add("orderItem", itemsArray);

                    ordersArray.add(orderJson);

                    // Add customer to unique set
                    if (order.getUser() != null) {
                        uniqueCustomerIds.add(order.getUser().getId());
                    }

                    // Sum total sales
                    totalSales += total;
                }

                responseObject.addProperty("status", true);
                responseObject.add("orders", ordersArray);

                // Add summary data here:
                responseObject.addProperty("totalSales", totalSales);
                responseObject.addProperty("totalOrders", orders.size());
                responseObject.addProperty("pendingDeliveries", pendingDeliveriesCount);
                responseObject.addProperty("customers", uniqueCustomerIds.size());

            } else {
                responseObject.addProperty("message", "User not logged in as admin.");
            }
        } catch (Exception e) {
            e.printStackTrace();
            responseObject.addProperty("status", false);
            responseObject.addProperty("message", "Error loading dashboard data.");
        } finally {
            s.close();
        }

        response.setContentType("application/json");
        response.getWriter().write(gson.toJson(responseObject));
    }

}
