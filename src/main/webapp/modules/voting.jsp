<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html lang="de">
<head>
    <meta charset="UTF-8">
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
    <meta http-equiv="Content-Script-Type" content="text/javascript; charset=UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Abstimmen</title>

    <link rel="stylesheet" href="../assets/css/main.css">
    <link rel="stylesheet" href="../assets/css/voting.css">

    <link rel="icon" type="image/png" href="../assets/img/fav/favicon-96x96.png" sizes="96x96" />
    <link rel="icon" type="image/svg+xml" href="../assets/img/fav/favicon.svg" />
    <link rel="shortcut icon" href="../favicon.ico" />
    <link rel="apple-touch-icon" sizes="180x180" href="../assets/img/fav/apple-touch-icon.png" />
    <meta name="apple-mobile-web-app-title" content="Livepoll" />
    <link rel="manifest" href="../assets/img/fav/site.webmanifest" />
</head>
<body>

<div class="voting-container">
    <header class="voting-header">
        <div class="voting-logo">
            <a href="../index.jsp" style="text-decoration: none; display: flex; align-items: center; gap: 1rem;">
                <img src="../assets/img/logo.svg" alt="Livepoll Logo">
                <span class="voting-logo-text">Livepoll</span>
            </a>
        </div>
        <div class="voting-shortcode" id="voting-shortcode">
            <i class="fas fa-hashtag"></i>
            <span id="shortcode-text">...</span>
        </div>
    </header>

    <main class="voting-main">
        <div id="poll-content" class="voting-content">
        </div>
        
        <div id="no-poll" class="voting-closed" style="display: none;">
            <h2>Umfrage nicht verfügbar</h2>
            <p>Die angeforderte Umfrage konnte nicht geladen werden oder ist nicht aktiv.</p>
            <a href="../index.jsp" class="btn btn-primary">
                <i class="fas fa-arrow-left"></i>
                Zurück zum Start
            </a>
        </div>
    </main>
</div>

<div id="loading-overlay" class="loading-overlay">
    <div>…</div>
</div>

<div id="toast-container"></div>

<jsp:include page="/modules/footer.jsp" />
<script src="/assets/js/util.js" charset="UTF-8"></script>
<script src="/assets/js/main.js" charset="UTF-8"></script>
<script src="/assets/js/voting.js" charset="UTF-8"></script>

</body>
</html>
