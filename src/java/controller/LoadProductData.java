package controller;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import hibernate.Brand;
import hibernate.Category;
import hibernate.Color;
import hibernate.HibernateUtil;
import hibernate.Model;
import hibernate.Product;
import hibernate.Size;
import hibernate.Status;
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

@WebServlet(name = "LoadProductData", urlPatterns = {"/LoadProductData"})
public class LoadProductData extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        System.out.println("Ok");

        JsonObject responseObject = new JsonObject();
        responseObject.addProperty("status", false);

        SessionFactory sf = HibernateUtil.getSessionFactory();
        Session s = null;
        Gson gson = new Gson();

        try {
            s = sf.openSession();

            // get brands
            Criteria c1 = s.createCriteria(Brand.class);
            List<Brand> brandList = c1.list();

            // get models
            Criteria c2 = s.createCriteria(Model.class);
            List<Model> modelList = c2.list();

            // get category
            Criteria c3 = s.createCriteria(Category.class);
            List<Category> categoryList = c3.list();

            // get products
            Criteria c4 = s.createCriteria(Product.class);
            List<Product> productList = c4.list();

            // get colors
            Criteria c5 = s.createCriteria(Color.class);
            List<Color> colorList = c5.list();

            // get sizes
            Criteria c6 = s.createCriteria(Size.class);
            List<Size> sizeList = c6.list();

            // get status
            Criteria c7 = s.createCriteria(Status.class);
            List<Status> statusList = c7.list();

            // prepare response
            responseObject.add("brandList", gson.toJsonTree(brandList));
            responseObject.add("modelList", gson.toJsonTree(modelList));
            responseObject.add("categoryList", gson.toJsonTree(categoryList));
            responseObject.add("productList", gson.toJsonTree(productList));
            responseObject.add("colorList", gson.toJsonTree(colorList));
            responseObject.add("sizeList", gson.toJsonTree(sizeList));
            responseObject.add("statusList", gson.toJsonTree(statusList));

            responseObject.addProperty("status", true);

        } catch (Exception e) {
            e.printStackTrace(); // Log to server console
            responseObject.addProperty("error", e.getMessage());
        } finally {
            if (s != null) {
                s.close();
            }
        }

// send JSON response
        response.setContentType("application/json");
        response.getWriter().write(gson.toJson(responseObject));
    }

}
