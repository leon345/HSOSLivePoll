package link.hsos.livepoll.service;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;

import java.util.HashMap;
import java.util.Map;

/**
 * Service für die Generierung von QR-Codes als SVG
 */
public class QRCodeService {
    
    /**
     * Generiert einen QR-Code als SVG-String
     * 
     * @param content Der zu kodierende Inhalt (z.B. URL)
     * @param size Die Größe des QR-Codes in Pixeln
     * @param margin Der Rand um den QR-Code
     * @return SVG-String des QR-Codes
     * @throws WriterException Falls die Generierung fehlschlägt
     */
    public String generateQRCodeSVG(String content, int size, int margin) throws WriterException {
        Map<EncodeHintType, Object> hints = new HashMap<>();
        hints.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.M);
        hints.put(EncodeHintType.MARGIN, margin);
        hints.put(EncodeHintType.CHARACTER_SET, "UTF-8");
        
        QRCodeWriter qrCodeWriter = new QRCodeWriter();
        BitMatrix bitMatrix = qrCodeWriter.encode(content, BarcodeFormat.QR_CODE, size, size, hints);
        
        return generateSVG(bitMatrix, size);
    }
    
    /**
     * Generiert einen QR-Code als SVG-String mit Standardeinstellungen
     * 
     * @param content Der zu kodierende Inhalt
     * @return SVG-String des QR-Codes
     * @throws WriterException Falls die Generierung fehlschlägt
     */
    public String generateQRCodeSVG(String content) throws WriterException {
        return generateQRCodeSVG(content, 200, 2);
    }
    
    /**
     * Konvertiert eine BitMatrix in SVG
     * 
     * @param bitMatrix Die BitMatrix des QR-Codes
     * @param size Die Größe des QR-Codes
     * @return SVG-String
     */
    private String generateSVG(BitMatrix bitMatrix, int size) {
        StringBuilder svg = new StringBuilder();
        svg.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
        svg.append("<svg xmlns=\"http://www.w3.org/2000/svg\" version=\"1.1\" ");
        svg.append("width=\"").append(size).append("\" height=\"").append(size).append("\" ");
        svg.append("viewBox=\"0 0 ").append(size).append(" ").append(size).append("\">\n");
        svg.append("<rect width=\"").append(size).append("\" height=\"").append(size).append("\" fill=\"white\"/>\n");
        
        for (int x = 0; x < size; x++) {
            for (int y = 0; y < size; y++) {
                if (bitMatrix.get(x, y)) {
                    svg.append("<rect x=\"").append(x).append("\" y=\"").append(y).append("\" ");
                    svg.append("width=\"1\" height=\"1\" fill=\"black\"/>\n");
                }
            }
        }
        
        svg.append("</svg>");
        return svg.toString();
    }
}
