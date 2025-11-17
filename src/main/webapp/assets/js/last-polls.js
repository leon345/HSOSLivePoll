let recentPollsData = [];

function initRecentPolls() {
    loadRecentPolls();
    
    const stopRecentPollsRefresh = startAutoRefresh(loadRecentPolls, 30000);
    window.addEventListener('beforeunload', stopRecentPollsRefresh);
}

async function loadRecentPolls() {
    const container = document.getElementById('recent-polls-list');
    const loading = document.getElementById('recent-polls-loading');
    const error = document.getElementById('recent-polls-error');
    
    if (!container) return;
    
    try {
        container.style.display = 'none';
        loading.style.display = 'flex';
        error.style.display = 'none';
        
        const polls = await getPolls();
        recentPollsData = polls.slice(0, 6);
        
        updateRecentPollsList(recentPollsData);
        
        container.style.display = 'block';
        loading.style.display = 'none';
        
    } catch (error) {
        
        container.style.display = 'none';
        loading.style.display = 'none';
        document.getElementById('recent-polls-error').style.display = 'flex';
    }
}

function updateRecentPollsList(polls) {
    const container = document.getElementById('recent-polls-list');
    if (!container) return;
    
    container.innerHTML = '';
    
    if (polls.length === 0) {
        container.innerHTML = `
            <div class="no-data">
                <i class="fas fa-inbox"></i>
                <p>Noch keine Umfragen erstellt</p>
                <a href="/dashboard/create" class="btn btn-primary btn-create-poll">
                    <i class="fas fa-plus"></i> Erste Umfrage erstellen
                </a>
            </div>
        `;
        return;
    }
    
    const table = document.createElement('table');
    table.className = 'polls-table';
    
    const thead = document.createElement('thead');
    thead.innerHTML = `
        <tr>
            <th>Status</th>
            <th>Frage</th>
            <th>Erstellt</th>
            <th>Stimmen</th>
            <th>Optionen</th>
            <th>Fortschritt</th>
            <th>Aktionen</th>
        </tr>
    `;
    table.appendChild(thead);
    
    const tbody = document.createElement('tbody');
    
    polls.forEach(poll => {
        const row = createRecentPollTableRow(poll);
        tbody.appendChild(row);
    });
    
    table.appendChild(tbody);
    container.appendChild(table);
}

function createRecentPollTableRow(poll) {
    const row = document.createElement('tr');
    
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
            <div class="table-progress-bar">
                <div class="table-progress-fill" style="width: ${progress}%"></div>
            </div>
            <div class="table-progress-text">${Math.round(progress)}%</div>
        `;
    } else {
        progressHtml = '<span class="poll-meta-cell">-</span>';
    }
    
    row.innerHTML = `
        <td>
            <span class="table-status-badge ${statusClass}">
                <i class="fas ${statusIcon}"></i>
                ${statusText}
            </span>
        </td>
        <td>
            <div class="poll-question" title="${poll.question}">${poll.question}</div>
        </td>
        <td class="poll-meta-cell">
            <i class="fas fa-clock"></i> ${formatRelativeTime(poll.createdAt)}
        </td>
        <td class="poll-meta-cell">
            <i class="fas fa-users"></i> ${poll.totalVotes || 0}
        </td>
        <td class="poll-meta-cell">
            <i class="fas fa-list"></i> ${poll.options ? poll.options.length : 0}
        </td>
        <td class="poll-progress-cell">
            ${progressHtml}
        </td>
        <td>
            <div class="table-actions">
                <button class="btn btn-sm btn-primary" onclick="navigateToPollDetail('${poll.id}')" title="Details anzeigen">
                    <i class="fas fa-eye"></i>
                </button>
                ${poll.status === 'ACTIVE' ? `
                    <button class="btn btn-sm btn-danger" onclick="closePollByIdWithModal('${poll.id}')" title="Umfrage beenden">
                        <i class="fas fa-stop"></i>
                    </button>
                ` : `
                    <button class="btn btn-sm btn-success" onclick="activatePollByIdWithModal('${poll.id}')" title="Umfrage aktivieren">
                        <i class="fas fa-play"></i>
                    </button>
                    <button class="btn btn-sm btn-secondary" onclick="deletePollByIdWithModal('${poll.id}')" title="Umfrage löschen">
                        <i class="fas fa-trash"></i>
                    </button>
                `}
            </div>
        </td>
    `;
    
    return row;
}

function navigateToPollDetail(pollId) {
    window.location.href = `/dashboard/poll/${pollId}`;
}

function refreshRecentPolls() {
    loadRecentPolls();
    showToast('Letzte Umfragen aktualisiert', 'success');
}

async function deletePollById(pollId) {
    if (!confirm('Sind Sie sicher, dass Sie diese Umfrage löschen möchten?')) {
        return;
    }
    
    try {
        showLoading();
        await deletePoll(pollId);
        showToast('Umfrage erfolgreich gelöscht', 'success');
        
        loadRecentPolls();
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

async function closePollById(pollId) {
    if (!confirm('Sind Sie sicher, dass Sie diese Umfrage beenden möchten?')) {
        return;
    }
    
    try {
        showLoading();
        await closePoll(pollId);
        showToast('Umfrage erfolgreich beendet', 'success');
        
        loadRecentPolls();
    } catch (error) {
        if (error.message.includes('Zugriff verweigert')) {
            showToast('Zugriff verweigert: Sie können nur Ihre eigenen Umfragen beenden', 'error');
        } else {
            showToast('Fehler beim Beenden der Umfrage: ' + error.message, 'error');
        }
    } finally {
        hideLoading();
    }
}

async function activatePollById(pollId) {
    if (!confirm('Sind Sie sicher, dass Sie diese Umfrage aktivieren möchten?')) {
        return;
    }
    
    try {
        showLoading();
        await activatePoll(pollId);
        showToast('Umfrage erfolgreich aktiviert', 'success');
        
        loadRecentPolls();
    } catch (error) {
        if (error.message.includes('Zugriff verweigert')) {
            showToast('Zugriff verweigert: Sie können nur Ihre eigenen Umfragen aktivieren', 'error');
        } else {
            showToast('Fehler beim Aktivieren der Umfrage: ' + error.message, 'error');
        }
    } finally {
        hideLoading();
    }
}

document.addEventListener('DOMContentLoaded', function() {
    if (window.location.pathname === '/dashboard' || window.location.pathname === '/dashboard/') {
        initRecentPolls();
    }
});