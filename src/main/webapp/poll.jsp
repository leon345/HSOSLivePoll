<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html lang="de">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>LivePoll - Umfrage Details</title>
    <link rel="stylesheet" href="/assets/css/main.css">
    <link rel="stylesheet" href="/assets/css/poll.css">
    <link rel="stylesheet" href="/assets/css/delete-modal.css">
    <link href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.0.0/css/all.min.css" rel="stylesheet">
    <script src="https://cdn.jsdelivr.net/npm/chart.js"></script>
</head>
<body>
    <div class="container">
        <jsp:include page="/modules/header.jsp" />

        <main class="main-content">
            <div class="poll-detail-container">
                <div id="poll-content">
                </div>
                
                <div id="no-poll" class="no-poll" style="display: none;">
                    <i class="fas fa-exclamation-triangle"></i>
                    <h2>Umfrage nicht gefunden</h2>
                    <p>Die angeforderte Umfrage konnte nicht geladen werden.</p>
                    <a href="/dashboard" class="action-button">
                        <i class="fas fa-arrow-left"></i>
                        Zurück zum Dashboard
                    </a>
                </div>
            </div>
        </main>
    </div>

    <jsp:include page="/modules/footer.jsp" />

    <script src="/assets/js/util.js"></script>
    <script src="/assets/js/main.js"></script>
    <script src="/assets/js/delete-modal.js"></script>
    <script src="/assets/js/poll.js"></script>
</body>
</html> 