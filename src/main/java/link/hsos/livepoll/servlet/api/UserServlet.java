package link.hsos.livepoll.servlet.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import link.hsos.livepoll.service.VotingTokenService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
/**
 * Servlet zur Bereitstellung von Benutzerinformationen.
 *
 * <p>Unterstützt GET-Anfragen für Benutzerdaten und Token-Generierung.
 * Gibt signierte User-ID zurück.
 */
@WebServlet({"/api/user", "/api/user/", "/api/user/userId"})
public class UserServlet extends HttpServlet {

    private static final Logger logger = LoggerFactory.getLogger(UserServlet.class);
    private static final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String pathInfo = request.getRequestURI();
        logger.info("pathInfo: " + pathInfo);
        String createdBy = (String) request.getAttribute("createdBy");
        String givenName = (String) request.getAttribute("givenName");
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");


        if (pathInfo == null || pathInfo.equals("/api/user/")) {
            if (createdBy != null) {
                Map<String, Object> result = new HashMap<>();
                result.put("createdBy", createdBy);
                result.put("givenName", givenName);

                objectMapper.writeValue(response.getWriter(), result);
            } else  {
                response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                objectMapper.writeValue(response.getWriter(), Map.of("error", "Interner Server-Fehler"));
            }
        } else if (pathInfo.matches("/api/user/userId")) {
            try{
                Map<String, String> result = new HashMap<>();
                result.put("userId", VotingTokenService.getSignedUserId().getLeft());
                result.put("signature", VotingTokenService.getSignedUserId().getRight());
                objectMapper.writeValue(response.getWriter(), result);

            }catch (Exception e){
                response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                objectMapper.writeValue(response.getWriter(), Map.of("error", "Interner Server-Fehler"));
            }
        }


    }



}
