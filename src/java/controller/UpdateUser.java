package controller;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import hibernate.Address;
import hibernate.City;
import hibernate.Gender;
import hibernate.HibernateUtil;
import hibernate.User;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import model.Util;
import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.criterion.Restrictions;

@WebServlet(name = "UpdateUser", urlPatterns = {"/UpdateUser"})
public class UpdateUser extends HttpServlet {

    Gson gson = new Gson();

    @Override
    protected void doPut(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        JsonObject jsonObject = gson.fromJson(request.getReader(), JsonObject.class);

        String action = jsonObject.get("action").getAsString();

        switch (action) {
            case "updateUserInfo":
                updateUserInfo(jsonObject, request, response);
                System.out.println("updateUserInfo - switch");
                break;
            case "changePassword":
//            changePassword(jsonObject, request, response);
                break;
            case "updateAddress":
//            updateAddress(jsonObject, request, response);
                break;
            default:
                response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid action");
        }

//        String lineOne = userData.get("lineOne").getAsString();
//        String lineTwo = userData.get("lineTwo").getAsString();
//
//        String postalCode = userData.get("postalCode").getAsString();
//        int cityId = userData.get("cityId").getAsInt();
//
//        String currentPassword = userData.get("currentPassword").getAsString();
//
//        String newPassword = userData.get("newPassword").getAsString();
//        String confirmPassword = userData.get("confirmPassword").getAsString();
//
//        JsonObject responseObject = new JsonObject();
//        responseObject.addProperty("status", false);
//
//        if (lineOne.isEmpty()) {
//            responseObject.addProperty("message", "Enter address line one");
//        } else if (lineTwo.isEmpty()) {
//            responseObject.addProperty("message", "Enter address line two");
//        } else if (postalCode.isEmpty()) {
//            responseObject.addProperty("message", "Enter address line one");
//        } else if (!Util.isCodeValid(postalCode)) {
//            responseObject.addProperty("message", "Enter address line one");
//        } else if (cityId == 0) {
//            responseObject.addProperty("message", "Select a city");
//        } else if (lineOne.isEmpty()) {
//            responseObject.addProperty("message", "Enter address line one");
//        } else if (currentPassword.isEmpty()) {
//            responseObject.addProperty("message", "Password can not be empty!");
//        } else if (!newPassword.isEmpty() && !Util.isPasswordValid(newPassword)) {
//            responseObject.addProperty("message", "The password must contains at least uppercase, lowercase,"
//                    + " number, special character and to be minimum eight characters long!");
//        } else if (!newPassword.isEmpty() && newPassword.equals(currentPassword)) {
//            responseObject.addProperty("message", "The new password cannot be same as the old password");
//        } else if (!confirmPassword.isEmpty() && !Util.isPasswordValid(confirmPassword)) {
//            responseObject.addProperty("message", "The password must contains at least uppercase, lowercase,"
//                    + " number, special character and to be minimum eight characters long!");
//        } else if (!confirmPassword.equals(newPassword)) {
//            responseObject.addProperty("message", "Confirmed password does not match with the new entered password");
//        } else {
//            HttpSession ses = request.getSession();
//            if (ses.getAttribute("user") != null) {
//
//                User u = (User) ses.getAttribute("user");
//
//                SessionFactory sf = HibernateUtil.getSessionFactory();
//                Session s = sf.openSession();
//                Criteria c = s.createCriteria(User.class);
//                c.add(Restrictions.eq("email", u.getEmail()));//session user email
//                if (!c.list().isEmpty()) {
//                    User u1 = (User) c.list().get(0); //db user
//
////                    u1.setFirst_name(firstName);
////                    u1.setLast_name(lastName);
//                    if (!confirmPassword.isEmpty()) {
//                        u1.setPassword(confirmPassword);
//                    } else {
//                        u1.setPassword(currentPassword);
//                    }
//
//                    City city = (City) s.load(City.class, cityId); // primary key search
//                    Address address = new Address();
//                    address.setLineOne(lineOne);
//                    address.setLineTwo(lineTwo);
//                    address.setPostalCode(postalCode);
//                    address.setCity(city);
//                    address.setUser(u1);
//
//                    //session-management
//                    ses.setAttribute("user", u1);
//
//                    //session-management-end
//                    s.merge(u1);
//                    s.save(address);
//
//                    s.beginTransaction().commit();
//                    responseObject.addProperty("status", true);
//                    responseObject.addProperty("message", "User profile details update successfully!");
//                    s.close();
//                }
//
//            }
//        }
    }

    private void addAddress(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        // Read request parameters, create new address
    }

    private void updateUserInfo(JsonObject data, HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        JsonObject responseObject = new JsonObject();
        System.out.println("updateUserInfo - 2");

        try {
            String firstName = data.get("firstName").getAsString();
            String lastName = data.get("lastName").getAsString();
            String email = data.get("email").getAsString();
            String mobile = data.get("mobile").getAsString();
            int genderId = data.get("genderId").getAsInt();

            if (firstName.isEmpty()) {
                responseObject.addProperty("message", "First Name can not be empty!");
            } else if (lastName.isEmpty()) {
                responseObject.addProperty("message", "Last Name can not be empty!");
            } else {

                HttpSession ses = request.getSession();
                if (ses.getAttribute("user") != null) {

                    User u = (User) ses.getAttribute("user");

                    SessionFactory sf = HibernateUtil.getSessionFactory();
                    Session s = sf.openSession();
                    Criteria c = s.createCriteria(User.class);
                    c.add(Restrictions.eq("email", u.getEmail()));//session user email
                    if (!c.list().isEmpty()) {
                        User u1 = (User) c.list().get(0); //db user

                        u1.setFirst_name(firstName);
                        u1.setLast_name(lastName);
                        u1.setEmail(email);
                        u1.setMobile(mobile);

                        Gender gender = (Gender) s.get(Gender.class, genderId);
                        u1.setGender(gender);

                        s.update(u1);
                        s.beginTransaction().commit();

                        ses.setAttribute("user", u1);

                        responseObject.addProperty("status", "success");
                        responseObject.addProperty("message", "User information updated.");
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            responseObject.addProperty("status", "error");
            responseObject.addProperty("message", "Failed to update user.");
        }

        // Write response
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write(gson.toJson(responseObject));
    }

    private void changePassword(JsonObject data, HttpServletResponse response)
            throws IOException {
        // Validate and update password
    }

    private void updateAddress(JsonObject data, HttpServletResponse response)
            throws IOException {
        // Update the address
    }

}
