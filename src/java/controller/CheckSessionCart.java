
package controller;

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
import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.criterion.Restrictions;

@WebServlet(name = "CheckSessionCart", urlPatterns = {"/CheckSessionCart"})
public class CheckSessionCart extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        JsonObject responseObject = new JsonObject();
        responseObject.addProperty("status", false);
        User user = (User) request.getSession().getAttribute("user");

        if (user != null) {
            ArrayList<Cart> sessionCarts = (ArrayList<Cart>) request.getSession().getAttribute("sessionCart");

            if (sessionCarts != null && !sessionCarts.isEmpty()) {
                SessionFactory sf = HibernateUtil.getSessionFactory();
                Session s = sf.openSession();
                Transaction tr = s.beginTransaction();

                try {
                    for (Cart sessionCart : sessionCarts) {
                        Stock stock = (Stock) s.get(Stock.class, sessionCart.getStock().getId());

                        Criteria c1 = s.createCriteria(Cart.class);
                        c1.add(Restrictions.eq("user", user));
                        c1.add(Restrictions.eq("stock", stock));

                        Cart dbCart = (Cart) c1.uniqueResult();

                        if (dbCart == null) {
                            sessionCart.setUser(user);
                            s.save(sessionCart);
                        } else {
                            int updatedQty = sessionCart.getQty() + dbCart.getQty();
                            if (updatedQty <= stock.getQty()) {
                                dbCart.setQty(updatedQty);
                                s.update(dbCart);
                            }
                        }
                    }

                    tr.commit();
                    request.getSession().removeAttribute("sessionCart");

                } catch (Exception e) {
                    tr.rollback();
                    e.printStackTrace();
                } finally {
                    s.close();
                }
            }
        }
    }

}
