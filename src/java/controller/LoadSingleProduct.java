
package controller;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import hibernate.Brand;
import hibernate.HibernateUtil;
import hibernate.Model;
import hibernate.Product;
import hibernate.Stock;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import model.Util;
import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.Restrictions;


@WebServlet(name = "LoadSingleProduct", urlPatterns = {"/LoadSingleProduct"})
public class LoadSingleProduct extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        Gson gson = new Gson();
        JsonObject responseObject = new JsonObject();
        responseObject.addProperty("status", false);

        String stockIdParam = request.getParameter("id");
        if (Util.isInteger(stockIdParam)) {
            SessionFactory sf = HibernateUtil.getSessionFactory();
            Session session = sf.openSession();
            try{

                int stockId = Integer.parseInt(stockIdParam);
                Stock selectedStock = (Stock) session.get(Stock.class, stockId);

                if (selectedStock != null && "Active".equalsIgnoreCase(selectedStock.getStatus().getValue())) {

                    // Get the brand of the selected stock via model -> brand
                    Brand brand = selectedStock.getProduct().getModel().getBrand();

                    // Get all models under the same brand
                    Criteria c1 = session.createCriteria(Model.class);
                    c1.add(Restrictions.eq("brand", brand));
                    
                    List<Model> modelList = c1.list();
                    
                    // Get all products under the same brand
                    Criteria c2 = session.createCriteria(Product.class);
                    c2.add(Restrictions.in("model", modelList));
                    List<Product> productList = c2.list();

                    // Get other active stocks for similar products (not the selected one)
                    Criteria stockCriteria = session.createCriteria(Stock.class);
                    stockCriteria.add(Restrictions.in("product", productList));
                    stockCriteria.add(Restrictions.ne("id", stockId));
                    stockCriteria.add(Restrictions.eq("status", selectedStock.getStatus()));
                    stockCriteria.setMaxResults(6);
                    List<Stock> similarStocks = stockCriteria.list();

                    responseObject.add("stock", gson.toJsonTree(selectedStock));
                    responseObject.add("stockList", gson.toJsonTree(similarStocks));
                    responseObject.addProperty("status", true);
                } else {
                    responseObject.addProperty("message", "Stock not active or not found.");
                }

            } catch (Exception e) {
                e.printStackTrace();
                responseObject.addProperty("message", "Server Error: " + e.getMessage());
            }
        } else {
            responseObject.addProperty("message", "Invalid stock ID");
        }

        response.setContentType("application/json");
        response.getWriter().write(gson.toJson(responseObject));
    }

}
