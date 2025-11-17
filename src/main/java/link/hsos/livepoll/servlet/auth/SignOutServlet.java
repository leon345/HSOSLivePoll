// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package link.hsos.livepoll.servlet.auth;


import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import link.hsos.livepoll.servlet.auth.helpers.AuthHelper;
import link.hsos.livepoll.servlet.auth.helpers.IdentityContextAdapterServlet;

import java.io.IOException;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Azure-Samples/ms-identity-msal-java-samples. (28. Mai 2025). Java. Microsoft Corporation. Zugegriffen: 23. Juli 2025. [Online]. Verfügbar unter: https://github.com/Azure-Samples/ms-identity-msal-java-samples
 * Klasse verfügbar unter https://github.com/Azure-Samples/ms-identity-msal-java-samples/blob/main/3-java-servlet-web-app/1-Authentication/sign-in/src/main/java/com/microsoft/azuresamples/msal4j/authservlets/SignOutServlet.java
 * This class defines the endpoint for processing sign out
 * MSAL Java apps using this sample's paradigm will require this.
 */
@WebServlet(name = "SignOutServlet", urlPatterns = "/auth/sign_out")
public class SignOutServlet extends HttpServlet {
    private static final Logger logger = Logger.getLogger(SignOutServlet.class.getName());

    @Override
    protected void doGet(final HttpServletRequest req, final HttpServletResponse resp) throws ServletException, IOException {
        try {
            AuthHelper.signOut(new IdentityContextAdapterServlet(req, resp));
        } catch (Exception ex){
            logger.log(Level.WARNING, "Unable to sign out");
            logger.log(Level.WARNING, ex.getMessage());
            logger.log(Level.FINEST, Arrays.toString(ex.getStackTrace()));
            resp.sendRedirect(resp.encodeRedirectURL(String.format(req.getContextPath() + "/auth_error_details?details=%s", ex.getMessage())));

        }
    }
}