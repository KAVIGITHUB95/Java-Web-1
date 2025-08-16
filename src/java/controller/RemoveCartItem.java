package controller;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import hibernate.Cart;
import hibernate.HibernateUtil;
import hibernate.User;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Iterator;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import model.Util;
import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.criterion.Restrictions;

@WebServlet(name = "RemoveCartItem", urlPatterns = {"/RemoveCartItem"})
public class RemoveCartItem extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        JsonObject responseObject = new JsonObject();

        responseObject.addProperty("status", false);
        String stockIdStr = request.getParameter("stockId");

        if (!Util.isInteger(stockIdStr)) {

            responseObject.addProperty("message", "Invalid product ID!");
        } else {
            int stockId = Integer.parseInt(stockIdStr);
            Session s = HibernateUtil.getSessionFactory().openSession();
            Transaction tx = s.beginTransaction();
            try {

                User user = (User) request.getSession().getAttribute("user");

                if (user != null) {
                    Criteria c = s.createCriteria(Cart.class);
                    c.add(Restrictions.eq("user", user));
                    c.add(Restrictions.eq("stock.id", stockId));
                    Cart cart = (Cart) c.uniqueResult();

                    if (cart != null) {

                        s.delete(cart);
                        tx.commit();
                        responseObject.addProperty("status", true);
                        responseObject.addProperty("message", "Item removed from cart.");
                    } else {
                        responseObject.addProperty("message", "Cart item not found.");
                    }

                } else {
                    ArrayList<Cart> sessionCarts = (ArrayList<Cart>) request.getSession().getAttribute("sessionCart");

                    if (sessionCarts != null) {
                        boolean removed = false;
                        Iterator<Cart> iterator = sessionCarts.iterator();
                        while (iterator.hasNext()) {
                            
                            Cart cart = iterator.next();
                            if (cart.getStock().getId() == stockId) {
                                
                                iterator.remove();
                                removed = true;
                                break;
                            }
                        }

                        if (removed) {
                            responseObject.addProperty("status", true);
                            responseObject.addProperty("message", "Item removed from cart.");
                        } else {
                            responseObject.addProperty("message", "Cart item not found in session.");
                        }

                    } else {
                        responseObject.addProperty("message", "Session cart is empty.");
                    }
                }

            } catch (Exception e) {
                if (tx != null) {
                    tx.rollback();
                }
                e.printStackTrace();
                responseObject.addProperty("message", "Error removing item.");
            } finally {
                s.close();
            }
        }

        response.setContentType("application/json");
        response.getWriter().write(new Gson().toJson(responseObject));
    }

}
