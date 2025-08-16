
package controller;

import com.google.gson.Gson;
import hibernate.Gender;
import hibernate.HibernateUtil;
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

@WebServlet(name = "LoadGender", urlPatterns = {"/LoadGender"})
public class LoadGender extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        SessionFactory sf = HibernateUtil.getSessionFactory();
        
        Session s = sf.openSession();
        
        Criteria c = s.createCriteria(Gender.class);
        List<Gender> genderList = c.list();
        Gson gson = new Gson();
        
        response.setContentType("application/json");
        response.getWriter().write(gson.toJson(genderList));
        
        System.out.println("Gender");
    }

}
