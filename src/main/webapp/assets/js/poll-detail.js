let pollDetailWebSocket = null;
let pollDetailCurrentPoll = null;
let pollDetailChart = null;
let pollDetailIsVoting = false;
let pollDetailSelectedOptions = [];
let pollDetailVotingToken = null;
let pollDetailPollId = null;
let pollDetailReconnectAttempts = 0;
let pollDetailMaxReconnectAttempts = 5; 
let pollDetailReconnectDelay = 1000;

function closeWebSocketConnection() {
    if (pollDetailWebSocket) {
        if (pollDetailWebSocket.readyState === WebSocket.OPEN) {
            pollDetailWebSocket.close(1000, 'Verbindung wird geschlossen');
        }
        pollDetailWebSocket = null;
    }
}

function getPollIdFromUrl() {
    const path = window.location.pathname;
    const match = path.match(/\/dashboard\/poll\/([^\/]+)/);
    return match ? match[1] : null;
}

async function updatePollDetailUI(poll) {
    const statusClass = poll.status === 'ACTIVE' ? 'active' : 'closed';
    const statusText = poll.status === 'ACTIVE' ? 'Aktiv' : 'Geschlossen';
    const pollTypeText = poll.pollType === 'SINGLE_CHOICE' ? 'Eine Antwort' : 'Mehrere Antworten';
    
    document.getElementById('poll-question').textContent = poll.question;
    
    document.getElementById('poll-code').textContent = poll.id;
    
    const shortcodeElement = document.getElementById('poll-shortcode');
    const shortcodeLink = document.getElementById('shortcode-link');
    
    if (poll.shortCode) {
        shortcodeElement.textContent = poll.shortCode;
        shortcodeLink.href = `/s/${poll.shortCode}`;
        shortcodeLink.style.display = 'inline-block';
        shortcodeLink.title = `Zur Umfrage: ${poll.shortCode}`;
    } else {
        shortcodeElement.textContent = 'Nicht verfügbar';
        shortcodeLink.style.display = 'none';
    }
    
    document.getElementById('info-type').textContent = pollTypeText;
    document.getElementById('info-created').textContent = formatRelativeTime(poll.createdAt);
    document.getElementById('info-status').textContent = statusText;
    
    const liveIndicator = document.getElementById('live-indicator');
    if (poll.status === 'ACTIVE') {
        liveIndicator.style.display = 'flex';
    } else {
        liveIndicator.style.display = 'none';
    }
    
    const activateButton = document.getElementById('activate-poll-btn');
    const closeButton = document.getElementById('close-poll-btn');
    
    if (poll.status === 'ACTIVE') {
        if (activateButton) activateButton.style.display = 'none';
        if (closeButton) {
            closeButton.style.display = 'flex';
            closeButton.onclick = () => closePollById(poll.id);
        }
        if (!pollDetailWebSocket || pollDetailWebSocket.readyState === WebSocket.CLOSED) {
            await setupWebSocket();
        }
    } else {
        if (activateButton) {
            activateButton.style.display = 'flex';
            activateButton.onclick = () => activatePollById(poll.id);
        }
        if (closeButton) closeButton.style.display = 'none';
        closeWebSocketConnection();
    }
    
    document.getElementById('info-status').textContent = statusText;
    document.getElementById('info-status').className = `card-value ${statusClass}`;
    document.getElementById('info-type').textContent = pollTypeText;
    document.getElementById('info-options').textContent = poll.options.length;
    document.getElementById('info-created').textContent = formatRelativeTime(poll.createdAt);
    
    updateResultsChart(poll.options, 'doughnut');
    updateResultsList(poll.options);
}

async function activatePollById(pollId) {
    if (!pollId) {
        pollId = pollDetailCurrentPoll ? pollDetailCurrentPoll.id : null;
    }
    
    if (!pollId) return;
    
    openActionModal('activate', pollId, pollDetailCurrentPoll);
}

async function closePollById(pollId) {
    if (!pollId) {
        pollId = pollDetailCurrentPoll ? pollDetailCurrentPoll.id : null;
    }
    
    if (!pollId) return;
    
    openActionModal('close', pollId, pollDetailCurrentPoll);
}

async function deletePollByIdWithModal(pollId) {
    if (!pollId) {
        pollId = pollDetailCurrentPoll ? pollDetailCurrentPoll.id : null;
    }
    
    if (!pollId) return;
    
    openActionModal('delete', pollId, pollDetailCurrentPoll);
}

function openPresentation() {
    const pollId = getPollIdFromUrl();
    if (pollId) {
        window.open(`/presentation/${pollId}`, '_blank');
    }
}

async function setupWebSocket() {
    const pollId = getPollIdFromUrl();
    if (!pollId) {
        return;
    }
    
    if (pollDetailWebSocket && pollDetailWebSocket.readyState !== WebSocket.CLOSED) {
        pollDetailWebSocket.close();
    }
    
    if (!pollDetailCurrentPoll || pollDetailCurrentPoll.status !== 'ACTIVE') {
        return;
    }
    
    try {
        const pollExists = await getPoll(pollId);
        if (!pollExists) {
            console.error('Poll existiert nicht mehr in der Datenbank');
            closeWebSocketConnection();
            return;
        }
    } catch (error) {
        console.error('Fehler beim Validieren des Polls:', error);
        closeWebSocketConnection();
        return;
    }
    
    
    
    const protocol = window.location.protocol === 'https:' ? 'wss:' : 'ws:';
    const wsUrl = `${protocol}//${window.location.host}/ws/polls/${pollId}`;
    
    try {
        pollDetailWebSocket = new WebSocket(wsUrl);
        
        pollDetailWebSocket.onopen = function(event) {
            pollDetailReconnectAttempts = 0;
            
            pollDetailWebSocket.send(JSON.stringify({
                type: 'SUBSCRIBE',
                pollId: pollId
            }));
        };
        
        pollDetailWebSocket.onmessage = function(event) {
            try {
                const data = JSON.parse(event.data);
                handleWebSocketMessage(data);
            } catch (error) {
                console.error('Fehler beim Parsen der WebSocket-Nachricht:', error);
            }
        };
        
        pollDetailWebSocket.onclose = function(event) {
            
            if (event.code !== 1000 && pollDetailReconnectAttempts < pollDetailMaxReconnectAttempts) {
                pollDetailReconnectAttempts++;
                
                setTimeout(() => {
                    if (pollDetailCurrentPoll && pollDetailCurrentPoll.status === 'ACTIVE') {
                        setupWebSocket();
                    }
                }, pollDetailReconnectDelay * pollDetailReconnectAttempts);
            }
        };
        
        pollDetailWebSocket.onerror = function(error) {
            console.error('WebSocket Fehler in Poll-Detail:', error);
        };
        
        window.addEventListener('beforeunload', function() {
            if (pollDetailWebSocket && pollDetailWebSocket.readyState === WebSocket.OPEN) {
                pollDetailWebSocket.close(1000, 'Seite wird verlassen');
            }
        });
        
    } catch (error) {
        console.error('Fehler beim Erstellen der WebSocket-Verbindung:', error);
    }
}

function handleWebSocketMessage(data) {

    
    if (data.pollId && data.results) {
        if (pollDetailCurrentPoll && pollDetailCurrentPoll.options) {
            pollDetailCurrentPoll.options.forEach(option => {
                if (data.results.hasOwnProperty(option.text)) {
                    option.votes = data.results[option.text];
                }
            });
            
            pollDetailCurrentPoll.totalVotes = pollDetailCurrentPoll.options.reduce((sum, option) => sum + option.votes, 0);
            

            
            updateResultsChart(pollDetailCurrentPoll.options, 'doughnut');
            updateResultsList(pollDetailCurrentPoll.options);
            
            if (data.status && data.status !== pollDetailCurrentPoll.status) {
                pollDetailCurrentPoll.status = data.status;
                updatePollDetailUI(pollDetailCurrentPoll);
            }
        }
    }
}

async function loadPollDetail() {
    const pollId = getPollIdFromUrl();
    
    if (!pollId) {
        showNoPoll();
        return;
    }
    
    try {
        const poll = await getPoll(pollId);
        
        if (!poll) {
            showNoPoll();
            return;
        }
        
        pollDetailCurrentPoll = poll;
        await updatePollDetailUI(poll);
        
        if (poll.status === 'ACTIVE') {
            setupWebSocket();
        } else {
            closeWebSocketConnection();
        }
        
    } catch (error) {
        console.error('Fehler beim Laden der Umfrage:', error);
        showNoPoll();
    }
}

function showNoPoll() {
    const pollHeader = document.querySelector('.poll-header-simple');
    const contentGrid = document.querySelector('.content-grid');
    const noPoll = document.getElementById('no-poll');
    
    if (pollHeader) pollHeader.style.display = 'none';
    if (contentGrid) contentGrid.style.display = 'none';
    if (noPoll) noPoll.style.display = 'block';
}

document.addEventListener('DOMContentLoaded', function() {
    if (typeof getPoll === 'undefined') {
        showNoPoll();
        return;
    }
    
    loadPollDetail();
});

window.addEventListener('beforeunload', function() {
    closeWebSocketConnection();
});

function downloadPollResults() {
    window.location.href  = window.location.protocol + "//" + window.location.host + "/api/polls/" + pollDetailCurrentPoll.id + "/export.csv";
}

function downloadPollTemplate(){
    window.location.href  = window.location.protocol + "//" + window.location.host + "/api/polls/" + pollDetailCurrentPoll.id + "/export.xml";
}


