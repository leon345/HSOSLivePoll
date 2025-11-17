package link.hsos.livepoll.servlet.dashboard;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
/**
 * Controller für die Detailansicht einer Umfrage im Dashboard.
 * View: /dashboard/poll.jsp
 */
public class PollDetailServlet extends HttpServlet {
    
    private static final Logger logger = LoggerFactory.getLogger(PollDetailServlet.class);
    
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        try {
            response.setContentType("text/html;charset=UTF-8");
            request.getRequestDispatcher("/dashboard/poll.jsp").forward(request, response);
            
        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.setContentType("text/html;charset=UTF-8");
            response.getWriter().println("<html><body><h1>500 - Internal Server Error</h1><p>Fehler: " + e.getMessage() + "</p></body></html>");
        }
    }
    
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        // POST-Requests werden auch zur dashboard/poll.jsp weitergeleitet
        doGet(request, response);
    }
}