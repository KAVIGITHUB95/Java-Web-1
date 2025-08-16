
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





@WebFilter(filterName = "SignInCheckFilter", urlPatterns = {"/user-account.html"})
public class SignInCheckFilter implements Filter {

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse res = (HttpServletResponse) response;
        
        HttpSession ses = req.getSession(false);
        
        if (ses != null && ses.getAttribute("user") != null) {
            
            chain.doFilter(request, response);
        
        } else {
            res.sendRedirect("signin.html");
        }
    }

    @Override
    public void destroy() {
        
    }
    
}
