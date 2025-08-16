
package controller;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import hibernate.HibernateUtil;
import hibernate.Stock;
import java.io.IOException;
import java.io.PrintWriter;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import model.Util;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;

@WebServlet(name = "DeleteProduct", urlPatterns = {"/DeleteProduct"})
public class DeleteProduct extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        System.out.println("Delete product servlet");

        
        JsonObject responseObject = new JsonObject();
        responseObject.addProperty("status", false);

        SessionFactory sf = HibernateUtil.getSessionFactory();
        Session s = sf.openSession();
        Transaction tx = null;

        try {
            String stockId = request.getParameter("stockId");

            System.out.println("Received parameters:");
            System.out.println("stockId=" + stockId);

            // Validations
            if (request.getSession().getAttribute("admin") == null) {
                responseObject.addProperty("message", "Please sign in!");
                System.out.println("Validation failed: user not signed in");
                writeResponse(response, responseObject);
                return;
            }
            if (!Util.isInteger(stockId) || Integer.parseInt(stockId) == 0) {
                responseObject.addProperty("message", "Please select a valid stock!");
                System.out.println("Validation failed: invalid brandId");
                writeResponse(response, responseObject);
                return;
            }

            Stock stock = (Stock) s.get(Stock.class, Integer.parseInt(stockId));
            if (stock == null) {
                responseObject.addProperty("message", "Invalid stock selected!");
                System.out.println("Stock not found for id: " + stock);
                writeResponse(response, responseObject);
                return;
            }
            
            s.delete(stock);

            tx = s.beginTransaction();
            

            responseObject.addProperty("status", true);
            responseObject.addProperty("message", "Product deleted successfully!");

        } catch (Exception e) {
            if (tx != null) {
                tx.rollback();
            }
            System.out.println("Error saving product: " + e.getMessage());
            e.printStackTrace();
            responseObject.addProperty("message", "Error saving product: " + e.getMessage());
        } finally {
            if (s != null && s.isOpen()) {
                s.close();
            }
            System.out.println("Session closed");
        }

        writeResponse(response, responseObject);
    }
    
    private void writeResponse(HttpServletResponse response, JsonObject responseObject) throws IOException {
        response.setContentType("application/json");
        response.getWriter().write(new Gson().toJson(responseObject));
    }

}
