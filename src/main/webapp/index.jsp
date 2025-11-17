<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html lang="de">
<head>
    <base href="<%= request.getContextPath() %>/">
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>LivePoll</title>
    <link rel="stylesheet" href="./assets/css/landing.css">
    <link rel="stylesheet" href="./assets/css/main.css">


    <link rel="icon" type="image/png" href="/assets/img/fav/favicon-96x96.png" sizes="96x96" />
    <link rel="icon" type="image/svg+xml" href="/assets/img/fav/favicon.svg" />
    <link rel="shortcut icon" href="/favicon.ico" />
    <link rel="apple-touch-icon" sizes="180x180" href="/assets/img/fav/apple-touch-icon.png" />
    <meta name="apple-mobile-web-app-title" content="Livepoll" />
    <link rel="manifest" href="./assets/img/fav/site.webmanifest" />
</head>
<body>
<div class="landing-center">
    <img src="assets/img/logo.svg" alt="LivePoll Logo" class="landing-logo">
    <form class="landing-form" id="poll-form" onsubmit="handlePollSubmission(event)">
        <label class="landing-label" for="poll-code">An Umfrage teilnehmen</label>
        <div class="landing-input-row">
            <input class="landing-input" id="poll-code" name="code" type="text" placeholder="Poll-ID eingeben" autocomplete="off" required>
            <button class="landing-btn-go" type="submit" title="Teilnehmen">
                <img src="assets/img/icons/next.svg" alt="Weiter" style="width: 20px; height: 20px;">
            </button>
        </div>
        <div class="landing-divider">
            <div class="landing-divider-line"></div>
            <div class="landing-divider-text">oder</div>
            <div class="landing-divider-line"></div>
        </div>
        <a href="./dashboard">
            <button type="button" class="landing-btn-create">Eigene Poll erstellen</button>
        </a>

    </form>
</div>
<div class="landing-footer">
    <a href="legal/datenschutz.jsp">Datenschutz</a>
    <a href="legal/impressum.jsp">Impressum</a>
</div>

<script>
function handlePollSubmission(event) {
    event.preventDefault();
    
    const pollCode = document.getElementById('poll-code').value.trim();
    
    if (!pollCode) {
        return;
    }
    
    if (pollCode.match(/^[A-Z0-9]{3,10}$/)) {
        window.location.href = './s/' + pollCode;
    } else {
        window.location.href = './modules/voting.jsp?code=' + encodeURIComponent(pollCode);
    }
}
</script>
</body>
</html>