package link.hsos.livepoll.websocket;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.websocket.*;
import jakarta.websocket.server.PathParam;
import jakarta.websocket.server.ServerEndpoint;
import link.hsos.livepoll.model.Option;
import link.hsos.livepoll.model.Poll;
import link.hsos.livepoll.service.PollService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
/**
 * WebSocket Endpoint für Live-Umfragen.
 */
@ServerEndpoint("/ws/polls/{pollId}")
public class PollWebSocketEndpoint {
    
    private static final Logger logger = LoggerFactory.getLogger(PollWebSocketEndpoint.class);
    private static final ObjectMapper objectMapper = new ObjectMapper();
    private static final Map<String, Session> sessions = new ConcurrentHashMap<>();
    private static final Map<String, String> sessionPollMap = new ConcurrentHashMap<>();
    private static final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(2);
    
    private final PollService pollService = PollService.getInstance();
    /**
     * Methode, die aufgerufen wird, wenn eine neue WebSocket-Verbindung eröffnet wird.
     *
     * @param session die WebSocket-Session des Clients
     * @param pollId die ID der Umfrage, mit der die Session verbunden ist
     */
    @OnOpen
    public void onOpen(Session session, @PathParam("pollId") String pollId) {
        logger.info("WebSocket-Verbindung geöffnet für Poll: {}", pollId);
        
        // Validiere, ob der Poll existiert
        var pollOpt = pollService.getPoll(pollId);
        if (pollOpt.isEmpty()) {
            logger.warn("Poll mit ID {} existiert nicht", pollId);
            try {
                session.close(new CloseReason(CloseReason.CloseCodes.CANNOT_ACCEPT, "Poll existiert nicht"));
            } catch (IOException e) {
                logger.error("Fehler beim Schließen der WebSocket-Verbindung", e);
            }
            return;
        }
        
        // Speichere Session-Informationen
        String sessionId = session.getId();
        sessions.put(sessionId, session);
        sessionPollMap.put(sessionId, pollId);
        
        // Sende initiale Poll-Daten
        sendPollUpdate(pollId, pollOpt.get());
        
        logger.info("WebSocket-Session {} erfolgreich für Poll {} registriert", sessionId, pollId);
    }
    /**
     * Methode zur Verarbeitung eingehender WebSocket-Nachrichten.
     *
     * @param message JSON-kodierte Nachricht als String
     * @param session WebSocket-Session des sendenden Clients
     */
    @OnMessage
    public void onMessage(String message, Session session) {
        try {
            var messageData = objectMapper.readTree(message);
            String type = messageData.get("type").asText();
            String pollId = messageData.get("pollId").asText();
            
            logger.debug("WebSocket-Nachricht empfangen: {} für Poll: {}", type, pollId);
            
            switch (type) {
                case "SUBSCRIBE":
                    handleSubscribe(session, pollId);
                    break;
                case "PING":
                    handlePing(session);
                    break;
                default:
                    logger.warn("Unbekannter Nachrichtentyp: {}", type);
            }
            
        } catch (Exception e) {
            logger.error("Fehler beim Verarbeiten der WebSocket-Nachricht", e);
        }
    }
    /**
     * Methode, die aufgerufen wird, wenn eine WebSocket-Verbindung geschlossen wird.
     *
     * @param session die WebSocket-Session, die geschlossen wird
     */
    @OnClose
    public void onClose(Session session) {
        String sessionId = session.getId();
        String pollId = sessionPollMap.remove(sessionId);
        sessions.remove(sessionId);
        
        logger.info("WebSocket-Verbindung geschlossen für Session: {} (Poll: {})", sessionId, pollId);
    }
    /**
     * Methode, die bei einem Fehler während der WebSocket-Kommunikation aufgerufen wird.
     *
     * @param session die WebSocket-Session, in der der Fehler aufgetreten ist
     * @param throwable das aufgetretene Throwable-Objekt mit den Fehlerinformationen
     */
    @OnError
    public void onError(Session session, Throwable throwable) {
        logger.error("WebSocket-Fehler für Session: {}", session.getId(), throwable);
    }
    
    private void handleSubscribe(Session session, String pollId) {
        String sessionId = session.getId();
        sessionPollMap.put(sessionId, pollId);
        
        // Sende aktuelle Poll-Daten
        var pollOpt = pollService.getPoll(pollId);
        if (pollOpt.isPresent()) {
            sendPollUpdate(pollId, pollOpt.get());
        }
        
        logger.debug("Session {} für Poll {} abonniert", sessionId, pollId);
    }
    
    private void handlePing(Session session) {
        try {
            session.getBasicRemote().sendText("{\"type\":\"PONG\"}");
        } catch (IOException e) {
            logger.error("Fehler beim Senden der PONG-Antwort", e);
        }
    }
    
    /**
     * Sendet ein Update an alle verbundenen Clients für einen bestimmten Poll
     */
    public static void broadcastPollUpdate(String pollId, Poll poll) {
        if (poll == null) {
            logger.warn("Versuche Update für null-Poll zu senden");
            return;
        }
        
        try {
            String updateMessage = createPollUpdateMessage(poll);
            
            sessions.entrySet().stream()
                .filter(entry -> pollId.equals(sessionPollMap.get(entry.getKey())))
                .forEach(entry -> {
                    Session session = entry.getValue();
                    if (session.isOpen()) {
                        try {
                            session.getBasicRemote().sendText(updateMessage);
                        } catch (IOException e) {
                            logger.error("Fehler beim Senden der Update-Nachricht an Session: {}", entry.getKey(), e);
                        }
                    }
                });
                
            logger.debug("Poll-Update für Poll {} an {} Sessions gesendet", pollId, 
                sessions.entrySet().stream()
                    .filter(entry -> pollId.equals(sessionPollMap.get(entry.getKey())))
                    .count());
                    
        } catch (Exception e) {
            logger.error("Fehler beim Erstellen der Poll-Update-Nachricht", e);
        }
    }
    
    private static String createPollUpdateMessage(Poll poll) throws Exception {
        Map<String, Integer> results = poll.getOptions().stream()
            .collect(java.util.stream.Collectors.toMap(
                Option::getText,
                Option::getVotes
            ));
        
        var updateData = Map.of(
            "pollId", poll.getId(),
            "question", poll.getQuestion(),
            "status", poll.getStatus().toString(),
            "results", results
        );
        
        return objectMapper.writeValueAsString(updateData);
    }
    
    private void sendPollUpdate(String pollId, Poll poll) {
        try {
            String updateMessage = createPollUpdateMessage(poll);
            // Sende an alle Sessions für diesen Poll
            sessions.entrySet().stream()
                .filter(entry -> pollId.equals(sessionPollMap.get(entry.getKey())))
                .forEach(entry -> {
                    Session session = entry.getValue();
                    if (session != null && session.isOpen()) {
                        try {
                            session.getBasicRemote().sendText(updateMessage);
                        } catch (IOException e) {
                            logger.error("Fehler beim Senden der Update-Nachricht", e);
                        }
                    }
                });
        } catch (Exception e) {
            logger.error("Fehler beim Senden der initialen Poll-Daten", e);
        }
    }
    
    /**
     * Startet den Scheduler für regelmäßige Updates
     */
    public static void startUpdateScheduler() {
        scheduler.scheduleAtFixedRate(() -> {
            try {
                logger.debug("WebSocket-Update-Scheduler läuft");
            } catch (Exception e) {
                logger.error("Fehler im WebSocket-Update-Scheduler", e);
            }
        }, 30, 30, TimeUnit.SECONDS);
    }
    
    /**
     * Stoppt den Scheduler
     */
    public static void stopUpdateScheduler() {
        if (!scheduler.isShutdown()) {
            scheduler.shutdown();
            try {
                if (!scheduler.awaitTermination(5, TimeUnit.SECONDS)) {
                    scheduler.shutdownNow();
                }
            } catch (InterruptedException e) {
                scheduler.shutdownNow();
                Thread.currentThread().interrupt();
            }
        }
    }
}
