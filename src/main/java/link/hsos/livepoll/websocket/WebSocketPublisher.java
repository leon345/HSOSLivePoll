package link.hsos.livepoll.websocket;

import link.hsos.livepoll.service.events.PollUpdatedEvent;
import link.hsos.livepoll.service.events.PollUpdatePublisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class WebSocketPublisher implements PollUpdatePublisher {
    private static final Logger logger = LoggerFactory.getLogger(WebSocketPublisher.class);
    private final Executor executor = Executors.newSingleThreadExecutor();
    
    @Override
    public void publish(PollUpdatedEvent event) {
        executor.execute(() -> {
            try {
                WebSocketManager.broadcastPollUpdate(event.getPollId(), null);
                logger.debug("WebSocket-Update für Poll {} gesendet", event.getPollId());
            } catch (Exception e) {
                logger.warn("Fehler beim Senden des WebSocket-Updates für Poll: {}", event.getPollId(), e);
            }
        });
    }
}
