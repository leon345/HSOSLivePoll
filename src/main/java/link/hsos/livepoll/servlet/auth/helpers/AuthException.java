// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.


package link.hsos.livepoll.servlet.auth.helpers;

/**
 * Quelle: Azure-Samples/ms-identity-msal-java-samples. (28. Mai 2025). Java. Microsoft Corporation. Zugegriffen: 23. Juli 2025. [Online]. Verfügbar unter: https://github.com/Azure-Samples/ms-identity-msal-java-samples
 * Klasse ist verfügbar unter https://github.com/Azure-Samples/ms-identity-msal-java-samples/blob/main/3-java-servlet-web-app/1-Authentication/sign-in/src/main/java/com/microsoft/azuresamples/msal4j/helpers/AuthException.java
 *
 * Required exception class for using AuthHelper.java
 */

public class AuthException extends Exception {
    public AuthException(String message) {
        super(message);
    }
}
