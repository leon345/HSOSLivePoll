let pollId = null;
let currentPoll = null;
let webSocket = null;
let isFullscreen = false;
let isPageVisible = true;
let longPollingInterval = null;
let reconnectAttempts = 0;
let maxReconnectAttempts = 5; // Sollte mit Backend-Konfiguration synchronisiert werden
let reconnectDelay = 1000; // Sollte mit Backend-Konfiguration synchronisiert werden

document.addEventListener('DOMContentLoaded', function() {
    const path = window.location.pathname;
    const match = path.match(/\/presentation\/([^\/]+)/);
    pollId = match ? match[1] : null;
    
    if (pollId) {
        loadPollData();
        setupWebSocket();
        startLongPolling();
        
        setInterval(() => {
            if (isPageVisible && pollId) {
                loadPollResults();
            }
        }, 30000);
    } else {
        showError('Keine Umfrage-ID gefunden');
    }
});

function startLongPolling() {
    if (longPollingInterval) {
        clearInterval(longPollingInterval);
    }
    
    performLongPolling();
    
    longPollingInterval = setInterval(() => {
        if (isPageVisible && pollId) {
            performLongPolling();
        }
    }, 35000);
}

async function performLongPolling() {
    try {
        const response = await fetch(`/api/polls/${pollId}/wait`, {
            method: 'GET',
            headers: {
                'Cache-Control': 'no-cache',
                'Connection': 'keep-alive'
            }
        });
        
        if (!response.ok) {
            throw new Error(`HTTP ${response.status}: ${response.statusText}`);
        }
        
        const poll = await response.json();
        currentPoll = poll;
        
        handlePollStatusChange(poll);
        
        await loadPollResults();
        
    } catch (error) {
    }
}

function handlePollStatusChange(poll) {
    if (!poll) return;
    
    updateLiveIndicator(poll.status === 'ACTIVE');
    
    const questionElement = document.getElementById('poll-question');
    if (questionElement && poll.question) {
        questionElement.textContent = poll.question;
    }
    
    if (poll.status === 'ACTIVE') {
    } else {
        updateStatusDisplay(poll.status);
    }
}

function updateStatusDisplay(status) {
    const chartContainer = document.getElementById('custom-chart');
    
    switch (status) {
        case 'DRAFT':
            chartContainer.innerHTML = `
                <div class="status-display status-draft">
                    <div class="status-icon">
                        <i class="fas fa-edit"></i>
                    </div>
                    <div class="status-text">Umfrage noch nicht gestartet</div>
                    <div class="status-subtext">Die Umfrage befindet sich noch im Entwurfsmodus</div>
                </div>
            `;
            break;
            
        case 'ACTIVE':
            break;
            
        case 'CLOSED':
            chartContainer.innerHTML = `
                <div class="status-display status-closed">
                    <div class="status-icon">
                        <i class="fas fa-lock"></i>
                    </div>
                    <div class="status-text">Umfrage beendet</div>
                    <div class="status-subtext">Die Abstimmung ist abgeschlossen</div>
                </div>
            `;
            break;
            
        case 'EXPIRED':
            chartContainer.innerHTML = `
                <div class="status-display status-expired">
                    <div class="status-icon">
                        <i class="fas fa-clock"></i>
                    </div>
                    <div class="status-text">Umfrage abgelaufen</div>
                    <div class="status-subtext">Der Zeitraum für die Abstimmung ist vorbei</div>
                </div>
            `;
            break;
            
        default:
            chartContainer.innerHTML = `
                <div class="status-display">
                    <div class="status-icon">
                        <i class="fas fa-question-circle"></i>
                    </div>
                    <div class="status-text">Unbekannter Status</div>
                    <div class="status-subtext">Status: ${status}</div>
                </div>
            `;
    }
}

async function loadPollData() {
    try {
        const response = await fetch(`/api/polls/${pollId}`);
        
        if (!response.ok) {
            throw new Error(`HTTP ${response.status}: ${response.statusText}`);
        }
        
        const poll = await response.json();
        currentPoll = poll;
        displayPollData(poll);
        
        if (poll.status !== 'ACTIVE') {
            updateStatusDisplay(poll.status);
        }
        
        await loadPollResults();
        
    } catch (error) {
        showError('Umfrage konnte nicht geladen werden');
    }
}

async function loadPollResults() {
    try {
        const response = await fetch(`/api/polls/${pollId}/results`);
        
        if (!response.ok) {
            throw new Error(`HTTP ${response.status}: ${response.statusText}`);
        }
        
        const results = await response.json();
        
        if (currentPoll && currentPoll.options) {
            currentPoll.options.forEach(option => {
                if (results[option.text] !== undefined) {
                    option.voteCount = results[option.text];
                }
            });
            
        if (currentPoll.status === 'ACTIVE') {
            const chartContainer = document.getElementById('custom-chart');
            if (chartContainer.children.length === 0 || 
                chartContainer.querySelector('.status-display') || 
                chartContainer.querySelector('.waiting-for-participants')) {
                createCustomChart(currentPoll);
            } else {
                updateCustomChart(currentPoll);
            }
        } else {
            updateStatusDisplay(currentPoll.status);
        }
        }
        
    } catch (error) {
        
        if (currentPoll && currentPoll.status === 'ACTIVE') {
            const chartContainer = document.getElementById('custom-chart');
            if (chartContainer.children.length === 0 || 
                chartContainer.querySelector('.status-display') || 
                chartContainer.querySelector('.waiting-for-participants')) {
                createCustomChart(currentPoll);
            }
        } else if (currentPoll) {
            updateStatusDisplay(currentPoll.status);
        }
    }
}

function displayPollData(poll) {
    document.getElementById('poll-question').textContent = poll.question;
    
    document.getElementById('poll-code').textContent = `Code: ${poll.shortCode}`;
    
    updateLiveIndicator(poll.status === 'ACTIVE');
    
    generateQRCode(poll.shortCode);
}

function generateQRCode(shortCode) {
    const qrContainer = document.getElementById('qr-code');
    const votingUrl = `${window.location.origin}/s/${shortCode}`;
    
    qrContainer.innerHTML = '';
    
    try {
        const qrCodeApiUrl = `/api/qrcode?content=${encodeURIComponent(votingUrl)}&size=300&margin=2`;
        
        fetch(qrCodeApiUrl)
            .then(response => {
                if (!response.ok) {
                    throw new Error(`HTTP ${response.status}: ${response.statusText}`);
                }
                return response.text();
            })
            .then(svgContent => {
                qrContainer.innerHTML = svgContent;
            })
            .catch(error => {
                showQRFallback(qrContainer, votingUrl);
            });
    } catch (error) {
        showQRFallback(qrContainer, votingUrl);
    }
}

function showQRFallback(qrContainer, votingUrl) {
    qrContainer.innerHTML = `
        <div class="qr-fallback">
            <div class="qr-placeholder">
                <i class="fas fa-link"></i>
            </div>
            <div class="voting-url-display">
                <p><strong>Voting-URL:</strong></p>
                <div class="url-box">
                    <a href="${votingUrl}" target="_blank" class="voting-link">${votingUrl}</a>
                    <button class="copy-btn" onclick="copyToClipboard('${votingUrl}')">
                        <i class="fas fa-copy"></i>
                    </button>
                </div>
            </div>
        </div>
    `;
}

function copyToClipboard(text) {
    if (navigator.clipboard) {
        navigator.clipboard.writeText(text).then(() => {
            const copyBtn = event.target.closest('.copy-btn');
            const originalText = copyBtn.innerHTML;
            copyBtn.innerHTML = '<i class="fas fa-check"></i>';
            copyBtn.style.background = '#4CAF50';
            
            setTimeout(() => {
                copyBtn.innerHTML = originalText;
                copyBtn.style.background = '';
            }, 2000);
        });
    } else {
        const textArea = document.createElement('textarea');
        textArea.value = text;
        document.body.appendChild(textArea);
        textArea.select();
        document.execCommand('copy');
        document.body.removeChild(textArea);
        
        const copyBtn = event.target.closest('.copy-btn');
        const originalText = copyBtn.innerHTML;
        copyBtn.innerHTML = '<i class="fas fa-check"></i>';
        copyBtn.style.background = '#4CAF50';
        
        setTimeout(() => {
            copyBtn.innerHTML = originalText;
            copyBtn.style.background = '';
        }, 2000);
    }
}

function createCustomChart(poll) {
    const chartContainer = document.getElementById('custom-chart');
    
    if (poll.status !== 'ACTIVE') {
        updateStatusDisplay(poll.status);
        return;
    }
    
    const existingStatusDisplay = chartContainer.querySelector('.status-display');
    if (existingStatusDisplay) {
        existingStatusDisplay.remove();
    }
    
    const sortedOptions = [...poll.options].sort((a, b) => (b.voteCount || 0) - (a.voteCount || 0));
    const totalVotes = sortedOptions.reduce((sum, option) => sum + (option.voteCount || 0), 0);
    
    if (totalVotes === 0) {
        chartContainer.innerHTML = `
            <div class="waiting-for-participants">
                <div class="waiting-icon">
                    <i class="fas fa-users"></i>
                </div>
                <div class="waiting-text">Warten auf Teilnehmer</div>
                <div class="waiting-subtext">Noch keine Stimmen abgegeben</div>
            </div>
        `;
        return;
    }
    
    let chartHTML = '';
    
    sortedOptions.forEach((option, index) => {
        const voteCount = option.voteCount || 0;
        const percentage = totalVotes > 0 ? Math.round((voteCount / totalVotes) * 100) : 0;
        
        if (voteCount === 0) {
            chartHTML += `
                <div class="chart-row no-votes-row" data-index="${index}">
                    <div class="chart-label">${option.text}</div>
                    <div class="no-votes-message">Keine Stimmen abgegeben</div>
                </div>
            `;
        } else {
            const barWidth = totalVotes > 0 ? Math.max((voteCount / totalVotes) * 100, 5) : 5;
            
            chartHTML += `
                <div class="chart-row" data-index="${index}">
                    <div class="chart-label">${option.text}</div>
                    <div class="chart-bar-container">
                        <div class="chart-bar" style="width: 0%;" data-width="${barWidth}">
                            <span class="bar-value">${voteCount}</span>
                            <span class="bar-percentage">${percentage}%</span>
                        </div>
                    </div>
                </div>
            `;
        }
    });
    
    chartContainer.innerHTML = chartHTML;
    
    setTimeout(() => {
        animateBars();
    }, 100);
}

function animateBars() {
    const bars = document.querySelectorAll('.chart-bar');
    
    bars.forEach((bar, index) => {
        const targetWidth = bar.getAttribute('data-width');
        
        setTimeout(() => {
            bar.style.width = targetWidth + '%';
            bar.classList.add('animated');
        }, index * 100);
    });
}

function updateCustomChart(poll) {
    if (!poll) return;
    
    if (poll.status !== 'ACTIVE') {
        updateStatusDisplay(poll.status);
        return;
    }
    
    const chartContainer = document.getElementById('custom-chart');
    const existingStatusDisplay = chartContainer.querySelector('.status-display');
    if (existingStatusDisplay) {
        existingStatusDisplay.remove();
    }
    
    const sortedOptions = [...poll.options].sort((a, b) => (b.voteCount || 0) - (a.voteCount || 0));
    const totalVotes = sortedOptions.reduce((sum, option) => sum + (option.voteCount || 0), 0);
    
    if (totalVotes === 0) {
        chartContainer.innerHTML = `
            <div class="waiting-for-participants">
                <div class="waiting-icon">
                    <i class="fas fa-users"></i>
                </div>
                <div class="waiting-text">Warten auf Teilnehmer</div>
                <div class="waiting-subtext">Noch keine Stimmen abgegeben</div>
            </div>
        `;
        return;
    }
    
    const rows = chartContainer.querySelectorAll('.chart-row');
    
    const newOrder = sortedOptions.map(option => option.text);
    
    rows.forEach((row, index) => {
        const label = row.querySelector('.chart-label');
        const option = sortedOptions[index];
        
        if (option) {
            const voteCount = option.voteCount || 0;
            const percentage = totalVotes > 0 ? Math.round((voteCount / totalVotes) * 100) : 0;
            
            label.textContent = option.text;
            
            if (voteCount === 0) {
                row.className = 'chart-row no-votes-row';
                
                const existingBar = row.querySelector('.chart-bar-container');
                if (existingBar) {
                    existingBar.remove();
                }
                
                if (!row.querySelector('.no-votes-message')) {
                    const noVotesMessage = document.createElement('div');
                    noVotesMessage.className = 'no-votes-message';
                    noVotesMessage.textContent = 'Keine Stimmen abgegeben';
                    row.appendChild(noVotesMessage);
                }
            } else {
                row.className = 'chart-row';
                
                const existingMessage = row.querySelector('.no-votes-message');
                if (existingMessage) {
                    existingMessage.remove();
                }
                
                let barContainer = row.querySelector('.chart-bar-container');
                if (!barContainer) {
                    barContainer = document.createElement('div');
                    barContainer.className = 'chart-bar-container';
                    row.appendChild(barContainer);
                }
                
                let bar = barContainer.querySelector('.chart-bar');
                if (!bar) {
                    bar = document.createElement('div');
                    bar.className = 'chart-bar';
                    bar.style.width = '0%';
                    barContainer.appendChild(bar);
                }
                
                let barValue = bar.querySelector('.bar-value');
                if (!barValue) {
                    barValue = document.createElement('span');
                    barValue.className = 'bar-value';
                    bar.appendChild(barValue);
                }
                
                let barPercentage = bar.querySelector('.bar-percentage');
                if (!barPercentage) {
                    barPercentage = document.createElement('span');
                    barPercentage.className = 'bar-percentage';
                    bar.appendChild(barPercentage);
                }
                
                const barWidth = totalVotes > 0 ? Math.max((voteCount / totalVotes) * 100, 5) : 5;
                
                barValue.textContent = voteCount;
                barPercentage.textContent = percentage + '%';
                
                setTimeout(() => {
                    bar.style.width = barWidth + '%';
                    bar.setAttribute('data-width', barWidth);
                }, index * 50);
            }
        }
    });
    
    if (sortedOptions.length > rows.length) {
        for (let i = rows.length; i < sortedOptions.length; i++) {
            const option = sortedOptions[i];
            const voteCount = option.voteCount || 0;
            const percentage = totalVotes > 0 ? Math.round((voteCount / totalVotes) * 100) : 0;
            
            const newRow = document.createElement('div');
            
            if (voteCount === 0) {
                newRow.className = 'chart-row no-votes-row';
                newRow.innerHTML = `
                    <div class="chart-label">${option.text}</div>
                    <div class="no-votes-message">Keine Stimmen abgegeben</div>
                `;
            } else {
                const barWidth = totalVotes > 0 ? Math.max((voteCount / totalVotes) * 100, 5) : 5;
                
                newRow.className = 'chart-row';
                newRow.innerHTML = `
                    <div class="chart-label">${option.text}</div>
                    <div class="chart-bar-container">
                        <div class="chart-bar" style="width: 0%;" data-width="${barWidth}">
                            <span class="bar-value">${voteCount}</span>
                            <span class="bar-percentage">${percentage}%</span>
                        </div>
                    </div>
                `;
                
                setTimeout(() => {
                    const newBar = newRow.querySelector('.chart-bar');
                    newBar.style.width = barWidth + '%';
                    newBar.classList.add('animated');
                }, i * 50);
            }
            
            chartContainer.appendChild(newRow);
        }
    }
    
    while (rows.length > sortedOptions.length) {
        rows[rows.length - 1].remove();
    }
}

function updateLiveIndicator(isLive) {
    const liveIndicator = document.getElementById('live-indicator');
    if (isLive) {
        liveIndicator.style.display = 'flex';
    } else {
        liveIndicator.style.display = 'none';
    }
}

function setupWebSocket() {
    if (webSocket && webSocket.readyState !== WebSocket.CLOSED) {
        webSocket.close();
    }
    
    // WebSocket-URL basierend auf dem aktuellen Protokoll
    const protocol = window.location.protocol === 'https:' ? 'wss:' : 'ws:';
    const wsUrl = `${protocol}//${window.location.host}/ws/polls/${pollId}`;
    

    
    try {
        webSocket = new WebSocket(wsUrl);
        
        webSocket.onopen = function(event) {
            reconnectAttempts = 0;
            
            // Sende eine Nachricht zur Authentifizierung/Registrierung
            webSocket.send(JSON.stringify({
                type: 'SUBSCRIBE',
                pollId: pollId
            }));
        };
        
        webSocket.onmessage = function(event) {
            try {
                const data = JSON.parse(event.data);
                handleWebSocketMessage(data);
            } catch (error) {
                console.error('Fehler beim Parsen der WebSocket-Nachricht:', error);
            }
        };
        
        webSocket.onclose = function(event) {
            
            if (event.code !== 1000 && reconnectAttempts < maxReconnectAttempts) {
                reconnectAttempts++;
                
                setTimeout(() => {
                    if (pollId && currentPoll && currentPoll.status === 'ACTIVE') {
                        setupWebSocket();
                    }
                }, reconnectDelay * reconnectAttempts);
            }
        };
        
        webSocket.onerror = function(error) {
            console.error('WebSocket Fehler:', error);
        };
        
        window.addEventListener('beforeunload', function() {
            if (webSocket && webSocket.readyState === WebSocket.OPEN) {
                webSocket.close(1000, 'Seite wird verlassen');
            }
        });
        
    } catch (error) {
        console.error('Fehler beim Erstellen der WebSocket-Verbindung:', error);
    }
}

function closeWebSocketConnection() {
    if (webSocket) {
        if (webSocket.readyState === WebSocket.OPEN) {
            webSocket.close(1000, 'Verbindung wird geschlossen');
        }
        webSocket = null;
    }
}

function handleWebSocketMessage(data) {
    
    if (data.pollId && data.results) {

        const poll = {
            id: data.pollId,
            question: data.question,
            status: data.status,
            options: Object.entries(data.results).map(([text, voteCount]) => ({
                text: text,
                voteCount: voteCount
            }))
        };
        
        currentPoll = poll;
        
        handlePollStatusChange(poll);
        
        if (poll.status === 'ACTIVE') {
            const chartContainer = document.getElementById('custom-chart');
            if (chartContainer.querySelector('.status-display') || 
                chartContainer.querySelector('.waiting-for-participants')) {
                createCustomChart(poll);
            } else {
                updateCustomChart(poll);
            }
        } else {
            updateStatusDisplay(poll.status);
        }
    }
}

function toggleFullscreen() {
    const container = document.querySelector('.presentation-container');
    const fullscreenBtn = document.getElementById('fullscreen-btn');
    const icon = fullscreenBtn.querySelector('i');
    
    if (!isFullscreen) {
        if (container.requestFullscreen) {
            container.requestFullscreen();
        } else if (container.webkitRequestFullscreen) {
            container.webkitRequestFullscreen();
        } else if (container.msRequestFullscreen) {
            container.msRequestFullscreen();
        }
        
        container.classList.add('fullscreen');
        icon.className = 'fas fa-compress';
        fullscreenBtn.innerHTML = '<i class="fas fa-compress"></i> Vollbild beenden';
        isFullscreen = true;
    } else {
        if (document.exitFullscreen) {
            document.exitFullscreen();
        } else if (document.webkitExitFullscreen) {
            document.webkitExitFullscreen();
        } else if (document.msExitFullscreen) {
            document.msExitFullscreen();
        }
        
        container.classList.remove('fullscreen');
        icon.className = 'fas fa-expand';
        fullscreenBtn.innerHTML = '<i class="fas fa-expand"></i> Vollbild';
        isFullscreen = false;
    }
}

document.addEventListener('fullscreenchange', handleFullscreenChange);
document.addEventListener('webkitfullscreenchange', handleFullscreenChange);
document.addEventListener('msfullscreenchange', handleFullscreenChange);

function handleFullscreenChange() {
    const container = document.querySelector('.presentation-container');
    const fullscreenBtn = document.getElementById('fullscreen-btn');
    const icon = fullscreenBtn.querySelector('i');
    
    if (document.fullscreenElement || document.webkitFullscreenElement || document.msFullscreenElement) {
        container.classList.add('fullscreen');
        icon.className = 'fas fa-compress';
        fullscreenBtn.innerHTML = '<i class="fas fa-compress"></i> Vollbild beenden';
        isFullscreen = true;
    } else {
        container.classList.remove('fullscreen');
        icon.className = 'fas fa-expand';
        fullscreenBtn.innerHTML = '<i class="fas fa-expand"></i> Vollbild';
        isFullscreen = false;
        
        setTimeout(() => {
            const notification = document.createElement('div');
            notification.className = 'reload-notification';
            notification.innerHTML = '<i class="fas fa-sync-alt fa-spin"></i> Seite wird neu geladen...';
            document.body.appendChild(notification);
            
            setTimeout(() => {
                window.location.reload();
            }, 500);
        }, 100);
    }
}

function showError(message) {
    document.getElementById('no-poll').style.display = 'block';
    document.querySelector('.presentation-main').style.display = 'none';
    document.querySelector('.fullscreen-controls').style.display = 'none';
}

document.addEventListener('keydown', function(event) {
    if (event.key === 'F11') {
        event.preventDefault();
        toggleFullscreen();
    }
    
    if (event.key === 'Escape' && isFullscreen) {
        toggleFullscreen();
    }
    
    if (event.key === 'F5') {
        event.preventDefault();
        loadPollData();
    }
});

document.addEventListener('visibilitychange', function() {
    if (document.hidden) {
        if (isPageVisible) {
            isPageVisible = false;
        }
    } else {
        if (!isPageVisible) {
            isPageVisible = true;
            
            if (pollId) {
                loadPollResults();
            }
        }
    }
});

window.addEventListener('beforeunload', function() {
    if (webSocket && typeof webSocket.close === 'function') {
        webSocket.close();
    }
    
    if (longPollingInterval) {
        clearInterval(longPollingInterval);
    }
});
