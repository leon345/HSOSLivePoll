package link.hsos.livepoll.service;

import link.hsos.livepoll.model.Poll;
import link.hsos.livepoll.model.PollType;
import link.hsos.livepoll.model.Option;
import link.hsos.livepoll.repository.PollDAO;
import link.hsos.livepoll.repository.VoteDAO;
import link.hsos.livepoll.service.helper.ForbiddenException;
import link.hsos.livepoll.service.events.PollUpdatedEvent;
import link.hsos.livepoll.service.events.PollUpdatePublisher;
import link.hsos.livepoll.servlet.auth.helpers.Config;
import link.hsos.livepoll.websocket.WebSocketPublisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.StringJoiner;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;


/**
 * Service-Klasse für die Geschäftslogik des Live-Poll-Systems.
 * Diese Klasse implementiert das Service-Layer-Pattern und fungiert als Schicht
 * zwischen der Präsentationsschicht (Servlets) und der Datenzugriffsschicht (DAOs).
 */
public class PollService {
    private static final Logger logger = LoggerFactory.getLogger(PollService.class);
    private static PollService instance;

    private final PollDAO pollDAO;
    private final VoteDAO voteDAO;
    private final PollUpdatePublisher publisher;
    private final ScheduledExecutorService scheduler;

    public static synchronized PollService getInstance() {
        if (instance == null) {
            instance = new PollService();
        }
        return instance;
    }

    private PollService() {
        this.pollDAO = new PollDAO();
        this.voteDAO = new VoteDAO();
        this.publisher = new WebSocketPublisher();
        this.scheduler = Executors.newScheduledThreadPool(2);

        startPollScheduler();

        logger.info("PollService erfolgreich initialisiert - ohne Caching");
    }

    public PollService(PollDAO pollDAO, VoteDAO voteDAO) {
        this(pollDAO, voteDAO, new WebSocketPublisher());
    }

    public PollService(PollDAO pollDAO, VoteDAO voteDAO, PollUpdatePublisher publisher) {
        this.pollDAO = pollDAO;
        this.voteDAO = voteDAO;
        this.publisher = publisher;
        this.scheduler = null;
    }


    /**
     * Erstellt eine neue Umfrage mit den angegebenen Parametern.
     *
     * @param question           Die Fragestellung der Umfrage
     * @param pollType           Der Typ der Umfrage (SINGLE_CHOICE oder MULTIPLE_CHOICE)
     * @param options            Liste der Antwortoptionen
     * @param startTime          Optionale Startzeit
     * @param endTime            Optionale Endzeit
     * @param createdBy          Identifikation des Erstellers für Autorisierung
     * @param allowMultipleVotes true wenn Mehrfachabstimmungen erlaubt sind
     * @return Die erstellte und persistierte Poll-Entität mit generierter ID und Short-Code
     */
    public Poll createPoll(String question, PollType pollType, List<String> options,
                           LocalDateTime startTime, LocalDateTime endTime, String createdBy, boolean allowMultipleVotes) {

        Poll poll = new Poll(question, pollType);
        poll.setStartTime(startTime);
        poll.setEndTime(endTime);
        poll.setCreatedBy(createdBy);
        poll.setAllowMultipleVotes(allowMultipleVotes);

        String shortCode = pollDAO.generateShortCode();
        poll.setShortCode(shortCode);

        for (String optionText : options) {
            Option option = new Option(optionText);
            poll.addOption(option);
        }

        Poll savedPoll = pollDAO.create(poll);

        logger.info("Neuer Poll erstellt: {} mit Short-Code: {}", savedPoll.getId(), savedPoll.getShortCode());
        return savedPoll;
    }

    /**
     * Lädt eine Umfrage anhand ihrer ID.
     *
     * @param pollId Die  ID der gesuchten Umfrage
     * @return Optional mit der gefundenen Umfrage oder empty() falls nicht vorhanden
     */
    public Optional<Poll> getPoll(String pollId) {
        logger.debug("Lade Poll mit ID: {}", pollId);
        return pollDAO.findById(pollId);
    }

    /**
     * Sucht eine Umfrage anhand ihres Short-Codes.
     *
     * @param shortCode Der Short-Code der gesuchten Umfrage
     * @return Optional mit der gefundenen Umfrage oder empty() falls nicht vorhanden
     */
    public Optional<Poll> getPollByShortCode(String shortCode) {
        logger.debug("Suche Poll mit Short-Code: {}", shortCode);
        return pollDAO.findByShortCode(shortCode);
    }

    /**
     * Lädt alle verfügbaren Umfragen.
     *
     * @return Liste aller Umfragen.
     */
    public List<Poll> getAllPolls() {
        return pollDAO.findAll();
    }

    /**
     * Lädt alle aktuell aktiven Umfragen.
     *
     * @return Liste aller aktiven Umfragen.
     */
    public List<Poll> getActivePolls() {
        logger.debug("Lade aktive Polls");
        List<Poll> activePolls = pollDAO.findActivePolls();

        for (Poll poll : activePolls) {
            logger.debug("Aktiver Poll: ID={}, Frage={}, Status={}, Optionen={}",
                    poll.getId(), poll.getQuestion(), poll.getStatus(),
                    poll.getOptions() != null ? poll.getOptions().size() : "null");

            if (poll.getOptions() != null) {
                for (Option option : poll.getOptions()) {
                    logger.debug("  - Option: ID={}, Text={}, Votes={}",
                            option.getId(), option.getText(), option.getVotes());
                }
            }
        }

        return activePolls;
    }

    /**
     * Validiert einen Poll und gibt eine detaillierte Fehlermeldung zurück, falls der Poll nicht gültig ist
     * @param pollId Die ID der Umfrage
     * @param optionId Die ID der gewählten Option
     * @return PollValidationResult mit Validierungsergebnis und Fehlerdetails
     */
    private PollValidationResult validatePollForVoting(String pollId, Long optionId) {
        Optional<Poll> pollOpt = getPoll(pollId);
        if (pollOpt.isEmpty()) {
            return new PollValidationResult(false, "Poll nicht gefunden", "POLL_NOT_FOUND");
        }

        Poll poll = pollOpt.get();

        if (!poll.isActive()) {
            return new PollValidationResult(false, "Poll ist nicht aktiv (Status: " + poll.getStatus() + ")", "POLL_INACTIVE");
        }

        boolean optionExists = poll.getOptions().stream()
                .anyMatch(option -> option.getId().equals(optionId));
        if (!optionExists) {
            return new PollValidationResult(false, "Option nicht gefunden", "OPTION_NOT_FOUND");
        }

        return new PollValidationResult(true, null, null);
    }

    /**
     * Ergebnis der Poll-Validierung
     */
    private static class PollValidationResult {
        private final boolean isValid;
        private final String errorMessage;
        private final String errorCode;

        public PollValidationResult(boolean isValid, String errorMessage, String errorCode) {
            this.isValid = isValid;
            this.errorMessage = errorMessage;
            this.errorCode = errorCode;
        }

        public boolean isValid() {
            return isValid;
        }

        public String getErrorMessage() {
            return errorMessage;
        }

        public String getErrorCode() {
            return errorCode;
        }
    }
    /**
     * Prüft ob ein Benutzer bereits für eine bestimmte Umfrage abgestimmt hat.
     *
     * @param pollId Die ID der Umfrage
     * @param userId Die ID des Benutzers
     * @return true wenn der Benutzer bereits abgestimmt hat, false sonst
     */
    public boolean hasUserAlreadyVoted(String pollId, String userId) {
        return voteDAO.hasUserVoted(pollId, userId);
    }

    /**
     * Verarbeitet eine einzelne Stimmabgabe für eine Antwortoption.
     *
     * @param pollId Die ID der Umfrage
     * @param optionId Die ID der gewählten Antwortoption
     * @param requestingUserId Die ID des abstimmenden Benutzers
     * @return true wenn die Abstimmung erfolgreich war, false bei Validierungsfehlern oder Fehlern
     */
    public boolean vote(String pollId, Long optionId, String requestingUserId) {

        PollValidationResult validation = validatePollForVoting(pollId, optionId);
        if (!validation.isValid()) {
            logger.warn("Poll-Validierung fehlgeschlagen: {} - {}", pollId, validation.getErrorMessage());
            return false;
        }

        Optional<Poll> pollOpt = getPoll(pollId);
        Poll poll = pollOpt.get();

        if (!poll.isAllowMultipleVotes() && voteDAO.hasUserVoted(pollId, requestingUserId)) {
            logger.warn("Benutzer {} hat bereits für Poll {} gestimmt (Einzelauswahl nicht erlaubt)", requestingUserId, pollId);
            return false;
        }

        boolean success = voteDAO.vote(pollId, optionId, requestingUserId);

        if (success) {
            logger.info("Stimme erfolgreich abgegeben: Poll={}, Option={}, User={}", pollId, optionId, requestingUserId);

            publisher.publish(new PollUpdatedEvent(pollId));
            logger.debug("Poll-Update-Event für Poll {} veröffentlicht", pollId);
        } else {
            logger.warn("Stimme konnte nicht abgegeben werden: Poll={}, Option={}, User={}", pollId, optionId, requestingUserId);
        }

        return success;
    }
    /**
     * Verarbeitet eine Mehrfach-Abstimmung für verschiedene Antwortoptionen.
     *
     * @param pollId Die ID der Umfrage
     * @param optionIds Liste der IDs der gewählten Antwortoptionen
     * @param requestingUserId Die ID des abstimmenden Benutzers
     * @return true wenn alle Abstimmungen erfolgreich waren, false bei Fehlern
     */
    public boolean voteMultiple(String pollId, List<Long> optionIds, String requestingUserId) {

        if (optionIds == null || optionIds.isEmpty()) {
            logger.warn("Keine Optionen für Batch-Stimmabgabe angegeben: Poll={}, User={}", pollId, requestingUserId);
            return false;
        }

        Optional<Poll> pollOpt = getPoll(pollId);
        if (pollOpt.isEmpty()) {
            logger.warn("Poll nicht gefunden für Batch-Stimmabgabe: {}", pollId);
            return false;
        }

        Poll poll = pollOpt.get();

        if (!poll.isActive()) {
            logger.warn("Batch-Stimmabgabe für inaktiven Poll: {} (Status: {})", pollId, poll.getStatus());
            return false;
        }

        for (Long optionId : optionIds) {
            boolean optionExists = poll.getOptions().stream()
                    .anyMatch(option -> option.getId().equals(optionId));
            if (!optionExists) {
                logger.warn("Option {} existiert nicht in Poll {}", optionId, pollId);
                return false;
            }
        }

        if (!poll.isAllowMultipleVotes() && voteDAO.hasUserVoted(pollId, requestingUserId)) {
            logger.warn("Benutzer {} hat bereits für Poll {} gestimmt (Einzelauswahl nicht erlaubt)", requestingUserId, pollId);
            return false;
        }

        boolean success = voteDAO.voteMultiple(pollId, optionIds, requestingUserId);

        if (success) {
            logger.info("Batch-Stimmen erfolgreich abgegeben: Poll={}, Optionen={}, User={}", pollId, optionIds, requestingUserId);

            publisher.publish(new PollUpdatedEvent(pollId));
            logger.debug("Poll-Update-Event für Poll {} veröffentlicht", pollId);
        } else {
            logger.warn("Batch-Stimmen konnten nicht abgegeben werden: Poll={}, Optionen={}, User={}", pollId, optionIds, requestingUserId);
        }

        return success;
    }
    /**
     * Berechnet und gibt die aktuellen Abstimmungsergebnisse zurück.
     * Die Ergebnisse werden als Map mit Optionstext als Schlüssel und
     * Stimmenanzahl als Wert zurückgegeben.
     *
     * @param pollId Die ID der Umfrage
     * @return Map mit Abstimmungsergebnissen
     */
    public Map<String, Integer> getPollResults(String pollId) {
        Optional<Poll> pollOpt = getPoll(pollId);
        if (pollOpt.isEmpty()) {
            logger.warn("Poll nicht gefunden für Ergebnisse: {}", pollId);
            return Map.of();
        }

        Poll poll = pollOpt.get();
        Map<String, Integer> results = new java.util.HashMap<>();

        for (Option option : poll.getOptions()) {
            int voteCount = option.getVotes();
            results.put(option.getText(), voteCount);
        }

        return results;
    }
    /**
     * Lädt die vollständige Abstimmungshistorie für eine Umfrage.
     *
     * @param pollId Die ID der Umfrage
     * @return Liste von VoteRecord-Objekten
     */
    public List<VoteDAO.VoteRecord> getVoteHistory(String pollId) {
        return voteDAO.getVoteHistory(pollId);
    }
    /**
     * Aktualisiert eine bestehende Umfrage.
     *
     * @param poll Die aktualisierte Umfrage-Entität
     * @param requestingCreatedBy Die ID des anfragenden Benutzers für Autorisierung
     * @return true wenn die Aktualisierung erfolgreich war, false sonst
     * @throws ForbiddenException wenn der Benutzer nicht berechtigt ist
     */
    public boolean updatePoll(Poll poll, String requestingCreatedBy) throws ForbiddenException {
        checkAuthorization(poll.getId(), requestingCreatedBy);

        boolean success = pollDAO.update(poll);
        if (success) {
            logger.info("Poll erfolgreich aktualisiert: {}", poll.getId());
        } else {
            logger.warn("Poll konnte nicht aktualisiert werden: {}", poll.getId());
        }
        return success;
    }
    /**
     * Löscht eine Umfrage nach Autorisierungsprüfung.
     *
     * @param pollId Die ID der zu löschenden Umfrage
     * @param requestingCreatedBy Die ID des anfragenden Benutzers für Autorisierung
     * @return true wenn die Löschung erfolgreich war, false sonst
     * @throws ForbiddenException wenn der Benutzer nicht berechtigt ist
     */
    public boolean deletePoll(String pollId, String requestingCreatedBy) throws ForbiddenException {
        checkAuthorization(pollId, requestingCreatedBy);

        boolean success = pollDAO.delete(pollId);
        if (success) {
            logger.info("Poll erfolgreich gelöscht: {}", pollId);
        } else {
            logger.warn("Poll konnte nicht gelöscht werden: {}", pollId);
        }
        return success;
    }

    /**
     * Startet oder reaktiviert eine Umfrage nach Autorisierungsprüfung.
     * Diese Methode ändert den Status der Umfrage auf ACTIVE und veröffentlicht
     * ein Update-Event für WebSocket-Clients.
     *
     * @param pollId Die ID der zu startenden Umfrage
     * @param requestingCreatedBy Die ID des anfragenden Benutzers für Autorisierung
     * @return true wenn der Start erfolgreich war, false bei Fehlern
     * @throws ForbiddenException wenn der Benutzer nicht berechtigt ist
     */
    public boolean startPoll(String pollId, String requestingCreatedBy) throws ForbiddenException {
        checkAuthorization(pollId, requestingCreatedBy);

        Optional<Poll> pollOpt = getPoll(pollId);
        if (pollOpt.isEmpty()) {
            logger.warn("Poll nicht gefunden zum Starten: {}", pollId);
            return false;
        }

        Poll poll = pollOpt.get();
        if (poll.isActive()) {
            logger.info("Poll ist bereits aktiv: {}", pollId);
            return true;
        }

        if (poll.getOptions() == null || poll.getOptions().isEmpty()) {
            logger.warn("Poll {} hat keine Optionen - kann nicht gestartet werden", pollId);
            return false;
        }

        logger.info("Starte/Reaktiviere Poll {} mit {} Optionen", pollId, poll.getOptions().size());

        poll.startOrReactivate();
        boolean success = pollDAO.update(poll);

        if (success) {
            Optional<Poll> updatedPollOpt = getPoll(pollId);
            if (updatedPollOpt.isPresent()) {
                Poll updatedPoll = updatedPollOpt.get();
                logger.info("Poll {} erfolgreich gestartet/reaktiviert mit {} Optionen", pollId, updatedPoll.getOptions().size());

                publisher.publish(new PollUpdatedEvent(pollId));
                logger.debug("Poll-Update-Event nach Poll-Start für Poll {} veröffentlicht", pollId);
            } else {
                logger.warn("Poll {} konnte nach dem Start/Reaktivierung nicht mehr geladen werden", pollId);
            }
        } else {
            logger.warn("Poll konnte nicht gestartet/reaktiviert werden: {}", pollId);
        }

        return success;
    }
    /**
     * Schließt eine aktive Umfrage nach Autorisierungsprüfung.
     * Diese Methode ändert den Status der Umfrage auf CLOSED
     *
     * @param pollId Die ID der zu schließenden Umfrage
     * @param requestingCreatedBy Die ID des anfragenden Benutzers für Autorisierung
     * @return true wenn das Schließen erfolgreich war, false bei Fehlern
     * @throws ForbiddenException wenn der Benutzer nicht berechtigt ist
     */
    public boolean closePoll(String pollId, String requestingCreatedBy) throws ForbiddenException {
        checkAuthorization(pollId, requestingCreatedBy);

        Optional<Poll> pollOpt = getPoll(pollId);
        if (pollOpt.isEmpty()) {
            logger.warn("Poll nicht gefunden zum Schließen: {}", pollId);
            return false;
        }

        Poll poll = pollOpt.get();
        if (!poll.isActive()) {
            logger.info("Poll ist bereits geschlossen: {}", pollId);
            return true;
        }

        if (poll.getOptions() == null || poll.getOptions().isEmpty()) {
            logger.warn("Poll {} hat keine Optionen - kann nicht geschlossen werden", pollId);
            return false;
        }

        logger.info("Schließe Poll {} mit {} Optionen", pollId, poll.getOptions().size());

        poll.close();
        boolean success = pollDAO.update(poll);

        if (success) {
            Optional<Poll> updatedPollOpt = getPoll(pollId);
            if (updatedPollOpt.isPresent()) {
                Poll updatedPoll = updatedPollOpt.get();
                logger.info("Poll {} erfolgreich geschlossen mit {} Optionen", pollId, updatedPoll.getOptions().size());

                // Event über Publisher veröffentlichen
                publisher.publish(new PollUpdatedEvent(pollId));
                logger.debug("Poll-Update-Event nach Poll-Schließung für Poll {} veröffentlicht", pollId);
            } else {
                logger.warn("Poll {} konnte nach dem Schließen nicht mehr geladen werden", pollId);
            }
        } else {
            logger.warn("Poll konnte nicht geschlossen werden: {}", pollId);
        }

        return success;
    }
    /**
     * Exportiert die Abstimmungshistorie einer Umfrage als CSV-Format.
     *
     * @param pollId Die ID der Umfrage deren Historie exportiert werden soll
     * @return CSV-String mit der vollständigen Abstimmungshistorie
     */
    public String exportPollHistoryToCSV(String pollId) {
        Optional<Poll> pollOpt = getPoll(pollId);
        if (pollOpt.isEmpty()) {
            return "";
        }

        Poll poll = pollOpt.get();
        List<VoteDAO.VoteRecord> votes = getVoteHistory(pollId);

        StringJoiner csv = new StringJoiner("\n");
        csv.add("Poll ID,Question,Option,Voter ID,Timestamp");

        for (VoteDAO.VoteRecord vote : votes) {
            csv.add(String.format("%s,%s,%s,%s,%s",
                    pollId,
                    specialCharactersCSV(poll.getQuestion()),
                    specialCharactersCSV(vote.getOptionText()),
                    specialCharactersCSV(vote.getUserId()),
                    vote.getTimestamp().toString()
            ));
        }

        return csv.toString();
    }

    /**
     * Hilfsmethode zur CSV-konformen Behandlung von Sonderzeichen.
     *
     * @param value Der zu behandelnde String-Wert
     * @return CSV-konformer String
     */
    private String specialCharactersCSV(String value) {
        if (value == null) return "";
        return "\"" + value.replace("\"", "\"\"") + "\"";
    }
    /**
     * Startet den automatischen Scheduler für Umfragen-Überwachung.
     * Der Scheduler prüft in regelmäßigen Abständen (30 Sekunden) alle Umfragen
     * auf zeitbasierte Zustandsübergänge (automatischer Start/Stopp).
     */
    private void startPollScheduler() {
        scheduler.scheduleAtFixedRate(this::checkAndUpdatePollStatus, 0, 30, TimeUnit.SECONDS);
        logger.info("Poll-Scheduler gestartet");
    }
    /**
     * Prüft und aktualisiert den Status aller Umfragen basierend auf Zeitkriterien.
     * Diese Methode wird vom Scheduler regelmäßig aufgerufen und führt
     * automatische Zustandsübergänge für zeitgesteuerte Umfragen durch.
     */
    private void checkAndUpdatePollStatus() {
        try {
            List<Poll> allPolls = pollDAO.findAll();

            for (Poll poll : allPolls) {
                if (poll.canStart()) {
                    poll.start();
                    pollDAO.update(poll);
                    logger.info("Poll automatisch gestartet: {}", poll.getId());
                }

                if (poll.isActive() && poll.isExpired()) {
                    poll.close();
                    pollDAO.update(poll);
                    logger.info("Poll automatisch geschlossen: {}", poll.getId());
                }
            }
        } catch (Exception e) {
            logger.error("Fehler beim Überprüfen der Poll-Status", e);
        }
    }
    /**
     * Fährt den PollService ordnungsgemäß herunter.
     * Diese Methode stoppt den Scheduler und gibt alle Ressourcen frei.
     * Sie sollte beim Herunterfahren der Anwendung aufgerufen werden.
     */
    public void shutdown() {
        if (scheduler != null) {
            scheduler.shutdown();
            try {
                if (!scheduler.awaitTermination(60, TimeUnit.SECONDS)) {
                    scheduler.shutdownNow();
                }
            } catch (InterruptedException e) {
                scheduler.shutdownNow();
                Thread.currentThread().interrupt();
            }
        }
        logger.info("PollService heruntergefahren");
    }
    /**
     * Prüft ob ein Benutzer zur Modifikation einer Umfrage berechtigt ist..
     *
     * @param pollId Die ID der zu prüfenden Umfrage
     * @param requestingCreatedBy Die ID des anfragenden Benutzers
     * @return true wenn berechtigt, false sonst
     */
    private boolean isAuthorizedToModifyPoll(String pollId, String requestingCreatedBy) {
        // Wenn Authentifizierung deaktiviert ist, erlauben wir alle Zugriffe
        if (Config.AUTH_DISABLED) {
            logger.debug("Authentifizierung deaktiviert - Zugriff erlaubt");
            return true;
        }

        if (requestingCreatedBy == null || pollId == null) {
            logger.warn("PollId oder requestingCreatedBy ist null bei Autorisierungsprüfung");
            return false;
        }

        Optional<Poll> pollOpt = getPoll(pollId);
        if (pollOpt.isEmpty()) {
            logger.warn("Poll nicht gefunden bei Autorisierungsprüfung: {}", pollId);
            return false;
        }

        Poll poll = pollOpt.get();
        boolean isAuthorized = poll.getCreatedBy().equals(requestingCreatedBy);
        if (!isAuthorized) {
            logger.warn("Unbefugter Zugriffsversuch: User {} versucht Poll {} zu ändern",
                    requestingCreatedBy, pollId);
        }

        return isAuthorized;
    }
    /**
     * Führt eine Autorisierungsprüfung durch und wirft eine Exception bei fehlender Berechtigung.
     *
     * @param pollId Die ID der zu prüfenden Umfrage
     * @param requestingCreatedBy Die ID des anfragenden Benutzers
     * @throws ForbiddenException wenn der Benutzer nicht berechtigt ist
     */
    public void checkAuthorization(String pollId, String requestingCreatedBy)
            throws ForbiddenException {
        if (Config.AUTH_DISABLED) {
            logger.debug("Authentifizierung deaktiviert - Autorisierungsprüfung übersprungen");
            return;
        }

        if (!isAuthorizedToModifyPoll(pollId, requestingCreatedBy)) {
            throw new ForbiddenException("Nicht berechtigt, diese Umfrage zu bearbeiten");
        }
    }
} 