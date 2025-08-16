package controller;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import hibernate.Cart;
import hibernate.HibernateUtil;
import hibernate.Product;
import hibernate.Stock;
import hibernate.User;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
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
import org.hibernate.Transaction;
import org.hibernate.criterion.Restrictions;

@WebServlet(name = "AddToCart", urlPatterns = {"/AddToCart"})
public class AddToCart extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        String stId = request.getParameter("stId");
        String qty = request.getParameter("qty");

        System.out.println("StockId: " + stId + " " + "Quantity: " + qty);

        Gson gson = new Gson();
        JsonObject responseObject = new JsonObject();
        responseObject.addProperty("status", false);
        
        SessionFactory sf = HibernateUtil.getSessionFactory();
        Session s = sf.openSession();
        Transaction tr = s.beginTransaction();

        try {

            if (!Util.isInteger(stId)) {
                responseObject.addProperty("message", "Invalid product Id!");
            } else if (!Util.isInteger(qty)) {
                responseObject.addProperty("message", "Invalid product Quantity!");
            } else {

                // add-to-cart process
                

                Stock stock = (Stock) s.get(Stock.class, Integer.valueOf(stId));

                if (stock == null) {
                    responseObject.addProperty("message", "Stock not found");
                } else { // product avaliable in database

                    if (stock.getQty() == 0) {
                        responseObject.addProperty("message", "This product is out of stock");
                        return;

                    } else {
                        User user = (User) request.getSession().getAttribute("user");
                        if (user != null) { // add product to database cart => user avaliable
                            Criteria c1 = s.createCriteria(Cart.class);
                            System.out.println(c1.list());
                            c1.add(Restrictions.eq("user", user));
                            c1.add(Restrictions.eq("stock", stock));
                            System.out.println(c1.list());
                            if (c1.list().isEmpty()) { // product not avalible in same product id

                                if (Integer.parseInt(qty) <= stock.getQty()) { // product quantity avalaible
                                    Cart cart = new Cart();
                                    cart.setQty(Integer.parseInt(qty));
                                    cart.setUser(user);
                                    cart.setStock(stock);

                                    s.save(cart);
                                    tr.commit();
                                    responseObject.addProperty("status", true);
                                    responseObject.addProperty("message", "Product add to cart successfully");
                                } else {
                                    responseObject.addProperty("message", "OOPS... Insufficient Product quantity!!!");

                                }

                            } else {// product available

                                Cart cart = (Cart) c1.uniqueResult();
                                int newQty = cart.getQty() + Integer.parseInt(qty);
                                if (newQty <= stock.getQty()) {

                                    cart.setQty(newQty);
                                    s.update(cart);
                                    tr.commit();
                                    responseObject.addProperty("status", true);

                                    responseObject.addProperty("message", "Product add to cart successfully");
                                } else {
                                    responseObject.addProperty("message", "OOPS... Insufficient Product quantity!!!");
                                }

                            }

                        } else { // add product to session cart => user not available in the HttpSession
                            HttpSession ses = request.getSession();

                            if (ses.getAttribute("sessionCart") == null) {
                                System.out.println("Cart is empty. Creating new sessionCart...");

                                ArrayList<Cart> sessCarts = new ArrayList<>();
                                Cart cart = new Cart();

                                cart.setQty(Integer.parseInt(qty));
                                cart.setUser(null);
                                cart.setStock(stock);
                                sessCarts.add(cart);

                                ses.setAttribute("sessionCart", sessCarts);

                                System.out.println("Added new item to sessionCart: Stock ID = " + stock.getId() + ", Qty = " + qty);

                                responseObject.addProperty("status", true);
                                responseObject.addProperty("message", "Product added to cart successfully");

                            } else {
                                System.out.println("sessionCart already exists. Checking for existing item...");

                                ArrayList<Cart> sessionList = (ArrayList<Cart>) ses.getAttribute("sessionCart");
                                Cart foundedCart = null;

                                for (Cart cart : sessionList) {
                                    System.out.println("Checking existing cart item: Stock ID = " + cart.getStock().getId() + ", Qty = " + cart.getQty());

                                    if (cart.getStock().getId() == stock.getId()) {
                                        foundedCart = cart;
                                        break;
                                    }
                                }

                                if (foundedCart != null) {
                                    int newQty = foundedCart.getQty() + Integer.parseInt(qty);
                                    if (newQty <= stock.getQty()) {
                                        foundedCart.setQty(newQty);
                                        System.out.println("Updated quantity for Stock ID = " + stock.getId() + " to " + newQty);

                                        responseObject.addProperty("status", true);
                                        responseObject.addProperty("message", "Product added to cart successfully");
                                    } else {
                                        System.out.println("Insufficient stock for update. Stock = " + stock.getQty() + ", Requested = " + newQty);
                                        responseObject.addProperty("message", "OOPS... Insufficient Product quantity!!!");
                                    }

                                } else {
                                    if (Integer.parseInt(qty) <= stock.getQty()) {
                                        foundedCart = new Cart();
                                        foundedCart.setQty(Integer.parseInt(qty));
                                        foundedCart.setUser(null);
                                        foundedCart.setStock(stock);
                                        sessionList.add(foundedCart);

                                        System.out.println("Added new item to existing sessionCart: Stock ID = " + stock.getId() + ", Qty = " + qty);

                                        responseObject.addProperty("status", true);
                                        responseObject.addProperty("message", "Product added to cart successfully");
                                    } else {
                                        System.out.println("Insufficient stock to add new item. Stock = " + stock.getQty() + ", Requested = " + qty);
                                        responseObject.addProperty("message", "OOPS... Insufficient Product quantity!!!");
                                    }
                                }

                                // Optional: print final sessionCart for full debug
                                System.out.println("Final sessionCart items:");
                                for (Cart c : sessionList) {
                                    System.out.println("Stock ID: " + c.getStock().getId() + ", Qty: " + c.getQty());
                                }
                            }
                        }

                    }

                }
            }
        } catch (Exception e) {
            if (tr != null) {
                tr.rollback();
            }
            responseObject.addProperty("message", e.getMessage());
        } finally {
            s.close();
        }
        // add-to-cart-process-end
        response.setContentType("application/json");

        String toJson = gson.toJson(responseObject);
        response.getWriter().write(toJson);
    }

}
