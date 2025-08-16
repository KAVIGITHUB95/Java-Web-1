package controller;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import hibernate.Address;
import hibernate.City;
import hibernate.Gender;
import hibernate.HibernateUtil;
import hibernate.User;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.List;
import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
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

@MultipartConfig
@WebServlet(name = "UserAccount", urlPatterns = {"/UserAccount"})
public class UserAccount extends HttpServlet {

    Gson gson = new Gson();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        HttpSession ses = request.getSession(false);
        if (ses != null && ses.getAttribute("user") != null) {
            User user = (User) ses.getAttribute("user");

            JsonObject responseObject = new JsonObject();

            responseObject.addProperty("firstName", user.getFirst_name());
            responseObject.addProperty("lastName", user.getLast_name());
            responseObject.addProperty("email", user.getEmail());
            responseObject.addProperty("mobile", user.getMobile());
            responseObject.addProperty("gender", user.getGender().getId());
            responseObject.addProperty("password", user.getPassword());

            String since = new SimpleDateFormat("MMM yyyy").format(user.getCreated_at());

            responseObject.addProperty("since", since);

            SessionFactory sf = HibernateUtil.getSessionFactory();
            Session s = sf.openSession();

            Criteria c = s.createCriteria(Address.class);
            c.add(Restrictions.eq("user", user));

            if (!c.list().isEmpty()) {
                List<Address> addressList = c.list();
                responseObject.add("addressList", gson.toJsonTree(addressList));
            }

            String toJson = gson.toJson(responseObject);
            response.setContentType("application/json");
            response.getWriter().write(toJson);
        
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String action = request.getParameter("action");

        // Case 1: FormData (e.g., updateUserInfo)
        if ("updateUserInfo".equals(action)) {
            updateUserInfo(request, response);
            return;
        }

        // Case 2: JSON (e.g., addAddress)
        if (action == null) {
            // Try parsing JSON if action param not found (likely JSON body)
            try {
                JsonObject userData = gson.fromJson(request.getReader(), JsonObject.class);
                if (userData != null && userData.has("action")) {
                    action = userData.get("action").getAsString();

                    if ("addAddress".equals(action)) {
                        addAddress(userData, request, response);
                        return;
                    }
                } else {
                    response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Missing 'action' in JSON body");
                }
            } catch (Exception e) {
                response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid JSON format");
                e.printStackTrace();
            }
        } else {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Unsupported action: " + action);
        }
    }

    @Override
    protected void doPut(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        JsonObject responseObject = new JsonObject();
        responseObject.addProperty("status", false);
        System.out.println("ChangePassword");
        Session s = null;

        JsonObject userData = gson.fromJson(request.getReader(), JsonObject.class);

        try {

            String currentPassword = userData.get("currentPassword").getAsString();

            String newPassword = userData.get("newPassword").getAsString();
            String confirmPassword = userData.get("confirmPassword").getAsString();

            if (currentPassword.isEmpty()) {
                responseObject.addProperty("message", "Password can not be empty!");
            } else if (!newPassword.isEmpty() && !Util.isPasswordValid(newPassword)) {
                responseObject.addProperty("message", "The password must contains at least uppercase, lowercase,"
                        + " number, special character and to be minimum eight characters long!");
            } else if (!newPassword.isEmpty() && newPassword.equals(currentPassword)) {
                responseObject.addProperty("message", "The new password cannot be same as the old password");
            } else if (!confirmPassword.isEmpty() && !Util.isPasswordValid(confirmPassword)) {
                responseObject.addProperty("message", "The password must contains at least uppercase, lowercase,"
                        + " number, special character and to be minimum eight characters long!");
            } else if (!confirmPassword.equals(newPassword)) {
                responseObject.addProperty("message", "Confirmed password does not match with the new entered password");
            } else {
                HttpSession ses = request.getSession();
                if (ses.getAttribute("user") != null) {

                    User u = (User) ses.getAttribute("user");

                    SessionFactory sf = HibernateUtil.getSessionFactory();
                    s = sf.openSession();
                    Criteria c = s.createCriteria(User.class);
                    c.add(Restrictions.eq("email", u.getEmail()));//session user email
                    if (!c.list().isEmpty()) {
                        User u1 = (User) c.list().get(0); //db user

                        if (!confirmPassword.isEmpty()) {
                            u1.setPassword(confirmPassword);
                        } else {
                            u1.setPassword(currentPassword);
                        }

                        //session-management
                        ses.setAttribute("user", u1);

                        //session-management-end
                        s.merge(u1);

                        s.beginTransaction().commit();
                        responseObject.addProperty("status", true);
                        responseObject.addProperty("message", "Your password is changed!");
                    }

                } else {
                    responseObject.addProperty("message", "Session expired or user not logged in -1");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();

//            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            responseObject.addProperty("message", "Failed to update user.");
        } finally {
            if (s != null && s.isOpen()) {
                s.close();
            }
        }

        sendJson(response, responseObject);
        return;
    }

    private void addAddress(JsonObject userData, HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        // Read request parameters, create new address

        JsonObject responseObject = new JsonObject();
        responseObject.addProperty("status", false);
        System.out.println("addAddress -2");

        Session s = null;

        try {
            String lineOne = userData.get("lineOne").getAsString();
            String lineTwo = userData.get("lineTwo").getAsString();
            String postalCode = userData.get("postalCode").getAsString();
            int cityId = userData.get("cityId").getAsInt();

            if (lineOne.isEmpty()) {
                responseObject.addProperty("message", "Enter address line one");
            } else if (!Util.isAddressLineValid(lineOne)) {
                responseObject.addProperty("message", "Invalid characters in address line one");
            } else if (lineTwo.isEmpty()) {
                responseObject.addProperty("message", "Enter address line two");
            } else if (!Util.isAddressLineValid(lineTwo)) {
                responseObject.addProperty("message", "Invalid characters in address line two");
            } else if (postalCode.isEmpty()) {
                responseObject.addProperty("message", "Enter postal code");
            } else if (!Util.isCodeValid(postalCode)) {
                responseObject.addProperty("message", "Postal code must be 4–5 digits only");
            } else if (cityId == 0) {
                responseObject.addProperty("message", "Select a city");
            } else {
                HttpSession ses = request.getSession();
                if (ses.getAttribute("user") != null) {

                    User u = (User) ses.getAttribute("user");

                    SessionFactory sf = HibernateUtil.getSessionFactory();
                    s = sf.openSession();
                    Criteria c = s.createCriteria(User.class);
                    c.add(Restrictions.eq("email", u.getEmail()));//session user email
                    if (!c.list().isEmpty()) {
                        User u1 = (User) c.list().get(0); //db user

                        City city = (City) s.load(City.class, cityId); // primary key search
                        Address address = new Address();
                        address.setLineOne(lineOne);
                        address.setLineTwo(lineTwo);
                        address.setPostalCode(postalCode);
                        address.setCity(city);
                        address.setUser(u1);

                        //session-management
                        ses.setAttribute("user", u1);

                        //session-management-end
                        s.merge(u1);
                        s.save(address);

                        s.beginTransaction().commit();
                        responseObject.addProperty("status", true);
                        responseObject.addProperty("message", "User profile details update successfully!");
                    } else {
                        responseObject.addProperty("message", "User not found in database.");

                    }

                } else {
                    responseObject.addProperty("message", "Session expired or user not logged in -1");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();

//            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            responseObject.addProperty("message", "Failed to update user.");
        } finally {
            if (s != null && s.isOpen()) {
                s.close();
            }
        }

        sendJson(response, responseObject);
        return;

    }

    private void updateUserInfo(HttpServletRequest request, HttpServletResponse response)
            throws IOException {

        JsonObject responseObject = new JsonObject();
        responseObject.addProperty("status", false);
        System.out.println("updateUserInfo - 2");

        Session s = null;

        try {
            String firstName = request.getParameter("firstName");
            String lastName = request.getParameter("lastName");
            String email = request.getParameter("email");
            String mobile = request.getParameter("mobile");
            String genderId = request.getParameter("genderId");

            // Input validation
            if (firstName == null || firstName.isEmpty()) {
                responseObject.addProperty("message", "First Name cannot be empty!");
            } else if (!Util.isNameValid(firstName)) {
                responseObject.addProperty("message", "Invalid First Name format!");
            } else if (lastName == null || lastName.isEmpty()) {
                responseObject.addProperty("message", "Last Name cannot be empty!");
            } else if (!Util.isNameValid(lastName)) {
                responseObject.addProperty("message", "Invalid Last Name format!");
            } else if (email == null || email.isEmpty()) {
                responseObject.addProperty("message", "Email cannot be empty!");
            } else if (!Util.isEmailValidation(email)) {
                responseObject.addProperty("message", "Invalid email address!");
            } else if (mobile == null || mobile.isEmpty()) {
                responseObject.addProperty("message", "Mobile number cannot be empty!");
            } else if (!Util.isMobileValid(mobile)) {
                responseObject.addProperty("message", "Invalid Sri Lankan mobile number!");
            } else if (!Util.isInteger(genderId)) {
                responseObject.addProperty("message", "Invalid Gender!");
            } else if (Integer.parseInt(genderId) == 0) {
                responseObject.addProperty("message", "Please select a gender!");
            } else {
                HttpSession ses = request.getSession();
                if (ses.getAttribute("user") != null) {
                    User user = (User) ses.getAttribute("user");

                    if (user != null) {
                        SessionFactory sf = HibernateUtil.getSessionFactory();
                        s = sf.openSession();

                        Criteria c = s.createCriteria(User.class);
                        c.add(Restrictions.eq("email", user.getEmail()));
                        if (!c.list().isEmpty()) {
                            User u1 = (User) c.list().get(0);

                            u1.setFirst_name(firstName);
                            u1.setLast_name(lastName);
                            u1.setEmail(email);
                            u1.setMobile(mobile);

                            Gender gender = (Gender) s.get(Gender.class, Integer.valueOf(genderId));
                            if (gender != null) {
                                u1.setGender(gender);
                            } else {
                                responseObject.addProperty("message", "Invalid Gender!");

                            }

                            Transaction tx = s.beginTransaction();
                            s.update(u1);
                            tx.commit();

                            ses.setAttribute("user", u1);

                            responseObject.addProperty("status", true);
                            responseObject.addProperty("message", "User information updated.");
                        } else {
                            responseObject.addProperty("message", "User not found in database.");

                        }
                    } else {
                        responseObject.addProperty("message", "Session expired or user not logged in -1");
                    }

                } else {
                    response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                    responseObject.addProperty("message", "Session expired or user not logged in -2");
                }
            }

        } catch (Exception e) {
            e.printStackTrace();

//            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            responseObject.addProperty("message", "Failed to update user.");
        } finally {
            if (s != null && s.isOpen()) {
                s.close();
            }
        }

        sendJson(response, responseObject);
        return;
    }

    private void sendJson(HttpServletResponse response, JsonObject obj) throws IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write(gson.toJson(obj));

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
