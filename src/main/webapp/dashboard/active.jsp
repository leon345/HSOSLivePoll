<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<!DOCTYPE html>
<html lang="de">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>LivePoll - Aktive Umfragen</title>
    <link rel="stylesheet" href="../assets/css/main.css">
    <link rel="stylesheet" href="../assets/css/last-polls.css">
    <link href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.0.0/css/all.min.css" rel="stylesheet">
    <script src="https://cdn.jsdelivr.net/npm/chart.js"></script>


    <link rel="icon" type="image/png" href="../assets/img/fav/favicon-96x96.png" sizes="96x96" />
    <link rel="icon" type="image/svg+xml" href="../assets/img/fav/favicon.svg" />
    <link rel="shortcut icon" href="../favicon.ico" />
    <link rel="apple-touch-icon" sizes="180x180" href="../assets/img/fav/apple-touch-icon.png" />
    <meta name="apple-mobile-web-app-title" content="Livepoll" />
    <link rel="manifest" href="../assets/img/fav/site.webmanifest" />
</head>
<body>
    <div class="container">
        <jsp:include page="/modules/header.jsp" />

        <main class="main-content">
            <div class="active-header">
                <h2>Alle Umfragen</h2>
                <p>Laufende Umfragen und deren Ergebnisse</p>
            </div>
            
            <div id="active-polls-list" class="polls-grid">
            </div>

            <div id="active-polls-loading" class="loading-state" style="display: none;">
                <div class="loader"></div>
                <p>Lade  Umfragen...</p>
            </div>

            <div id="active-polls-error" class="error-state" style="display: none;">
                <i class="fas fa-exclamation-triangle"></i>
                <p>Fehler beim Laden der Umfragen</p>
                <button class="btn btn-primary" onclick="loadActivePols()">
                    <i class="fas fa-redo"></i>
                    Erneut versuchen
                </button>
            </div>
        </main>
    </div>

    <jsp:include page="/modules/footer.jsp" />

    <script src="../assets/js/util.js"></script>
    <script src="../assets/js/main.js"></script>
    <script src="../assets/js/active-polls.js"></script>
</body>
</html>