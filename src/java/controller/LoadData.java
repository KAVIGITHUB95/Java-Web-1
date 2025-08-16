
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

@WebServlet(name = "LoadData", urlPatterns = {"/LoadData"})
public class LoadData extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        
        JsonObject responseObject = new JsonObject();
        responseObject.addProperty("status", false);
        
        SessionFactory sf = HibernateUtil.getSessionFactory();
        
        Session s = sf.openSession();
        
        //get brands
        Criteria c1 = s.createCriteria(Brand.class);
        List<Brand> brandList = c1.list();
        //get brands
        
        
        //get models
        Criteria c2 = s.createCriteria(Product.class);
        List<Product> productList = c2.list();
        //get models
        
        //get categories
        Criteria c3 = s.createCriteria(Category.class);
        List<Category> categoryList = c3.list();
        //get categories
        
        //get colors
        Criteria c4 = s.createCriteria(Color.class);
        List<Color> colorList = c4.list();
        //get colors
        
        //get sizes
        
        Criteria c5 = s.createCriteria(Size.class);
        List<Size> sizeList = c5.list();
        //get sizes
        
        //get status
//        Status status = (Status) s.load(Status.class, 2);
//        Hibernate.initialize(status); // force fetch from DB
        Status status = (Status) s.get(Status.class, 1);
        Criteria c6 = s.createCriteria(Stock.class);
        c6.addOrder(Order.desc("id"));
        c6.add(Restrictions.eq("status", status));
        
        c6.setFirstResult(0);
        c6.setMaxResults(6);
        
        
        List<Stock> stockList = c6.list();
        //get status
        Gson gson = new Gson();
        
        responseObject.add("brandList", gson.toJsonTree(brandList));
        responseObject.add("productList", gson.toJsonTree(productList));
        responseObject.add("categoryList", gson.toJsonTree(categoryList));
        responseObject.add("colorList", gson.toJsonTree(colorList));
        responseObject.add("sizeList", gson.toJsonTree(sizeList));
        
        responseObject.add("productList", gson.toJsonTree(productList));
        responseObject.addProperty("allStockProductList", stockList.size());
        responseObject.add("stockList", gson.toJsonTree(stockList));
        
        responseObject.addProperty("status", true);
        
        
        response.setContentType("application/json");
        response.getWriter().write(gson.toJson(responseObject));
    }

}
