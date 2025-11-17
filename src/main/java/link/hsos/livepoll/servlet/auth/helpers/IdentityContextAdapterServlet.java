// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package link.hsos.livepoll.servlet.auth.helpers;


import jakarta.servlet.http.*;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Quelle: Azure-Samples/ms-identity-msal-java-samples. (28. Mai 2025). Java. Microsoft Corporation. Zugegriffen: 23. Juli 2025. [Online]. Verfügbar unter: https://github.com/Azure-Samples/ms-identity-msal-java-samples
 * Klasse ist verfügbar unter https://github.com/Azure-Samples/ms-identity-msal-java-samples/blob/main/3-java-servlet-web-app/1-Authentication/sign-in/src/main/java/com/microsoft/azuresamples/msal4j/helpers/IdentityContextAdapterServlet.java
 * Implementation of IdentityContextAdapter for AuthHelper for use with Java
 * HttpServletRequests/Responses MUST BE INSTANTIATED ONCE PER REQUEST IN WEB
 * APPS / WEB APIs before passing to AuthHelper
 */

public class IdentityContextAdapterServlet implements IdentityContextAdapter, HttpSessionActivationListener {
    private static final Logger logger = Logger.getLogger(IdentityContextAdapterServlet.class.getName());
    private HttpSession session = null;
    private IdentityContextData context = null;
    private HttpServletRequest request = null;
    private HttpServletResponse response = null;

    public IdentityContextAdapterServlet(HttpServletRequest request, HttpServletResponse response) {
        this.request = request;
        this.session = request.getSession();
        this.response = response;
    }

    // load from session on session activation
    @Override
    public void sessionDidActivate(HttpSessionEvent se) {
        this.session = se.getSession();
        loadContext();
    }

    // save to session on session passivation
    @Override
    public void sessionWillPassivate(HttpSessionEvent se) {
        this.session = se.getSession();
        saveContext();
    }

    public void saveContext() {
        if (this.context == null)
            this.context = new IdentityContextData();

        this.session.setAttribute(Config.SESSION_PARAM, context);
    }

    public void loadContext() {
        this.context = (IdentityContextData) session.getAttribute(Config.SESSION_PARAM);
        if (this.context == null) {
            this.context = new IdentityContextData();
        }
    }

    @Override
    public IdentityContextData getContext() {
        loadContext();
        return this.context;
    }

    @Override
    public void setContext(IdentityContextData context) {
        this.context = context;
        saveContext();
    }

    @Override
    public void redirectUser(String location) throws IOException {
        logger.log(Level.INFO, "Redirecting user to {0}", location);
        this.response.sendRedirect(location);
    }

    @Override
    public String getParameter(String parameterName) {
        return this.request.getParameter(parameterName);
    }

}
