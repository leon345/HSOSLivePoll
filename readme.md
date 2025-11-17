# LivePoll

Ein modernes, webbasiertes Umfragetool für Live-Abstimmungen und Präsentationen mit Echtzeit-Ergebnissen von [Paul Horstmann](mailto:paul.horstmann@hs-osnabrueck.de) und Leon Seeger für die Lehrveranstaltung „Verteilte Systeme” der Hochschule Osnabrück.


## Features

- **Interaktive Umfragen**: Erstellen Sie Single-Choice und Multiple-Choice Umfragen
- **Echtzeit-Abstimmung**: Live-Updates der Ergebnisse während der Abstimmung
- **QR-Code Integration**: Einfache Teilnahme über QR-Codes oder Kurzlinks
- **Präsentationsmodus**: Live-Anzeige der Ergebnisse für Präsentationen
- **Microsoft Azure AD**: Sichere Authentifizierung über Azure Active Directory
- **Import/Export**: XML-Import/Export für Umfragen, CSV-Export für Ergebnisse
- **Responsive Design**: Funktioniert auf allen Geräten und Bildschirmgrößen
- **WebSocket-Support**: Echtzeit-Kommunikation für Live-Updates

## Live-Demo

**Testumgebung verfügbar unter:** [poll.hsos.link](https://poll.hsos.link)

Demo-Account:
- **E-Mail:** HSOSVerteilteSysteme@leonseegergmail.onmicrosoft.com
- **Passwort:** K6.7v_Cs@o2@qH

## Installation

1. **Repository klonen**
   ```bash
   git clone <repository-url>
   cd Projekt
   ```

2. **Umgebungsvariablen konfigurieren**
   ```bash
   # .env Datei erstellen
   cp .env.example .env
   ```

3. **Anwendung starten**
   ```bash
   docker-compose up -d
   ```

4. **Anwendung aufrufen**
   ```
   http://localhost:8089
   ```


## Konfiguration

### Datenbank-Einstellungen

```properties
# src/main/resources/database.properties
jdbc.url=jdbc:mysql://localhost:3306/livepoll
jdbc.username=livepoll
jdbc.password=your_password
jdbc.driver=com.mysql.cj.jdbc.Driver
```

### Azure AD-Konfiguration

```properties
# src/main/resources/authentication.properties
azure.client.id=your_client_id
azure.client.secret=your_client_secret
azure.tenant.id=your_tenant_id
azure.redirect.uri=http://localhost:8080/auth/callback
```

### Docker-Umgebungsvariablen

```bash
.env
DB_ROOT_PASSWORD=your_root_password
DB_NAME=livepoll
DB_USER=livepoll
DB_PASSWORD=your_password
AZURE_CLIENT_ID=your_client_id
AZURE_CLIENT_SECRET=your_client_secret
AZURE_TENANT_ID=your_tenant_id
```

