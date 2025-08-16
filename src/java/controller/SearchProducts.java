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
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;

@WebServlet(name = "SearchProducts", urlPatterns = {"/SearchProducts"})
public class SearchProducts extends HttpServlet {

    private static final int MAX_RESULT = 12;
    private static final int ACTIVE_ID = 1;

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

//        Gson gson = new Gson();
//
//        JsonObject responseObject = new JsonObject();
//        responseObject.addProperty("status", false);
//
//        JsonObject requestJsonObject = gson.fromJson(request.getReader(), JsonObject.class);
//
//        SessionFactory sf = HibernateUtil.getSessionFactory();
//        Session s = sf.openSession();
//
//        try {
//
//            Criteria c1 = s.createCriteria(Stock.class); // get all product for the filter
//            System.out.println(c1.list());
//
//            // Brand filter
//            if (requestJsonObject.has("brandName") && !requestJsonObject.get("brandName").isJsonNull()) {
//                String brandName = requestJsonObject.get("brandName").getAsString();
//                Criteria c2 = s.createCriteria(Brand.class);
//                c2.add(Restrictions.eq("name", brandName));
//                Brand brand = (Brand) c2.uniqueResult();
//
//                if (brand != null) {
//                    Criteria c3 = s.createCriteria(Model.class);
//                    c3.add(Restrictions.eq("brand", brand));
//                    List<Model> models = c3.list();
//
//                    if (!models.isEmpty()) {
//                        Criteria c4 = s.createCriteria(Product.class);
//                        c4.add(Restrictions.in("model", models));
//                        List<Product> productList1 = c4.list();
//
//                        if (!productList1.isEmpty()) {
//                            c1.add(Restrictions.in("product", productList1));
//                        }
//                    }
//                }
//            }
//
//            System.out.println(c1.list());
//            if (requestJsonObject.has("categoryName") && !requestJsonObject.get("categoryName").isJsonNull()) {
//                String categoryName = requestJsonObject.get("categoryName").getAsString();
//                Criteria c5 = s.createCriteria(Category.class);
//                c5.add(Restrictions.eq("name", categoryName));
//                Category category = (Category) c5.uniqueResult();
//
//                if (category != null) {
//                    Criteria c6 = s.createCriteria(Product.class);
//                    c6.add(Restrictions.eq("category", category));
//                    List<Product> productList2 = c6.list();
//
//                    if (!productList2.isEmpty()) {
//                        c1.add(Restrictions.in("product", productList2));
//                    }
//                }
//            }
//
//            System.out.println(c1.list());
//            if (requestJsonObject.has("colorName") && !requestJsonObject.get("colorName").isJsonNull()) {
//                String colorName = requestJsonObject.get("colorName").getAsString();
//                Criteria c6 = s.createCriteria(Color.class);
//                c6.add(Restrictions.eq("value", colorName));
//                Color color = (Color) c6.uniqueResult();
//
//                if (color != null) {
//                    c1.add(Restrictions.eq("color", color));
//                }
//            }
//            System.out.println(c1.list());
//
//            if (requestJsonObject.has("sizeValue") && !requestJsonObject.get("sizeValue").isJsonNull()) {
//                String sizeValue = requestJsonObject.get("sizeValue").getAsString();
//                Criteria c7 = s.createCriteria(Size.class);
//                c7.add(Restrictions.eq("value", sizeValue));
//                Size size = (Size) c7.uniqueResult();
//
//                if (size != null) {
//                    c1.add(Restrictions.eq("size", size));
//                }
//            }
//            System.out.println(c1.list());
//            if (requestJsonObject.has("priceStart") & requestJsonObject.has("priceEnd")) {
//                double priceStart = requestJsonObject.get("priceStart").getAsDouble();
//                double priceEnd = requestJsonObject.get("priceEnd").getAsDouble();
//
//                c1.add(Restrictions.ge("price", priceStart));
//                c1.add(Restrictions.le("price", priceEnd));
//            }
//            System.out.println(c1.list());
//            if (requestJsonObject.has("sortValue")) {
//
//                String sortValue = requestJsonObject.get("sortValue").getAsString();
//                if (sortValue.equals("Sort by Latest")) {
//                    c1.addOrder(Order.desc("id"));
//                } else if (sortValue.equals("Sort by Oldest")) {
//                    c1.addOrder(Order.asc("id"));
//                } else if (sortValue.equals("Sort By Name")) {
//                    c1.addOrder(Order.asc("title"));
//                } else if (sortValue.equals("Sort By Price")) {
//                    c1.addOrder(Order.asc("price"));
//                }
//
//            }
//            System.out.println(c1.list());
//
//            responseObject.addProperty("allProductCount", c1.list().size());
//
//            if (requestJsonObject.has("firstResult")) {
//                int firstResult = requestJsonObject.get("firstResult").getAsInt();
//                c1.setFirstResult(firstResult);
//                c1.setMaxResults(SearchProducts.MAX_RESULT);
//            }
//
//            // get filtered product list
//            Status status = (Status) s.get(Status.class, SearchProducts.ACTIVE_ID);
//
//            c1.add(Restrictions.eq("status", status));
//            List<Stock> stockList = c1.list();
//            System.out.println("Filtered stocks: " + stockList.size());
//            responseObject.add("stockList", gson.toJsonTree(stockList));
//            responseObject.addProperty("status", true);
//            //hibernate session clse
//
//            response.setContentType("application/json");
//            String toJson = gson.toJson(responseObject);
//            response.getWriter().write(toJson);
//            response.setContentType("application/json");
//            response.getWriter().write(gson.toJson(responseObject));
//
//        } catch (Exception e) {
//            e.printStackTrace();
//            responseObject.addProperty("message", "Something went wrong.");
//        } finally {
//            s.close();
//        }
        Gson gson = new Gson();
        JsonObject responseObject = new JsonObject();
        responseObject.addProperty("status", false);

        JsonObject requestJsonObject = gson.fromJson(request.getReader(), JsonObject.class);

        SessionFactory sf = HibernateUtil.getSessionFactory();
        Session s = sf.openSession();

        try {
            // Base Criteria with aliases for joins
            Criteria c1 = s.createCriteria(Stock.class, "stock")
                    .createAlias("stock.product", "product")
                    .createAlias("product.model", "model")
                    .createAlias("model.brand", "brand")
                    .createAlias("product.category", "category")
                    .createAlias("stock.color", "color")
                    .createAlias("stock.size", "size");

            // --- Brand filter ---
            if (requestJsonObject.has("brandName") && !requestJsonObject.get("brandName").isJsonNull()) {
                String brandName = requestJsonObject.get("brandName").getAsString();
                c1.add(Restrictions.eq("brand.name", brandName));
            }

            // --- Category filter ---
            if (requestJsonObject.has("categoryName") && !requestJsonObject.get("categoryName").isJsonNull()) {
                String categoryName = requestJsonObject.get("categoryName").getAsString();
                c1.add(Restrictions.eq("category.name", categoryName));
            }

            // --- Color filter ---
            if (requestJsonObject.has("colorName") && !requestJsonObject.get("colorName").isJsonNull()) {
                String colorName = requestJsonObject.get("colorName").getAsString();
                c1.add(Restrictions.eq("color.value", colorName));
            }

            // --- Size filter ---
            if (requestJsonObject.has("sizeValue") && !requestJsonObject.get("sizeValue").isJsonNull()) {
                String sizeValue = requestJsonObject.get("sizeValue").getAsString();
                c1.add(Restrictions.eq("size.value", sizeValue));
            }

            // --- Price range filter ---
            if (requestJsonObject.has("priceStart") && requestJsonObject.has("priceEnd")) {
                double priceStart = requestJsonObject.get("priceStart").getAsDouble();
                double priceEnd = requestJsonObject.get("priceEnd").getAsDouble();
                c1.add(Restrictions.ge("stock.price", priceStart));
                c1.add(Restrictions.le("stock.price", priceEnd));
            }

            // --- Sorting ---
            if (requestJsonObject.has("sortValue") && !requestJsonObject.get("sortValue").isJsonNull()) {
                String sortValue = requestJsonObject.get("sortValue").getAsString();
                if ("Sort by Latest".equals(sortValue)) {
                    c1.addOrder(Order.desc("stock.id"));
                } else if ("Sort by Oldest".equals(sortValue)) {
                    c1.addOrder(Order.asc("stock.id"));
                } else if ("Sort By Name".equals(sortValue)) {
                    c1.addOrder(Order.asc("product.title"));
                } else if ("Sort By Price".equals(sortValue)) {
                    c1.addOrder(Order.asc("stock.price"));
                }
            }

            // --- Status filter ---
            Status status = (Status) s.get(Status.class, SearchProducts.ACTIVE_ID);
            c1.add(Restrictions.eq("stock.status", status));

            // --- Get total count before pagination ---
            int totalCount = ((Long) c1.setProjection(Projections.rowCount()).uniqueResult()).intValue();

            // Reset projection to get actual results
            c1.setProjection(null);
            c1.setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY);

            // --- Pagination ---
            if (requestJsonObject.has("firstResult")) {
                int firstResult = requestJsonObject.get("firstResult").getAsInt();
                c1.setFirstResult(firstResult);
                c1.setMaxResults(SearchProducts.MAX_RESULT);
            }

            // Execute final query once
            List<Stock> stockList = c1.list();

            // --- Response ---
            responseObject.addProperty("allProductCount", totalCount);
            responseObject.add("stockList", gson.toJsonTree(stockList));
            responseObject.addProperty("status", true);

        } catch (Exception e) {
            e.printStackTrace();
            responseObject.addProperty("message", "Something went wrong.");
        } finally {
            s.close();
        }

        // Send JSON response once
        response.setContentType("application/json");
        response.getWriter().write(gson.toJson(responseObject));

    }

}
