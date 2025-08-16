package controller;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import hibernate.Brand;
import hibernate.Category;
import hibernate.Color;
import hibernate.HibernateUtil;
import hibernate.Model;
import hibernate.OrderItem;
import hibernate.Orders;
import hibernate.Product;
import hibernate.Size;
import hibernate.Status;
import hibernate.Stock;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.servlet.http.Part;
import model.Util;
import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.criterion.Restrictions;

@MultipartConfig

@WebServlet(name = "GetSavedData", urlPatterns = {"/GetSavedData"})
public class GetSavedData extends HttpServlet {

    private static final int PENDING_STATUS_ID = 1;

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String stockIdstr = request.getParameter("stockId");

        Gson gson = new Gson();
        JsonObject responseObject = new JsonObject();
        responseObject.addProperty("status", false);

        SessionFactory sf = HibernateUtil.getSessionFactory();
        Session s = sf.openSession();

        try {
            HttpSession ses = request.getSession(false);
            if (ses != null && ses.getAttribute("admin") != null) {

                if (stockIdstr == null) {
                    responseObject.addProperty("message", "Missing stockId parameter.");
                } else {
                    int stockId = Integer.parseInt(stockIdstr);
                    Stock stock = (Stock) s.get(Stock.class, stockId);

                    if (stock == null) {
                        responseObject.addProperty("message", "No stock found for given ID.");
                    } else {
                        // Convert stock (with product info inside) to JSON
                        JsonObject stockJson = gson.toJsonTree(stock).getAsJsonObject();

                        // If you want, you can also include product details explicitly:
                        if (stock.getProduct() != null) {
                            stockJson.add("product", gson.toJsonTree(stock.getProduct()));
                        }

                        responseObject.add("stock", stockJson);
                        responseObject.addProperty("status", true);
                    }
                }

            } else {
                responseObject.addProperty("message", "User not logged in.");
            }
        } catch (Exception e) {
            e.printStackTrace();
            responseObject.addProperty("message", "Something went wrong.");
        } finally {
            s.close();
        }

        response.setContentType("application/json");
        response.getWriter().write(gson.toJson(responseObject));
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        System.out.println("UpdateProduct product servlet");

        JsonObject responseObject = new JsonObject();
        responseObject.addProperty("status", false);

        SessionFactory sf = HibernateUtil.getSessionFactory();
        Session s = sf.openSession();
        Transaction tx = null;

        try {
            String stockId = request.getParameter("stockId");
            String productId = request.getParameter("productId");
            String statusId = request.getParameter("statusId");
            String sizeId = request.getParameter("sizeId");
            String colorId = request.getParameter("colorId");
            String title = request.getParameter("title");
            String description = request.getParameter("description");
            String price = request.getParameter("price");
            String qty = request.getParameter("qty");

            System.out.println("Received parameters:");
            System.out.println("stockId=" + stockId);
            System.out.println("productId=" + productId);
            System.out.println("statusId=" + statusId);
            System.out.println("sizeId=" + sizeId);
            System.out.println("colorId=" + colorId);
            System.out.println("title=" + title);
            System.out.println("description=" + description);
            System.out.println("price=" + price);
            System.out.println("qty=" + qty);

            Part part1 = request.getPart("image1");
            Part part2 = request.getPart("image2");
            Part part3 = request.getPart("image3");

            System.out.println("Image files received:");
            System.out.println("image1: " + (part1 != null ? part1.getSubmittedFileName() : "null"));
            System.out.println("image2: " + (part2 != null ? part2.getSubmittedFileName() : "null"));
            System.out.println("image3: " + (part3 != null ? part3.getSubmittedFileName() : "null"));

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
            if (!Util.isInteger(productId) || Integer.parseInt(productId) == 0) {
                responseObject.addProperty("message", "Please select a valid product!");
                System.out.println("Validation failed: invalid brandId");
                writeResponse(response, responseObject);
                return;
            }
            if (!Util.isInteger(statusId) || Integer.parseInt(statusId) == 0) {
                responseObject.addProperty("message", "Please select a valid status!");
                System.out.println("Validation failed: invalid brandId");
                writeResponse(response, responseObject);
                return;
            }
            if (title == null || title.trim().isEmpty()) {
                responseObject.addProperty("message", "Product title cannot be empty!");
                System.out.println("Validation failed: empty title");
                writeResponse(response, responseObject);
                return;
            }
            if (description == null || description.trim().isEmpty()) {
                responseObject.addProperty("message", "Product description cannot be empty!");
                System.out.println("Validation failed: empty description");
                writeResponse(response, responseObject);
                return;
            }
            if (!Util.isInteger(sizeId) || Integer.parseInt(sizeId) == 0) {
                responseObject.addProperty("message", "Please select a valid size!");
                System.out.println("Validation failed: invalid sizeId");
                writeResponse(response, responseObject);
                return;
            }
            if (!Util.isInteger(colorId) || Integer.parseInt(colorId) == 0) {
                responseObject.addProperty("message", "Please select a valid color!");
                System.out.println("Validation failed: invalid colorId");
                writeResponse(response, responseObject);
                return;
            }
            if (!Util.isDouble(price) || Double.parseDouble(price) <= 0) {
                responseObject.addProperty("message", "Invalid price!");
                System.out.println("Validation failed: invalid price");
                writeResponse(response, responseObject);
                return;
            }
            if (!Util.isInteger(qty) || Integer.parseInt(qty) <= 0) {
                responseObject.addProperty("message", "Invalid quantity!");
                System.out.println("Validation failed: invalid quantity");
                writeResponse(response, responseObject);
                return;
            }
            if (part1 == null || part1.getSubmittedFileName() == null || part1.getSubmittedFileName().isEmpty()) {
                responseObject.addProperty("message", "Product image one is required");
                System.out.println("Validation failed: missing image1");
                writeResponse(response, responseObject);
                return;
            }
            if (part2 == null || part2.getSubmittedFileName() == null || part2.getSubmittedFileName().isEmpty()) {
                responseObject.addProperty("message", "Product image two is required");
                System.out.println("Validation failed: missing image2");
                writeResponse(response, responseObject);
                return;
            }
            if (part3 == null || part3.getSubmittedFileName() == null || part3.getSubmittedFileName().isEmpty()) {
                responseObject.addProperty("message", "Product image three is required");
                System.out.println("Validation failed: missing image3");
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
            Product product = (Product) s.get(Product.class, Integer.parseInt(productId));
            if (product == null) {
                responseObject.addProperty("message", "Invalid stock selected!");
                System.out.println("Stock not found for id: " + stock);
                writeResponse(response, responseObject);
                return;
            }
            Size size = (Size) s.get(Size.class, Integer.parseInt(sizeId));
            if (size == null) {
                responseObject.addProperty("message", "Invalid size selected!");
                System.out.println("Size not found for id: " + sizeId);
                writeResponse(response, responseObject);
                return;
            }
            Color color = (Color) s.get(Color.class, Integer.parseInt(colorId));
            if (color == null) {
                responseObject.addProperty("message", "Invalid color selected!");
                System.out.println("Color not found for id: " + colorId);
                writeResponse(response, responseObject);
                return;
            }
            Status status = (Status) s.get(Status.class, Integer.parseInt(statusId));
            if (status == null) {
                responseObject.addProperty("message", "Invalid status selected!");
                System.out.println("Status not found for id: " + statusId);
                writeResponse(response, responseObject);
                return;
            }
            product.setTitle(title);
            product.setDescription(description);
            s.update(product);
            s.flush();
            
            stock.setColor(color);
            stock.setSize(size);
            stock.setPrice(Double.valueOf(price));
            stock.setStatus(status);
            stock.setQty(Integer.parseInt(qty));
            
            s.update(stock);


            tx = s.beginTransaction();
            
            System.out.println("Product saved with id: " + product.getId());
            tx.commit();
            System.out.println("Stock saved successfully");

            String appPath = getServletContext().getRealPath("");
            String newPath = appPath.replace("build" + File.separator + "web", "web" + File.separator + "product-images");

            File productFolder = new File(newPath, String.valueOf(product.getId()));

            // Create folder if it doesn't exist
            if (!productFolder.exists()) {
                productFolder.mkdir();
                System.out.println("Product image directory created: " + productFolder.getAbsolutePath());
            } else {
                // If updating, optionally clean old images before saving new ones
                File[] oldFiles = productFolder.listFiles();
                if (oldFiles != null) {
                    for (File file : oldFiles) {
                        if (file.isFile()) {
                            file.delete();
                            System.out.println("Deleted old image: " + file.getName());
                        }
                    }
                }
            }

            // Copy new images (overwriting if same names)
            if (part1 != null) {
                Files.copy(part1.getInputStream(), new File(productFolder, "image1.png").toPath(),
                        StandardCopyOption.REPLACE_EXISTING);
            }
            if (part2 != null) {
                Files.copy(part2.getInputStream(), new File(productFolder, "image2.png").toPath(),
                        StandardCopyOption.REPLACE_EXISTING);
            }
            if (part3 != null) {
                Files.copy(part3.getInputStream(), new File(productFolder, "image3.png").toPath(),
                        StandardCopyOption.REPLACE_EXISTING);
            }

            System.out.println("Product images updated");

            responseObject.addProperty("status", true);
            responseObject.addProperty("message", "Product updated successfully!");

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

    @Override
    protected void doDelete(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {    
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

}
