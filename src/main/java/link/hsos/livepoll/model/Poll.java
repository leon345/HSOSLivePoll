package link.hsos.livepoll.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
/**
 * Entity-Klasse zur Darstellung einer Umfrage (Poll).
 *
 */
@Entity
@Table(name = "polls")
public class Poll {
    
    @Id
    private String id;
    
    @Column(nullable = false, length = 500)
    private String question;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "poll_type", nullable = false)
    private PollType pollType;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PollStatus status;
    
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "start_time")
    private LocalDateTime startTime;
    
    @Column(name = "end_time")
    private LocalDateTime endTime;
    
    @Column(name = "created_by")
    private String createdBy;
    
    @Column(name = "is_public")
    private boolean isPublic = true;
    
    @Column(name = "allow_multiple_votes")
    private boolean allowMultipleVotes = false;
    
    @Column(name = "short_code", unique = true)
    private String shortCode;
    
    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    @JoinColumn(name = "poll_id")
    private List<Option> options = new ArrayList<>();
    
    public Poll() {
        this.id = UUID.randomUUID().toString();
        this.createdAt = LocalDateTime.now();
        this.status = PollStatus.DRAFT;
    }
    
    public Poll(String question, PollType pollType) {
        this();
        this.question = question;
        this.pollType = pollType;
    }

    
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    
    public String getQuestion() { return question; }
    public void setQuestion(String question) { this.question = question; }
    
    public PollType getPollType() { return pollType; }
    public void setPollType(PollType pollType) { this.pollType = pollType; }
    
    public PollStatus getStatus() { return status; }
    public void setStatus(PollStatus status) { this.status = status; }
    
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    
    public LocalDateTime getStartTime() { return startTime; }
    public void setStartTime(LocalDateTime startTime) { this.startTime = startTime; }
    
    public LocalDateTime getEndTime() { return endTime; }
    public void setEndTime(LocalDateTime endTime) { this.endTime = endTime; }
    
    public String getCreatedBy() { return createdBy; }
    public void setCreatedBy(String createdBy) { this.createdBy = createdBy; }
    
    public boolean isPublic() { return isPublic; }
    public void setPublic(boolean isPublic) { this.isPublic = isPublic; }
    
    public boolean isAllowMultipleVotes() { return allowMultipleVotes; }
    public void setAllowMultipleVotes(boolean allowMultipleVotes) { this.allowMultipleVotes = allowMultipleVotes; }
    
    public String getShortCode() { return shortCode; }
    public void setShortCode(String shortCode) { this.shortCode = shortCode; }
    
    public List<Option> getOptions() { return options; }
    public void setOptions(List<Option> options) { this.options = options; }
    
    public void addOption(Option option) {
        this.options.add(option);
    }
    
    public void removeOption(Option option) {
        this.options.remove(option);
    }
    
    public boolean isActive() {
        return status == PollStatus.ACTIVE;
    }
    
    public boolean isExpired() {
        return endTime != null && LocalDateTime.now().isAfter(endTime);
    }
    
    public boolean canStart() {
        if (status == PollStatus.DRAFT) {
            return startTime != null && LocalDateTime.now().isAfter(startTime);
        } else return status == PollStatus.CLOSED;
    }
    
    public void start() {
        if (status == PollStatus.DRAFT) {
            this.status = PollStatus.ACTIVE;
        }
    }
    
    public void close() {
        this.status = PollStatus.CLOSED;
    }
    
    /**
     * Reaktiviert eine geschlossene Umfrage
     */
    public void reactivate() {
        if (status == PollStatus.CLOSED) {
            this.status = PollStatus.ACTIVE;
        }
    }
    
    /**
     * Startet oder reaktiviert eine Umfrage
     */
    public void startOrReactivate() {
        if (status == PollStatus.DRAFT) {
            this.status = PollStatus.ACTIVE;
        } else if (status == PollStatus.CLOSED) {
            this.status = PollStatus.ACTIVE;
        }
    }
    
    public int getTotalVotes() {
        return options.stream().mapToInt(Option::getVotes).sum();
    }
    
    @Override
    public String toString() {
        return "Poll{" +
                "id='" + id + '\'' +
                ", question='" + question + '\'' +
                ", pollType=" + pollType +
                ", status=" + status +
                ", totalVotes=" + getTotalVotes() +
                '}';
    }
}
