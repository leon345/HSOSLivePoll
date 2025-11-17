package link.hsos.livepoll.service.events;

public interface PollUpdatePublisher {
    void publish(PollUpdatedEvent event);
}
