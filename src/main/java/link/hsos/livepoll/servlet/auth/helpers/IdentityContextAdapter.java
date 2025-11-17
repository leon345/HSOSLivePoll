// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package link.hsos.livepoll.servlet.auth.helpers;

import java.io.IOException;

/**
 * Quelle: Azure-Samples/ms-identity-msal-java-samples. (28. Mai 2025). Java. Microsoft Corporation. Zugegriffen: 23. Juli 2025. [Online]. Verfügbar unter: https://github.com/Azure-Samples/ms-identity-msal-java-samples
 * Interface ist verfügbar unter https://github.com/Azure-Samples/ms-identity-msal-java-samples/blob/main/3-java-servlet-web-app/1-Authentication/sign-in/src/main/java/com/microsoft/azuresamples/msal4j/helpers/IdentityContextAdapter.java
 * Implement this so that AuthHelper can be customized to your needs!
 * This Sample project implements this in IdentityContextAdapterServlet.java
 * MUST BE INSTANTIATED ONCE PER REQUEST IN WEB APPS / WEB APIs before passing to AuthHelper
 */
public interface IdentityContextAdapter {
    public void setContext(IdentityContextData context);
    public IdentityContextData getContext();
    public void redirectUser(String location) throws IOException;
    public String getParameter(String parameterName);
}
