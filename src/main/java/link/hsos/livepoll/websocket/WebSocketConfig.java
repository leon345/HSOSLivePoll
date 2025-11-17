package link.hsos.livepoll.websocket;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Konfigurationsklasse für WebSocket-Einstellungen
 */
public class WebSocketConfig {
    
    private static final Logger logger = LoggerFactory.getLogger(WebSocketConfig.class);
    
    // WebSocket-Verbindungseinstellungen
    public static final int MAX_RECONNECT_ATTEMPTS = 5;
    public static final int RECONNECT_DELAY_MS = 1000;
    public static final int HEARTBEAT_INTERVAL_MS = 30000;
    public static final int CONNECTION_TIMEOUT_MS = 10000;
    
    // Poll-Update-Einstellungen
    public static final int POLL_UPDATE_INTERVAL_MS = 30000;
    public static final boolean ENABLE_REAL_TIME_UPDATES = true;
    
    // Session-Management
    public static final int MAX_SESSIONS_PER_POLL = 100;
    public static final boolean ENABLE_SESSION_LIMITING = true;
    
    private WebSocketConfig() {
        // Utility-Klasse - keine Instanzen erlaubt
    }
    
    /**
     * Gibt die WebSocket-URL für einen bestimmten Poll zurück
     * 
     * @param host Der Host (z.B. localhost:8080)
     * @param pollId Die Poll-ID
     * @param useSecure Ob HTTPS/WSS verwendet werden soll
     * @return Die WebSocket-URL
     */
    public static String getWebSocketUrl(String host, String pollId, boolean useSecure) {
        String protocol = useSecure ? "wss" : "ws";
        return String.format("%s://%s/ws/polls/%s", protocol, host, pollId);
    }
    
    /**
     * Validiert eine Poll-ID
     * 
     * @param pollId Die zu validierende Poll-ID
     * @return true, wenn die Poll-ID gültig ist
     */
    public static boolean isValidPollId(String pollId) {
        return pollId != null && !pollId.trim().isEmpty() && pollId.matches("^[a-zA-Z0-9-_]+$");
    }
    
    /**
     * Gibt die maximale Anzahl von Reconnect-Versuchen zurück
     * 
     * @return Die maximale Anzahl von Reconnect-Versuchen
     */
    public static int getMaxReconnectAttempts() {
        return MAX_RECONNECT_ATTEMPTS;
    }
    
    /**
     * Gibt die Verzögerung zwischen Reconnect-Versuchen zurück
     * 
     * @return Die Verzögerung in Millisekunden
     */
    public static int getReconnectDelay() {
        return RECONNECT_DELAY_MS;
    }
    
    /**
     * Gibt das Heartbeat-Intervall zurück
     * 
     * @return Das Heartbeat-Intervall in Millisekunden
     */
    public static int getHeartbeatInterval() {
        return HEARTBEAT_INTERVAL_MS;
    }
}
