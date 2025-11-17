package link.hsos.livepoll.servlet;

import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import jakarta.servlet.annotation.WebListener;
import link.hsos.livepoll.repository.JpaDatabaseManager;
import link.hsos.livepoll.websocket.WebSocketManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * ServletContextListener, der die Datenbank beim Start der Anwendung initialisiert.
 * Dies stellt sicher, dass die Datenbankdatei sofort erstellt wird und nicht erst
 * beim ersten Datenbankzugriff.
 */
@WebListener
public class DatabaseInitializationListener implements ServletContextListener {
    
    private static final Logger logger = LoggerFactory.getLogger(DatabaseInitializationListener.class);
    
    @Override
    public void contextInitialized(ServletContextEvent sce) {
        logger.info("Initialisiere Datenbank beim Start der Anwendung...");
        
        try {
            JpaDatabaseManager dbManager = JpaDatabaseManager.getInstance();
            
            Thread.sleep(1000);
            
            int maxRetries = 3;
            boolean connectionOk = false;
            
            for (int i = 0; i < maxRetries && !connectionOk; i++) {
                try {
                    if (dbManager.testConnection()) {
                        connectionOk = true;
                        logger.info("Datenbankverbindung erfolgreich getestet (Versuch {})", i + 1);
                    } else {
                        logger.warn("Datenbankverbindungstest fehlgeschlagen (Versuch {}/{})", i + 1, maxRetries);
                        if (i < maxRetries - 1) {
                            Thread.sleep(2000);
                        }
                    }
                } catch (Exception e) {
                    logger.warn("Fehler beim Datenbankverbindungstest (Versuch {}/{}): {}", i + 1, maxRetries, e.getMessage());
                    if (i < maxRetries - 1) {
                        Thread.sleep(2000);
                    }
                }
            }
            
            if (connectionOk) {
                logger.info("Datenbank erfolgreich initialisiert und Verbindung getestet");
                logger.info("Datenbankstatus: {}", dbManager.getDatabaseStatus());

                try {
                    WebSocketManager.startUpdateScheduler();
                    logger.info("WebSocket-Update-Scheduler erfolgreich gestartet");
                } catch (Exception e) {
                    logger.warn("Fehler beim Starten des WebSocket-Schedulers: {}", e.getMessage());
                }
                
            } else {
                logger.error("Datenbankverbindung konnte nach {} Versuchen nicht hergestellt werden", maxRetries);
                throw new RuntimeException("Datenbankverbindung konnte nicht hergestellt werden");
            }
            
        } catch (Exception e) {
            logger.error("Kritischer Fehler beim Initialisieren der Datenbank beim Start", e);
            throw new RuntimeException("Datenbank konnte nicht initialisiert werden", e);
        }
    }
    
    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        logger.info("Schließe Datenbankverbindungen beim Herunterfahren der Anwendung...");
        
        try {
            // WebSocket-Scheduler stoppen
            try {
                WebSocketManager.stopUpdateScheduler();
                logger.info("WebSocket-Update-Scheduler erfolgreich gestoppt");
            } catch (Exception e) {
                logger.warn("Fehler beim Stoppen des WebSocket-Schedulers: {}", e.getMessage());
            }
            
            JpaDatabaseManager dbManager = JpaDatabaseManager.getInstance();
            dbManager.shutdown();
            logger.info("Datenbankverbindungen erfolgreich geschlossen");
            
        } catch (Exception e) {
            logger.error("Fehler beim Schließen der Datenbankverbindungen", e);
        }
    }
}
