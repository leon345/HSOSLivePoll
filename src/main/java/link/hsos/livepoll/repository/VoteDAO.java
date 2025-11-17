package link.hsos.livepoll.repository;

import link.hsos.livepoll.model.Option;
import link.hsos.livepoll.model.Poll;
import link.hsos.livepoll.model.Vote;
// WebSocket-Import entfernt - DAO macht nur Persistenz/Queries
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object (DAO) für Vote-Entitäten und Abstimmungsoperationen.
 */
public class VoteDAO {
    
    private static final Logger logger = LoggerFactory.getLogger(VoteDAO.class);
    private final JpaDatabaseManager dbManager;
    
    public VoteDAO() {
        this.dbManager = JpaDatabaseManager.getInstance();
    }

    /**
     * Gibt eine einzelne Stimme für eine Antwortoption ab.
     * @param pollId Die ID der Umfrage
     * @param optionId Die ID der gewählten Antwortoption
     * @param userId Die ID des abstimmenden Benutzers
     * @return true wenn die Abstimmung erfolgreich war, false bei Validierungsfehlern oder Fehlern
     */
    public boolean vote(String pollId, Long optionId, String userId) {
        EntityManager em = dbManager.createEntityManager();
        try {
            em.getTransaction().begin();
            
            Poll poll = em.find(Poll.class, pollId);
            Option option = em.find(Option.class, optionId);
            
            if (poll == null || option == null) {
                em.getTransaction().rollback();
                logger.error("Poll oder Option nicht gefunden: Poll={}, Option={}", pollId, optionId);
                return false;
            }
            
            if (!poll.isAllowMultipleVotes()) {
                TypedQuery<Long> existingVoteQuery = em.createQuery(
                    "SELECT COUNT(v) FROM Vote v WHERE v.pollId = :pollId AND v.option.id = :optionId AND v.userId = :userId", Long.class);
                existingVoteQuery.setParameter("pollId", pollId);
                existingVoteQuery.setParameter("optionId", optionId);
                existingVoteQuery.setParameter("userId", userId);
                
                Long existingVotes = existingVoteQuery.getSingleResult();
                if (existingVotes > 0) {
                    em.getTransaction().rollback();
                    logger.warn("Benutzer {} hat bereits für Option {} in Poll {} gestimmt (Einzelauswahl)", userId, optionId, pollId);
                    return false;
                }
            }
            
            Vote vote = new Vote(option, pollId, userId);
            em.persist(vote);
            
            int affected = em.createNativeQuery(
                "UPDATE options SET votes = votes + 1 WHERE id = ? AND poll_id = ?")
                .setParameter(1, optionId)
                .setParameter(2, pollId)
                .executeUpdate();
            
            if (affected > 0) {
                em.getTransaction().commit();
                logger.info("Stimme erfolgreich abgegeben: Poll={}, Option={}, User={}", pollId, optionId, userId);
                
                // WebSocket-Update entfernt - wird jetzt über Service/Publisher behandelt
                
                return true;
            } else {
                em.getTransaction().rollback();
                logger.error("Vote-Counter konnte nicht aktualisiert werden: Poll={}, Option={}", pollId, optionId);
                return false;
            }
            
        } catch (Exception e) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            logger.error("Fehler beim Abgeben der Stimme: Poll={}, Option={}, User={}", pollId, optionId, userId, e);
            return false;
        } finally {
            em.close();
        }
    }
    /**
     * Gibt mehrere Stimmen für verschiedene Antwortoptionen ab
     * Diese Methode ermöglicht die simultane Abstimmung für mehrere Optionen
     * in Multiple-Choice-Umfragen. Alle Stimmen werden in einer einzigen Transaktion verarbeitet.
     *
     * @param pollId Die ID der Umfrage
     * @param optionIds Liste der IDs der gewählten Antwortoptionen
     * @param userId Die ID des abstimmenden Benutzers
     * @return true wenn alle Abstimmungen erfolgreich waren, false bei Validierungsfehlern oder Fehlern
     */
    public boolean voteMultiple(String pollId, List<Long> optionIds, String userId) {
        EntityManager em = dbManager.createEntityManager();
        try {
            em.getTransaction().begin();
            
            Poll poll = em.find(Poll.class, pollId);
            if (poll == null) {
                em.getTransaction().rollback();
                logger.error("Poll nicht gefunden: Poll={}", pollId);
                return false;
            }
            
            // Prüfe ob Benutzer bereits für diesen Poll gestimmt hat (nur bei Einzelauswahl)
            if (!poll.isAllowMultipleVotes()) {
                TypedQuery<Long> existingVoteQuery = em.createQuery(
                    "SELECT COUNT(v) FROM Vote v WHERE v.pollId = :pollId AND v.userId = :userId", Long.class);
                existingVoteQuery.setParameter("pollId", pollId);
                existingVoteQuery.setParameter("userId", userId);
                
                Long existingVotes = existingVoteQuery.getSingleResult();
                if (existingVotes > 0) {
                    em.getTransaction().rollback();
                    logger.warn("Benutzer {} hat bereits für Poll {} gestimmt (Einzelauswahl nicht erlaubt)", userId, pollId);
                    return false;
                }
            }
            
            // Alle Stimmen abgeben
            for (Long optionId : optionIds) {
                Option option = em.find(Option.class, optionId);
                if (option == null) {
                    em.getTransaction().rollback();
                    logger.error("Option nicht gefunden: Poll={}, Option={}", pollId, optionId);
                    return false;
                }
                
                Vote vote = new Vote(option, pollId, userId);
                em.persist(vote);
                
                // Vote-Counter aktualisieren
                int affected = em.createNativeQuery(
                    "UPDATE options SET votes = votes + 1 WHERE id = ? AND poll_id = ?")
                    .setParameter(1, optionId)
                    .setParameter(2, pollId)
                    .executeUpdate();
                
                if (affected == 0) {
                    em.getTransaction().rollback();
                    logger.error("Vote-Counter konnte nicht aktualisiert werden: Poll={}, Option={}", pollId, optionId);
                    return false;
                }
            }
            
            em.getTransaction().commit();
            logger.info("Batch-Stimmen erfolgreich abgegeben: Poll={}, Optionen={}, User={}", pollId, optionIds, userId);
            
            // WebSocket-Update entfernt - wird jetzt über Service/Publisher behandelt
            
            return true;
            
        } catch (Exception e) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            logger.error("Fehler beim Abgeben der Batch-Stimmen: Poll={}, Optionen={}, User={}", pollId, optionIds, userId, e);
            return false;
        } finally {
            em.close();
        }
    }
    /**
     * Lädt die vollständige Abstimmungshistorie für eine Umfrage.
     * Diese Methode erstellt eine  Liste aller abgegebenen Stimmen
     * inklusive Zeitstempel, Benutzer-IDs und Antwortoptionen.
     *
     * @param pollId Die ID der Umfrage deren Historie geladen werden soll
     * @return Liste von VoteRecord-Objekten, sortiert nach Zeitstempel
     */
    public List<VoteRecord> getVoteHistory(String pollId) {
        EntityManager em = dbManager.createEntityManager();
        try {
            TypedQuery<Vote> query = em.createQuery(
                "SELECT v FROM Vote v JOIN FETCH v.option WHERE v.pollId = :pollId ORDER BY v.timestamp DESC", Vote.class);
            query.setParameter("pollId", pollId);
            
            List<Vote> votes = query.getResultList();
            List<VoteRecord> voteRecords = new ArrayList<>();
            
            for (Vote vote : votes) {
                VoteRecord record = new VoteRecord();
                record.setId(vote.getId());
                // Poll-ID direkt aus der Vote-Entität holen
                record.setPollId(vote.getPollId());
                record.setOptionId(vote.getOption().getId());
                record.setTimestamp(vote.getTimestamp());
                record.setUserId(vote.getUserId());
                record.setOptionText(vote.getOption().getText());
                voteRecords.add(record);
            }
            
            return voteRecords;
            
        } catch (Exception e) {
            logger.error("Fehler beim Laden der Abstimmungshistorie", e);
            return new ArrayList<>();
        } finally {
            em.close();
        }
    }
    /**
     * Prüft ob ein Benutzer bereits für eine bestimmte Umfrage gestimmt hat.
     *
     * @param pollId Die ID der Umfrage
     * @param userId Die ID des Benutzers
     * @return true wenn der Benutzer bereits gestimmt hat, false sonst
     */
    public boolean hasUserVoted(String pollId, String userId) {
        EntityManager em = dbManager.createEntityManager();
        try {
            TypedQuery<Long> query = em.createQuery(
                "SELECT COUNT(v) FROM Vote v WHERE v.pollId = :pollId AND v.userId = :userId", Long.class);
            query.setParameter("pollId", pollId);
            query.setParameter("userId", userId);
            
            List<Long> results = query.getResultList();
            if (results.isEmpty()) {
                return false;
            }
            
            Long count = results.get(0);
            return count > 0;
            
        } catch (Exception e) {
            logger.error("Fehler beim Prüfen der Benutzerstimme", e);
            return false;
        } finally {
            em.close();
        }
    }
    /**
     * Zählt die Gesamtanzahl aller abgegebenen Stimmen für eine Umfrage.
     *
     * @param pollId Die ID der Umfrage
     * @return Die Gesamtanzahl aller abgegebenen Stimmen
     */
    public int getVoteCount(String pollId) {
        EntityManager em = dbManager.createEntityManager();
        try {
            TypedQuery<Long> query = em.createQuery(
                "SELECT COUNT(v) FROM Vote v WHERE v.pollId = :pollId", Long.class);
            query.setParameter("pollId", pollId);
            
            List<Long> results = query.getResultList();
            if (results.isEmpty()) {
                return 0;
            }
            
            Long count = results.get(0);
            return count.intValue();
            
        } catch (Exception e) {
            logger.error("Fehler beim Zählen der Stimmen", e);
            return 0;
        } finally {
            em.close();
        }
    }
    /**
     * Löscht alle Stimmen für eine bestimmte Umfrage.
     *
     * @param pollId Die ID der Umfrage deren Stimmen gelöscht werden sollen
     * @return true wenn mindestens eine Stimme gelöscht wurde, false wenn keine Stimmen vorhanden waren
     */
    public boolean deleteVotesForPoll(String pollId) {
        EntityManager em = dbManager.createEntityManager();
        try {
            em.getTransaction().begin();
            
            int affected = em.createQuery("DELETE FROM Vote v WHERE v.pollId = :pollId")
                .setParameter("pollId", pollId)
                .executeUpdate();
            
            if (affected > 0) {
                em.getTransaction().commit();
                logger.info("Stimmen für Poll gelöscht: {}", pollId);
                return true;
            } else {
                em.getTransaction().rollback();
                return false;
            }
            
        } catch (Exception e) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            logger.error("Fehler beim Löschen der Stimmen", e);
            return false;
        } finally {
            em.close();
        }
    }
    /**
     * Data Transfer Object für die Übertragung von Abstimmungsdaten.
     */
    public static class VoteRecord {
        private Long id;
        private String pollId;
        private Long optionId;
        private String optionText;
        private LocalDateTime timestamp;
        private String userId;
        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        
        public String getPollId() { return pollId; }
        public void setPollId(String pollId) { this.pollId = pollId; }
        
        public Long getOptionId() { return optionId; }
        public void setOptionId(Long optionId) { this.optionId = optionId; }
        
        public String getOptionText() { return optionText; }
        public void setOptionText(String optionText) { this.optionText = optionText; }
        
        public LocalDateTime getTimestamp() { return timestamp; }
        public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }
        
        public String getUserId() { return userId; }
        public void setUserId(String userId) { this.userId = userId; }
        
        @Override
        public String toString() {
            return "VoteRecord{" +
                    "id=" + id +
                    ", pollId='" + pollId + '\'' +
                    ", optionId=" + optionId +
                    ", optionText='" + optionText + '\'' +
                    ", timestamp=" + timestamp +
                    ", userId='" + userId + '\'' +
                    '}';
        }
    }
} 