let currentPoll = null;
let resultsChart = null;

const API_BASE = '/api/';

function updateNavigation() {
    const currentPath = window.location.pathname;

    document.querySelectorAll('.nav-btn').forEach(btn => {
        btn.classList.remove('active');
    });

    if (currentPath === '/dashboard' || currentPath === '/dashboard/') {
        document.getElementById('nav-dashboard').classList.add('active');
    } else if (currentPath === '/dashboard/create') {
        document.getElementById('nav-create').classList.add('active');
    } else if (currentPath === '/dashboard/active') {
        document.getElementById('nav-active').classList.add('active');
    }
}

async function apiRequest(endpoint, options = {}) {
    const url = `${API_BASE}${endpoint}`;
    const defaultOptions = {
        headers: {
            'Content-Type': 'application/json',
        },
    };



    try {
        const response = await fetch(url, {...defaultOptions, ...options});



        if (!response.ok) {
            const errorData = await response.json().catch(() => ({}));

            
            if (response.status === 403) {
                throw new Error('Zugriff verweigert: Sie sind nicht berechtigt, diese Aktion durchzuführen');
            }
            
            if (errorData.errorCode === 'ALREADY_VOTED') {
                throw new Error('Sie haben bereits für diese Umfrage gestimmt');
            } else if (errorData.errorCode === 'POLL_INACTIVE') {
                throw new Error('Diese Umfrage ist nicht mehr aktiv');
            } else if (errorData.errorCode === 'OPTION_NOT_FOUND') {
                throw new Error('Die ausgewählte Option wurde nicht gefunden');
            }
            
            throw new Error(errorData.error || `HTTP ${response.status}`);
        }

        const data = await response.json();

        return data;
    } catch (error) {
        console.error('API Error:', error);
        throw error;
    }
}

async function createPoll(pollData) {
    return apiRequest('polls', {
        method: 'POST',
        body: JSON.stringify(pollData)
    });
}

async function getPolls() {
    return apiRequest('polls');
}

async function getActivePolls() {
    return apiRequest('polls/active');
}

async function getPoll(pollId) {
    return apiRequest(`polls/${pollId}`, {
        method: 'GET'
    });
}

async function getPollByShortCode(shortCode) {
    return apiRequest(`polls/shortcode/${shortCode}`, {
        method: 'GET'
    });
}

async function getPollResults(id) {
    const poll = await getPoll(id);
    const results = {};

    poll.options.forEach(option => {
        results[option.text] = option.votes;
    });

    return results;
}

async function vote(pollId, optionId, userIddd = 'anonymous') {
    const { userId, signature } = await getUserIdAndsignature();
    return apiRequest(`polls/${pollId}/vote`, {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json',
            'X-Voter-ID': userId,
            'X-Signature': signature
        },
        body: JSON.stringify({optionId, userId})
    });
}

async function voteMultiple(pollId, optionIds, userIddd = 'anonymous') {
    const { userId, signature } = await getUserIdAndsignature();
    return apiRequest(`polls/${pollId}/vote-multiple`, {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json',
            'X-Voter-ID': userId,
            'X-Signature': signature
        },
        body: JSON.stringify({optionIds, userId})
    });
}

async function getUserIdAndsignature() {
    let userId = localStorage.getItem('userId');
    let signature = localStorage.getItem('signature');

    if (userId && signature) {

        return {
            userId: userId,
            signature: signature
        }
    }

    const response = await apiRequest("user/userId");

    localStorage.setItem('userId', response.userId);
    localStorage.setItem('signature', response.signature);



    return {
        userId: response.userId,
        signature: response.signature
    }
}

async function getUserInfo() {
    return apiRequest('user/');
}

async function closePoll(id) {
    return apiRequest(`polls/${id}/close`, {
        method: 'PUT'
    });
}

async function activatePoll(id) {
    return apiRequest(`polls/${id}/start`, {
        method: 'PUT'
    });
}

async function deletePoll(id) {
    return apiRequest(`polls/${id}`, {
        method: 'DELETE'
    });
}

async function closePollById(pollId) {
    if (!confirm('Möchten Sie diese Umfrage wirklich beenden?')) {
        return;
    }

    try {
        await closePoll(pollId);
        showToast('Umfrage erfolgreich beendet', 'success');

        const currentPath = window.location.pathname;
        if (currentPath.includes('/dashboard')) {
            if (typeof loadDashboard === 'function') {
                loadDashboard();
            }
        } else if (currentPath.includes('/active')) {
            if (typeof loadActivePolls === 'function') {
                loadActivePolls();
            }
        }

    } catch (error) {
        if (error.message.includes('Zugriff verweigert')) {
            showToast('Zugriff verweigert: Sie können nur Ihre eigenen Umfragen beenden', 'error');
        } else {
            showToast('Fehler beim Beenden der Umfrage: ' + error.message, 'error');
        }
    }
}

document.addEventListener('DOMContentLoaded', function () {
    updateNavigation();
}); 