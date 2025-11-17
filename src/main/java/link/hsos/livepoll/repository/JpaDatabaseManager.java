    package link.hsos.livepoll.repository;

    import jakarta.persistence.EntityManager;
    import jakarta.persistence.EntityManagerFactory;
    import jakarta.persistence.Persistence;
    import org.slf4j.Logger;
    import org.slf4j.LoggerFactory;

    import java.io.File;
    import java.io.IOException;
    import java.io.InputStream;
    import java.util.HashMap;
    import java.util.Map;
    import java.util.Properties;

    public class JpaDatabaseManager {

        private static final Logger logger = LoggerFactory.getLogger(JpaDatabaseManager.class);

        private static JpaDatabaseManager instance;
        private EntityManagerFactory emf;

        static {
            try {
                Class.forName("org.h2.Driver");
                logger.info("H2 JDBC-Treiber erfolgreich im statischen Block geladen");

            } catch (ClassNotFoundException e) {
                logger.error("H2 JDBC-Treiber konnte im statischen Block nicht geladen werden", e);
            }
        }

        private JpaDatabaseManager() {
            logger.info("Initialisiere JpaDatabaseManager...");
            initializeDatabase();
            logger.info("JpaDatabaseManager erfolgreich initialisiert");
        }

        public static JpaDatabaseManager getInstance() {
            if (instance == null) {
                synchronized (JpaDatabaseManager.class) {
                    if (instance == null) {
                        instance = new JpaDatabaseManager();
                    }
                }
            }
            return instance;
        }

        /**
         * Initialisiert die Datenbank beim Start der Anwendung
         */
        public static void initialize() {
            getInstance();
        }

        /**
         * Erzwingt eine neue Initialisierung der Datenbank
         */
        public static void reinitialize() {
            synchronized (JpaDatabaseManager.class) {
                if (instance != null) {
                    instance.close();
                    instance = null;
                }
                instance = new JpaDatabaseManager();
            }
        }

        /**
         * Testet die Datenbankverbindung
         */
        public boolean testConnection() {
            if (emf == null || !emf.isOpen()) {
                logger.warn("EntityManagerFactory ist nicht verfügbar oder geschlossen");
                return false;
            }

            try (EntityManager em = createEntityManager()) {
                em.getTransaction().begin();
                em.createNativeQuery("SELECT 1").getSingleResult();
                em.getTransaction().commit();
                logger.debug("Datenbankverbindungstest erfolgreich");
                return true;
            } catch (Exception e) {
                logger.error("Datenbankverbindungstest fehlgeschlagen", e);
                return false;
            }
        }

        /**
         * Gibt den aktuellen Datenbankstatus zurück
         */
        public String getDatabaseStatus() {
            if (emf == null) {
                return "Nicht initialisiert";
            }
            if (!emf.isOpen()) {
                return "Geschlossen";
            }
            if (testConnection()) {
                return "Verbindung OK";
            } else {
                return "Verbindungsfehler";
            }
        }

        private void initializeDatabase() {
            int maxRetries = 3;
            Exception lastException = null;

            for (int attempt = 1; attempt <= maxRetries; attempt++) {
                try {
                    logger.info("Versuche Datenbankinitialisierung (Versuch {}/{})", attempt, maxRetries);
                    initializeDatabaseAttempt();
                    logger.info("Datenbankinitialisierung erfolgreich abgeschlossen");
                    return;

                } catch (Exception e) {
                    lastException = e;
                    logger.warn("Datenbankinitialisierung fehlgeschlagen (Versuch {}/{}): {}", attempt, maxRetries, e.getMessage());

                    if (attempt < maxRetries) {
                        try {
                            Thread.sleep(2000);
                        } catch (InterruptedException ie) {
                            Thread.currentThread().interrupt();
                            throw new RuntimeException("Datenbankinitialisierung wurde unterbrochen", ie);
                        }
                    }
                }
            }

            logger.error("Datenbankinitialisierung nach {} Versuchen fehlgeschlagen", maxRetries);

            try {
                Map<String, String> memoryProperties = new HashMap<>();
                memoryProperties.put("jakarta.persistence.jdbc.driver", "org.h2.Driver");
                memoryProperties.put("jakarta.persistence.jdbc.url", "jdbc:h2:mem:livepollMemory;DB_CLOSE_DELAY=-1");
                memoryProperties.put("jakarta.persistence.jdbc.user", "sa");
                memoryProperties.put("jakarta.persistence.jdbc.password", "");
                memoryProperties.put("hibernate.dialect", "org.hibernate.dialect.H2Dialect");
                memoryProperties.put("hibernate.hbm2ddl.auto", "create");
                memoryProperties.put("hibernate.show_sql", "true");
                memoryProperties.put("hibernate.format_sql", "true");

                emf = Persistence.createEntityManagerFactory(null, memoryProperties);
                logger.info("In-Memory-Datenbank erfolgreich initialisiert");

                try (EntityManager em = emf.createEntityManager()) {
                    em.getTransaction().begin();
                    em.createNativeQuery("SELECT 1").getSingleResult();
                    em.getTransaction().commit();
                    logger.info("In-Memory-Datenbankverbindung erfolgreich getestet");
                    return;
                }

            } catch (Exception memoryException) {
                logger.error("Fehler beim Initialisieren der In-Memory-Datenbank", memoryException);
            }

            throw new RuntimeException("JPA-Datenbank konnte nicht initialisiert werden", lastException);
        }

        private void initializeDatabaseAttempt() throws Exception {
            Properties config = loadDatabaseConfig();

            String dbPath = config.getProperty("database.path");
            String dbName = config.getProperty("database.name", "livepoll");
            String dbUser = config.getProperty("database.user", "sa");
            String dbPassword = config.getProperty("database.password", "");
            String autoServer = config.getProperty("database.auto_server", "true");

            File dbDir = new File(dbPath);
            logger.info("Initialisiere Datenbank im konfigurierten Verzeichnis '{}'...", dbDir.getAbsolutePath());

            if (!dbDir.exists()) {
                boolean created = dbDir.mkdirs();
                if (created) {
                    logger.info("Datenbankverzeichnis '{}' erfolgreich erstellt", dbDir.getAbsolutePath());
                } else {
                    logger.warn("Konnte Datenbankverzeichnis '{}' nicht erstellen", dbDir.getAbsolutePath());
                }
            }

            File dbFile = new File(dbDir, dbName + ".mv.db");
            boolean dbExists = dbFile.exists();
            logger.info("Datenbankdatei existiert: {} ({})", dbExists, dbFile.getAbsolutePath());

            Map<String, String> properties = new HashMap<>();
            properties.put("jakarta.persistence.jdbc.driver", "org.h2.Driver");

            String jdbcUrl = "jdbc:h2:file:" + dbDir.getAbsolutePath() + "/" + dbName;
            if ("true".equals(autoServer)) {
                jdbcUrl += ";AUTO_SERVER=TRUE";
            }
            jdbcUrl += ";DB_CLOSE_DELAY=-1";

            properties.put("jakarta.persistence.jdbc.url", jdbcUrl);
            properties.put("jakarta.persistence.jdbc.user", dbUser);
            properties.put("jakarta.persistence.jdbc.password", dbPassword);

            properties.put("hibernate.dialect", config.getProperty("hibernate.dialect", "org.hibernate.dialect.H2Dialect"));
            properties.put("hibernate.hbm2ddl.auto", config.getProperty("hibernate.hbm2ddl.auto", "create"));
            properties.put("hibernate.show_sql", config.getProperty("hibernate.show_sql", "true"));
            properties.put("hibernate.format_sql", config.getProperty("hibernate.format_sql", "true"));

            // Zusätzliche Hibernate-Einstellungen für bessere Stabilität
            properties.put("hibernate.connection.provider_disables_autocommit", "true");
            properties.put("hibernate.connection.isolation", "2"); // READ_COMMITTED
            properties.put("hibernate.jdbc.batch_size", "20");
            properties.put("hibernate.order_inserts", "true");
            properties.put("hibernate.order_updates", "true");

            // Wenn die Datenbank bereits existiert, verwende update statt create
            if (dbExists) {
                properties.put("hibernate.hbm2ddl.auto", "update");
                logger.info("Verwende hibernate.hbm2ddl.auto=update für existierende Datenbank");
            } else {
                logger.info("Verwende hibernate.hbm2ddl.auto=create für neue Datenbank");
            }

            logger.info("Erstelle EntityManagerFactory mit Hibernate-Einstellungen: {}", properties);

            emf = Persistence.createEntityManagerFactory(null, properties);
            logger.info("JPA EntityManagerFactory erfolgreich initialisiert");

            Thread.sleep(500);

            try (EntityManager em = emf.createEntityManager()) {
                em.getTransaction().begin();
                em.createNativeQuery("SELECT 1").getSingleResult();
                em.getTransaction().commit();
                logger.info("Datenbankverbindung erfolgreich getestet - Datenbank wird in '{}' gespeichert",
                          new File(dbDir, dbName + ".mv.db").getAbsolutePath());

                if (!dbExists) {
                    logger.info("Teste das neu erstellte Schema...");
                    em.getTransaction().begin();

                    try {
                        em.createNativeQuery("SELECT COUNT(*) FROM Poll").getSingleResult();
                        logger.info("Poll-Tabelle erfolgreich erstellt und getestet");
                    } catch (Exception schemaException) {
                        logger.warn("Poll-Tabelle konnte nicht abgefragt werden: {}", schemaException.getMessage());
                    }

                    em.getTransaction().commit();
                }
            }
        }

        /**
         * Lädt die Datenbankkonfiguration aus der database.properties Datei
         */
        private Properties loadDatabaseConfig() {
            Properties config = new Properties();

            try (InputStream input = getClass().getClassLoader().getResourceAsStream("database.properties")) {
                if (input != null) {
                    config.load(input);
                    logger.info("Datenbankkonfiguration erfolgreich geladen");

                    String dbPath = config.getProperty("database.path");
                    if (dbPath != null) {
                        logger.info("Konfigurierter Datenbankpfad: {}", dbPath);
                    }

                } else {
                    logger.warn("Konnte database.properties nicht finden, verwende Standardeinstellungen");
                    config.setProperty("database.path", System.getProperty("user.dir") + "/db");
                    config.setProperty("database.name", "livepoll");
                    config.setProperty("database.user", "sa");
                    config.setProperty("database.password", "");
                    config.setProperty("database.auto_server", "true");
                    config.setProperty("hibernate.dialect", "org.hibernate.dialect.H2Dialect");
                    config.setProperty("hibernate.hbm2ddl.auto", "update");
                    config.setProperty("hibernate.show_sql", "true");
                    config.setProperty("hibernate.format_sql", "true");
                }

            } catch (IOException e) {
                logger.error("Fehler beim Laden der Datenbankkonfiguration", e);
                config.setProperty("database.path", System.getProperty("user.dir") + "/db");
                config.setProperty("database.name", "livepoll");
                config.setProperty("database.user", "sa");
                config.setProperty("database.password", "");
                config.setProperty("database.auto_server", "true");
                config.setProperty("hibernate.dialect", "org.hibernate.dialect.H2Dialect");
                config.setProperty("hibernate.hbm2ddl.auto", "update");
                config.setProperty("hibernate.show_sql", "true");
                config.setProperty("hibernate.format_sql", "true");
            }

            return config;
        }

        /**
         * Ermittelt das tatsächliche Projektverzeichnis, auch wenn SmartTomcat in einem temporären Verzeichnis läuft
         */
        private String getProjectDirectory() {
            try {
                String classPath = System.getProperty("java.class.path");
                if (classPath != null && classPath.contains("target")) {
                    String targetPath = classPath.split("target")[0];
                    File projectDir = new File(targetPath);
                    if (projectDir.exists() && projectDir.isDirectory()) {
                        logger.info("Projektverzeichnis aus Classpath ermittelt: {}", projectDir.getAbsolutePath());
                        return projectDir.getAbsolutePath();
                    }
                }
            } catch (Exception e) {
                logger.debug("Konnte Projektverzeichnis nicht aus Classpath ermitteln", e);
            }

            // 2. Versuche, das Projektverzeichnis aus dem aktuellen Arbeitsverzeichnis zu ermitteln
            String currentDir = System.getProperty("user.dir");
            logger.info("Aktuelles Arbeitsverzeichnis: {}", currentDir);

            if (currentDir.contains(".SmartTomcat")) {
                File currentFile = new File(currentDir);
                while (currentFile != null && currentFile.exists()) {
                    if (new File(currentFile, "pom.xml").exists() ||
                        new File(currentFile, "src").exists() ||
                        new File(currentFile, "target").exists()) {
                        logger.info("Projektverzeichnis gefunden: {}", currentFile.getAbsolutePath());
                        return currentFile.getAbsolutePath();
                    }
                    currentFile = currentFile.getParentFile();
                }
            }

            logger.info("Verwende aktuelles Arbeitsverzeichnis als Projektverzeichnis: {}", currentDir);
            return currentDir;
        }

        public EntityManager createEntityManager() {
            if (emf == null) {
                throw new RuntimeException("EntityManagerFactory ist nicht initialisiert");
            }
            return emf.createEntityManager();
        }

        public void close() {
            if (emf != null && emf.isOpen()) {
                        try {
                emf.close();
                logger.info("EntityManagerFactory erfolgreich geschlossen");
            } catch (Exception e) {
                logger.error("Fehler beim Schließen der EntityManagerFactory", e);
            }
            }
        }

        /**
         * Schließt die Datenbank und setzt die Instanz zurück
         */
        public void shutdown() {
            close();
            instance = null;
            logger.info("JpaDatabaseManager heruntergefahren und Instanz zurückgesetzt");
        }

        /**
         * Prüft, ob die Datenbankverbindung noch aktiv ist
         */
        public boolean isConnected() {
            if (emf == null || !emf.isOpen()) {
                return false;
            }
            return testConnection();
        }

        public void executeInTransaction(Runnable operation) {
            EntityManager em = createEntityManager();
            try {
                em.getTransaction().begin();
                operation.run();
                em.getTransaction().commit();
            } catch (Exception e) {
                if (em.getTransaction().isActive()) {
                    em.getTransaction().rollback();
                }
                throw new RuntimeException("Transaktion fehlgeschlagen", e);
            } finally {
                em.close();
            }
        }

        public <T> T executeInTransactionWithResult(TransactionOperation<T> operation) {
            EntityManager em = createEntityManager();
            try {
                em.getTransaction().begin();
                T result = operation.execute(em);
                em.getTransaction().commit();
                return result;
            } catch (Exception e) {
                if (em.getTransaction().isActive()) {
                    em.getTransaction().rollback();
                }
                throw new RuntimeException("Transaktion fehlgeschlagen", e);
            } finally {
                em.close();
            }
        }

        @FunctionalInterface
        public interface TransactionOperation<T> {
            T execute(EntityManager em);
        }
    }