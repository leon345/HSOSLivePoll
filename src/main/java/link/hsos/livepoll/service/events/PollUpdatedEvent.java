package link.hsos.livepoll.service.events;

/**
 * Event-Klasse zur Signalisierung von Änderungen an einer Umfrage.
 */
public final class PollUpdatedEvent {
    private final String pollId;
    
    public PollUpdatedEvent(String pollId) { 
        this.pollId = pollId; 
    }
    
    public String getPollId() { 
        return pollId; 
    }
}