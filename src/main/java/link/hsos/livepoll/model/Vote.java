package link.hsos.livepoll.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
/**
 * Entity-Klasse zur Darstellung einer einzelnen Stimme (Vote)
 */
@Entity
@Table(name = "votes")
public class Vote {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "option_id", nullable = false)
    private Option option;
    
    @Column(name = "poll_id", nullable = false)
    private String pollId;
    
    @Column(nullable = false)
    private LocalDateTime timestamp;
    
    @Column(name = "user_id")
    private String userId;
    
    public Vote() {
        this.timestamp = LocalDateTime.now();
    }
    
    public Vote(Option option, String pollId) {
        this();
        this.option = option;
        this.pollId = pollId;
    }
    
    public Vote(Option option, String pollId, String userId) {
        this(option, pollId);
        this.userId = userId;
    }
    
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public Option getOption() { return option; }
    public void setOption(Option option) { this.option = option; }
    
    public String getPollId() { return pollId; }
    public void setPollId(String pollId) { this.pollId = pollId; }
    
    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }
    
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
    
    @Override
    public String toString() {
        return "Vote{" +
                "id=" + id +
                ", optionId=" + (option != null ? option.getId() : "null") +
                ", timestamp=" + timestamp +
                ", userId='" + userId + '\'' +
                '}';
    }
} 