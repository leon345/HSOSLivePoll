<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html lang="de">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
            <title>LivePoll - Präsentation</title>
        <link rel="stylesheet" href="/assets/css/main.css">
        <link rel="stylesheet" href="/assets/css/presentation.css">
        <link href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.0.0/css/all.min.css" rel="stylesheet">

        <link rel="icon" type="image/png" href="/assets/img/fav/favicon-96x96.png" sizes="96x96" />
        <link rel="icon" type="image/svg+xml" href="/assets/img/fav/favicon.svg" />
        <link rel="shortcut icon" href="/favicon.ico" />
        <link rel="apple-touch-icon" sizes="180x180" href="/assets/img/fav/apple-touch-icon.png" />
        <meta name="apple-mobile-web-app-title" content="Livepoll" />
        <link rel="manifest" href="/assets/img/fav/site.webmanifest" />
</head>
<body>
    <div class="presentation-container">
        <header class="presentation-header">
            <div class="header-content">
                <h1 class="poll-question" id="poll-question">Lade Umfrage...</h1>
                <div class="poll-status">
                    <div class="live-indicator" id="live-indicator" style="display: none;">
                        <div class="live-dot"></div> LIVE
                    </div>
                    <div class="poll-code" id="poll-code">Code: -</div>
    
                </div>
            </div>
        </header>

        <main class="presentation-main">
            <div class="presentation-grid">
                <div class="results-section">
                    <div class="chart-container">
                        <div id="custom-chart" class="custom-chart">
                        </div>
                    </div>
                </div>

                <div class="share-section">
                    <div class="qr-container">
                        <div id="qr-code" class="qr-code"></div>
                    </div>
                </div>
            </div>  
        </main>

        <div class="fullscreen-controls">
            <button class="btn-fullscreen" id="fullscreen-btn" onclick="toggleFullscreen()">
                <i class="fas fa-expand"></i>
                Vollbild
            </button>
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

                <script src="/assets/js/util.js"></script>
            <script src="/assets/js/presentation.js"></script>
</body>
</html>
