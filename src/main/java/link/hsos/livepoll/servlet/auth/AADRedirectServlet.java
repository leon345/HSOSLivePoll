// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package link.hsos.livepoll.servlet.auth;


import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import link.hsos.livepoll.servlet.auth.helpers.AuthException;
import link.hsos.livepoll.servlet.auth.helpers.AuthHelper;
import link.hsos.livepoll.servlet.auth.helpers.Config;
import link.hsos.livepoll.servlet.auth.helpers.IdentityContextAdapterServlet;

import java.io.IOException;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Quelle: Azure-Samples/ms-identity-msal-java-samples. (28. Mai 2025). Java. Microsoft Corporation. Zugegriffen: 23. Juli 2025. [Online]. Verfügbar unter: https://github.com/Azure-Samples/ms-identity-msal-java-samples
 * Klasse ist verfügbar unter: https://github.com/Azure-Samples/ms-identity-msal-java-samples/blob/main/3-java-servlet-web-app/1-Authentication/sign-in/src/main/java/com/microsoft/azuresamples/msal4j/authservlets/AADRedirectServlet.java
 *
 * This class defines the endpoint for processing the redirect from AAD MSAL
 * Java apps using this sample's paradigm will require this.
 */
@WebServlet(name = "AADRedirectServlet", urlPatterns = "/auth/redirect")
public class AADRedirectServlet extends HttpServlet {
    private static final Logger logger = Logger.getLogger(AADRedirectServlet.class.getName());

    @Override
    protected void doGet(final HttpServletRequest req, final HttpServletResponse resp)
            throws ServletException, IOException {
        logger.log(Level.FINE, "Request has come with params {0}", req.getQueryString());
        try {
            AuthHelper.processAADCallback(new IdentityContextAdapterServlet(req, resp));
            logger.log(Level.INFO, "redirecting to home page.");
            resp.sendRedirect(Config.DASHBOARD);
        } catch (AuthException ex) {
            logger.log(Level.WARNING, ex.getMessage());
            logger.log(Level.WARNING, Arrays.toString(ex.getStackTrace()));
            logger.log(Level.INFO, "redirecting to error page to display auth error to user.");
            resp.sendRedirect(resp.encodeRedirectURL(String.format(req.getContextPath() + "/auth_error_details?details=%s", ex.getMessage())));
        }
    }

}
