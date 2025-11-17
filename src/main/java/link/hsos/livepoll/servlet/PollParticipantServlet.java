package link.hsos.livepoll.servlet;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
/**
 * Controller für die Teilnehmeransicht einer Umfrage.
 * View: /poll.jsp
 */
public class PollParticipantServlet extends HttpServlet {
    
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        // Weiterleitung zur poll.jsp
        request.getRequestDispatcher("/poll.jsp").forward(request, response);
    }
} 