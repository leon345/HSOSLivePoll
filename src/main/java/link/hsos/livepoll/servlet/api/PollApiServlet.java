package link.hsos.livepoll.servlet.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import link.hsos.livepoll.model.Poll;
import link.hsos.livepoll.model.PollType;

import link.hsos.livepoll.service.PollService;
import link.hsos.livepoll.repository.VoteDAO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.Optional;
/**
 * RESTful Web Service Servlet für die Verwaltung von Live-Poll-Umfragen.
 */
@WebServlet({"/api/polls", "/api/polls/", "/api/polls/*"})
public class PollApiServlet extends HttpServlet {
    
    private static final Logger logger = LoggerFactory.getLogger(PollApiServlet.class);
    private static final ObjectMapper objectMapper = new ObjectMapper();
    private static final XmlMapper xmlMapper = new XmlMapper();
    private static final PollService pollService = PollService.getInstance();
    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");
    /**
     * Verarbeitet HTTP GET-Requests für das Abrufen von Umfrage-Daten.
     *
     * @param request HTTP-Request mit Pfadparametern und Query-Parametern
     * @param response HTTP-Response für JSON-Ausgabe oder Datei-Download
     * @throws ServletException bei Servlet-Konfigurationsfehlern
     * @throws IOException bei I/O-Fehlern während Request/Response-Verarbeitung
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        String pathInfo = request.getPathInfo();
        String createdBy = (String) request.getAttribute("createdBy");
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        
        try {
            handleGetRequest(pathInfo, response);
        } catch (Exception e) {
            logger.error("Fehler beim GET-Request", e);
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            objectMapper.writeValue(response.getWriter(), Map.of("error", "Interner Server-Fehler: " + e.getMessage()));
        }
    }
    /**
     * Zentrale Request-Routing-Methode für GET-Anfragen.
     * Analysiert den Pfad und delegiert an entsprechende Handler-Methoden.
     *
     * @param pathInfo der Pfad-Teil der URL nach dem Servlet-Mapping
     * @param response HTTP-Response-Objekt für die Ausgabe
     * @throws IOException bei Fehlern
     */
    private void handleGetRequest(String pathInfo, HttpServletResponse response) throws IOException {
        if (pathInfo == null || pathInfo.equals("/")) {
            handleGetAllPolls(response);
        } else if (pathInfo.equals("/active")) {
            handleGetActivePolls(response);
        } else if (pathInfo.equals("/active/wait")) {
            handleGetActivePollsWithLongPolling(response);
        } else if (pathInfo.matches("/[^/]+/results")) {
            handleGetPollResults(pathInfo, response);
        } else if (pathInfo.matches("/[^/]+/history")) {
            handleGetPollHistory(pathInfo, response);
        } else if (pathInfo.matches("/[^/]+/export.csv")) {
            handleExportPollToCSV(pathInfo, response);
        } else if (pathInfo.matches("/[^/]+/export.xml")) {
            handleExportPollToXML(pathInfo, response);
        } else if (pathInfo.matches("/[^/]+/wait")) {
            handleGetPollWithLongPolling(pathInfo, response);
        } else if (pathInfo.matches("/[^/]+")) {
            handleGetPollById(pathInfo, response);
        } else if (pathInfo.matches("/shortcode/[^/]+")) {
            handleGetPollByShortCode(pathInfo, response);
        } else {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            objectMapper.writeValue(response.getWriter(), Map.of("error", "Ungültiger Pfad"));
        }
    }
    
    private void handleGetAllPolls(HttpServletResponse response) throws IOException {
        List<Poll> polls = pollService.getAllPolls();
        List<PollResponse> pollResponses = polls.stream()
                .map(PollResponse::new)
                .collect(java.util.stream.Collectors.toList());
        objectMapper.writeValue(response.getWriter(), pollResponses);
    }
    
    private void handleGetActivePolls(HttpServletResponse response) throws IOException {
        List<Poll> activePolls = pollService.getActivePolls();
        List<PollResponse> pollResponses = activePolls.stream()
                .map(PollResponse::new)
                .collect(java.util.stream.Collectors.toList());
        objectMapper.writeValue(response.getWriter(), pollResponses);
    }
    /**
     * Implementiert Long Polling für aktive Umfragen mit Echtzeit-Updates.
     * @param response HTTP-Response für Long Polling
     * @throws IOException bei Verbindungsfehlern oder Timeout
     */
    private void handleGetActivePollsWithLongPolling(HttpServletResponse response) throws IOException {
        logger.info("Long Polling Request für aktive Polls");
        
        response.setHeader("Cache-Control", "no-cache");
        response.setHeader("Connection", "keep-alive");
        
        List<Poll> initialActivePolls = pollService.getActivePolls();
        int initialCount = initialActivePolls.size();
        logger.info("Initiale Anzahl aktiver Polls: {}", initialCount);
        
        long startTime = System.currentTimeMillis();
        long timeout = 30000; // 30 Sekunden
        
        while (System.currentTimeMillis() - startTime < timeout) {
            List<Poll> currentActivePolls = pollService.getActivePolls();
            int currentCount = currentActivePolls.size();
            
            // Prüfe ob sich die Anzahl oder der Status geändert hat
            if (currentCount != initialCount || hasActivePollsChanged(initialActivePolls, currentActivePolls)) {
                logger.info("Änderung bei aktiven Polls erkannt: {} -> {}", initialCount, currentCount);
                List<PollResponse> pollResponses = currentActivePolls.stream()
                        .map(PollResponse::new)
                        .collect(java.util.stream.Collectors.toList());
                objectMapper.writeValue(response.getWriter(), pollResponses);
                return;
            }
            
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }

        List<Poll> currentActivePolls = pollService.getActivePolls();
        List<PollResponse> pollResponses = currentActivePolls.stream()
                .map(PollResponse::new)
                .collect(java.util.stream.Collectors.toList());
        objectMapper.writeValue(response.getWriter(), pollResponses);
    }
    
    private void handleGetPollResults(String pathInfo, HttpServletResponse response) throws IOException {
        String pollId = pathInfo.substring(1, pathInfo.lastIndexOf("/"));
        Map<String, Integer> results = pollService.getPollResults(pollId);
        objectMapper.writeValue(response.getWriter(), results);
    }
    
    private void handleGetPollHistory(String pathInfo, HttpServletResponse response) throws IOException {
        String pollId = pathInfo.substring(1, pathInfo.lastIndexOf("/"));
        List<VoteDAO.VoteRecord> history = pollService.getVoteHistory(pollId);
        objectMapper.writeValue(response.getWriter(), history);
    }
    
    private void handleExportPollToCSV(String pathInfo, HttpServletResponse response) throws IOException {
        String pollId = pathInfo.substring(1, pathInfo.lastIndexOf("/"));
        String csv = pollService.exportPollHistoryToCSV(pollId);

        response.setContentType("text/csv");
        response.setCharacterEncoding("UTF-8");
        response.setHeader("Content-Disposition",
                "attachment; filename=\"poll_" + pollId + "_votes.csv\"");

        response.getWriter().write(csv);
        response.getWriter().flush();
    }
    
    private void handleExportPollToXML(String pathInfo, HttpServletResponse response) throws IOException {
        String pollId = pathInfo.substring(1, pathInfo.lastIndexOf("/"));
        Optional<Poll> poll = pollService.getPoll(pollId);

        if (poll.isPresent()) {
            TemplatePollXml templatePollXml = new TemplatePollXml(poll.get());
            String xml = xmlMapper.writeValueAsString(templatePollXml);

            response.setContentType("application/xml");
            response.setCharacterEncoding("UTF-8");
            response.setHeader("Content-Disposition",
                    "attachment; filename=\"poll_" + pollId + "_template.xml\"");

            response.getWriter().write(xml);
            response.getWriter().flush();
        } else {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
        }
    }
    
    private void handleGetPollWithLongPolling(String pathInfo, HttpServletResponse response) throws IOException {
        String pollId = pathInfo.substring(1, pathInfo.lastIndexOf("/"));
        logger.info("Long Polling Request für Status-Änderungen: {}", pollId);
        
        response.setHeader("Cache-Control", "no-cache");
        response.setHeader("Connection", "keep-alive");
        
        Optional<Poll> currentPollOpt = pollService.getPoll(pollId);
        if (currentPollOpt.isEmpty()) {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            objectMapper.writeValue(response.getWriter(), Map.of("error", "Poll nicht gefunden"));
            return;
        }
        
        Poll currentPoll = currentPollOpt.get();
        String initialStatus = currentPoll.getStatus().name();
        logger.info("Initialer Status für Poll {}: {}", pollId, initialStatus);
        
        long startTime = System.currentTimeMillis();
        long timeout = 30000; // 30 Sekunden
        
        while (System.currentTimeMillis() - startTime < timeout) {
            Optional<Poll> pollOpt = pollService.getPoll(pollId);
            if (pollOpt.isPresent()) {
                Poll poll = pollOpt.get();
                if (!poll.getStatus().name().equals(initialStatus)) {
                    logger.info("Status-Änderung erkannt: {} -> {}", initialStatus, poll.getStatus().name());
                    PollResponse pollResponse = new PollResponse(poll);
                    objectMapper.writeValue(response.getWriter(), pollResponse);
                    return;
                }
            }
            
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
        
        Optional<Poll> pollOpt = pollService.getPoll(pollId);
        if (pollOpt.isPresent()) {
            PollResponse pollResponse = new PollResponse(pollOpt.get());
            objectMapper.writeValue(response.getWriter(), pollResponse);
        } else {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            objectMapper.writeValue(response.getWriter(), Map.of("error", "Poll nicht gefunden"));
        }
    }
    
    private void handleGetPollById(String pathInfo, HttpServletResponse response) throws IOException {
        String pollId = pathInfo.substring(1);
        Optional<Poll> poll = pollService.getPoll(pollId);
        
        if (poll.isPresent()) {
            PollResponse pollResponse = new PollResponse(poll.get());
            objectMapper.writeValue(response.getWriter(), pollResponse);
        } else {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            objectMapper.writeValue(response.getWriter(), Map.of("error", "Poll nicht gefunden"));
        }
    }
    
    private void handleGetPollByShortCode(String pathInfo, HttpServletResponse response) throws IOException {
        String shortCode = pathInfo.substring(pathInfo.lastIndexOf("/") + 1);
        Optional<Poll> pollOpt = pollService.getPollByShortCode(shortCode);

        if (pollOpt.isPresent()) {
            PollResponse pollResponse = new PollResponse(pollOpt.get());
            objectMapper.writeValue(response.getWriter(), pollResponse);
        } else {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            objectMapper.writeValue(response.getWriter(), Map.of("error", "Poll mit Short-Code nicht gefunden"));
        }
    }
    /**
     * Verarbeitet HTTP POST-Requests für die Erstellung neuer Ressourcen.
     *
     * @param request HTTP-Request mit JSON-Body und Authentifizierungskontext
     * @param response HTTP-Response für Ergebnis-JSON oder Fehlermeldung
     * @throws ServletException bei Servlet-Konfigurationsfehlern
     * @throws IOException bei I/O-Fehlern während Request/Response-Verarbeitung
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        String pathInfo = request.getPathInfo();
        String createdBy = (String) request.getAttribute("createdBy");
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        
        try {
            handlePostRequest(pathInfo, request, response, createdBy);
        } catch (Exception e) {
            logger.error("Fehler beim POST-Request", e);
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            objectMapper.writeValue(response.getWriter(), Map.of("error", "Interner Server-Fehler: " + e.getMessage()));
        }
    }
    /**
     * Zentrale Request-Routing-Methode für HTTP POST-Anfragen.
     *
     * @param pathInfo der Pfad-Teil der URL nach dem Servlet-Mapping
     * @param request das HttpServletRequest-Objekt mit JSON-Body
     * @param response das HttpServletResponse-Objekt für die JSON-Antwort
     * @param createdBy die Benutzer-ID des authentifizierten Benutzers (für Umfrageerstellung)
     *
     * @throws IOException bei Fehlern beim Lesen des Request-Bodies oder Schreiben der Response
     */
    private void handlePostRequest(String pathInfo, HttpServletRequest request, HttpServletResponse response, String createdBy) throws IOException {
        if (pathInfo == null || pathInfo.equals("/")) {
            handleCreatePoll(request, response, createdBy);
        } else if (pathInfo.matches("/[^/]+/vote")) {
            handleVote(pathInfo, request, response);
        } else if (pathInfo.matches("/[^/]+/vote-multiple")) {
            handleMultipleVote(pathInfo, request, response);
        } else if (pathInfo.matches("/shortcode/[^/]+")) {
            handleGetPollByShortCode(pathInfo, response);
        } else {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            objectMapper.writeValue(response.getWriter(), Map.of("error", "Ungültiger Pfad"));
        }
    }
    
    private void handleCreatePoll(HttpServletRequest request, HttpServletResponse response, String createdBy) throws IOException {
        logger.info("Creating new poll...");
        
        CreatePollRequest createRequest = objectMapper.readValue(request.getReader(), CreatePollRequest.class);
        logger.info("Received poll data: question={}, pollType={}, options={}, allowMultipleVotes={}", 
            createRequest.getQuestion(), createRequest.getPollType(), createRequest.getOptions(), createRequest.isAllowMultipleVotes());
        
        Poll poll = pollService.createPoll(
            createRequest.getQuestion(),
            createPollTypeFromString(createRequest.getPollType()),
            createRequest.getOptions(),
            createRequest.getStartTime() != null ? LocalDateTime.parse(createRequest.getStartTime(), formatter) : null,
            createRequest.getEndTime() != null ? LocalDateTime.parse(createRequest.getEndTime(), formatter) : null,
            createdBy,
            createRequest.isAllowMultipleVotes()
        );
        
        logger.info("Poll created successfully with ID: {}", poll.getId());
        response.setStatus(HttpServletResponse.SC_CREATED);

        PollResponse pollResponse = new PollResponse(poll);
        objectMapper.writeValue(response.getWriter(), pollResponse);
    }
    
    private void handleVote(String pathInfo, HttpServletRequest request, HttpServletResponse response) throws IOException {
        String pollId = pathInfo.substring(1, pathInfo.lastIndexOf("/"));
        logger.info("Vote-Request für Poll: {}", pollId);

        VoteRequest voteRequest = objectMapper.readValue(request.getReader(), VoteRequest.class);
        logger.info("Vote-Daten: optionId={}, userId={}", voteRequest.getOptionId(), voteRequest.getUserId());

        Optional<Poll> pollOpt = pollService.getPoll(pollId);
        if (pollOpt.isEmpty()) {
            logger.warn("Vote-Request für nicht existierenden Poll: {}", pollId);
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            objectMapper.writeValue(response.getWriter(), Map.of(
                "error", "Poll nicht gefunden",
                "pollId", pollId,
                "errorCode", "POLL_NOT_FOUND"
            ));
            return;
        }

        Poll poll = pollOpt.get();

        if (!poll.isActive()) {
            logger.warn("Vote-Request für inaktiven Poll: {} (Status: {})", pollId, poll.getStatus());
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            objectMapper.writeValue(response.getWriter(), Map.of(
                "error", "Poll ist nicht aktiv",
                "status", poll.getStatus().toString(),
                "errorCode", "POLL_INACTIVE"
            ));
            return;
        }

        boolean optionExists = poll.getOptions().stream()
            .anyMatch(option -> option.getId().equals(voteRequest.getOptionId()));
        if (!optionExists) {
            logger.warn("Vote-Request für nicht existierende Option: {} in Poll: {}", voteRequest.getOptionId(), pollId);
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            objectMapper.writeValue(response.getWriter(), Map.of(
                "error", "Option nicht gefunden",
                "optionId", voteRequest.getOptionId(),
                "errorCode", "OPTION_NOT_FOUND"
            ));
            return;
        }

        boolean success = pollService.vote(pollId, voteRequest.getOptionId(), voteRequest.getUserId());
        
        if (success) {
            Map<String, Integer> results = pollService.getPollResults(pollId);
            logger.info("Stimme erfolgreich abgegeben für Poll: {}", pollId);
            objectMapper.writeValue(response.getWriter(), results);
        } else {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            
            String errorMessage = "Stimme konnte nicht abgegeben werden";
            String errorCode = "VOTE_FAILED";
            
            if (pollService.hasUserAlreadyVoted(pollId, voteRequest.getUserId())) {
                errorMessage = "Sie haben bereits für diese Umfrage gestimmt";
                errorCode = "ALREADY_VOTED";
            }
            
            objectMapper.writeValue(response.getWriter(), Map.of(
                "error", errorMessage,
                "pollId", pollId,
                "errorCode", errorCode
            ));
            logger.warn("Stimme konnte nicht abgegeben werden für Poll: {} - {}", pollId, errorMessage);
        }
    }
    
    private void handleMultipleVote(String pathInfo, HttpServletRequest request, HttpServletResponse response) throws IOException {
        String pollId = pathInfo.substring(1, pathInfo.lastIndexOf("/"));
        logger.info("Vote-Multiple-Request für Poll: {}", pollId);
        
        // Neue Route für Batch-Stimmabgabe
        if (request.getMethod().equals("POST")) {
            try {
                VoteMultipleRequest voteRequest = objectMapper.readValue(request.getReader(), VoteMultipleRequest.class);
                
                if (voteRequest.getOptionIds() == null || voteRequest.getOptionIds().isEmpty()) {
                    response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                    objectMapper.writeValue(response.getWriter(), Map.of(
                        "error", "Keine Optionen angegeben",
                        "pollId", pollId,
                        "errorCode", "NO_OPTIONS"
                    ));
                    return;
                }
                
                boolean success = pollService.voteMultiple(pollId, voteRequest.getOptionIds(), voteRequest.getUserId());
                
                if (success) {
                    Map<String, Integer> results = pollService.getPollResults(pollId);
                    logger.info("Mehrfach-Stimmen erfolgreich abgegeben für Poll: {}", pollId);
                    objectMapper.writeValue(response.getWriter(), results);
                } else {
                    response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                    objectMapper.writeValue(response.getWriter(), Map.of(
                        "error", "Stimmen konnten nicht abgegeben werden",
                        "pollId", pollId,
                        "errorCode", "VOTE_FAILED"
                    ));
                }
            } catch (Exception e) {
                logger.error("Fehler bei der Batch-Stimmabgabe", e);
                response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            }
        }
    }
    /**
     * Verarbeitet HTTP PUT-Requests für die Aktualisierung bestehender Ressourcen.
     *
     * @param request HTTP-Request mit JSON-Body und Authentifizierungskontext
     * @param response HTTP-Response für Bestätigung oder Fehlermeldung
     * @throws ServletException bei Servlet-Konfigurationsfehlern
     * @throws IOException bei I/O-Fehlern während Request/Response-Verarbeitung
     */
    @Override
    protected void doPut(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        String pathInfo = request.getPathInfo();
        String createdBy = (String) request.getAttribute("createdBy");
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        
        try {
            handlePutRequest(pathInfo, request, response, createdBy);
        } catch (Exception e) {
            logger.error("Fehler beim PUT-Request", e);
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            objectMapper.writeValue(response.getWriter(), Map.of("error", "Interner Server-Fehler"));
        }
    }
    /**
     * Zentrale Request-Routing-Methode für HTTP PUT-Anfragen.
     *
     * @param pathInfo der Pfad-Teil der URL nach dem Servlet-Mapping
     * @param request das HttpServletRequest-Objekt mit Request-Body und Headers
     * @param response das HttpServletResponse-Objekt für die Antwort
     * @param createdBy die Benutzer-ID des authentifizierten Benutzers für Autorisierungsprüfung
     *
     * @throws IOException wenn Fehler beim Lesen des Request-Bodies oder Schreiben der Response auftreten
     * @throws link.hsos.livepoll.service.helper.ForbiddenException wenn der Benutzer nicht autorisiert ist,
     *         die angeforderte Operation durchzuführen (wird von Handler-Methoden geworfen)
     */
    private void handlePutRequest(String pathInfo, HttpServletRequest request, HttpServletResponse response, String createdBy) throws IOException, link.hsos.livepoll.service.helper.ForbiddenException {
        if (pathInfo.matches("/[^/]+")) {
            handleUpdatePoll(pathInfo, request, response, createdBy);
        } else if (pathInfo.matches("/[^/]+/start")) {
            handleStartPoll(pathInfo, response, createdBy);
        } else if (pathInfo.matches("/[^/]+/close")) {
            handleClosePoll(pathInfo, response, createdBy);
        } else {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            objectMapper.writeValue(response.getWriter(), Map.of("error", "Ungültiger Pfad"));
        }
    }
    
    private void handleUpdatePoll(String pathInfo, HttpServletRequest request, HttpServletResponse response, String createdBy) throws IOException, link.hsos.livepoll.service.helper.ForbiddenException {
        String pollId = pathInfo.substring(1);
        Poll poll = objectMapper.readValue(request.getReader(), Poll.class);
        poll.setId(pollId);
        
        boolean success = pollService.updatePoll(poll, createdBy);
        
        if (success) {
            objectMapper.writeValue(response.getWriter(), poll);
        } else {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            objectMapper.writeValue(response.getWriter(), Map.of("error", "Poll nicht gefunden"));
        }
    }
    
    private void handleStartPoll(String pathInfo, HttpServletResponse response, String createdBy) throws IOException, link.hsos.livepoll.service.helper.ForbiddenException {
        String pollId = pathInfo.substring(1, pathInfo.lastIndexOf("/"));
        boolean success = pollService.startPoll(pollId, createdBy);
        
        if (success) {
            objectMapper.writeValue(response.getWriter(), Map.of("message", "Poll gestartet"));
        } else {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            objectMapper.writeValue(response.getWriter(), Map.of("error", "Poll nicht gefunden"));
        }
    }
    
    private void handleClosePoll(String pathInfo, HttpServletResponse response, String createdBy) throws IOException, link.hsos.livepoll.service.helper.ForbiddenException {
        String pollId = pathInfo.substring(1, pathInfo.lastIndexOf("/"));
        boolean success = pollService.closePoll(pollId, createdBy);
        
        if (success) {
            objectMapper.writeValue(response.getWriter(), Map.of("message", "Poll geschlossen"));
        } else {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            objectMapper.writeValue(response.getWriter(), Map.of("error", "Poll nicht gefunden"));
        }
    }
    /**
     * Verarbeitet HTTP DELETE-Requests für das Löschen von Ressourcen.
     *
     * @param request HTTP-Request mit Authentifizierungskontext
     * @param response HTTP-Response für Bestätigung oder Fehlermeldung
     * @throws ServletException bei Servlet-Konfigurationsfehlern
     * @throws IOException bei I/O-Fehlern während Request/Response-Verarbeitung
     */
    @Override
    protected void doDelete(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        String pathInfo = request.getPathInfo();
        String createdBy = (String) request.getAttribute("createdBy");
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        
        try {
            handleDeleteRequest(pathInfo, response, createdBy);
        } catch (Exception e) {
            logger.error("Fehler beim DELETE-Request", e);
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            objectMapper.writeValue(response.getWriter(), Map.of("error", "Interner Server-Fehler"));
        }
    }
    /**
     * Verarbeitet HTTP DELETE-Anfragen zum Löschen.
     *
     * @param pathInfo der URI-Pfad nach dem Servlet-Mapping
     * @param response das HttpServletResponse-Objekt
     * @param createdBy die Benutzer-ID des authentifizierten Benutzers für Autorisierungsprüfung
     *
     * @throws IOException bei Fehlern beim Schreiben der Response
     * @throws link.hsos.livepoll.service.helper.ForbiddenException wenn der Benutzer nicht berechtigt ist, die Umfrage zu löschen
     */
    private void handleDeleteRequest(String pathInfo, HttpServletResponse response, String createdBy) throws IOException, link.hsos.livepoll.service.helper.ForbiddenException {
        if (pathInfo.matches("/[^/]+")) {
            String pollId = pathInfo.substring(1);
            boolean success = pollService.deletePoll(pollId, createdBy);
            
            if (success) {
                objectMapper.writeValue(response.getWriter(), Map.of("message", "Poll gelöscht"));
            } else {
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                objectMapper.writeValue(response.getWriter(), Map.of("error", "Poll nicht gefunden"));
            }
        } else {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            objectMapper.writeValue(response.getWriter(), Map.of("error", "Ungültiger Pfad"));
        }
    }

    /**
     * DTO für HTTP POST-Anfragen zur Erstellung neuer Umfragen.
     */
    public static class CreatePollRequest {
        private String question;
        private String pollType;
        private List<String> options;
        private String startTime;
        private String endTime;
        private String createdBy;
        private boolean allowMultipleVotes;
        
        public String getQuestion() { return question; }
        public void setQuestion(String question) { this.question = question; }
        
        public String getPollType() { return pollType; }
        public void setPollType(String pollType) { this.pollType = pollType; }
        
        public List<String> getOptions() { return options; }
        public void setOptions(List<String> options) { this.options = options; }
        
        public String getStartTime() { return startTime; }
        public void setStartTime(String startTime) { this.startTime = startTime; }
        
        public String getEndTime() { return endTime; }
        public void setEndTime(String endTime) { this.endTime = endTime; }
        
        public String getCreatedBy() { return createdBy; }
        public void setCreatedBy(String createdBy) { this.createdBy = createdBy; }
        
        public boolean isAllowMultipleVotes() { return allowMultipleVotes; }
        public void setAllowMultipleVotes(boolean allowMultipleVotes) { this.allowMultipleVotes = allowMultipleVotes; }
    }
    /**
     * DTO für HTTP POST-Anfragen zur Abgabe einzelner Stimmen.
     */
    public static class VoteRequest {
        private Long optionId;
        private String userId;
        
        public Long getOptionId() { return optionId; }
        public void setOptionId(Long optionId) { this.optionId = optionId; }
        
        public String getUserId() { return userId; }
        public void setUserId(String userId) { this.userId = userId; }
    }
    /**
     * DTO für HTTP POST-Anfragen zur Abgabe mehrerer Stimmen gleichzeitig.
     */
    public static class VoteMultipleRequest {
        private List<Long> optionIds;
        private String userId;

        public List<Long> getOptionIds() { return optionIds; }
        public void setOptionIds(List<Long> optionIds) { this.optionIds = optionIds; }
        
        public String getUserId() { return userId; }
        public void setUserId(String userId) { this.userId = userId; }
    }
    
    private PollType createPollTypeFromString(String pollTypeString) {
        try {
            return PollType.valueOf(pollTypeString);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Unbekannter Poll-Typ: " + pollTypeString);
        }
    }
    /**
     * DTO für HTTP-Antworten mit vollständigen Umfragedaten.
     */
    public static class PollResponse {
        private final String id;
        private final String question;
        private final String pollType;
        private final String status;
        private final String createdAt;
        private final String startTime;
        private final String endTime;
        private final String createdBy;
        private final boolean isPublic;
        private final boolean allowMultipleVotes;
        private final String shortCode;
        private final List<OptionResponse> options;
        private final int totalVotes;
        
        public PollResponse(Poll poll) {
            this.id = poll.getId();
            this.question = poll.getQuestion();
            this.pollType = poll.getPollType().name();
            this.status = poll.getStatus().name();
            this.createdAt = poll.getCreatedAt().format(formatter);
            this.startTime = poll.getStartTime() != null ? poll.getStartTime().format(formatter) : null;
            this.endTime = poll.getEndTime() != null ? poll.getEndTime().format(formatter) : null;
            this.createdBy = poll.getCreatedBy();
            this.isPublic = poll.isPublic();
            this.allowMultipleVotes = poll.isAllowMultipleVotes();
            this.shortCode = poll.getShortCode();
            this.options = poll.getOptions().stream()
                    .map(OptionResponse::new)
                    .collect(java.util.stream.Collectors.toList());
            this.totalVotes = poll.getTotalVotes();
        }
        
        public String getId() { return id; }
        public String getQuestion() { return question; }
        public String getPollType() { return pollType; }
        public String getStatus() { return status; }
        public String getCreatedAt() { return createdAt; }
        public String getStartTime() { return startTime; }
        public String getEndTime() { return endTime; }
        public String getCreatedBy() { return createdBy; }
        public boolean isPublic() { return isPublic; }
        public boolean isAllowMultipleVotes() { return allowMultipleVotes; }
        public String getShortCode() { return shortCode; }
        public List<OptionResponse> getOptions() { return options; }
        public int getTotalVotes() { return totalVotes; }
    }
    /**
     * DTO für Umfrageoptionen in HTTP-Antworten.
     */
    public static class OptionResponse {
        private final Long id;
        private final String text;
        private final int votes;

        public OptionResponse(link.hsos.livepoll.model.Option option) {
            this.id = option.getId();
            this.text = option.getText();
            this.votes = option.getVotes();
        }

        public Long getId() {
            return id;
        }

        public String getText() {
            return text;
        }

        public int getVotes() {
            return votes;
        }
    }
    /**
     * DTO für XML-Export von Umfragevorlagen.
     */
    public static class TemplatePollXml {
        private final String question;
        private final String pollType;
        private final boolean allowMultipleVotes;
        private final List<TemplateOptionXml> options;


        public TemplatePollXml(Poll poll) {
            this.question = poll.getQuestion();
            this.pollType = poll.getPollType().name();
            this.allowMultipleVotes = poll.isAllowMultipleVotes();
            this.options = poll.getOptions().stream()
                    .map(TemplateOptionXml::new)
                    .collect(java.util.stream.Collectors.toList());
        }


        public String getQuestion() {
            return question;
        }

        public String getPollType() {
            return pollType;
        }

        public boolean isAllowMultipleVotes() {
            return allowMultipleVotes;
        }

        public List<TemplateOptionXml> getOptions() {
            return options;
        }

    }

    /**
     * DTO für Optionen in XML-Umfragevorlagen.
     */
    public static class TemplateOptionXml {
        private final String text;

        public TemplateOptionXml(link.hsos.livepoll.model.Option option) {
            this.text = option.getText();
        }

        public String getText() {
            return text;
        }
    }
    
    /**
     * Prüft ob sich aktive Polls geändert haben
     */
    private boolean hasActivePollsChanged(List<Poll> initialPolls, List<Poll> currentPolls) {
        if (initialPolls.size() != currentPolls.size()) {
            return true;
        }
        
        // Prüfe ob sich der Status oder die Stimmen geändert haben
        for (int i = 0; i < initialPolls.size(); i++) {
            Poll initialPoll = initialPolls.get(i);
            Poll currentPoll = currentPolls.get(i);
            
            if (!initialPoll.getId().equals(currentPoll.getId()) ||
                !initialPoll.getStatus().equals(currentPoll.getStatus()) ||
                initialPoll.getTotalVotes() != currentPoll.getTotalVotes()) {
                return true;
            }
        }
        
        return false;
    }
}