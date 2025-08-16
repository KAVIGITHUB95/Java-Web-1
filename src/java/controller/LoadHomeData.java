
package controller;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import hibernate.Brand;
import hibernate.HibernateUtil;
import hibernate.Product;
import hibernate.Status;
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
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;

@WebServlet(name = "LoadHomeData", urlPatterns = {"/LoadHomeData"})
public class LoadHomeData extends HttpServlet {

    private static final int ACTIVE_STATUS_ID = 1;

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        Gson gson = new Gson();
        JsonObject responseObject = new JsonObject();
        responseObject.addProperty("status", false);

        SessionFactory sf = HibernateUtil.getSessionFactory();
        Session session = sf.openSession();

        try {
            // 🔹 Load all brands
            List<Brand> brandList = session.createCriteria(Brand.class).list();
            responseObject.add("brandList", gson.toJsonTree(brandList));

            // 🔹 Load 8 active stock items (status = 1)
            Criteria stockCriteria = session.createCriteria(Stock.class);
            Status activeStatus = (Status) session.get(Status.class, ACTIVE_STATUS_ID);
            stockCriteria.add(Restrictions.eq("status", activeStatus));
            stockCriteria.addOrder(Order.asc("id"));
            stockCriteria.setFirstResult(0);
            stockCriteria.setMaxResults(8);

            List<Stock> stockList = stockCriteria.list();
            responseObject.add("stockList", gson.toJsonTree(stockList));

            // ✅ Finalize response
            responseObject.addProperty("status", true);
            response.setContentType("application/json");
            response.getWriter().write(gson.toJson(responseObject));

        } catch (Exception e) {
            e.printStackTrace();
            responseObject.addProperty("error", "Something went wrong.");
            response.getWriter().write(gson.toJson(responseObject));
        } finally {
            session.close();
        }
    }
}
