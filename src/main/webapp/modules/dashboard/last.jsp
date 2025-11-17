<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<div class="recent-polls">
    <div class="section-header">
        <h3>Letzte Umfragen</h3>
        <div class="section-actions">
            <button class="btn btn-sm btn-secondary" onclick="refreshRecentPolls()">
                <i class="fas fa-sync-alt"></i>
                Aktualisieren
            </button>
        </div>
    </div>
    
    <div id="recent-polls-list" class="polls-table-container">
    </div>
    
    <div id="recent-polls-loading" class="loading-state" style="display: none;">
        <div class="loader"></div>
        <p>Lade letzte Umfragen...</p>
    </div>
    
    <div id="recent-polls-error" class="error-state" style="display: none;">
        <i class="fas fa-exclamation-triangle"></i>
        <p>Fehler beim Laden der letzten Umfragen</p>
        <button class="btn btn-primary" onclick="loadRecentPolls()">
            <i class="fas fa-redo"></i>
            Erneut versuchen
        </button>
    </div>
</div>

<link rel="stylesheet" href="/assets/css/last-polls.css">
<link rel="stylesheet" href="/assets/css/delete-modal.css">
    <script src="/assets/js/util.js"></script>
    <script src="/assets/js/delete-modal.js"></script>
    <script src="/assets/js/last-polls.js"></script>