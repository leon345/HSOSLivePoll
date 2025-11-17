let currentModalData = {
    type: null,
    pollId: null,
    pollData: null,
    onConfirm: null
};

function createModals() {
    const actionModalHTML = `
        <div id="action-modal" class="action-modal">
            <div class="action-modal-content">
                <div class="action-modal-header">
                    <div class="action-modal-icon" id="action-modal-icon">
                        <i class="fas fa-exclamation-triangle"></i>
                    </div>
                    <h3 class="action-modal-title" id="action-modal-title">Aktion bestätigen</h3>
                </div>
                <div class="action-modal-body">
                    <p class="action-modal-message" id="action-modal-message">
                        Sind Sie sicher, dass Sie diese Aktion ausführen möchten?
                    </p>
                    <div class="action-modal-poll-info">
                        <div class="action-modal-poll-question" id="action-modal-poll-question">
                        </div>
                        <div class="action-modal-poll-meta" id="action-modal-poll-meta">
                        </div>
                    </div>
                </div>
                <div class="action-modal-actions">
                    <button class="btn btn-secondary" onclick="closeActionModal()">
                        <i class="fas fa-times"></i>
                        Abbrechen
                    </button>
                    <button class="btn" id="action-confirm-btn" onclick="confirmAction()">
                        <i class="fas fa-check"></i>
                        Bestätigen
                    </button>
                </div>
            </div>
        </div>
    `;
    
    document.body.insertAdjacentHTML('beforeend', actionModalHTML);
    
    document.addEventListener('keydown', function(e) {
        if (e.key === 'Escape') {
            if (isActionModalOpen()) {
                closeActionModal();
            }
        }
    });
    
    document.getElementById('action-modal').addEventListener('click', function(e) {
        if (e.target === this) {
            closeActionModal();
        }
    });
}

const modalConfigs = {
    delete: {
        title: 'Umfrage löschen',
        message: 'Sind Sie sicher, dass Sie diese Umfrage unwiderruflich löschen möchten? Diese Aktion kann nicht rückgängig gemacht werden.',
        icon: 'fa-exclamation-triangle',
        iconClass: 'danger',
        buttonClass: 'btn-danger',
        buttonText: 'Löschen',
        buttonIcon: 'fa-trash',
        loadingText: 'Löschen...',
        successMessage: 'Umfrage erfolgreich gelöscht',
        errorMessage: 'Fehler beim Löschen der Umfrage'
    },
    close: {
        title: 'Umfrage beenden',
        message: 'Sind Sie sicher, dass Sie diese Umfrage beenden möchten? Nach dem Beenden können keine weiteren Stimmen abgegeben werden.',
        icon: 'fa-stop-circle',
        iconClass: 'warning',
        buttonClass: 'btn-warning',
        buttonText: 'Beenden',
        buttonIcon: 'fa-stop',
        loadingText: 'Beenden...',
        successMessage: 'Umfrage erfolgreich beendet',
        errorMessage: 'Fehler beim Beenden der Umfrage'
    },
    activate: {
        title: 'Umfrage aktivieren',
        message: 'Sind Sie sicher, dass Sie diese Umfrage aktivieren möchten? Nach der Aktivierung können Teilnehmer abstimmen.',
        icon: 'fa-play-circle',
        iconClass: 'success',
        buttonClass: 'btn-success',
        buttonText: 'Aktivieren',
        buttonIcon: 'fa-play',
        loadingText: 'Aktivieren...',
        successMessage: 'Umfrage erfolgreich aktiviert',
        errorMessage: 'Fehler beim Aktivieren der Umfrage'
    }
};

function openActionModal(type, pollId, pollData = null, onConfirm = null) {
    const config = modalConfigs[type];
    if (!config) {
        return;
    }
    
    currentModalData = {
        type: type,
        pollId: pollId,
        pollData: pollData,
        onConfirm: onConfirm
    };
    
    const modal = document.getElementById('action-modal');
    if (!modal) {
        createModals();
    }
    
    updateActionModalContent(config, pollData);
    
    document.getElementById('action-modal').classList.add('active');
    
    setTimeout(() => {
        const cancelBtn = document.querySelector('.action-modal-actions .btn-secondary');
        if (cancelBtn) cancelBtn.focus();
    }, 100);
}

function closeActionModal() {
    const modal = document.getElementById('action-modal');
    if (modal) {
        modal.classList.remove('active');
    }
    
    currentModalData = {
        type: null,
        pollId: null,
        pollData: null,
        onConfirm: null
    };
    
    const confirmBtn = document.getElementById('action-confirm-btn');
    if (confirmBtn) {
        confirmBtn.classList.remove('loading');
        const config = modalConfigs[currentModalData.type];
        if (config) {
            confirmBtn.innerHTML = `<i class="fas ${config.buttonIcon}"></i> ${config.buttonText}`;
        }
    }
}

function isActionModalOpen() {
    const modal = document.getElementById('action-modal');
    return modal && modal.classList.contains('active');
}

function updateActionModalContent(config, pollData) {
    const iconElement = document.getElementById('action-modal-icon');
    const titleElement = document.getElementById('action-modal-title');
    const messageElement = document.getElementById('action-modal-message');
    const questionElement = document.getElementById('action-modal-poll-question');
    const metaElement = document.getElementById('action-modal-poll-meta');
    const confirmBtn = document.getElementById('action-confirm-btn');
    
    if (iconElement) {
        iconElement.className = `action-modal-icon ${config.iconClass}`;
        iconElement.innerHTML = `<i class="fas ${config.icon}"></i>`;
    }
    
    if (titleElement) {
        titleElement.textContent = config.title;
    }
    
    if (messageElement) {
        messageElement.textContent = config.message;
    }
    
    if (confirmBtn) {
        confirmBtn.className = `btn ${config.buttonClass}`;
        confirmBtn.innerHTML = `<i class="fas ${config.buttonIcon}"></i> ${config.buttonText}`;
    }
    
    if (pollData && questionElement && metaElement) {
        questionElement.textContent = pollData.question;
        
        const statusText = pollData.status === 'ACTIVE' ? 'Aktiv' : 'Geschlossen';
        const statusIcon = pollData.status === 'ACTIVE' ? 'fa-play' : 'fa-stop';
        
        metaElement.innerHTML = `
            <span><i class="fas ${statusIcon}"></i> ${statusText}</span>
            <span><i class="fas fa-users"></i> ${pollData.totalVotes || 0} Stimmen</span>
            <span><i class="fas fa-list"></i> ${pollData.options ? pollData.options.length : 0} Optionen</span>
            <span><i class="fas fa-clock"></i> ${formatRelativeTime(pollData.createdAt)}</span>
        `;
    }
}

async function confirmAction() {
    if (!currentModalData.pollId || !currentModalData.type) return;
    
    const config = modalConfigs[currentModalData.type];
    if (!config) return;
    
    const confirmBtn = document.getElementById('action-confirm-btn');
    if (!confirmBtn) return;
    
    confirmBtn.classList.add('loading');
    confirmBtn.innerHTML = `<i class="fas fa-spinner fa-spin"></i> ${config.loadingText}`;
    
    try {
        if (currentModalData.onConfirm) {
            await currentModalData.onConfirm(currentModalData.pollId);
        } else {
            switch (currentModalData.type) {
                case 'delete':
                    await deletePoll(currentModalData.pollId);
                    break;
                case 'close':
                    await closePoll(currentModalData.pollId);
                    if (typeof currentPoll !== 'undefined' && currentPoll && currentPoll.id === currentModalData.pollId) {
                        currentPoll.status = 'CLOSED';
                        if (typeof updatePollDetailUI === 'function') {
                            updatePollDetailUI(currentPoll);
                        }
                    }
                    break;
                case 'activate':
                    await activatePoll(currentModalData.pollId);
                    if (typeof currentPoll !== 'undefined' && currentPoll && currentPoll.id === currentModalData.pollId) {
                        currentPoll.status = 'ACTIVE';
                        if (typeof updatePollDetailUI === 'function') {
                            updatePollDetailUI(currentPoll);
                        }
                    }
                    break;
                default:
                    throw new Error('Unbekannter Aktionstyp');
            }
        }
        
        showToast(config.successMessage, 'success');
        
        closeActionModal();
        
        if (typeof loadRecentPolls === 'function') {
            loadRecentPolls();
        } else if (typeof loadActivePols === 'function') {
            loadActivePols();
        } else if (typeof loadPollDetail === 'function') {
            loadPollDetail();
        } else {
            // Wenn wir auf der Detail-Seite sind, zum Dashboard weiterleiten
            if (window.location.pathname.includes('/poll/') || window.location.pathname.includes('/s/') || window.location.pathname.includes('/dashboard/poll/')) {
                window.location.href = '/dashboard';
            }
        }
        
    } catch (error) {
        console.error(`${currentModalData.type} Poll Error:`, error);
        
        let errorMessage = config.errorMessage;
        if (error.message.includes('Zugriff verweigert')) {
            errorMessage = `Zugriff verweigert: Sie können nur Ihre eigenen Umfragen ${currentModalData.type === 'delete' ? 'löschen' : currentModalData.type === 'close' ? 'beenden' : 'aktivieren'}`;
        } else if (error.message) {
            errorMessage += ': ' + error.message;
        }
        
        showToast(errorMessage, 'error');
        
    } finally {
        confirmBtn.classList.remove('loading');
        confirmBtn.innerHTML = `<i class="fas ${config.buttonIcon}"></i> ${config.buttonText}`;
    }
}

function deletePollByIdWithModal(pollId) {
    let pollData = null;
    if (typeof recentPollsData !== 'undefined' && recentPollsData) {
        pollData = recentPollsData.find(poll => poll.id === pollId);
    }
    openActionModal('delete', pollId, pollData);
}

function closePollByIdWithModal(pollId) {
    let pollData = null;
    if (typeof recentPollsData !== 'undefined' && recentPollsData) {
        pollData = recentPollsData.find(poll => poll.id === pollId);
    }
    openActionModal('close', pollId, pollData);
}

function activatePollByIdWithModal(pollId) {
    let pollData = null;
    if (typeof recentPollsData !== 'undefined' && recentPollsData) {
        pollData = recentPollsData.find(poll => poll.id === pollId);
    }
    openActionModal('activate', pollId, pollData);
}

function openDeleteModal(pollId, pollData = null) {
    openActionModal('delete', pollId, pollData);
}

function openCloseModal(pollId, pollData = null) {
    openActionModal('close', pollId, pollData);
}

function closeDeleteModal() {
    closeActionModal();
}

function closeCloseModal() {
    closeActionModal();
}

function isDeleteModalOpen() {
    return isActionModalOpen();
}

function isCloseModalOpen() {
    return isActionModalOpen();
}

function updateDeleteModalContent(pollData) {
    const config = modalConfigs.delete;
    updateActionModalContent(config, pollData);
}

function updateCloseModalContent(pollData) {
    const config = modalConfigs.close;
    updateActionModalContent(config, pollData);
}

async function confirmDeletePoll() {
    currentModalData.type = 'delete';
    await confirmAction();
}

async function confirmClosePoll() {
    currentModalData.type = 'close';
    await confirmAction();
}

document.addEventListener('DOMContentLoaded', function() {
    if (!document.getElementById('action-modal')) {
        createModals();
    }
});
