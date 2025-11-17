let currentPoll = null;
let selectedOptions = new Set();
let resultsChart = null;
let autoRefreshInterval = null;

function getPollIdFromUrl() {
    const path = window.location.pathname;
    
    const pollIdMatch = path.match(/\/(?:dashboard\/)?poll\/([^\/]+)/);
    if (pollIdMatch) {
        return pollIdMatch[1];
    }
    
    const shortCodeMatch = path.match(/^\/s\/([^\/]+)$/);
    if (shortCodeMatch) {
        return shortCodeMatch[1];
    }
    
    return null;
}

function createPollUI(poll) {
    const container = document.getElementById('poll-content');
    
    const statusClass = poll.status === 'ACTIVE' ? 'active' : 'closed';
    const statusText = poll.status === 'ACTIVE' ? 'Aktiv' : 'Geschlossen';
    const pollTypeText = poll.pollType === 'SINGLE_CHOICE' ? 'Eine Antwort' : 'Mehrere Antworten';
    
    let optionsHTML = '';
    poll.options.forEach(option => {
        const optionClass = selectedOptions.has(option.id.toString()) ? 'selected' : '';
        optionsHTML += '<div class="option-card ' + optionClass + '" onclick="toggleOption(' + option.id + ')">' +
            '<div class="option-text">' + option.text + '</div>' +
            '<div class="option-votes">' + formatNumber(option.votes) + ' Stimmen</div>' +
            '</div>';
    });
    
    const voteButtonDisabled = poll.status !== 'ACTIVE' || selectedOptions.size === 0;
    const voteButtonText = poll.status === 'ACTIVE' ? 'Stimme abgeben' : 'Umfrage beendet';
    const statusIcon = poll.status === 'ACTIVE' ? 'play' : 'stop';
    const pollTypeText2 = poll.pollType === 'SINGLE_CHOICE' ? 'eine' : 'eine oder mehrere';
    const liveIndicator = poll.status === 'ACTIVE' ? '<div class="live-indicator"><div class="live-dot"></div>Live</div>' : '';
    
    container.innerHTML = 
        '<div class="poll-header">' +
            '<div class="status-badge ' + statusClass + '">' +
                '<i class="fas fa-' + statusIcon + '"></i>' +
                statusText +
            '</div>' +
            '<h1 class="poll-question">' + poll.question + '</h1>' +
            '<div class="poll-meta">' +
                '<div class="meta-item">' +
                    '<i class="fas fa-calendar"></i>' +
                    '<span>Erstellt ' + formatRelativeTime(poll.createdAt) + '</span>' +
                '</div>' +
                '<div class="meta-item">' +
                    '<i class="fas fa-users"></i>' +
                    '<span>' + formatNumber(poll.totalVotes) + ' Stimmen</span>' +
                '</div>' +
                '<div class="meta-item">' +
                    '<i class="fas fa-list"></i>' +
                    '<span>' + pollTypeText + '</span>' +
                '</div>' +
            '</div>' +
        '</div>' +
        
        '<div class="poll-content">' +
            '<div class="voting-section">' +
                '<div class="voting-header">' +
                    '<h3>Ihre Stimme</h3>' +
                    '<p>Wählen Sie ' + pollTypeText2 + ' Antwort(en) aus:</p>' +
                '</div>' +
                
                '<div class="options-grid">' +
                    optionsHTML +
                '</div>' +
                
                '<button class="vote-button" onclick="submitVoteHandler()"' + (voteButtonDisabled ? ' disabled' : '') + '>' +
                    '<i class="fas fa-check"></i>' +
                    voteButtonText +
                '</button>' +
            '</div>' +
            
            '<div class="results-section">' +
                '<div class="results-header">' +
                    '<h3>Live Ergebnisse</h3>' +
                    liveIndicator +
                '</div>' +
                
                '<div class="chart-container">' +
                    '<canvas id="results-chart"></canvas>' +
                '</div>' +
                
                '<div id="results-list" class="results-list">' +
                '</div>' +
            '</div>' +
        '</div>' +
        
        '<div class="poll-actions">' +
            '<a href="/dashboard" class="action-button">' +
                '<i class="fas fa-arrow-left"></i>' +
                'Dashboard' +
            '</a>' +
            '<button class="action-button primary" onclick="sharePoll()">' +
                '<i class="fas fa-share"></i>' +
                'Teilen' +
            '</button>' +
            '<button class="action-button secondary" onclick="deletePollByIdWithModal(currentPoll.id)" title="Umfrage löschen">' +
                '<i class="fas fa-trash"></i>' +
                'Löschen' +
            '</button>' +
        '</div>';
    
    updateResultsChart(poll.options, 'doughnut');
    updateResultsList(poll.options);
}

function toggleOption(optionId) {
    if (currentPoll.status !== 'ACTIVE') return;
    
    const optionIdStr = optionId.toString();
    
    if (currentPoll.pollType === 'SINGLE_CHOICE') {
        selectedOptions.clear();
        document.querySelectorAll('.option-card').forEach(item => {
            item.classList.remove('selected');
        });
    }
    
    if (selectedOptions.has(optionIdStr)) {
        selectedOptions.delete(optionIdStr);
        document.querySelector('[onclick="toggleOption(' + optionId + ')"]').classList.remove('selected');
    } else {
        selectedOptions.add(optionIdStr);
        document.querySelector('[onclick="toggleOption(' + optionId + ')"]').classList.add('selected');
    }
    
    const voteButton = document.querySelector('.vote-button');
    if (voteButton) {
        voteButton.disabled = selectedOptions.size === 0;
    }
}

async function submitVoteHandler() {
    if (!currentPoll || selectedOptions.size === 0) return;
    
    try {
        const votePromises = Array.from(selectedOptions).map(optionId => 
            vote(currentPoll.id, parseInt(optionId))
        );
        
        await Promise.all(votePromises);
        showToast('Ihre Stimme wurde erfolgreich abgegeben!', 'success');
        
        selectedOptions.clear();
        document.querySelectorAll('.option-card').forEach(item => {
            item.classList.remove('selected');
        });
        
        const voteButton = document.querySelector('.vote-button');
        if (voteButton) {
            voteButton.disabled = true;
        }
        
        await loadPollResults();
        
        
    } catch (error) {
        showToast('Fehler beim Abgeben der Stimme', 'error');
    }
}

async function loadPollResults() {
    if (!currentPoll) return;
    
    try {
        const results = await getPollResults(currentPoll.id);
        
        currentPoll.options.forEach(option => {
            option.votes = results[option.text] || 0;
        });
        
        currentPoll.totalVotes = currentPoll.options.reduce((sum, option) => sum + option.votes, 0);
        
        updateResultsChart(currentPoll.options);
        updateResultsList(currentPoll.options);
        
        const totalVotesElement = document.querySelector('.meta-item span');
        if (totalVotesElement) {
            totalVotesElement.textContent = formatNumber(currentPoll.totalVotes) + ' Stimmen';
        }
        
    } catch (error) {
        console.error('Fehler beim Laden der Ergebnisse:', error);
    }
}

async function loadPoll() {
    const pollIdOrShortCode = getPollIdFromUrl();
    
    if (!pollIdOrShortCode) {
        showNoPoll();
        return;
    }
    
    try {
        let poll = await getPoll(pollIdOrShortCode);
        
        if (!poll) {
            poll = await getPollByShortCode(pollIdOrShortCode);
        }
        
        if (poll) {
            currentPoll = poll;
            createPollUI(poll);
            
            if (poll.status === 'ACTIVE') {
                // Verwende Long Polling statt Auto-Refresh
            }
        } else {
            showNoPoll();
        }
        
    } catch (error) {
        console.error('Fehler beim Laden der Umfrage:', error);
        showNoPoll();
    }
}

function showNoPoll() {
    document.getElementById('poll-content').style.display = 'none';
    document.getElementById('no-poll').style.display = 'block';
}

function deletePollByIdWithModal(pollId) {
    let pollData = currentPoll;
    if (typeof openActionModal === 'function') {
        openActionModal('delete', pollId, pollData);
    } else {
        // Fallback falls das Modal nicht verfügbar ist
        if (confirm('Sind Sie sicher, dass Sie diese Umfrage löschen möchten?')) {
            deletePollDirectly(pollId);
        }
    }
}

async function deletePollDirectly(pollId) {
    try {
        await deletePoll(pollId);
        showToast('Umfrage erfolgreich gelöscht', 'success');
        // Weiterleitung zum Dashboard nach dem Löschen
        window.location.href = '/dashboard';
    } catch (error) {
        if (error.message.includes('Zugriff verweigert')) {
            showToast('Zugriff verweigert: Sie können nur Ihre eigenen Umfragen löschen', 'error');
        } else {
            showToast('Fehler beim Löschen der Umfrage: ' + error.message, 'error');
        }
    }
}



document.addEventListener('DOMContentLoaded', function() {
    if (typeof getPoll === 'undefined') {
        showNoPoll();
        return;
    }
    
    loadPoll();
}); 