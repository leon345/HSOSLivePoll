package link.hsos.livepoll.model;
/**
 * Enumeration zur Darstellung der verschiedenen Status einer Umfrage
 */
public enum PollStatus {
    DRAFT("Entwurf"),
    ACTIVE("Aktiv"),
    CLOSED("Geschlossen"),
    EXPIRED("Abgelaufen");
    
    private final String displayName;
    
    PollStatus(String displayName) {
        this.displayName = displayName;
    }
    
    public String getDisplayName() {
        return displayName;
    }
}
