let activePollsData = [];
let activePollsRefreshInterval = null;

function initActivePolls() {
    loadActivePols();

    if (activePollsRefreshInterval) {
        clearInterval(activePollsRefreshInterval);
    }

    activePollsRefreshInterval = setInterval(() => {
        loadActivePols();
    }, 30000);
}

async function loadActivePols() {
    const container = document.getElementById('active-polls-list');
    const loading = document.getElementById('active-polls-loading');
    const error = document.getElementById('active-polls-error');

    if (!container) return;

    try {
        container.style.display = 'none';
        loading.style.display = 'flex';
        error.style.display = 'none';

        activePollsData = await getPolls();

        updateActivePollsList(activePollsData);

        container.style.display = 'block';
        container.style.display = 'block';
        loading.style.display = 'none';

    } catch (error) {
        container.style.display = 'none';
        loading.style.display = 'none';
        container.style.display = 'none';
    }
}

function updateActivePollsList(polls) {
    const container = document.getElementById('active-polls-list');
    if (!container) return;

    container.innerHTML = '';

    if (polls.length === 0) {
        container.innerHTML = `
            <div class="no-data">
                <i class="fas fa-inbox"></i>
                <p>Noch keine aktive Umfragen vorhanden</p>
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
            <th>Frage</th>
            <th>Erstellt</th>
            <th>Stimmen</th>
            <th>Optionen</th>
            <th>Aktionen</th>
        </tr>
    `;
    table.appendChild(thead);

    const tbody = document.createElement('tbody');

    polls.forEach(poll => {
        const row = createActivePollTableRow(poll);
        tbody.appendChild(row);
    });

    table.appendChild(tbody);
    container.appendChild(table);
}

function createActivePollTableRow(poll) {
    const row = document.createElement('tr');

    row.innerHTML = `
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
        <td>
            <div class="table-actions">
                <button class="btn btn-sm btn-primary" onclick="navigateToPollDetail('${poll.id}')" title="Details anzeigen">
                    <i class="fas fa-eye"></i>
                </button>
                ${poll.status === 'ACTIVE' ? `
                    <button class="btn btn-sm btn-danger" onclick="closePollById('${poll.id}')" title="Umfrage beenden">
                        <i class="fas fa-stop"></i>
                    </button>
                ` : `
                    <button class="btn btn-sm btn-success" onclick="activatePollById('${poll.id}')" title="Umfrage aktivieren">
                        <i class="fas fa-play"></i>
                    </button>
                    <button class="btn btn-sm btn-secondary" onclick="deletePollById('${poll.id}')" title="Umfrage löschen">
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

async function activatePollById(pollId) {
    if (!confirm('Sind Sie sicher, dass Sie diese Umfrage aktivieren möchten?')) {
        return;
    }
    
    try {
        if (typeof activatePollByIdWithModal === 'function') {
            activatePollByIdWithModal(pollId);
        } else {
            await activatePoll(pollId);
            showToast('Umfrage erfolgreich aktiviert', 'success');
            loadActivePols();
        }
    } catch (error) {
        if (error.message.includes('Zugriff verweigert')) {
            showToast('Zugriff verweigert: Sie können nur Ihre eigenen Umfragen aktivieren', 'error');
        } else {
            showToast('Fehler beim Aktivieren der Umfrage: ' + error.message, 'error');
        }
    }
}

async function closePollById(pollId) {
    if (!confirm('Sind Sie sicher, dass Sie diese Umfrage beenden möchten?')) {
        return;
    }
    
    try {
        if (typeof closePollByIdWithModal === 'function') {
            closePollByIdWithModal(pollId);
        } else {
            await closePoll(pollId);
            showToast('Umfrage erfolgreich beendet', 'success');
            loadActivePols();
        }
    } catch (error) {
        if (error.message.includes('Zugriff verweigert')) {
            showToast('Zugriff verweigert: Sie können nur Ihre eigenen Umfragen beenden', 'error');
        } else {
            showToast('Fehler beim Beenden der Umfrage: ' + error.message, 'error');
        }
    }
}

async function deletePollById(pollId) {
    if (!confirm('Sind Sie sicher, dass Sie diese Umfrage löschen möchten?')) {
        return;
    }
    
    try {
        if (typeof deletePollByIdWithModal === 'function') {
            deletePollByIdWithModal(pollId);
        } else {
            await deletePoll(pollId);
            showToast('Umfrage erfolgreich gelöscht', 'success');
            loadActivePols();
        }
    } catch (error) {
        if (error.message.includes('Zugriff verweigert')) {
            showToast('Zugriff verweigert: Sie können nur Ihre eigenen Umfragen löschen', 'error');
        } else {
            showToast('Fehler beim Löschen der Umfrage: ' + error.message, 'error');
        }
    }
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

document.addEventListener('DOMContentLoaded', function () {
    if (window.location.pathname === '/dashboard/active' || window.location.pathname === '/dashboard/active/') {
        initActivePolls();
    }
});

window.addEventListener('beforeunload', function () {
    if (activePollsRefreshInterval) {
        clearInterval(activePollsRefreshInterval);
    }
});