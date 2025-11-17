<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<!DOCTYPE html>
<html lang="de">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>LivePoll - Umfrage Details</title>
    <link rel="stylesheet" href="../../assets/css/main.css">
    <link rel="stylesheet" href="../../assets/css/poll-detail.css">
    <link rel="stylesheet" href="../../assets/css/delete-modal.css">
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
            <div class="poll-detail-container">
                <div class="poll-header-simple">
                    <div class="header-left">
                        <h1 class="poll-question" id="poll-question">Lade Umfrage...</h1>
                    </div>
                    <div class="header-right">
                        <button class="btn-activate" id="activate-poll-btn" onclick="activatePollById()" style="display: none;">
                            <i class="fas fa-play"></i>
                            Aktivieren
                        </button>
                        <button class="btn-danger" id="close-poll-btn" onclick="closePollById()" style="display: none;">
                            <i class="fas fa-stop"></i>
                            Beenden
                        </button>
                        <button class="btn-share" onclick="sharePoll()">
                            <i class="fas fa-share"></i>
                            Teilen
                        </button>
                        <button class="btn-presentation" onclick="openPresentation()">
                            <i class="fas fa-tv"></i>
                            Präsentation
                        </button>
                        <button class="btn-danger" onclick="deletePollByIdWithModal()" title="Umfrage löschen">
                            <i class="fas fa-trash"></i>
                            Löschen
                        </button>
                    </div>
                </div>



                <div class="content-grid">
                    <div class="results-section">
                        <div class="section-header">
                            <h3>Live Ergebnisse</h3>
                            <div class="live-indicator" id="live-indicator" style="display: none;">
                                <div class="live-dot"></div> Live
                            </div>
                        </div>
                        
                        <div class="chart-container">
                            <canvas id="results-chart"></canvas>
                        </div>
                        
                        <div id="results-list" class="results-list">
                        </div>
                    </div>
                    
                    <div class="info-section">
                        <div class="section-header">
                            <h3>Details</h3>
                        </div>
                        
                        <div class="info-cards">
                            <div class="info-card">
                                <div class="card-label">Code</div>
                                <div class="card-value" id="poll-code">-</div>
                            </div>
                            <div class="info-card">
                                <div class="card-label">Short-Code</div>
                                <div class="card-value">
                                    <span id="poll-shortcode">-</span>
                                    <a href="#" id="shortcode-link" target="_blank" class="shortcode-link" style="display: none;">
                                        <i class="fas fa-external-link-alt"></i>
                                    </a>
                                </div>
                            </div>
                            <div class="info-card">
                                <div class="card-label">Status</div>
                                <div class="card-value" id="info-status">-</div>
                            </div>
                            <div class="info-card">
                                <div class="card-label">Typ</div>
                                <div class="card-value" id="info-type">-</div>
                            </div>
                            <div class="info-card">
                                <div class="card-label">Optionen</div>
                                <div class="card-value" id="info-options">-</div>
                            </div>
                            <div class="info-card">
                                <div class="card-label">Erstellt</div>
                                <div class="card-value" id="info-created">-</div>
                            </div>
                            <div class="info-card">
                                    <button class="btn-share" type="button" onclick="downloadPollResults()"  >Umfrageergebnisse exportieren</button>
                            </div>
                            <div class="info-card">
                                <button class="btn-share" type="button" onclick="downloadPollTemplate()"  >Umfragevorlage exportieren</button>
                            </div>
                        </div>
                    </div>
                </div>
                
                <div id="no-poll" class="no-poll" style="display: none;">
                    <i class="fas fa-exclamation-triangle"></i>
                    <h2>Umfrage nicht gefunden</h2>
                    <p>Die angeforderte Umfrage konnte nicht geladen werden.</p>
                    <a href="/dashboard" class="btn-primary">
                        <i class="fas fa-arrow-left"></i> Zurück zum Dashboard
                    </a>
                </div>
            </div>
        </main>
    </div>

    <jsp:include page="/modules/footer.jsp" />

    <script src="../../assets/js/util.js"></script>
    <script src="../../assets/js/main.js"></script>
    <script src="../../assets/js/delete-modal.js"></script>
    <script src="../../assets/js/poll-detail.js"></script>
</body>
</html> 