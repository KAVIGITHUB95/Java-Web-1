
package controller;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import hibernate.Cart;
import hibernate.HibernateUtil;
import hibernate.User;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
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

@WebServlet(name = "UpdateCartQty", urlPatterns = {"/UpdateCartQty"})
public class UpdateCartQty extends HttpServlet {

    @Override
    
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        JsonObject responseObject = new JsonObject();
        responseObject.addProperty("status", false);

        String stockIdstr = request.getParameter("stockId");
        String qtystr = request.getParameter("qty");

        if (!Util.isInteger(stockIdstr)) {
            responseObject.addProperty("message", "Invalid product Id!");
        } else if (!Util.isInteger(qtystr)) {
            responseObject.addProperty("message", "Invalid product Quantity!");
        } else {
            int stockId = Integer.parseInt(stockIdstr);
            int qty = Integer.parseInt(qtystr);

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
                        if (qty <= cart.getStock().getQty()) {
                            cart.setQty(qty);
                            s.update(cart);
                            tx.commit();
                            responseObject.addProperty("status", true);
                            responseObject.addProperty("message", "Quantity updated.");
                        } else {
                            cart.setQty(cart.getStock().getQty());
                            responseObject.addProperty("message", "Not enough stock.");
                        }
                    } else {
                        responseObject.addProperty("message", "Cart item not found.");
                    }
                } else {
                    // Handle sessionCart for guests
                    ArrayList<Cart> sessionCarts = (ArrayList<Cart>) request.getSession().getAttribute("sessionCart");
                    if (sessionCarts != null) {
                        for (Cart cart : sessionCarts) {
                            if (cart.getStock().getId() == stockId) {
                                if (qty <= cart.getStock().getQty()) {
                                    cart.setQty(qty);
                                    responseObject.addProperty("status", true);
                                    responseObject.addProperty("message", "Quantity updated.");
                                } else {
                                    cart.setQty(cart.getStock().getQty());
                                    responseObject.addProperty("message", "Not enough stock.");
                                }
                                break;
                            }
                        }
                    }
                }

            } catch (Exception e) {
                tx.rollback();
                e.printStackTrace();
                responseObject.addProperty("message", "Error occurred.");
            } finally {
                s.close();
            }
        }

        response.setContentType("application/json");
        response.getWriter().write(new Gson().toJson(responseObject));
    }

}
