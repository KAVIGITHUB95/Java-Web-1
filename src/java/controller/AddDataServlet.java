
package controller;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import hibernate.Brand;
import hibernate.Category;
import hibernate.Color;
import hibernate.HibernateUtil;
import hibernate.Model;
import hibernate.Size;
import java.io.IOException;
import java.io.PrintWriter;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;

@WebServlet(name = "AddDataServlet", urlPatterns = {"/AddDataServlet"})
public class AddDataServlet extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        Gson gson = new Gson();
        JsonObject jsonResponse = new JsonObject();
        jsonResponse.addProperty("status", false);

        String type = request.getParameter("type");
        String value = request.getParameter("value"); // generic field for most types
        String brandIdParam = request.getParameter("brandId"); // for model

        SessionFactory sf = HibernateUtil.getSessionFactory();
        Session s = sf.openSession();
        Transaction tx = s.beginTransaction();

        try {
            switch (type) {
                case "brand": {
                    if (value == null || value.trim().isEmpty()) {
                        throw new Exception("Brand name cannot be empty");
                    }
                    Brand b = new Brand();
                    b.setName(value.trim());
                    s.save(b);
                    break;
                }
                case "category": {
                    if (value == null || value.trim().isEmpty()) {
                        throw new Exception("Category name cannot be empty");
                    }
                    Category c = new Category();
                    c.setName(value.trim());
                    s.save(c);
                    break;
                }
                case "model": {
                    if (value == null || value.trim().isEmpty()) {
                        throw new Exception("Model name cannot be empty");
                    }
                    if (brandIdParam == null || !brandIdParam.matches("\\d+")) {
                        throw new Exception("Valid Brand ID is required for Model");
                    }
                    int brandId = Integer.parseInt(brandIdParam);
                    Brand brand = (Brand) s.get(Brand.class, brandId);
                    if (brand == null) {
                        throw new Exception("Brand not found for Model");
                    }
                    Model m = new Model();
                    m.setName(value.trim());
                    m.setBrand(brand);
                    s.save(m);
                    break;
                }
                case "size": {
                    if (value == null || value.trim().isEmpty()) {
                        throw new Exception("Size cannot be empty");
                    }
                    Size sz = new Size();
                    sz.setValue(value.trim());
                    s.save(sz);
                    break;
                }
                case "color": {
                    if (value == null || value.trim().isEmpty()) {
                        throw new Exception("Color cannot be empty");
                    }
                    Color col = new Color();
                    col.setValue(value.trim());
                    s.save(col);
                    break;
                }
                default:
                    throw new Exception("Unknown type: " + type);
            }

            tx.commit();
            jsonResponse.addProperty("status", true);
            jsonResponse.addProperty("message", type + " added successfully!");

        } catch (Exception e) {
            if (tx != null) tx.rollback();
            jsonResponse.addProperty("message", e.getMessage());
        } finally {
            s.close();
        }

        response.setContentType("application/json");
        response.getWriter().write(gson.toJson(jsonResponse));
    }

}
