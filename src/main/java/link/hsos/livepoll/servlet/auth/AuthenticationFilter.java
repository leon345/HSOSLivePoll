// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package link.hsos.livepoll.servlet.auth;


import jakarta.servlet.*;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import link.hsos.livepoll.service.VotingTokenService;
import link.hsos.livepoll.servlet.auth.helpers.Config;
import link.hsos.livepoll.servlet.auth.helpers.IdentityContextAdapterServlet;
import link.hsos.livepoll.servlet.auth.helpers.IdentityContextData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;


/**
 * Quelle: Azure-Samples/ms-identity-msal-java-samples. (28. Mai 2025). Java. Microsoft Corporation. Zugegriffen: 23. Juli 2025. [Online]. Verfügbar unter: https://github.com/Azure-Samples/ms-identity-msal-java-samples
 * Klasse verfügbar unter https://github.com/Azure-Samples/ms-identity-msal-java-samples/blob/main/3-java-servlet-web-app/1-Authentication/sign-in/src/main/java/com/microsoft/azuresamples/msal4j/authservlets/AuthenticationFilter.java
 *
 * Servlet-Filter für die Authentifizierung und Autorisierung von HTTP-Anfragen.
 */
@WebFilter(filterName = "AuthenticationFilter", urlPatterns = {"/api/*", "/dashboard", "/dashboard/*"})
public class AuthenticationFilter implements Filter {
    private static final Logger logger = LoggerFactory.getLogger(AuthenticationFilter.class);

    /**
     * MEthode wurde angepasst
     * Filtert eingehende HTTP-Anfragen basierend auf Authentifizierungsstatus.
     *
     * @param req HTTP-Request
     * @param res HTTP-Response
     * @param chain Filter-Chain für nachgelagerte Filter
     * @throws ServletException bei Filter-Fehlern
     * @throws IOException bei Ein-/Ausgabefehlern
     */
    @Override
    public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain)
            throws ServletException, IOException {
        HttpServletRequest request = (HttpServletRequest) req;
        HttpServletResponse response = (HttpServletResponse) res;

        String path = request.getRequestURI();

        IdentityContextData context = new IdentityContextAdapterServlet(request, response).getContext();

        if (Config.AUTH_DISABLED) {
            logger.info("Authentication disabled for development - allowing access to {}", request.getPathInfo());
            req.setAttribute("isAuthenticated", true);
            req.setAttribute("username", "Development User");
            req.setAttribute("createdBy", "Development User");
            chain.doFilter(request, response);
            return;
        }

        if (path.matches("/[^/]+/vote")) {
            String signature = request.getHeader("X-Voter-ID");
            String createdBy = request.getHeader("X-Signature");
            if (VotingTokenService.verify(createdBy, signature)) {
                request.setAttribute("createdBy", createdBy);
                chain.doFilter(request, response);
            } else {
                sendUnauthorized(response);
            }
        } else if (context.getAuthenticated()) {
            setAttributeToRequest(request, context);
            chain.doFilter(request, response);
        } else if (isPublicApi(request.getRequestURI(), request.getMethod())) {
            chain.doFilter(request, response);
        } else {
            response.sendRedirect("/auth/sign_in");
        }
    }
    /**
     * Methode wurde hinzugefügt
     * Sendet HTTP 401 Unauthorized Fehler an den Client.
     *
     * @param response HTTP-Response für Fehlerübertragung
     * @throws IOException bei Fehlern beim Senden der Antwort
     */
    void sendUnauthorized(HttpServletResponse response) throws IOException {
        response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
    }
    /**
     * Methode wurde hinzugefügt
     * Prüft ob ein API-Endpunkt öffentlich zugänglich ist.
     *
     * @param path URI-Pfad der Anfrage
     * @param method HTTP-Methode
     * @return true wenn öffentlich zugänglich
     */
    private boolean isPublicApi(String path, String method) {
        logger.info("isPublicApi path {}", path);
        String uuidPattern = "[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}";
        return (path.matches("^/api/polls/" + uuidPattern + "$") && "GET".equalsIgnoreCase(method)) ||
                (path.matches("^/api/polls/" + uuidPattern + "/vote$") && "POST".equalsIgnoreCase(method)) ||
                (path.matches("^/api/polls/" + uuidPattern + "/wait$") && "GET".equalsIgnoreCase(method)) ||
                (path.matches("^/api/user/userId") && ("GET".equalsIgnoreCase(method))) ||
                (path.matches("^/api/qrcode") && "GET".equalsIgnoreCase(method)) ||
                (path.matches("^/api/polls/shortcode/[^/]+") && "GET".equalsIgnoreCase(method));
    }

    /**
     * Methode wurde hinzugefügt
     * Extrahiert Benutzerinformationen aus dem Identity-Kontext und setzt diese als Request-Attribute.
     *
     * @param request HTTP-Request für Attribut-Zuweisung
     * @param context Identity-Kontext mit Authentifizierungsdaten
     */
    private void setAttributeToRequest(HttpServletRequest request, IdentityContextData context) {
        request.setAttribute("createdBy", context.getAccount().homeAccountId());
        String givenName = (String) context.getIdTokenClaims().get("given_name");
        if (givenName != null) {
            request.setAttribute("givenName", givenName);
        }
    }




}
