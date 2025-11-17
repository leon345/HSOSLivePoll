<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<!DOCTYPE html>
<html lang="de">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Datenschutz - LivePoll</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css" rel="stylesheet">
    <link rel="stylesheet" href="../assets/css/main.css">
    <style>
        .privacy-content {
            max-width: 800px;
            margin: 0 auto;
            padding: 2rem;
        }
        .privacy-header {
            text-align: center;
            margin-bottom: 3rem;
        }
        .privacy-section {
            margin-bottom: 2rem;
        }
        .privacy-section h2 {
            color: var(--primary-color);
            margin-bottom: 1rem;
            font-weight: 600;
        }
        .privacy-section h3 {
            color: var(--text-primary);
            margin-bottom: 0.75rem;
            font-weight: 500;
        }
        .privacy-section p {
            margin-bottom: 1rem;
            line-height: 1.7;
        }
        .privacy-section ul {
            margin-bottom: 1rem;
            padding-left: 1.5rem;
        }
        .privacy-section li {
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
    </style>
</head>
<body>
    <div class="container">
        <div class="privacy-content">
            <a href="../index.jsp" class="back-link">
                <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                    <path d="M19 12H5M12 19l-7-7 7-7"/>
                </svg>
                Zurück zur Startseite
            </a>
            
            <div class="privacy-header">
                <h1>Datenschutzerklärung</h1>
                <p class="text-muted">Stand: August 2025</p>
            </div>

            <div class="privacy-section">
                <h2>1. Verantwortlicher</h2>
                <p>
                    <strong>LivePoll</strong><br>
                    Hochschule Osnabrück<br>
                    Fakultät für Ingenieurwissenschaften und Informatik<br>
                    Albrechtstraße 30<br>
                    49076 Osnabrück<br>
                    Deutschland
                </p>
                <p>
                    <strong>Kontakt:</strong><br>
                    E-Mail: datenschutz@livepoll.de
                </p>
            </div>

            <div class="privacy-section">
                <h2>2. Erhebung und Verarbeitung personenbezogener Daten</h2>
                
                <h3>2.1 Automatisch erfasste Daten</h3>
                <p>Bei der Nutzung unserer Website werden automatisch folgende Daten erfasst:</p>
                <ul>
                    <li>IP-Adresse des zugreifenden Rechners</li>
                    <li>Datum und Uhrzeit des Zugriffs</li>
                    <li>Übertragene Datenmenge</li>
                    <li>Website, von der aus der Zugriff erfolgte</li>
                    <li>Verwendeter Browser und Betriebssystem</li>
                </ul>

                <h3>2.2 Daten bei der Nutzung von LivePoll</h3>
                <p>Bei der Erstellung und Teilnahme an Umfragen werden folgende Daten verarbeitet:</p>
                <ul>
                    <li>Umfrageinhalte (Fragen und Antwortoptionen)</li>
                    <li>Abgegebene Stimmen (anonymisiert)</li>
                    <li>Umfragecodes zur Identifikation</li>
                    <li>Zeitstempel für Umfrageerstellung und -teilnahme</li>
                </ul>
            </div>

            <div class="privacy-section">
                <h2>3. Zweck der Datenverarbeitung</h2>
                <p>Die Verarbeitung personenbezogener Daten erfolgt für folgende Zwecke:</p>
                <ul>
                    <li>Bereitstellung und Verbesserung der LivePoll-Dienste</li>
                    <li>Technische Administration und Sicherheit der Website</li>
                    <li>Analyse der Nutzung zur Optimierung des Angebots</li>
                    <li>Einhaltung gesetzlicher Verpflichtungen</li>
                </ul>
            </div>

            <div class="privacy-section">
                <h2>4. Rechtsgrundlagen</h2>
                <p>Die Verarbeitung personenbezogener Daten erfolgt auf folgenden Rechtsgrundlagen:</p>
                <ul>
                    <li><strong>Art. 6 Abs. 1 lit. a DSGVO:</strong> Einwilligung des Nutzers</li>
                    <li><strong>Art. 6 Abs. 1 lit. b DSGVO:</strong> Erfüllung eines Vertrags</li>
                    <li><strong>Art. 6 Abs. 1 lit. f DSGVO:</strong> Berechtigtes Interesse</li>
                </ul>
            </div>

            <div class="privacy-section">
                <h2>5. Speicherdauer</h2>
                <p>Personenbezogene Daten werden nur so lange gespeichert, wie es für die genannten Zwecke erforderlich ist:</p>
                <ul>
                    <li>Umfragedaten: Bis zum Ende der Umfrage + 30 Tage</li>
                    <li>Log-Daten: 90 Tage</li>
                    <li>Technische Daten: Bis zur Beendigung der Nutzung</li>
                </ul>
            </div>

            <div class="privacy-section">
                <h2>6. Ihre Rechte</h2>
                <p>Sie haben folgende Rechte bezüglich Ihrer personenbezogenen Daten:</p>
                <ul>
                    <li><strong>Auskunftsrecht:</strong> Sie können Auskunft über Ihre gespeicherten Daten verlangen</li>
                    <li><strong>Berichtigungsrecht:</strong> Sie können falsche Daten berichtigen lassen</li>
                    <li><strong>Löschungsrecht:</strong> Sie können die Löschung Ihrer Daten verlangen</li>
                    <li><strong>Einschränkungsrecht:</strong> Sie können die Verarbeitung einschränken lassen</li>
                    <li><strong>Widerspruchsrecht:</strong> Sie können der Verarbeitung widersprechen</li>
                    <li><strong>Datenübertragbarkeit:</strong> Sie können Ihre Daten in einem strukturierten Format erhalten</li>
                </ul>
            </div>

            <div class="privacy-section">
                <h2>7. Cookies</h2>
                <p>Wir verwenden Cookies für die technische Funktionalität der Website:</p>
                <ul>
                    <li><strong>Session-Cookies:</strong> Für die Verwaltung von Umfragesitzungen</li>
                    <li><strong>Technische Cookies:</strong> Für die grundlegende Funktionalität</li>
                </ul>
                <p>Sie können Cookies in Ihren Browsereinstellungen deaktivieren, was jedoch die Funktionalität beeinträchtigen kann.</p>
            </div>

            <div class="privacy-section">
                <h2>8. Sicherheit</h2>
                <p>Wir setzen technische und organisatorische Sicherheitsmaßnahmen ein, um Ihre Daten gegen Manipulation, Verlust, Zerstörung oder gegen den Zugriff unberechtigter Personen zu schützen.</p>
            </div>

            <div class="privacy-section">
                <h2>9. Änderungen der Datenschutzerklärung</h2>
                <p>Wir behalten uns vor, diese Datenschutzerklärung anzupassen, um sie an geänderte Rechtslagen oder bei Änderungen unserer Dienste aktuell zu halten. Die jeweils aktuelle Version ist auf dieser Seite verfügbar.</p>
            </div>

            <div class="privacy-section">
                <h2>10. Kontakt</h2>
                <p>Bei Fragen zur Verarbeitung Ihrer personenbezogenen Daten können Sie sich an uns wenden:</p>
                <p>
                    <strong>Datenschutzbeauftragter:</strong><br>
                    E-Mail: datenschutz@livepoll.de
                </p>
                <p>Sie haben auch das Recht, sich bei der zuständigen Aufsichtsbehörde zu beschweren.</p>
            </div>
        </div>
    </div>

    <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/js/bootstrap.bundle.min.js"></script>
</body>
</html>