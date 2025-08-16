
package model;

import java.io.IOException;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

@WebFilter(filterName = "AdminSignInCheckFilter", urlPatterns = {"/admin-dashboard.html"})
public class AdminSignInCheckFilter implements Filter {

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse res = (HttpServletResponse) response;
        
        HttpSession ses = req.getSession(false);
        
        if (ses != null && ses.getAttribute("admin") != null) {
            
            chain.doFilter(request, response);
        
        } else {
            res.sendRedirect("admin-signin.html");
        }
    }

    @Override
    public void destroy() {
        
    }
    
}
