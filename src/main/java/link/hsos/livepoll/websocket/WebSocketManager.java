package link.hsos.livepoll.websocket;

import link.hsos.livepoll.model.Poll;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Manager für WebSocket-Verbindungen und Updates
 * Diese Klasse stellt eine zentrale Schnittstelle für das Senden von Updates bereit
 */
public class WebSocketManager {
    
    private static final Logger logger = LoggerFactory.getLogger(WebSocketManager.class);
    
    private WebSocketManager() {
    }
    
    /**
     * Sendet ein Update für einen Poll an alle verbundenen WebSocket-Clients
     * 
     * @param pollId Die ID des Polls
     * @param poll Der aktualisierte Poll
     */
    public static void broadcastPollUpdate(String pollId, Poll poll) {
        try {
            PollWebSocketEndpoint.broadcastPollUpdate(pollId, poll);
            logger.debug("Poll-Update für Poll {} erfolgreich gesendet", pollId);
        } catch (Exception e) {
            logger.error("Fehler beim Senden des Poll-Updates für Poll: {}", pollId, e);
        }
    }
    
    /**
     * Startet den WebSocket-Update-Scheduler
     */
    public static void startUpdateScheduler() {
        try {
            // Direkter Aufruf der statischen Methode in PollWebSocketEndpoint
            PollWebSocketEndpoint.startUpdateScheduler();
            logger.info("WebSocket-Update-Scheduler gestartet");
        } catch (Exception e) {
            logger.error("Fehler beim Starten des WebSocket-Update-Schedulers", e);
        }
    }
    
    /**
     * Stoppt den WebSocket-Update-Scheduler
     */
    public static void stopUpdateScheduler() {
        try {
            // Direkter Aufruf der statischen Methode in PollWebSocketEndpoint
            PollWebSocketEndpoint.stopUpdateScheduler();
            logger.info("WebSocket-Update-Scheduler gestoppt");
        } catch (Exception e) {
            logger.error("Fehler beim Stoppen des WebSocket-Update-Schedulers", e);
        }
    }
}
