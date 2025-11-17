document.addEventListener('DOMContentLoaded', function() {
    if (window.location.pathname === '/dashboard' || window.location.pathname === '/dashboard/') {
        loadDashboard();
        
        const stopDashboardRefresh = startAutoRefresh(loadDashboard, 30000);
        window.addEventListener('beforeunload', stopDashboardRefresh);
    }
    
    if (window.location.pathname === '/dashboard/active') {
        loadActivePolls();
        
        const stopActivePollsRefresh = startAutoRefresh(loadActivePolls, 10000);
        window.addEventListener('beforeunload', stopActivePollsRefresh);
    }
});



async function loadDashboard() {
    try {
        showLoading();
        
        const [polls, activePolls] = await Promise.all([
            getPolls(),
            getActivePolls()
        ]);
        
        updateDashboardStats(polls, activePolls);
        
    } catch (error) {
        showToast('Fehler beim Laden des Dashboards', 'error');
        
        updateDashboardStats([], []);
    } finally {
        hideLoading();
    }
}

async function loadActivePolls() {
    try {
        showLoading();
        
        const activePolls = await getActivePolls();
        updateActivePollsList(activePolls);
        
    } catch (error) {
        showToast('Fehler beim Laden der aktiven Umfragen', 'error');
        
        updateActivePollsList([]);
    } finally {
        hideLoading();
    }
}

function updateDashboardStats(allPolls, activePolls) {
    const totalVotes = allPolls.reduce((sum, poll) => sum + (poll.totalVotes || 0), 0);
    const closedPolls = allPolls.filter(poll => poll.status === 'CLOSED').length;
    
    const totalPollsElement = document.getElementById('total-polls');
    const activePollsElement = document.getElementById('active-polls');
    const totalVotesElement = document.getElementById('total-votes');
    
    if (totalPollsElement) totalPollsElement.textContent = allPolls.length;
    if (activePollsElement) activePollsElement.textContent = activePolls.length;
    if (totalVotesElement) totalVotesElement.textContent = totalVotes;
    
    animateNumber(totalPollsElement, allPolls.length);
    animateNumber(activePollsElement, activePolls.length);
    animateNumber(totalVotesElement, totalVotes);
}

function animateNumber(element, targetValue) {
    if (!element) return;
    
    const currentValue = parseInt(element.textContent) || 0;
    const increment = (targetValue - currentValue) / 20;
    let current = currentValue;
    
    const timer = setInterval(() => {
        current += increment;
        if ((increment > 0 && current >= targetValue) || 
            (increment < 0 && current <= targetValue)) {
            current = targetValue;
            clearInterval(timer);
        }
        element.textContent = Math.floor(current);
    }, 50);
}

function updateActivePollsList(activePolls) {
    const container = document.getElementById('active-polls-list');
    if (!container) return;
    
    container.innerHTML = '';
    
    if (activePolls.length === 0) {
        container.innerHTML = `
            <div class="no-data">
                <i class="fas fa-play-circle fa-3x"></i>
                <p>Keine aktiven Umfragen</p>
                <a href="/dashboard/create" class="btn btn-primary">
                    <i class="fas fa-plus"></i> Neue Umfrage erstellen
                </a>
            </div>
        `;
        return;
    }
    
    activePolls.forEach(poll => {
        const pollCard = createEnhancedPollCard(poll);
        container.appendChild(pollCard);
    });
}

function createEnhancedPollCard(poll) {
    const card = document.createElement('div');
    card.className = 'poll-card';
    card.onclick = () => navigateToPollDetail(poll.id);
    
    const statusClass = poll.status === 'ACTIVE' ? 'active' : 'closed';
    const statusText = poll.status === 'ACTIVE' ? 'Aktiv' : 'Geschlossen';
    const statusIcon = poll.status === 'ACTIVE' ? 'fa-play' : 'fa-stop';
    
    let progressHtml = '';
    if (poll.status === 'ACTIVE' && poll.endTime) {
        const now = new Date();
        const end = new Date(poll.endTime);
        const start = new Date(poll.createdAt);
        const total = end - start;
        const elapsed = now - start;
        const progress = Math.min(Math.max((elapsed / total) * 100, 0), 100);
        
        progressHtml = `
            <div class="poll-progress">
                <div class="progress-bar">
                    <div class="progress-fill" style="width: ${progress}%"></div>
                </div>
                <span class="progress-text">${Math.round(progress)}%</span>
            </div>
        `;
    }
    
    card.innerHTML = `
        <div class="poll-header">
            <div class="poll-status">
                <span class="status-badge ${statusClass}">
                    <i class="fas ${statusIcon}"></i>
                    ${statusText}
                </span>
            </div>
            <div class="poll-actions">
                <button class="btn btn-sm btn-primary" onclick="event.stopPropagation(); navigateToPollDetail('${poll.id}')">
                    <i class="fas fa-eye"></i>
                </button>
                ${poll.status === 'ACTIVE' ? `
                    <button class="btn btn-sm btn-danger" onclick="event.stopPropagation(); closePollById('${poll.id}')">
                        <i class="fas fa-stop"></i>
                    </button>
                ` : `
                    <button class="btn btn-sm btn-secondary" onclick="event.stopPropagation(); deletePollById('${poll.id}')">
                        <i class="fas fa-trash"></i>
                    </button>
                `}
            </div>
        </div>
        <div class="poll-content">
            <h3>${poll.question}</h3>
            <div class="poll-meta">
                <span><i class="fas fa-clock"></i> ${formatRelativeTime(poll.createdAt)}</span>
                <span><i class="fas fa-users"></i> ${poll.totalVotes || 0} Stimmen</span>
                <span><i class="fas fa-list"></i> ${poll.options ? poll.options.length : 0} Optionen</span>
            </div>
            ${progressHtml}
        </div>
    `;
    
    return card;
}

async function deletePollById(pollId) {
    if (!confirm('Sind Sie sicher, dass Sie diese Umfrage löschen möchten?')) {
        return;
    }
    
    try {
        showLoading();
        await deletePoll(pollId);
        showToast('Umfrage erfolgreich gelöscht', 'success');
        
        const currentPath = window.location.pathname;
        if (currentPath === '/dashboard' || currentPath === '/dashboard/') {
            loadDashboard();
        } else if (currentPath === '/dashboard/active') {
            loadActivePolls();
        }
    } catch (error) {
        if (error.message.includes('Zugriff verweigert')) {
            showToast('Zugriff verweigert: Sie können nur Ihre eigenen Umfragen löschen', 'error');
        } else {
            showToast('Fehler beim Löschen der Umfrage: ' + error.message, 'error');
        }
    } finally {
        hideLoading();
    }
}

function calculatePollStats(polls) {
    const stats = {
        total: polls.length,
        active: polls.filter(p => p.status === 'ACTIVE').length,
        closed: polls.filter(p => p.status === 'CLOSED').length,
        totalVotes: polls.reduce((sum, p) => sum + (p.totalVotes || 0), 0),
        avgVotesPerPoll: 0,
        mostPopularPoll: null
    };
    
    if (stats.total > 0) {
        stats.avgVotesPerPoll = Math.round(stats.totalVotes / stats.total);
        stats.mostPopularPoll = polls.reduce((max, poll) => 
            (poll.totalVotes || 0) > (max.totalVotes || 0) ? poll : max
        );
    }
    
    return stats;
}

function exportPollData(pollId) {
    showToast('Export-Funktion wird implementiert', 'info');
}

function filterPolls(searchTerm, statusFilter = 'all') {
    const pollCards = document.querySelectorAll('.poll-card');
    
    pollCards.forEach(card => {
        const question = card.querySelector('h3').textContent.toLowerCase();
        const status = card.querySelector('.status-badge').textContent.toLowerCase();
        
        const matchesSearch = question.includes(searchTerm.toLowerCase());
        const matchesStatus = statusFilter === 'all' || status.includes(statusFilter.toLowerCase());
        
        card.style.display = matchesSearch && matchesStatus ? 'block' : 'none';
    });
}

function navigateToPollDetail(pollId) {
    window.location.href = `/dashboard/poll/${pollId}`;
}