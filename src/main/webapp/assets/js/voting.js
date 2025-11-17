const urlPrams = new URLSearchParams(window.location.search);
const pollId = urlPrams.get('code');
let hasVoted = false;
let votingPoll = null;
let longPollingActive = false;

if (pollId) {
    loadPollForVoting(pollId);
} else {
    showToast('Keine Umfrage gefunden', 'error');
}

window.addEventListener('beforeunload', () => {
    longPollingActive = false;
});

async function loadPollForVoting(pollId) {
    showLoading()

    try {
        votingPoll = await getPoll(pollId);

        renderVotingInterface();
        updateShortcodeFromPoll();
    } catch (errror) {
        console.error("Fehler beim Laden der Umfrage: ", errror);
        showToast("Umfrage konnte nicht geladen werden", errror);
        showNoPoll();
    } finally {
        hideLoading();
    }
}

function updateShortcodeFromPoll() {
    if (votingPoll && votingPoll.shortCode) {
        const shortcodeElement = document.getElementById('shortcode-text');
        if (shortcodeElement) {
            shortcodeElement.textContent = votingPoll.shortCode;
        } else {
            console.error('Shortcode element not found');
        }
    } else {
        const shortcodeElement = document.getElementById('shortcode-text');
        if (shortcodeElement) {
            shortcodeElement.textContent = pollId || 'N/A';
        }
    }
}

function renderVotingInterface() {
    const container = document.getElementById('poll-content');
    const mainContainer = document.querySelector('.voting-main');
    const optionsContainer = document.querySelector('.voting-options');

    if (!votingPoll) {
        showNoPoll();
        return;
    }

    if (votingPoll.status !== "ACTIVE") {
        let statusMessage = '';
        let showWaiting = false;
        
        if (votingPoll.status === "DRAFT") {
            statusMessage = 'Diese Umfrage ist noch nicht aktiv. Bitte warten Sie, bis sie gestartet wird...';
            showWaiting = true;
        } else if (votingPoll.status === "CLOSED") {
            statusMessage = 'Diese Umfrage wurde geschlossen und ist nicht mehr verfügbar.';
            showWaiting = false;
        } else {
            statusMessage = 'Diese Umfrage ist nicht aktiv.';
            showWaiting = false;
        }
        
        container.innerHTML = `
        <div class="voting-closed ${votingPoll.status === "CLOSED" ? 'poll-closed' : ''}">
            <h2>${votingPoll.question}</h2>
            <p>${statusMessage}</p>
            <div class="voting-meta">
                <p>Erstellt ${formatRelativeTime(votingPoll.createdAt)}</p>
            </div>
            ${showWaiting ? `
            <div class="voting-waiting">
                <div class="loading-spinner"></div>
                <p>Warte auf Aktivierung der Umfrage...</p>
            </div>
            ` : ''}
        </div>`;
        
        if (showWaiting) {
            startLongPollingForActivation(pollId);
        }
        return;
    }

    startLongPollingForStatusChanges(pollId);

    const inputType = votingPoll.pollType === 'SINGLE_CHOICE' ? 'radio' : 'checkbox';
    const pollTypeText = votingPoll.pollType === 'MULTIPLE_CHOICE' ? 'Mehrfachauswahl' : '';
    
    const useTwoColumns = votingPoll.options.length > 6;
    const gridClass = useTwoColumns ? 'two-columns' : 'single-column';
    
    if (mainContainer) {
        if (useTwoColumns) {
            mainContainer.classList.add('has-two-columns');
        } else {
            mainContainer.classList.remove('has-two-columns');
        }
    }

    container.innerHTML = `
        <div class="voting-question">${votingPoll.question}</div>
        ${pollTypeText ? `<div class="voting-type">${pollTypeText}</div>` : ''}
        
        <div class="voting-options ${useTwoColumns ? 'has-two-columns' : ''}">
            <form id="voting-form">
                <div class="voting-options-grid ${gridClass}">
                    ${votingPoll.options.map((option) => `
                        <label class="voting-option" for="option-${option.id}">
                            <input type="${inputType}"
                                   id="option-${option.id}"
                                   name="selectedOptions"
                                   value="${option.id}"
                                   ${!votingPoll.allowMultipleVotes && hasVoted ? 'disabled' : ''}>
                            <div class="voting-option-content">
                                ${inputType === 'radio' ? 
                                    '<div class="voting-option-radio"></div>' : 
                                    '<div class="voting-option-checkbox"></div>'
                                }
                                <div class="voting-option-text">${option.text}</div>
                            </div>
                        </label>
                    `).join('')}
                </div>
                
                <div class="voting-submit">
                    <button type="button" class="voting-submit-btn" onClick="submitVote()" ${!votingPoll.allowMultipleVotes && hasVoted ? 'disabled' : ''}>
                        Abstimmen
                    </button>
                </div>
            </form>
        </div>
    `;

    addOptionEventListeners();
}

function addOptionEventListeners() {
    const options = document.querySelectorAll('.voting-option');
    
    options.forEach(option => {
        option.addEventListener('click', function() {
            const input = this.querySelector('input');
            const isRadio = input.type === 'radio';
            
            if (isRadio) {
                options.forEach(opt => opt.classList.remove('selected'));
                this.classList.add('selected');
            } else {
                this.classList.toggle('selected');
            }
        });
    });
}

async function submitVote() {
    const form = document.getElementById('voting-form');
    const selectedOptions = form.querySelectorAll('input[name="selectedOptions"]:checked');

    if (selectedOptions.length === 0) {
        showToast('Bitte wähle eine Option aus', "warning");
        return;
    }

    if (votingPoll.pollType === 'SINGLE_CHOICE' && selectedOptions.length > 1) {
        showToast("Bei dieser Abstimmung ist nur eine Option erlaubt", "warning");
        return;
    }

    showLoading()

    try {
        const userId = (await getUserIdAndsignature()).userId;
        
        // Alle Optionen in einem Request senden
        const optionIds = Array.from(selectedOptions).map(option => option.value);
        await voteMultiple(votingPoll.id, optionIds, userId);
        
        hasVoted = true;
        showToast("Deine Stimme wurde erfolgreich abgegeben", "info");
        
        longPollingActive = false;
        
        const completionUrl = `../voting-complete.jsp?pollId=${encodeURIComponent(votingPoll.shortCode || pollId)}&allowMultipleVotes=${votingPoll.allowMultipleVotes}`;
        window.location.href = completionUrl;
    } catch (err) {
        
        if (err.message && err.message.includes('bereits gestimmt')) {
            showToast("Sie haben bereits für diese Umfrage gestimmt", "warning");
        } else if (err.message && err.message.includes('nicht aktiv')) {
            showToast("Diese Umfrage ist nicht mehr aktiv", "error");
        } else {
            showToast("Fehler bei der Stimmabgabe: " + err.message, "error");
        }
    } finally {
        hideLoading()
    }
}

function showNoPoll() {
    document.getElementById('poll-content').style.display = 'none';
    document.getElementById('no-poll').style.display = 'block';
}

async function startLongPollingForActivation(pollId) {
    if (longPollingActive) {
        return;
    }
    
    longPollingActive = true;
    
    try {
        const response = await fetch(`/api/polls/${pollId}/wait`, {
            method: 'GET',
            headers: {
                'Content-Type': 'application/json',
            }
        });
        
        if (response.ok) {
            const updatedPoll = await response.json();
            
            votingPoll = updatedPoll;
            
            if (updatedPoll.status === "ACTIVE") {
                showToast('Umfrage ist jetzt aktiv!', 'success');
            } else if (updatedPoll.status === "CLOSED") {
                showToast('Die Umfrage wurde geschlossen und ist nicht mehr verfügbar.', 'warning');
            } else if (updatedPoll.status === "DRAFT") {
                showToast('Die Umfrage wurde zurückgesetzt.', 'info');
            }
            
            renderVotingInterface();
            
        } else {
            console.error('Fehler beim Long Polling:', response.status);
            setTimeout(() => startLongPollingForActivation(pollId), 5000);
        }
        
    } catch (error) {
        console.error('Fehler beim Long Polling:', error);
        setTimeout(() => startLongPollingForActivation(pollId), 5000);
    } finally {
        longPollingActive = false;
    }
}

async function startLongPollingForStatusChanges(pollId) {
    if (longPollingActive) {
        return;
    }
    
    longPollingActive = true;
    
    try {
        const response = await fetch(`/api/polls/${pollId}/wait`, {
            method: 'GET',
            headers: {
                'Content-Type': 'application/json',
            }
        });
        
        if (response.ok) {
            const updatedPoll = await response.json();
            
            votingPoll = updatedPoll;
            
            if (updatedPoll.status === "CLOSED") {
                showToast('Die Umfrage wurde geschlossen und ist nicht mehr verfügbar.', 'warning');
            } else if (updatedPoll.status === "DRAFT") {
                showToast('Die Umfrage wurde zurückgesetzt.', 'info');
            }
            
            renderVotingInterface();
            
            if (updatedPoll.status === "ACTIVE") {
                longPollingActive = false;
                setTimeout(() => startLongPollingForStatusChanges(pollId), 1000);
            }
            
        } else {
            console.error('Fehler beim Long Polling für Status-Änderungen:', response.status);
            longPollingActive = false;
            setTimeout(() => startLongPollingForStatusChanges(pollId), 5000);
        }
        
    } catch (error) {
        console.error('Fehler beim Long Polling für Status-Änderungen:', error);
        longPollingActive = false;
        setTimeout(() => startLongPollingForStatusChanges(pollId), 5000);
    }
}



