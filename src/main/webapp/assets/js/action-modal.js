function duplicatePollByIdWithModal(pollId) {
    let pollData = null;
    if (typeof recentPollsData !== 'undefined' && recentPollsData) {
        pollData = recentPollsData.find(poll => poll.id === pollId);
    }
    
    const customConfirm = async (pollId) => {
    };
    
    openActionModal('duplicate', pollId, pollData, customConfirm);
}

function addCustomModalConfig() {
    modalConfigs.duplicate = {
        title: 'Umfrage duplizieren',
        message: 'Möchten Sie diese Umfrage duplizieren? Eine Kopie wird mit allen Einstellungen erstellt.',
        icon: 'fa-copy',
        iconClass: 'info',
        buttonClass: 'btn-info',
        buttonText: 'Duplizieren',
        buttonIcon: 'fa-copy',
        loadingText: 'Duplizieren...',
        successMessage: 'Umfrage erfolgreich dupliziert',
        errorMessage: 'Fehler beim Duplizieren der Umfrage'
    };
    
    modalConfigs.archive = {
        title: 'Umfrage archivieren',
        message: 'Möchten Sie diese Umfrage archivieren? Archivierte Umfragen werden nicht mehr angezeigt.',
        icon: 'fa-archive',
        iconClass: 'secondary',
        buttonClass: 'btn-secondary',
        buttonText: 'Archivieren',
        buttonIcon: 'fa-archive',
        loadingText: 'Archivieren...',
        successMessage: 'Umfrage erfolgreich archiviert',
        errorMessage: 'Fehler beim Archivieren der Umfrage'
    };
}

if (typeof module !== 'undefined' && module.exports) {
    module.exports = {
        openActionModal,
        closeActionModal,
        addCustomModalConfig,
        modalConfigs
    };
}
