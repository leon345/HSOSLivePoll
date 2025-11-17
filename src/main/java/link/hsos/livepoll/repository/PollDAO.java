package link.hsos.livepoll.repository;

import link.hsos.livepoll.model.Poll;
import link.hsos.livepoll.model.PollStatus;
import link.hsos.livepoll.model.Option;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
/**
 * Data Access Object (DAO) für Poll-Entitäten.
 * Diese Klasse stellt die Datenzugriffsschicht dar.
 */
public class PollDAO {
    
    private static final Logger logger = LoggerFactory.getLogger(PollDAO.class);
    private final JpaDatabaseManager dbManager;
    
    public PollDAO() {
        this.dbManager = JpaDatabaseManager.getInstance();
    }
    /**
     * Erstellt eine neue Umfrage in der Datenbank.
     * Diese Methode persistiert sowohl die Poll-Entity als auch alle zugehörigen
     * Option-Entitäten in einer einzigen Transaktion.
     *
     * @param poll Die zu erstellende Umfrage mit allen Antwortoptionen
     * @return Die erstellte Poll-Entity mit generierten IDs
     * @throws RuntimeException wenn die Erstellung fehlschlägt
     */
    public Poll create(Poll poll) {
        EntityManager em = dbManager.createEntityManager();
        try {
            em.getTransaction().begin();
            
            em.persist(poll);
            
            for (Option option : poll.getOptions()) {
                em.persist(option);
            }
            
            em.getTransaction().commit();
            logger.info("Poll erstellt: {}", poll.getId());
            return poll;
            
        } catch (Exception e) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            logger.error("Fehler beim Erstellen des Polls", e);
            throw new RuntimeException("Poll konnte nicht erstellt werden", e);
        } finally {
            em.close();
        }
    }
    /**
     * Sucht eine Umfrage anhand ihrer eindeutigen ID.
     * Lädt automatisch alle zugehörigen Antwortoptionen.
     *
     * @param id Die eindeutige ID der gesuchten Umfrage
     * @return Optional mit der gefundenen Umfrage oder empty() falls nicht gefunden
     */
    public Optional<Poll> findById(String id) {
        EntityManager em = dbManager.createEntityManager();
        try {
            logger.debug("Suche Poll mit ID: {}", id);
            
            TypedQuery<Poll> query = em.createQuery(
                "SELECT p FROM Poll p LEFT JOIN FETCH p.options WHERE p.id = :id", Poll.class);
            query.setParameter("id", id);
            
            List<Poll> results = query.getResultList();
            if (results.isEmpty()) {
                logger.debug("Kein Poll mit ID {} gefunden", id);
                return Optional.empty();
            }
            
            Poll poll = results.get(0);
            logger.debug("Poll mit ID {} gefunden, {} Optionen geladen", id, poll.getOptions().size());
            
            return Optional.of(poll);
            
        } catch (Exception e) {
            logger.error("Fehler beim Laden des Polls mit ID: {}", id, e);
            return Optional.empty();
        } finally {
            em.close();
        }
    }
    /**
     * Sucht eine Umfrage anhand ihres Short-Codes.
     * Lädt automatisch alle zugehörigen Antwortoptionen mit.
     *
     * @param shortCode Der eindeutige Short-Code der gesuchten Umfrage
     * @return Optional mit der gefundenen Umfrage oder empty() falls nicht gefunden
     */
    public Optional<Poll> findByShortCode(String shortCode) {
        EntityManager em = dbManager.createEntityManager();
        try {
            logger.debug("Suche Poll mit Short-Code: {}", shortCode);
            
            TypedQuery<Poll> query = em.createQuery(
                "SELECT p FROM Poll p LEFT JOIN FETCH p.options WHERE p.shortCode = :shortCode", Poll.class);
            query.setParameter("shortCode", shortCode);
            
            List<Poll> results = query.getResultList();
            if (results.isEmpty()) {
                logger.debug("Kein Poll mit Short-Code {} gefunden", shortCode);
                return Optional.empty();
            }
            
            Poll poll = results.get(0);
            logger.debug("Poll mit Short-Code {} gefunden, {} Optionen geladen", shortCode, poll.getOptions().size());
            
            return Optional.of(poll);
            
        } catch (Exception e) {
            logger.error("Fehler beim Laden des Polls mit Short-Code: {}", shortCode, e);
            return Optional.empty();
        } finally {
            em.close();
        }
    }
    /**
     * Generiert einen eindeutigen 6-stelligen Short-Code für eine Umfrage.
     *
     * @return Ein eindeutiger 6-stelliger Short-Code
     */
    public String generateShortCode() {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        String shortCode;
        
        do {
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < 6; i++) {
                sb.append(chars.charAt((int) (Math.random() * chars.length())));
            }
            shortCode = sb.toString();
        } while (isShortCodeExists(shortCode));
        
        return shortCode;
    }
    /**
     * Prüft ob ein Short-Code bereits in der Datenbank existiert.
     *
     * @param shortCode Der zu prüfende Short-Code
     * @return true wenn der Code bereits existiert, false sonst
     */
    private boolean isShortCodeExists(String shortCode) {
        EntityManager em = dbManager.createEntityManager();
        try {
            TypedQuery<Long> query = em.createQuery(
                "SELECT COUNT(p) FROM Poll p WHERE p.shortCode = :shortCode", Long.class);
            query.setParameter("shortCode", shortCode);
            
            List<Long> results = query.getResultList();
            if (results.isEmpty()) {
                return false;
            }
            
            Long count = results.get(0);
            return count > 0;
            
        } catch (Exception e) {
            logger.error("Fehler beim Prüfen des Short-Codes", e);
            return false;
        } finally {
            em.close();
        }
    }
    /**
     * Lädt alle Umfragen aus der Datenbank.
     * Die Ergebnisse werden nach Erstellungsdatum absteigend sortiert
     * (neueste zuerst). Alle Antwortoptionen werden automatisch mitgeladen.
     *
     * @return Liste aller Umfragen
     */
    public List<Poll> findAll() {
        EntityManager em = dbManager.createEntityManager();
        try {
            logger.debug("Lade alle Polls...");
            
            TypedQuery<Poll> query = em.createQuery(
                "SELECT p FROM Poll p LEFT JOIN FETCH p.options ORDER BY p.createdAt DESC", Poll.class);
            
            List<Poll> result = query.getResultList();
            logger.debug("Gefundene Polls: {}", result.size());
            
            return result;
            
        } catch (Exception e) {
            logger.error("Fehler beim Laden aller Polls", e);
            return new ArrayList<>();
        } finally {
            em.close();
        }
    }
    /**
     * Lädt alle aktiven Umfragen aus der Datenbank.
     * Aktive Umfragen haben den Status ACTIVE und sind für Abstimmungen geöffnet.
     * Die Ergebnisse werden nach Erstellungsdatum absteigend sortiert.
     *
     * @return Liste aller aktiven Umfragen
     */
    public List<Poll> findActivePolls() {
        EntityManager em = dbManager.createEntityManager();
        try {
            logger.debug("Suche nach aktiven Polls...");
            
            TypedQuery<Poll> query = em.createQuery(
                "SELECT p FROM Poll p LEFT JOIN FETCH p.options WHERE p.status = :status ORDER BY p.createdAt DESC", Poll.class);
            query.setParameter("status", PollStatus.ACTIVE);
            
            List<Poll> result = query.getResultList();
            logger.debug("Gefundene aktive Polls: {}", result.size());
            
            return result;
            
        } catch (Exception e) {
            logger.error("Fehler beim Laden aktiver Polls", e);
            return new ArrayList<>();
        } finally {
            em.close();
        }
    }
    /**
     * Aktualisiert eine bestehende Umfrage in der Datenbank.
     * Neue Optionen werden automatisch erstellt, bestehende werden aktualisiert.
     *
     * @param poll Die zu aktualisierende Umfrage
     * @return true wenn die Aktualisierung erfolgreich war, false bei Fehlern
     */
    public boolean update(Poll poll) {
        EntityManager em = dbManager.createEntityManager();
        try {
            em.getTransaction().begin();
            
            Poll managedPoll = em.merge(poll);
            
            updateOptions(em, managedPoll);
            
            em.getTransaction().commit();
            logger.info("Poll aktualisiert: {}", poll.getId());
            return true;
            
        } catch (Exception e) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            logger.error("Fehler beim Aktualisieren des Polls", e);
            return false;
        } finally {
            em.close();
        }
    }
    /**
     * Löscht eine Umfrage aus der Datenbank.
     * Durch die Cascade-Konfiguration werden automatisch alle zugehörigen
     * Optionen und Stimmen mit gelöscht.
     *
     * @param id Die ID der zu löschenden Umfrage
     * @return true wenn die Löschung erfolgreich war, false falls die Umfrage nicht gefunden wurde oder ein Fehler auftrat
     */
    public boolean delete(String id) {
        EntityManager em = dbManager.createEntityManager();
        try {
            em.getTransaction().begin();
            
            Poll poll = em.find(Poll.class, id);
            if (poll != null) {
                em.remove(poll);
                em.getTransaction().commit();
                logger.info("Poll gelöscht: {}", id);
                return true;
            } else {
                em.getTransaction().rollback();
                return false;
            }
            
        } catch (Exception e) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            logger.error("Fehler beim Löschen des Polls", e);
            return false;
        } finally {
            em.close();
        }
    }
    /**
     * Hilfsmethode zur Aktualisierung der Antwortoptionen einer Umfrage.
     * Diese Methode behandelt sowohl bestehende als auch neue Optionen:
     * - Bestehende Optionen (mit ID) werden aktualisiert
     * - Neue Optionen (ohne ID) werden erstellt
     *
     * @param em Der EntityManager für die Datenbankoperationen
     * @param poll Die Umfrage deren Optionen aktualisiert werden sollen
     */
    private void updateOptions(EntityManager em, Poll poll) {
        logger.debug("Aktualisiere Optionen für Poll: {}", poll.getId());
        
        if (poll.getOptions() != null && !poll.getOptions().isEmpty()) {
            for (Option option : poll.getOptions()) {
                if (option.getId() != null) {
                    Option existingOption = em.find(Option.class, option.getId());
                    if (existingOption != null) {
                        existingOption.setText(option.getText());
                        // Poll-ID wird automatisch über @JoinColumn gesetzt
                        em.merge(existingOption);
                        logger.debug("Option aktualisiert: {}", option.getId());
                    } else {
                        // Poll-ID wird automatisch über @JoinColumn gesetzt
                        em.persist(option);
                        logger.debug("Neue Option erstellt: {}", option.getText());
                    }
                } else {
                    // Poll-ID wird automatisch über @JoinColumn gesetzt
                    em.persist(option);
                    logger.debug("Neue Option erstellt: {}", option.getText());
                }
            }
        }
        
        logger.debug("Optionen für Poll {} erfolgreich aktualisiert", poll.getId());
    }
} 