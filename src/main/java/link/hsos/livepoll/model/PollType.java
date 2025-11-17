package link.hsos.livepoll.model;
/**
 * Enumeration zur Darstellung der verschiedenen Umfragetypen
 */
public enum PollType {
    SINGLE_CHOICE("Einzelauswahl"),
    MULTIPLE_CHOICE("Mehrfachauswahl");
    
    private final String displayName;
    
    PollType(String displayName) {
        this.displayName = displayName;
    }
    
    public String getDisplayName() {
        return displayName;
    }
    
    public String getTypeName() {
        return this.name();
    }
} 