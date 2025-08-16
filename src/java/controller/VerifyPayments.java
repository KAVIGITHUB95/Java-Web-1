package controller;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import hibernate.Cart;
import hibernate.HibernateUtil;
import hibernate.OrderItem;
import hibernate.OrderStatus;
import hibernate.Orders;
import hibernate.Product;
import hibernate.Stock;
import hibernate.User;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Date;
import java.util.List;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import model.PayHere;
import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.criterion.Restrictions;

@WebServlet(name = "VerifyPayments", urlPatterns = {"/VerifyPayments"})
public class VerifyPayments extends HttpServlet {

//    private static final int PAYHERE_SUCCESS = 2;
//    private static final int ORDER_PENDING = 5;
//
//    @Override
//    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
//        System.out.println("VerifyPayments");
//        Gson gson = new Gson();
//        JsonObject jsonResponse = new JsonObject();
//        jsonResponse.addProperty("status", false);
//        
//        String merchant_id = request.getParameter("merchant_id");
//        String order_id = request.getParameter("order_id");
//        String payhere_amount = request.getParameter("payhere_amount");
//        String payhere_currency = request.getParameter("payhere_currency");
//        String status_code = request.getParameter("status_code");
//        String md5sig = request.getParameter("md5sig");
////        JsonObject jsonRequest = gson.fromJson(request.getReader(), JsonObject.class);
//        
//        SessionFactory sf = HibernateUtil.getSessionFactory();
//        Session s = sf.openSession();
//        Transaction tr = s.beginTransaction();
//        
////        String merchant_id = jsonRequest.get("merchant_id").getAsString();
////        String order_id = jsonRequest.get("order_id").getAsString();
////        String payhere_amount = jsonRequest.get("payhere_amount").getAsString();
////        String payhere_currency = jsonRequest.get("payhere_currency").getAsString();
////        String status_code = jsonRequest.get("status_code").getAsString();
////        String md5sig = jsonRequest.get("md5sig").getAsString();
//        
//        System.out.println("---- Incoming Payment Verification ----");
//        System.out.println("Merchant ID: " + merchant_id);
//        System.out.println("Order ID: " + order_id);
//        System.out.println("Amount: " + payhere_amount);
//        System.out.println("Currency: " + payhere_currency);
//        System.out.println("Status Code: " + status_code);
//        System.out.println("MD5 Signature: " + md5sig);
//
//        String merchantSecret = "MTY0ODE1ODcyMTI4MDk3MTQ4OTgxNjg3MDU0ODQ5MzA1Mzc5MzA3NA==";
//        String merchantSecretMD5 = PayHere.generateMD5(merchantSecret);
//        String hash = PayHere.generateMD5(merchant_id + order_id + payhere_amount + payhere_currency + merchantSecretMD5);
//
//        if (md5sig.equals(hash) && Integer.parseInt(status_code) == VerifyPayments.PAYHERE_SUCCESS) {
//            System.out.println("Payment Completed. Order Id:" + order_id);
//            String orderId = order_id.substring(3);
//            System.out.println(orderId); // 1
//            
//            Orders order = (Orders) s.get(Orders.class, orderId);
//            OrderStatus orderStatus = (OrderStatus) s.get(OrderStatus.class, VerifyPayments.ORDER_PENDING);
//            order.setOrder_status(orderStatus);
//            System.out.println("New Payment Added for Order ID: " + order_id);
//            jsonResponse.addProperty("status", true);
//            jsonResponse.addProperty("message", "Payment verified and added");
//        } else {
//            System.out.println("False");
//        }
//    }
    private static final int PAYHERE_SUCCESS = 2;
    private static final int PAYHERE_PENDING = 0;
    private static final int PAYHERE_CANCEL = -1;
    private static final int PAYHERE_FAIL = -2;

    @Override
    protected synchronized void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String merchant_id = request.getParameter("merchant_id");
        String order_id = request.getParameter("order_id");
        String payhere_amount = request.getParameter("payhere_amount");
        String payhere_currency = request.getParameter("payhere_currency");
        String status_code = request.getParameter("status_code");
        String md5sig = request.getParameter("md5sig");

        if (merchant_id == null || order_id == null || md5sig == null) {
            System.err.println("Missing required parameters");
            return;
        }

        String merchantSecret = "MTY0ODE1ODcyMTI4MDk3MTQ4OTgxNjg3MDU0ODQ5MzA1Mzc5MzA3NA==";
        String merchantSecretMD5 = PayHere.generateMD5(merchantSecret).toUpperCase();
        String localHash = PayHere.generateMD5(
                merchant_id + order_id + payhere_amount + payhere_currency
                + status_code + merchantSecretMD5
        ).toUpperCase();

        int statusCodeInt = Integer.parseInt(status_code);

        if (localHash.equals(md5sig)) {
            SessionFactory sf = HibernateUtil.getSessionFactory();
            Session s = sf.openSession();
            Transaction tr = s.beginTransaction();

            try {
                String orderIdStr = order_id.replaceAll("[^0-9]", "");
                int orderId = Integer.parseInt(orderIdStr);

                Orders order = (Orders) s.get(Orders.class, orderId);
                if (order == null) {
                    System.err.println("Order not found for ID: " + orderId);
                    tr.rollback();
                    return;
                }

                if (statusCodeInt == PAYHERE_SUCCESS) {
                    // Payment successful
                    System.out.println("Payment verified. Updating order...");

                    OrderStatus paidStatus = (OrderStatus) s.load(OrderStatus.class, 1); // Paid
                    order.setOrder_status(paidStatus);
                    s.update(order);

                    // Deduct stock and clear cart
                    Criteria cartCriteria = s.createCriteria(Cart.class);
                    cartCriteria.add(Restrictions.eq("user", order.getUser()));
                    List<Cart> cartItems = cartCriteria.list();

                    for (Cart cartItem : cartItems) {
                        Stock stock = cartItem.getStock();
                        stock.setQty(stock.getQty() - cartItem.getQty());
                        s.update(stock);
                        s.delete(cartItem);
                    }

                    System.out.println("Order marked as PAID. Cart cleared.");
                } else {
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
                    System.out.println("Unknown payment status code: " + statusCodeInt);
                }

                tr.commit();

            } catch (Exception e) {
                tr.rollback();
                System.err.println("Error updating order:");
                e.printStackTrace();
            } finally {
                s.close();
            }
        } else {
            System.out.println("Payment verification failed.");
        }
    }

}
