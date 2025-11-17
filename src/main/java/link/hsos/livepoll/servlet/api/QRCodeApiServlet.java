package link.hsos.livepoll.servlet.api;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import link.hsos.livepoll.service.QRCodeService;

import java.io.IOException;
import java.io.PrintWriter;

/**
 * API-Servlet für die Generierung von QR-Codes als SVG
 */
@WebServlet("/api/qrcode")
public class QRCodeApiServlet extends HttpServlet {
    
    private QRCodeService qrCodeService;
    
    @Override
    public void init() throws ServletException {
        qrCodeService = new QRCodeService();
    }
    /**
     * Generiert einen QR-Code als SVG basierend auf URL-Parametern.
     *
     * @param request HTTP-Request mit Parametern: content (pflicht), size (optional), margin (optional)
     * @param response HTTP-Response mit SVG-Content-Type
     * @throws ServletException bei Servlet-Fehlern
     * @throws IOException bei Ein-/Ausgabefehlern
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        String content = request.getParameter("content");
        String sizeStr = request.getParameter("size");
        String marginStr = request.getParameter("margin");
        
        if (content == null || content.trim().isEmpty()) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Parameter 'content' ist erforderlich");
            return;
        }
        
        int size = 200;
        int margin = 2;
        
        try {
            if (sizeStr != null && !sizeStr.trim().isEmpty()) {
                size = Integer.parseInt(sizeStr);
                if (size < 50 || size > 1000) {
                    response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Größe muss zwischen 50 und 1000 liegen");
                    return;
                }
            }
            
            if (marginStr != null && !marginStr.trim().isEmpty()) {
                margin = Integer.parseInt(marginStr);
                if (margin < 0 || margin > 10) {
                    response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Rand muss zwischen 0 und 10 liegen");
                    return;
                }
            }
        } catch (NumberFormatException e) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Ungültige Zahlenparameter");
            return;
        }
        
        try {
            String svg = qrCodeService.generateQRCodeSVG(content, size, margin);
            
            response.setContentType("image/svg+xml");
            response.setCharacterEncoding("UTF-8");
            response.setHeader("Cache-Control", "public, max-age=3600");
            
            try (PrintWriter out = response.getWriter()) {
                out.print(svg);
            }
            
        } catch (Exception e) {
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, 
                "Fehler bei der QR-Code-Generierung: " + e.getMessage());
        }
    }
    /**
     * Lehnt POST-Anfragen ab, da nur GET unterstützt wird.
     *
     * @param request HTTP-Request
     * @param response HTTP-Response mit 405 Method Not Allowed
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        response.sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED, "Nur GET-Anfragen sind erlaubt");
    }
}
