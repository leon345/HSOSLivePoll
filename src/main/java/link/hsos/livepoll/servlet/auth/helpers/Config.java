// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package link.hsos.livepoll.servlet.auth.helpers;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Quelle: Azure-Samples/ms-identity-msal-java-samples. (28. Mai 2025). Java. Microsoft Corporation. Zugegriffen: 23. Juli 2025. [Online]. Verfügbar unter: https://github.com/Azure-Samples/ms-identity-msal-java-samples
 * Klasse ist verfügbar unter https://github.com/Azure-Samples/ms-identity-msal-java-samples/blob/main/3-java-servlet-web-app/1-Authentication/sign-in/src/main/java/com/microsoft/azuresamples/msal4j/helpers/Config.java
 * Loads properties file when the servlet starts.
 * MSAL Java apps using this sample repo's paradigm will require this.
 */

public class Config {
    private static final Logger logger = Logger.getLogger(Config.class.getName());
    private static final Properties props = instantiateProperties();
    private static final String[] REQUIRED = {"aad.authority", "aad.clientId", "aad.secret", "aad.signOutEndpoint", "aad.postSignOutFragment", "app.stateTTL", "app.homePage", "app.redirectEndpoint", "app.sessionParam", 
    "app.protect.authenticated", "add.hmac.secret"};
    private static final List<String> REQ_PROPS = Arrays.asList(REQUIRED);

    private static Properties instantiateProperties() {
        final Properties props = new Properties();
        try {
            props.load(Config.class.getClassLoader().getResourceAsStream("authentication.properties"));
        } catch (final IOException ex) {
            ex.printStackTrace();
            logger.log(Level.SEVERE, "Could not load properties file. Exiting");
            logger.log(Level.SEVERE, Arrays.toString(ex.getStackTrace()));
            System.exit(1);
            return null;
        }
        return props;
    }

    public static final String AUTHORITY = Config.getProperty("aad.authority");
    public static final String CLIENT_ID = Config.getProperty("aad.clientId");
    public static final String SECRET = Config.getProperty("aad.secret");
    public static final String SCOPES = Config.getProperty("aad.scopes");
    public static final String SIGN_OUT_ENDPOINT = Config.getProperty("aad.signOutEndpoint");
    public static final String POST_SIGN_OUT_FRAGMENT = Config.getProperty("aad.postSignOutFragment");
    public static final Long STATE_TTL = Long.parseLong(Config.getProperty("app.stateTTL"));
    public static final String HOME_PAGE = Config.getProperty("app.homePage");
    public static final String DASHBOARD = Config.getProperty("app.dashboard");
    public static final String REDIRECT_ENDPOINT = Config.getProperty("app.redirectEndpoint");
    public static final String REDIRECT_URI = String.format("%s%s", HOME_PAGE, REDIRECT_ENDPOINT);
    public static final String SESSION_PARAM = Config.getProperty("app.sessionParam");
    public static final String PROTECTED_ENDPOINTS = Config.getProperty("app.protect.authenticated");
    public static final String ROLES_PROTECTED_ENDPOINTS = Config.getProperty("app.protect.roles");
    public static final String ROLE_NAMES_AND_IDS = Config.getProperty("app.roles");
    public static final String GROUPS_PROTECTED_ENDPOINTS = Config.getProperty("app.protect.groups");
    public static final String GROUP_NAMES_AND_IDS = Config.getProperty("app.groups");
    public static final String HMAC_SECRET = Config.getProperty("add.hmac.secret");

    // Auth bypass for development
    public static final boolean AUTH_DISABLED = Boolean.parseBoolean(getPropertyOrDefault("app.auth.disabled", "false"));

    public static String getProperty(final String key) {
        // Zuerst nach Umgebungsvariablen suchen
        String envValue = getEnvironmentVariable(key);
        if (envValue != null && !envValue.trim().isEmpty()) {
            Config.logger.log(Level.FINE, "Using environment variable for {0}: {1}", new String[] { key, envValue });
            return envValue;
        }
        
        // Fallback auf Properties-Datei
        String prop = null;
        if (props != null) {
            prop = Config.props.getProperty(key);
            if (prop != null) {
                Config.logger.log(Level.FINE, "{0} is {1}", new String[] { key, prop });
                return prop;
            } else if (REQ_PROPS.contains(key)) {
                Config.logger.log(Level.SEVERE, "FATAL: Could not load required key {0} from config! EXITING", key);
                return null;
            } else {
                Config.logger.log(Level.WARNING, "Could not load {0}!", key);
                return "";
            }
        } else {
            Config.logger.log(Level.SEVERE, "FATAL: Could not load property reader! EXITING!");
            System.exit(1);
            return null;
        }
    }

    private static String getEnvironmentVariable(String key) {
        // Mapping von Properties-Schlüsseln zu Umgebungsvariablen
        switch (key) {
            case "aad.authority":
                return System.getenv("AAD_AUTHORITY");
            case "aad.clientId":
                return System.getenv("AAD_CLIENT_ID");
            case "aad.secret":
                return System.getenv("AAD_SECRET");
            case "aad.scopes":
                return System.getenv("AAD_SCOPES");
            case "app.homePage":
                return System.getenv("APP_HOME_PAGE");
            case "app.dashboard":
                return System.getenv("APP_DASHBOARD");
            case "app.redirectEndpoint":
                return System.getenv("APP_REDIRECT_ENDPOINT");
            case "app.stateTTL":
                return System.getenv("APP_STATE_TTL");
            case "app.sessionParam":
                return System.getenv("APP_SESSION_PARAM");
            case "app.auth.disabled":
                return System.getenv("APP_AUTH_DISABLED");
            case "add.hmac.secret":
                return System.getenv("ADD_HMAC_SECRET");
            default:
                return null;
        }
    }

    public static String getPropertyOrDefault(final String key, final String defaultValue) {
        String prop = getProperty(key);
        if (prop == null || prop.isEmpty()) {
            return defaultValue;
        }
        return prop;
    }
}
