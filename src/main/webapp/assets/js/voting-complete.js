const urlParams = new URLSearchParams(window.location.search);
const pollId = urlParams.get('pollId');
const allowMultipleVotes = urlParams.get('allowMultipleVotes') === 'true';

document.addEventListener('DOMContentLoaded', function() {
    initializePage();
});

function initializePage() {
    if (pollId) {
        updateShortcode(pollId);
    }
    
    if (allowMultipleVotes) {
        document.getElementById('multiple-votes-allowed').style.display = 'block';
        document.getElementById('single-vote-only').style.display = 'none';
    } else {
        document.getElementById('multiple-votes-allowed').style.display = 'none';
        document.getElementById('single-vote-only').style.display = 'block';
    }
}

function updateShortcode(pollId) {
    const shortcodeElement = document.getElementById('shortcode-text');
    if (shortcodeElement) {
        if (pollId && pollId.match(/^[A-Z0-9]{3,10}$/)) {
            shortcodeElement.textContent = pollId;
        } else {
            shortcodeElement.textContent = pollId || 'N/A';
        }
    }
}

function voteAgain() {
    if (pollId) {
        // Verwende den Shortcode-Link für "Erneut abstimmen"
        window.location.href = `./s/${encodeURIComponent(pollId)}`;
    } else {
        window.location.href = './index.jsp';
    }
}



document.addEventListener('DOMContentLoaded', function() {
    const buttons = document.querySelectorAll('button, .btn');
    
    buttons.forEach(button => {
        button.addEventListener('click', function() {
            this.style.transform = 'scale(0.95)';
            setTimeout(() => {
                this.style.transform = '';
            }, 150);
        });
    });
});
