package controller;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import hibernate.Address;
import hibernate.Cart;
import hibernate.City;
import hibernate.DeliveryType;
import hibernate.HibernateUtil;
import hibernate.OrderItem;
import hibernate.OrderStatus;
import hibernate.Orders;
import hibernate.Product;
import hibernate.Stock;
import hibernate.User;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.DecimalFormat;
import java.util.Date;
import java.util.List;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import model.PayHere;
import model.Util;
import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;

@WebServlet(name = "CheckOut", urlPatterns = {"/CheckOut"})
public class CheckOut extends HttpServlet {

    private static final int SELECTOR_DEFAULT_VALUE = 0;

    private static final int ORDER_PENDING = 5;
    private static final int WITHIN_COLOMBO = 1;
    private static final int OUT_OF_COLOMBO = 2;
    private static final String RATING_DEFAULT_VALUE = "0";

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        Gson gson = new Gson();

        JsonObject requJsonObject = gson.fromJson(request.getReader(), JsonObject.class);

        boolean isCurrentAddress = requJsonObject.get("isCurrentAddress").getAsBoolean();

        String firstName = requJsonObject.get("firstName").getAsString();
        String lastName = requJsonObject.get("lastName").getAsString();

        String email = requJsonObject.get("email").getAsString();
        String mobile = requJsonObject.get("mobile").getAsString();
        String lineOne = requJsonObject.get("lineOne").getAsString();

        String lineTwo = requJsonObject.get("lineTwo").getAsString();
        String citySelect = requJsonObject.get("citySelect").getAsString();

        String postalCode = requJsonObject.get("postalCode").getAsString();

        System.out.println(isCurrentAddress);
        JsonObject responseObject = new JsonObject();
        responseObject.addProperty("status", false);

        SessionFactory sf = HibernateUtil.getSessionFactory();
        Session s = sf.openSession();
        Transaction tr = s.beginTransaction();

        User user = (User) request.getSession().getAttribute("user");

        if (user == null) {

            responseObject.addProperty("message", "Session expired! Please log in again");
        } else {
            if (isCurrentAddress) {
                Criteria c1 = s.createCriteria(Address.class);
                c1.add(Restrictions.eq("user", user));
                c1.addOrder(Order.desc("id"));
                if (c1.list().isEmpty()) {
                    responseObject.addProperty("message", "You current address is not found. Please try again");
                } else {
                    Address address = (Address) c1.list().get(0);
                    processCheckout(s, tr, user, address, responseObject);
                }
            } else {
                if (firstName.isEmpty()) {
                    responseObject.addProperty("message", "First Name is required.");
                } else if (lastName.isEmpty()) {
                    responseObject.addProperty("message", "Last Name is required.");
                } else if (!Util.isInteger(citySelect)) {
                    responseObject.addProperty("message", "First Name is required.");
                } else if (Integer.parseInt(citySelect) == CheckOut.SELECTOR_DEFAULT_VALUE) {
                    responseObject.addProperty("message", "Invalid city");
                } else {
                    City city = (City) s.get(City.class, Integer.valueOf(citySelect));
                    if (city == null) {
                        responseObject.addProperty("message", "Invalid city name");
                    } else {
                        if (lineOne.isEmpty()) {
                            responseObject.addProperty("message", "Address line one is required");
                        } else if (lineTwo.isEmpty()) {
                            responseObject.addProperty("message", "Address line two is required");
                        } else if (postalCode.isEmpty()) {
                            responseObject.addProperty("message", "Your postal code is required");
                        } else if (!Util.isCodeValid(postalCode)) {
                            responseObject.addProperty("message", "Invalid postal code number");
                        } else if (mobile.isEmpty()) {
                            responseObject.addProperty("message", "Mobile number is required");
                        } else if (!Util.isMobileValid(mobile)) {
                            responseObject.addProperty("message", "Invalid mobile number");
                        } else {
                            Address address = new Address();
                            address.setLineOne(lineOne);
                            address.setLineTwo(lineTwo);
                            address.setCity(city);
                            address.setPostalCode(postalCode);
                            address.setUser(user);
                            s.save(address);

                            processCheckout(s, tr, user, address, responseObject);
                        }
                    }
                }
            }

        }

        response.setContentType("application/json");
        String toJson = gson.toJson(responseObject);

        response.getWriter().write(toJson);

    }

    private void processCheckout(Session s,
            Transaction tr,
            User user,
            Address address,
            JsonObject responseObject) {

        try {
            OrderStatus orderStatus = (OrderStatus) s.get(OrderStatus.class, CheckOut.ORDER_PENDING);
            DeliveryType withInColombo = (DeliveryType) s.get(DeliveryType.class, CheckOut.WITHIN_COLOMBO);
            DeliveryType outOfColombo = (DeliveryType) s.get(DeliveryType.class, CheckOut.OUT_OF_COLOMBO);

            // Decide delivery type once
            DeliveryType selectedDeliveryType;
            if (address.getCity().getName().equalsIgnoreCase("Colombo")) {
                selectedDeliveryType = withInColombo;
            } else {
                selectedDeliveryType = outOfColombo;
            }

            Orders orders = new Orders();
            orders.setAddress(address);
            orders.setCreated_at(new Date());
            orders.setUser(user);
            orders.setTotal(0);
            orders.setOrder_status(orderStatus);
            orders.setDelivery_type(selectedDeliveryType);

            s.save(orders);
            s.flush(); //Force insert before foreign keys rely on it

            int orderId = orders.getId(); //Get the auto-generated ID

            Criteria c1 = s.createCriteria(Cart.class);
            c1.add(Restrictions.eq("user", user));
            List<Cart> cartList = c1.list();

            double amount = 0;
            String items = "";

            for (Cart cart : cartList) {
                amount += cart.getQty() * cart.getStock().getPrice();

                OrderItem orderItem = new OrderItem();

                if (address.getCity().getName().equalsIgnoreCase("Colombo")) {
                    amount += cart.getQty() * withInColombo.getPrice();
//                    orders.setDelivery_type(withInColombo);
                    orderItem.setDelivery_fee(withInColombo.getPrice());
                } else {// out of colombo
                    amount += cart.getQty() * outOfColombo.getPrice();
//                    orders.setDelivery_type(outOfColombo);
                    orderItem.setDelivery_fee(outOfColombo.getPrice());
                }

                items += cart.getStock().getProduct().getTitle() + " x " + cart.getQty() + ", ";

                Stock stock = cart.getStock();
    
                orderItem.setOrders(orders);
                orderItem.setStock(stock);
                orderItem.setQty(cart.getQty());
                orderItem.setRating(CheckOut.RATING_DEFAULT_VALUE);
                orderItem.setPrice(stock.getPrice());

                s.save(orderItem);

                //update product qty
//                stock.setQty(stock.getQty() - cart.getQty());
//                s.update(stock);
//
//                // delete cart item
//                s.delete(cart);
            }

            Orders orderentity = (Orders) s.get(Orders.class, orderId);
            orderentity.setTotal(amount);

            s.merge(orderentity);
            s.flush();

            tr.commit();

            //Payhere Process
            String merahantID = "1225615";
            String merchantSecret = "MTY0ODE1ODcyMTI4MDk3MTQ4OTgxNjg3MDU0ODQ5MzA1Mzc5MzA3NA==";
            String orderID = "000" + orderId;
            String currency = "LKR";
            String formattedAmount = new DecimalFormat("0.00").format(amount);
            String merchantSecretMD5 = PayHere.generateMD5(merchantSecret);
            String hash = PayHere.generateMD5(merahantID + orderID + formattedAmount + currency + merchantSecretMD5);
            System.out.println("Generated Hash: " + hash);

            JsonObject payHereJSON = new JsonObject();
            payHereJSON.addProperty("sandbox", true); 
            payHereJSON.addProperty("merchant_id", "1225615");
            payHereJSON.addProperty("return_url", "https://8f685fec0189.ngrok-free.app/KickCart/index.html");
            payHereJSON.addProperty("cancel_url", "https://8f685fec0189.ngrok-free.app/KickCart/CancelOrder");
            payHereJSON.addProperty("notify_url", "https://5e7ccafec54a.ngrok-free.app/KickCart/VerifyPayments");
            payHereJSON.addProperty("order_id", orderID);
            payHereJSON.addProperty("items", items);
            payHereJSON.addProperty("amount", formattedAmount);
            payHereJSON.addProperty("currency", currency);
            payHereJSON.addProperty("hash", hash);
            payHereJSON.addProperty("first_name", user.getFirst_name());
            payHereJSON.addProperty("last_name", user.getLast_name());
            payHereJSON.addProperty("email", user.getEmail());
            payHereJSON.addProperty("phone", user.getMobile());
            payHereJSON.addProperty("address", address.getLineOne() + ", " + address.getLineTwo());
            payHereJSON.addProperty("city", address.getCity().getName());
            payHereJSON.addProperty("country", "Sri Lanka");

            payHereJSON.addProperty("status", true);
            payHereJSON.addProperty("message", "Checkout completed");

            responseObject.addProperty("status", true);
            responseObject.addProperty("message", "Checkout completed");
            responseObject.add("payhereJSON", new Gson().toJsonTree(payHereJSON));

        } catch (Exception e) {
            tr.rollback();
            e.printStackTrace();
            responseObject.addProperty("message", "Checkout failed due to an internal error");
        }
    }
}
