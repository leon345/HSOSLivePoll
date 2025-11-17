package link.hsos.livepoll.model;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Entity-Klasse zur Darstellung einer Antwortoption.
 */
@Entity
@Table(name = "options")
public class Option {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false, length = 255)
    private String text;
    
    @Column(nullable = false)
    private int votes = 0;
    
    
    @OneToMany(mappedBy = "option", cascade = CascadeType.ALL)
    private List<Vote> voteHistory = new ArrayList<>();
    
    public Option() {}
    
    public Option(String text) {
        this.text = text;
        // pollId wird über die Poll-Klasse verwaltet
    }
    
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public String getText() { return text; }
    public void setText(String text) { this.text = text; }
    
    public int getVotes() { return votes; }
    public void setVotes(int votes) { this.votes = votes; }
    
    public List<Vote> getVoteHistory() { return voteHistory; }
    public void setVoteHistory(List<Vote> voteHistory) { this.voteHistory = voteHistory; }

    public void incrementVotes() {
        this.votes++;
    }
    
    public void decrementVotes() {
        if (this.votes > 0) {
            this.votes--;
        }
    }
    
    public void addVote(Vote vote) {
        vote.setOption(this);
        this.voteHistory.add(vote);
        incrementVotes();
    }
    
    public void removeVote(Vote vote) {
        this.voteHistory.remove(vote);
        vote.setOption(null);
        decrementVotes();
    }
    
    @Override
    public String toString() {
        return "Option{" +
                "id=" + id +
                ", text='" + text + '\'' +
                ", votes=" + votes +
                '}';
    }
}
