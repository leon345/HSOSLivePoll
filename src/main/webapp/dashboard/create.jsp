<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<!DOCTYPE html>
<html lang="de">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>LivePoll - Neue Umfrage</title>
    <link rel="stylesheet" href="/assets/css/main.css">
    <link href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.0.0/css/all.min.css" rel="stylesheet">
    <script src="https://cdn.jsdelivr.net/npm/chart.js"></script>

    <link rel="icon" type="image/png" href="/assets/img/fav/favicon-96x96.png" sizes="96x96" />
    <link rel="icon" type="image/svg+xml" href="/assets/img/fav/favicon.svg" />
    <link rel="shortcut icon" href="/favicon.ico" />
    <link rel="apple-touch-icon" sizes="180x180" href="/assets/img/fav/apple-touch-icon.png" />
    <meta name="apple-mobile-web-app-title" content="Livepoll" />
    <link rel="manifest" href="/assets/img/fav/site.webmanifest" />
</head>
<body>
    <div class="container">
        <jsp:include page="/modules/header.jsp" />

        <main class="main-content">
            <form id="create-poll-form" class="create-form" novalidate>
                <div class="form-layout">
                    <div class="form-column">
                        <div class="form-section">
                            <h3 class="section-title">
                                <i class="fas fa-question"></i>
                                Umfrage
                            </h3>
                            
                            <div class="form-group">
                                <label for="question" class="required">Frage</label>
                                <input type="text" 
                                       id="question" 
                                       name="question" 
                                       required 
                                       maxlength="200"
                                       placeholder="Ihre Frage hier eingeben"
                                       aria-describedby="question-help">
                                <div class="char-counter">
                                    <span id="question-counter">0</span>/200
                                </div>
                            </div>
                            
                            <div class="form-group">
                                <label for="poll-type" class="required">Typ</label>
                                <select id="poll-type" name="pollType" required aria-describedby="poll-type-help">
                                    <option value="SINGLE_CHOICE">Eine Antwort</option>
                                    <option value="MULTIPLE_CHOICE">Mehrere Antworten</option>
                                </select>
                                <div id="poll-type-help" class="form-help">
                                    Eine Antwort: Nur eine Antwort wählbar<br>
                                    Mehrere Antworten: Mehrere Antworten wählbar
                                </div>
                            </div>
                        </div>
                        
                        <div class="form-section">
                            <h3 class="section-title">
                                <i class="fas fa-cog"></i>
                                Konfiguration
                            </h3>
                            

                            
                            <div class="form-group">
                                <label class="checkbox-label" for="allow-multiple">
                                    <input type="checkbox" 
                                           id="allow-multiple" 
                                           name="allowMultipleVotes"
                                           aria-describedby="allow-multiple-help">
                                    <div class="checkbox-content">
                                        <div class="checkbox-title">Mehrfachstimmen erlauben</div>
                                        <div class="checkbox-description">
                                            Teilnehmer können mehrfach abstimmen
                                        </div>
                                    </div>
                                </label>
                            </div>
                        </div>
                    </div>
                    
                    <div class="form-column">
                        <div class="form-section">
                            <h3 class="section-title">
                                <i class="fas fa-list"></i>
                                Antwortoptionen
                            </h3>
                            
                            <div id="options-container">
                                <div class="option-input" data-option-id="1">
                                    <input type="text" 
                                           name="options[]" 
                                           placeholder="Antwort 1" 
                                           required 
                                           maxlength="100"
                                           aria-label="Antwort 1">
                                    <button type="button" 
                                            class="remove-option" 
                                            onclick="removeOption(this)"
                                            aria-label="Entfernen"
                                            title="Entfernen"
                                            style="display: none;">
                                        <i class="fas fa-times"></i>
                                    </button>
                                </div>
                                
                                <div class="option-input" data-option-id="2">
                                    <input type="text" 
                                           name="options[]" 
                                           placeholder="Antwort 2" 
                                           required 
                                           maxlength="100"
                                           aria-label="Antwort 2">
                                    <button type="button" 
                                            class="remove-option" 
                                            onclick="removeOption(this)"
                                            aria-label="Entfernen"
                                            title="Entfernen"
                                            style="display: none;">
                                        <i class="fas fa-times"></i>
                                    </button>
                                </div>
                            </div>
                            
                            <button type="button" id="add-option-btn" class="btn btn-secondary" onclick="addOption()">
                                <i class="fas fa-plus"></i>
                                Antwort hinzufügen
                            </button>
                        </div>
                    </div>
                </div>
                
                <div class="form-actions">
                    <button type="button" id="import-btn" class="btn btn-secondary" onclick="importPollFromXml()">
                        <i class="fas fa-arrow-up"></i>
                        Umfrage importieren
                    </button>
                    <input type="file" id="import-file" accept=".xml" style="display:none" />
                    <a href="/dashboard" class="btn btn-secondary">
                        <i class="fas fa-times"></i>
                        Abbrechen
                    </a>
                    <button type="submit" id="submit-btn" class="btn btn-primary">
                        <i class="fas fa-check"></i>
                        Umfrage erstellen
                    </button>
                </div>
            </form>
        </main>
    </div>


    <jsp:include page="/modules/footer.jsp" />


    <script src="../assets/js/util.js"></script>
    <script src="../assets/js/main.js"></script>
    <script src="../assets/js/create-poll.js"></script>
    <script src="../assets/js/import-xml.js"></script>

</body>
</html>