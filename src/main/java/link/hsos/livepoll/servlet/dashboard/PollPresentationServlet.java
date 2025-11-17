package link.hsos.livepoll.servlet.dashboard;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
/**
 * Controller für die Präsentationsansicht einer Umfrage.
 * View: /presentation.jsp
 */
public class PollPresentationServlet extends HttpServlet {
    
    private static final Logger logger = LoggerFactory.getLogger(PollPresentationServlet.class);
    
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        try {
            logger.info("PollPresentationServlet: GET-Request empfangen");
            
            // Setze Content-Type für HTML
            response.setContentType("text/html;charset=UTF-8");
            
            // Weiterleitung zur presentation.jsp (Präsentations-Ansicht)
            logger.info("PollPresentationServlet: Weiterleitung zu presentation.jsp");
            request.getRequestDispatcher("/presentation.jsp").forward(request, response);
            
        } catch (Exception e) {
            logger.error("Fehler im PollPresentationServlet", e);
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.setContentType("text/html;charset=UTF-8");
            response.getWriter().println("<html><body><h1>500 - Internal Server Error</h1><p>Fehler: " + e.getMessage() + "</p></body></html>");
        }
    }
    
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        // POST-Requests werden auch zur presentation.jsp weitergeleitet
        doGet(request, response);
    }
}
