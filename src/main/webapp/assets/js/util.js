
function formatNumber(num) {
    return new Intl.NumberFormat('de-DE').format(num);
}

function formatRelativeTime(dateString) {
    const date = new Date(dateString);
    const now = new Date();
    const diffInSeconds = Math.floor((now - date) / 1000);
    
    if (diffInSeconds < 60) return 'Gerade eben';
    if (diffInSeconds < 3600) return 'vor ' + Math.floor(diffInSeconds / 60) + ' Minuten';
    if (diffInSeconds < 86400) return 'vor ' + Math.floor(diffInSeconds / 3600) + ' Stunden';
    return 'vor ' + Math.floor(diffInSeconds / 86400) + ' Tagen';
}

function showLoading() {
    const overlay = document.getElementById('loading-overlay');
    if (overlay) {
        overlay.classList.add('active');
    }
}

function hideLoading() {
    const overlay = document.getElementById('loading-overlay');
    if (overlay) {
        overlay.classList.remove('active');
    }
}

function showToast(message, type = 'info') {
    const toastContainer = document.getElementById('toast-container');
    if (!toastContainer) return;
    
    const toast = document.createElement('div');
    toast.className = `toast ${type}`;
    toast.textContent = message;

    toastContainer.appendChild(toast);
    
    setTimeout(() => {
        toast.classList.add('show');
    }, 100);

    setTimeout(() => {
        toast.classList.add('hide');
        setTimeout(() => {
            if (toast.parentNode) {
                toast.remove();
            }
        }, 500);
    }, 10000);
}

function updateResultsChart(options, chartType = 'doughnut', chartId = 'results-chart') {
    const ctx = document.getElementById(chartId);
    if (!ctx) return;
    
    if (window.resultsChart) {
        window.resultsChart.destroy();
    }
    
    const labels = options.map(option => option.text);
    const data = options.map(option => option.votes);
    const totalVotes = data.reduce((sum, votes) => sum + votes, 0);
    
    if (totalVotes === 0) {
        const chartConfig = {
            type: chartType,
            data: {
                labels: ['Noch keine Stimmen'],
                datasets: [{
                    data: [1],
                    backgroundColor: ['#e5e7eb'],
                    borderWidth: 2,
                    borderColor: '#ffffff'
                }]
            },
            options: {
                responsive: true,
                maintainAspectRatio: false,
                plugins: {
                    legend: {
                        position: 'bottom',
                        labels: {
                            padding: 20,
                            usePointStyle: true,
                            font: {
                                size: 12
                            }
                        }
                    },
                    tooltip: {
                        enabled: false
                    }
                }
            }
        };
        
        window.resultsChart = new Chart(ctx, chartConfig);
        return;
    }
    
    const chartConfig = {
        type: chartType,
        data: {
            labels: labels,
            datasets: [{
                data: data,
                backgroundColor: [
                    '#4f46e5',
                    '#06b6d4',
                    '#10b981',
                    '#f59e0b',
                    '#ef4444',
                    '#8b5cf6',
                    '#ec4899',
                    '#84cc16'
                ],
                borderWidth: 2,
                borderColor: '#ffffff'
            }]
        },
        options: {
            responsive: true,
            maintainAspectRatio: false,
            plugins: {
                legend: {
                    position: 'bottom',
                    labels: {
                        padding: 20,
                        usePointStyle: true,
                        font: {
                            size: 12
                        }
                    }
                },
                tooltip: {
                    callbacks: {
                        label: function(context) {
                            const votes = context.parsed;
                            const percentage = totalVotes > 0 ? Math.round((votes / totalVotes) * 100) : 0;
                            return context.label + ': ' + votes + ' Stimmen (' + percentage + '%)';
                        }
                    }
                }
            }
        }
    };
    
    if (chartType === 'bar') {
        chartConfig.data.datasets[0].backgroundColor = '#4f46e5';
        chartConfig.data.datasets[0].borderColor = '#4338ca';
        chartConfig.options.scales = {
            y: {
                beginAtZero: true,
                ticks: {
                    stepSize: 1
                }
            }
        };
        chartConfig.options.plugins.legend.display = false;
    }
    
    window.resultsChart = new Chart(ctx, chartConfig);
}

function updateResultsList(options, containerId = 'results-list') {
    const container = document.getElementById(containerId);
    if (!container) return;
    
    container.innerHTML = '';
    
    const totalVotes = options.reduce((sum, option) => sum + option.votes, 0);
    
    options.forEach(option => {
        const percentage = totalVotes > 0 ? Math.round((option.votes / totalVotes) * 100) : 0;
        
        const resultItem = document.createElement('div');
        resultItem.className = 'result-item';
        
        if (totalVotes === 0) {
            resultItem.classList.add('placeholder');
        }
        
        resultItem.innerHTML = 
            '<div class="result-info">' +
                '<div class="result-label">' + option.text + '</div>' +
                '<div class="result-bar-container">' +
                    '<div class="result-bar-fill" style="width: ' + percentage + '%"></div>' +
                '</div>' +
            '</div>' +
            '<div class="result-stats">' +
                '<div class="result-votes">' + formatNumber(option.votes) + '</div>' +
                '<div class="result-percentage">' + percentage + '%</div>' +
            '</div>';
        
        container.appendChild(resultItem);
    });
}

function sharePoll(pollQuestion = 'LivePoll Umfrage') {
    const pollId = getPollIdFromUrl();
    let url;
    
    if (pollId) {
        url = window.location.origin + '/modules/voting.jsp?code=' + pollId;
    } else {
        url = window.location.href;
    }
    
    if (navigator.share) {
        navigator.share({
            title: pollQuestion,
            url: url
        });
    } else {
        navigator.clipboard.writeText(url).then(() => {
            showToast('Link in Zwischenablage kopiert!', 'success');
        }).catch(() => {
            showToast('Fehler beim Kopieren des Links', 'error');
        });
    }
}

function startAutoRefresh(callback, interval = 3000) {
    let refreshInterval = setInterval(callback, interval);
    
    return () => {
        if (refreshInterval) {
            clearInterval(refreshInterval);
        }
    };
}

function navigateToPollDetail(pollId) {
    window.location.href = `/dashboard/poll/${pollId}`;
}

function getPollIdFromUrl() {
    const path = window.location.pathname;
    const urlParams = new URLSearchParams(window.location.search);
    
    const codeParam = urlParams.get('code');
    if (codeParam) {
        return codeParam;
    }
    
    const match = path.match(/\/(?:dashboard\/)?poll\/([^\/]+)/);
    return match ? match[1] : null;
}

function getStatusDisplayName(status) {
    const statusMap = {
        'DRAFT': 'Entwurf', 
        'ACTIVE': 'Aktiv', 
        'CLOSED': 'Geschlossen', 
        'EXPIRED': 'Abgelaufen'
    };
    return statusMap[status] || status;
}
