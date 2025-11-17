<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html lang="de">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>LivePoll - Dashboard</title>
    <link rel="stylesheet" href="../assets/css/main.css">
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
            <div class="dashboard-header">
                <h2>Dashboard</h2>
                <p>Verwalten Sie Ihre Live-Umfragen</p>
            </div>
            
            <div class="stats-grid">
                <div class="stat-card">
                    <div class="stat-icon">
                        <i class="fas fa-poll"></i>
                    </div>
                    <div class="stat-content">
                        <h3 id="total-polls">0</h3>
                        <p>Gesamte Umfragen</p>
                    </div>
                </div>
                <div class="stat-card">
                    <div class="stat-icon active">
                        <i class="fas fa-play"></i>
                    </div>
                    <div class="stat-content">
                        <h3 id="active-polls">0</h3>
                        <p>Aktive Umfragen</p>
                    </div>
                </div>
                <div class="stat-card">
                    <div class="stat-icon votes">
                        <i class="fas fa-vote-yea"></i>
                    </div>
                    <div class="stat-content">
                        <h3 id="total-votes">0</h3>
                        <p>Gesamte Stimmen</p>
                    </div>
                </div>
            </div>
            
            <jsp:include page="/modules/dashboard/last.jsp" />
        </main>
    </div>

    <jsp:include page="/modules/footer.jsp" />

    <script src="../assets/js/util.js"></script>
    <script src="../assets/js/main.js"></script>
    <script src="../assets/js/dashboard.js"></script>
</body>
</html> 