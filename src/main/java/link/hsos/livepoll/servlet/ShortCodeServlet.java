package link.hsos.livepoll.servlet;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import link.hsos.livepoll.model.Poll;
import link.hsos.livepoll.service.PollService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Optional;
/**
 * Ermöglicht den direkten Zugriff auf eine Umfrage über einen Short Code.
 */
public class ShortCodeServlet extends HttpServlet {
    
    private static final Logger logger = LoggerFactory.getLogger(ShortCodeServlet.class);
    private final PollService pollService;
    
    public ShortCodeServlet() {
        this.pollService = PollService.getInstance();
    }
    
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        String pathInfo = request.getPathInfo();
        String servletPath = request.getServletPath();
        String requestURI = request.getRequestURI();
        String contextPath = request.getContextPath();
        
        logger.info("ShortCodeServlet handling request: pathInfo={}, servletPath={}, requestURI={}, contextPath={}", 
                   pathInfo, servletPath, requestURI, contextPath);
        
        if (pathInfo == null || pathInfo.equals("/") || pathInfo.equals("")) {
            logger.info("Kein Short-Code angegeben, redirecte zur Startseite");
            response.sendRedirect(request.getContextPath() + "/index.jsp");
            return;
        }
        
        String shortCode = pathInfo.substring(1);
        
        logger.info("Verarbeite Short-Code: '{}'", shortCode);
        
        if (!shortCode.matches("[A-Z0-9]+")) {
            logger.warn("Ungültiger Short-Code: {} - enthält ungültige Zeichen", shortCode);
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Ungültiger Short-Code: " + shortCode);
            return;
        }
        
        if (shortCode.length() < 3 || shortCode.length() > 10) {
            logger.warn("Short-Code hat ungültige Länge: {} (Länge: {})", shortCode, shortCode.length());
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Short-Code hat ungültige Länge: " + shortCode);
            return;
        }
        
        try {
            logger.info("Suche Poll mit Short-Code: {}", shortCode);
            Optional<Poll> pollOpt = pollService.getPollByShortCode(shortCode);
            
            if (pollOpt.isPresent()) {
                Poll poll = pollOpt.get();
                
                logger.info("Weiterleitung zu /modules/voting.jsp mit Poll-ID: {}", poll.getId());
                response.sendRedirect(request.getContextPath() + "/modules/voting.jsp?code=" + poll.getId());
            } else {
                logger.warn("Short-Code nicht gefunden: {}", shortCode);
                response.sendError(HttpServletResponse.SC_NOT_FOUND, "Short-Code nicht gefunden: " + shortCode);
            }
        } catch (Exception e) {
            logger.error("Fehler beim Verarbeiten des Short-Codes: {}", shortCode, e);
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Interner Server-Fehler beim Verarbeiten des Short-Codes");
        }
    }
}
