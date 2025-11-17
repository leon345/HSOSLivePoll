package link.hsos.livepoll.servlet.dashboard;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
/**
 * Controller für die Dashboard-Hauptseite.
 * View: /dashboard/dashboard.jsp
 */
public class DashboardServlet extends HttpServlet {
    
    private static final Logger logger = LoggerFactory.getLogger(DashboardServlet.class);
    
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        try {
            logger.info("DashboardServlet: GET-Request empfangen");

            response.setContentType("text/html;charset=UTF-8");

            logger.info("DashboardServlet: Weiterleitung zu dashboard.jsp");
            request.getRequestDispatcher("/dashboard/dashboard.jsp").forward(request, response);
            
        } catch (Exception e) {
            logger.error("Fehler im DashboardServlet", e);
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.setContentType("text/html;charset=UTF-8");
            response.getWriter().println("<html><body><h1>500 - Internal Server Error</h1><p>Fehler: " + e.getMessage() + "</p></body></html>");
        }
    }
    
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        doGet(request, response);
    }
} 