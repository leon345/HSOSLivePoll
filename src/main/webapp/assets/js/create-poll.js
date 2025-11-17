let optionCounter = 2;

function updateQuestionCounter() {
    const questionInput = document.getElementById('question');
    const counter = document.getElementById('question-counter');
    if (questionInput && counter) {
        const length = questionInput.value.length;
        counter.textContent = length;
        
        if (length >= 160) {
            counter.style.color = '#ef4444';
        } else if (length >= 140) {
            counter.style.color = '#f59e0b';
        } else {
            counter.style.color = 'var(--text-secondary)';
        }
    }
}

function addOption() {
    const container = document.getElementById('options-container');
    const addButton = document.getElementById('add-option-btn');
    
    if (!container || container.children.length >= 10) {
        showToast('Maximal 10 Antworten erlaubt', 'warning');
        return;
    }
    
    const newOptionNumber = container.children.length + 1;
    const optionDiv = document.createElement('div');
    optionDiv.className = 'option-input';
    optionDiv.setAttribute('data-option-id', newOptionNumber);
    
    optionDiv.innerHTML = `
        <input type="text" 
               name="options[]" 
               placeholder="Antwort ${newOptionNumber}" 
               required 
               maxlength="100"
               aria-label="Antwort ${newOptionNumber}">
        <button type="button" 
                class="remove-option" 
                onclick="removeOption(this)"
                aria-label="Entfernen"
                title="Entfernen">
            <i class="fas fa-times"></i>
        </button>
    `;
    
    container.appendChild(optionDiv);
    
    const newInput = optionDiv.querySelector('input');
    if (newInput) {
        newInput.focus();
    }
    
    updateOptionNumbers();
    updateAddButtonState();
    
    optionDiv.style.opacity = '0';
    optionDiv.style.transform = 'translateY(-10px)';
    setTimeout(() => {
        optionDiv.style.transition = 'all 0.3s ease';
        optionDiv.style.opacity = '1';
        optionDiv.style.transform = 'translateY(0)';
    }, 10);
}

function removeOption(button) {
    const container = document.getElementById('options-container');
    if (!container || container.children.length <= 2) {
        showToast('Mindestens 2 Antworten erforderlich', 'warning');
        return;
    }
    
    const optionDiv = button.parentElement;
    
    optionDiv.style.transition = 'all 0.3s ease';
    optionDiv.style.opacity = '0';
    optionDiv.style.transform = 'translateX(-20px)';
    
    setTimeout(() => {
        optionDiv.remove();
        updateOptionNumbers();
        updateAddButtonState();
    }, 300);
}

function updateOptionNumbers() {
    const optionInputs = document.querySelectorAll('#options-container .option-input');
    optionInputs.forEach((optionDiv, index) => {
        const input = optionDiv.querySelector('input');
        const removeBtn = optionDiv.querySelector('.remove-option');
        
        if (input) {
            input.placeholder = `Antwort ${index + 1}`;
            input.setAttribute('aria-label', `Antwort ${index + 1}`);
        }
        if (removeBtn) {
            removeBtn.setAttribute('aria-label', 'Entfernen');
            if (optionInputs.length > 2) {
                removeBtn.style.display = 'flex';
            } else {
                removeBtn.style.display = 'none';
            }
        }
        
        optionDiv.setAttribute('data-option-id', index + 1);
    });
    
    optionCounter = optionInputs.length;
}

function updateAddButtonState() {
    const container = document.getElementById('options-container');
    const addButton = document.getElementById('add-option-btn');
    
    if (container && addButton) {
        const count = container.children.length;
        if (count >= 10) {
            addButton.disabled = true;
            addButton.classList.add('disabled');
            addButton.setAttribute('aria-label', 'Maximale Anzahl erreicht');
        } else {
            addButton.disabled = false;
            addButton.classList.remove('disabled');
            addButton.setAttribute('aria-label', 'Antwort hinzufügen');
        }
    }
}





function validateCreateForm() {
    const question = document.getElementById('question')?.value.trim();
    const pollType = document.getElementById('poll-type')?.value;
    const options = Array.from(document.querySelectorAll('#options-container input[type="text"]'))
                        .map(input => input.value.trim())
                        .filter(value => value.length > 0);

    if (!question) {
        showToast('Bitte Frage eingeben', 'error');
        document.getElementById('question')?.focus();
        return false;
    }
    
    if (question.length < 3) {
        showToast('Frage zu kurz (min. 3 Zeichen)', 'error');
        document.getElementById('question')?.focus();
        return false;
    }

    if (!pollType) {
        showToast('Bitte Typ wählen', 'error');
        document.getElementById('poll-type')?.focus();
        return false;
    }

    if (options.length < 2) {
        showToast('Mindestens 2 Antworten erforderlich', 'error');
        return false;
    }

    const uniqueOptions = new Set(options.map(opt => opt.toLowerCase()));
    if (uniqueOptions.size !== options.length) {
        showToast('Doppelte Antworten nicht erlaubt', 'error');
        return false;
    }

    const emptyOptions = options.some(opt => opt.length === 0);
    if (emptyOptions) {
        showToast('Alle Antworten ausfüllen', 'error');
        return false;
    }
    
    return true;
}

async function submitCreateForm(event) {
    event.preventDefault();
    
    if (!validateCreateForm()) {
        return;
    }
    
    const submitBtn = document.getElementById('submit-btn');
    const originalText = submitBtn.innerHTML;
    
    try {
        submitBtn.disabled = true;
        submitBtn.innerHTML = '<span class="loader small"></span> Erstelle...';
        
        showLoading();
        
        const formData = new FormData(event.target);
        
        const pollData = {
            question: formData.get('question').trim(),
            pollType: formData.get('pollType'),
            options: Array.from(formData.getAll('options[]'))
                        .map(option => option.trim())
                        .filter(option => option.length > 0),
            startTime: null,
            endTime: null,
            allowMultipleVotes: formData.get('allowMultipleVotes') === 'on'
        };
        
        const newPoll = await createPoll(pollData);
        
        showToast('Umfrage erstellt!', 'success');
        
        resetForm();
        
        window.location.href = `/dashboard/poll/${newPoll.id}`;
        
    } catch (error) {
        console.error('Create Poll Error:', error);
        
        let errorMessage = 'Fehler beim Erstellen';
        if (error.message) {
            errorMessage += ': ' + error.message;
        }
        
        showToast(errorMessage, 'error');
    } finally {
        hideLoading();
        
        submitBtn.disabled = false;
        submitBtn.innerHTML = originalText;
    }
}

function resetForm() {
    const form = document.getElementById('create-poll-form');
    if (form) {
        form.reset();
        
        const container = document.getElementById('options-container');
        if (container) {
            container.innerHTML = `
                <div class="option-input" data-option-id="1">
                    <input type="text" 
                           name="options[]" 
                           placeholder="Antwort 1" 
                           required 
                           maxlength="100"
                           aria-label="Antwort 1">
                    <button type="button" 
                            class="remove-option" 
                            onclick="removeOption(this)"
                            aria-label="Entfernen"
                            title="Entfernen"
                            style="display: none;">
                        <i class="fas fa-times"></i>
                    </button>
                </div>
                <div class="option-input" data-option-id="2">
                    <input type="text" 
                           name="options[]" 
                           placeholder="Antwort 2" 
                           required 
                           maxlength="100"
                           aria-label="Antwort 2">
                    <button type="button" 
                            class="remove-option" 
                            onclick="removeOption(this)"
                            aria-label="Entfernen"
                            title="Entfernen"
                            style="display: none;">
                        <i class="fas fa-times"></i>
                    </button>
                </div>
            `;
        }
        

        
        optionCounter = 2;
        updateQuestionCounter();
        updateAddButtonState();
        

    }
}

document.addEventListener('DOMContentLoaded', function() {
    if (window.location.pathname === '/dashboard/create') {
        const createForm = document.getElementById('create-poll-form');
        if (createForm) {
            createForm.addEventListener('submit', submitCreateForm);

            const questionInput = document.getElementById('question');
            if (questionInput) {
                questionInput.addEventListener('input', updateQuestionCounter);
                questionInput.addEventListener('keydown', function(e) {
                    if (e.key === 'Enter') {
                        e.preventDefault();
                        document.getElementById('poll-type')?.focus();
                    }
                });
            }


            const optionsContainer = document.getElementById('options-container');
            if (optionsContainer) {
                optionsContainer.addEventListener('keydown', function(e) {
                    if (e.key === 'Enter' && e.target.tagName === 'INPUT') {
                        e.preventDefault();
                        const addButton = document.getElementById('add-option-btn');
                        if (addButton && !addButton.disabled) {
                            addOption();
                        }
                    }
                });
            }
        }

        updateQuestionCounter();
        updateOptionNumbers();
        updateAddButtonState();
    }
});

