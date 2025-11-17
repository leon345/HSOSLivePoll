<%@ page contentType="text/html;charset=UTF-8" language="java" %>

<div id="share-modal" class="modal">
    <div class="modal-content">
        <div class="modal-header">
            <h3>Umfrage teilen</h3>
            <button class="close-btn" onclick="closeModal('share-modal')">
                <i class="fas fa-times"></i>
            </button>
        </div>
        <div class="modal-body">
            <p>Teilen Sie diesen Link mit Teilnehmern:</p>
            <div class="share-link">
                <input type="text" id="share-url" readonly>
                <button class="btn btn-primary" onclick="copyShareUrl()">
                    <i class="fas fa-copy"></i>
                    Kopieren
                </button>
            </div>
        </div>
    </div>
</div>