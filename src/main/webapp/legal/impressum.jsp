<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<!DOCTYPE html>
<html lang="de">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Impressum - LivePoll</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css" rel="stylesheet">
    <link rel="stylesheet" href="../assets/css/main.css">
    <style>
        .impressum-content {
            max-width: 800px;
            margin: 0 auto;
            padding: 2rem;
        }
        .impressum-header {
            text-align: center;
            margin-bottom: 3rem;
        }
        .impressum-section {
            margin-bottom: 2rem;
        }
        .impressum-section h2 {
            color: var(--primary-color);
            margin-bottom: 1rem;
            font-weight: 600;
        }
        .impressum-section h3 {
            color: var(--text-primary);
            margin-bottom: 0.75rem;
            font-weight: 500;
        }
        .impressum-section p {
            margin-bottom: 1rem;
            line-height: 1.7;
        }
        .impressum-section ul {
            margin-bottom: 1rem;
            padding-left: 1.5rem;
        }
        .impressum-section li {
            margin-bottom: 0.5rem;
        }
        .back-link {
            display: inline-flex;
            align-items: center;
            gap: 0.5rem;
            color: var(--primary-color);
            text-decoration: none;
            font-weight: 500;
            margin-bottom: 2rem;
        }
        .back-link:hover {
            color: var(--primary-hover);
        }
        .contact-info {
            background-color: var(--background-color);
            padding: 1.5rem;
            border-radius: var(--border-radius);
            border: 1px solid var(--border-color);
        }
    </style>
</head>
<body>
    <div class="container">
        <div class="impressum-content">
            <a href="../index.jsp" class="back-link">
                <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                    <path d="M19 12H5M12 19l-7-7 7-7"/>
                </svg>
                Zurück zur Startseite
            </a>
            
            <div class="impressum-header">
                <h1>Impressum</h1>
                <p class="text-muted">Angaben gemäß § 5 TMG</p>
            </div>

            <div class="impressum-section">
                <h2>1. Diensteanbieter</h2>
                <div class="contact-info">
                    <p>
                        <strong>LivePoll</strong><br>
                        Ein Projekt der Hochschule Osnabrück<br>
                        Fakultät für Ingenieurwissenschaften und Informatik<br>
                        Albrechtstraße 30<br>
                        49076 Osnabrück<br>
                        Deutschland
                    </p>
                </div>
            </div>

            <div class="impressum-section">
                <h2>2. Kontakt</h2>
                <div class="contact-info">
                    <p>
                        <strong>E-Mail:</strong> info@livepoll.de
                    </p>
                </div>
            </div>

            <div class="impressum-section">
                <h2>3. Aufsichtsbehörde</h2>
                <p>
                    <strong>Niedersächsisches Ministerium für Wissenschaft und Kultur</strong><br>
                    Leibnizufer 9<br>
                    30169 Hannover<br>
                    Deutschland
                </p>
            </div>

            <div class="impressum-section">
                <h2>4. Umsatzsteuer-ID</h2>
                <p>Die Hochschule Osnabrück ist eine öffentlich-rechtliche Körperschaft und daher von der Umsatzsteuer befreit.</p>
            </div>

            <div class="impressum-section">
                <h2>5. Verantwortlich für den Inhalt</h2>
                <p>Verantwortlich für den Inhalt nach § 55 Abs. 2 RStV:</p>
                <p>
                    Hochschule Osnabrück<br>
                    Fakultät für Ingenieurwissenschaften und Informatik<br>
                    Albrechtstraße 30<br>
                    49076 Osnabrück<br>
                    Deutschland
                </p>
            </div>

            <div class="impressum-section">
                <h2>6. Haftungsausschluss</h2>
                
                <h3>6.1 Haftung für Inhalte</h3>
                <p>Die Inhalte unserer Seiten wurden mit größter Sorgfalt erstellt. Für die Richtigkeit, Vollständigkeit und Aktualität der Inhalte können wir jedoch keine Gewähr übernehmen. Als Diensteanbieter sind wir gemäß § 7 Abs.1 TMG für eigene Inhalte auf diesen Seiten nach den allgemeinen Gesetzen verantwortlich.</p>

                <h3>6.2 Haftung für Links</h3>
                <p>Unser Angebot enthält Links zu externen Webseiten Dritter, auf deren Inhalte wir keinen Einfluss haben. Deshalb können wir für diese fremden Inhalte auch keine Gewähr übernehmen. Für die Inhalte der verlinkten Seiten ist stets der jeweilige Anbieter oder Betreiber der Seiten verantwortlich.</p>

                <h3>6.3 Urheberrecht</h3>
                <p>Die durch die Seitenbetreiber erstellten Inhalte und Werke auf diesen Seiten unterliegen dem deutschen Urheberrecht. Die Vervielfältigung, Bearbeitung, Verbreitung und jede Art der Verwertung außerhalb der Grenzen des Urheberrechtes bedürfen der schriftlichen Zustimmung des jeweiligen Autors bzw. Erstellers.</p>
            </div>

            <div class="impressum-section">
                <h2>7. Datenschutz</h2>
                <p>Die Nutzung unserer Webseite ist in der Regel ohne Angabe personenbezogener Daten möglich. Soweit auf unseren Seiten personenbezogene Daten (beispielsweise Name, Anschrift oder E-Mail-Adressen) erhoben werden, erfolgt dies, soweit möglich, stets auf freiwilliger Basis.</p>
                <p>Weitere Informationen zum Datenschutz finden Sie in unserer <a href="datenschutz.jsp" style="color: var(--primary-color);">Datenschutzerklärung</a>.</p>
            </div>

            <div class="impressum-section">
                <h2>8. Streitschlichtung</h2>
                <p>Die Europäische Kommission stellt eine Plattform zur Online-Streitbeilegung (OS) bereit: <a href="https://ec.europa.eu/consumers/odr/" target="_blank" style="color: var(--primary-color);">https://ec.europa.eu/consumers/odr/</a></p>
                <p>Wir sind nicht bereit oder verpflichtet, an Streitbeilegungsverfahren vor einer Verbraucherschlichtungsstelle teilzunehmen.</p>
            </div>

            <div class="impressum-section">
                <h2>10. Technische Informationen</h2>
                <p><strong>Entwicklung:</strong> LivePoll ist ein studentisches Projekt im Rahmen des Studiengangs Verteilte Systeme an der Hochschule Osnabrück.</p>
                <p><strong>Technologien:</strong> Java, JSP, WebSocket, Bootstrap, JavaScript</p>
            </div>
        </div>
    </div>

    <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/js/bootstrap.bundle.min.js"></script>
</body>
</html>