package link.hsos.livepoll.service;



import link.hsos.livepoll.servlet.auth.helpers.Config;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.UUID;

/**
 * Service-Klasse für die Generierung und Verifikation von kryptographisch signierten Voting-Tokens.
 * Diese Klasse implementiert ein tokenbasiertes System zur Identifikation von Abstimmenden
 * ohne vollständige Benutzerauthentifizierung. Sie verwendet HMAC-SHA256-Signaturen um
 * die Integrität und Authentizität der generierten Benutzer-IDs zu gewährleisten.
 */
public class VotingTokenService {
    private static final Logger logger = LoggerFactory.getLogger(VotingTokenService.class);

    /**
     * Generiert eine signierte Benutzer-ID für Abstimmungszwecke.

     * @return Pair mit Benutzer-ID (links) und zugehöriger HMAC-Signatur (rechts)
     * @throws RuntimeException wenn die Signaturerstellung fehlschlägt aufgrund von
     *                          kryptographischen Problemen oder ungültiger Konfiguration
     */
    public static Pair<String, String> getSignedUserId() {
        try {
            logger.info("Generating new UserID (UUID)");
            String userId = UUID.randomUUID().toString();
            logger.info("UserID generated: " + userId);

            String signature = sign(userId);
            logger.info("Signature generated for UserID: "+ signature);

            return Pair.of(userId, signature);
        } catch (Exception e) {
            logger.error( "Failed to generate signed UserId", e);
            throw new RuntimeException(e);
        }
    }

    /**
     * Erstellt eine HMAC-SHA256-Signatur für die gegebenen Daten.

     * @param data Die zu signierenden Daten als String
     * @return Base64-URL-kodierte HMAC-SHA256-Signatur
     * @throws NoSuchAlgorithmException wenn HMAC-SHA256 nicht verfügbar ist
     * @throws InvalidKeyException wenn das konfigurierte Secret ungültig ist
     */
    private static String sign(String data) throws NoSuchAlgorithmException, InvalidKeyException {
        Mac hmac = Mac.getInstance("HmacSHA256");
        SecretKeySpec key = new SecretKeySpec(Config.HMAC_SECRET.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
        hmac.init(key);
        byte[] signature = hmac.doFinal(data.getBytes(StandardCharsets.UTF_8));
        return Base64.getUrlEncoder().withoutPadding().encodeToString(signature);
    }
    /**
     * Verifiziert die Authentizität und Integrität einer Signatur für gegebene Daten.
     *
     * @param data Die ursprünglichen Daten (z.B. Benutzer-ID)
     * @param signature Die zu verifizierende Signatur
     * @return true wenn die Signatur gültig ist, false bei Manipulationen oder ungültigen Signaturen
     * @throws RuntimeException wenn die Signaturberechnung fehlschlägt aufgrund von
     *                          kryptographischen Problemen oder ungültiger Konfiguration
     */
    public static boolean verify(String data, String signature) {
        try{
            String calculatedSignature = sign(data);
            return calculatedSignature.equals(signature);
        } catch (Exception e) {
            logger.error("Failed to verify signature", e);
            throw new RuntimeException(e);
        }

    }



}
