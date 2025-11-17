function importPollFromXml() {
    const fileInput = document.getElementById('import-file');
    if (!fileInput) {
        return;}

    fileInput.value = '';
    fileInput.onchange = handleXmlFileSelected;
    fileInput.click();
}

async function handleXmlFileSelected(event) {
    const files = event.target.files && event.target.files;
    if (!files || files.length === 0) {
        return;
    }
    const file = files[0];
    if (file.name.toLowerCase().endsWith('.xml') === false) {
        showToast("Bitte eine XML-Datei auswählen", "warning");
        return;
    }

    try{
        showLoading();
        const text = await file.text();
        const parser = new DOMParser();
        const xml = parser.parseFromString(text, 'text/xml');

        const parseError = xml.querySelector('parsererror');
        if (parseError) {
            showToast("Ungültige XML-Datei", "error");
            return;
        }
        const root = xml.querySelector('TemplatePollXml');
        if (!root) {
            showToast("TemplatePollXml Wurzelelement fehlt", "error");
            return;
        }
        const question = root.querySelector('question')?.textContent.trim();
        const pollType = root.querySelector('pollType')?.textContent.trim();
        const allowMultipleVotesString = root.querySelector('allowMultipleVotes')?.textContent.trim();
        const allowMultipleVotes = allowMultipleVotesString?.toLowerCase() === 'true';
        const optionsParent = root.querySelector('options');

        if (!question || question.length < 3) {
            showToast('Frage fehlt oder ist zu kurz', 'error');
            return;
        }
        const allowedTypes = new Set(['SINGLE_CHOICE', 'MULTIPLE_CHOICE']);
        if (!pollType || !allowedTypes.has(pollType)) {
            showToast("pollType ungültig (SINGLE_CHOICE oder MULTIPLE_CHOICE)", "error");
            return;
        }

        let optionTexts = [];

        if (optionsParent) {
            const texts = optionsParent.querySelectorAll('text');
            texts.forEach(t => {
                const val = t.textContent?.trim();
                if (val) optionTexts.push(val);
            });
        }

        optionTexts = optionTexts
            .filter(Boolean)
            .map(s => s.trim())
            .filter(s => s.length > 0);
        const optionsUniqu = Array.from(new Map(optionTexts.map(s => [s.toLowerCase(), s])).values());

        if (optionsUniqu.length < 2){
            showToast("Mindestens 2 eindeutige Antwortoptionen erforderlich", "error");
            return;
        }

        if (optionsUniqu.length > 10) {
            showToast("Maximal 10 Antwortoptionen erlaubt – es werden die ersten 10 übernommen", "warning");
        }
        const options = optionsUniqu.slice(0, 10);

        fillCreatePollForm({
            question,
            pollType,
            allowMultipleVotes,
            options: options
        });
        showToast('XML erfolgreich importiert', 'success');

    } catch (err) {
        showToast('Fehler beim Importieren der XML', 'error');
    } finally {
        hideLoading();
    }
}

function fillCreatePollForm({ question, pollType, allowMultipleVotes, options }) {
    const inputQuestion = document.getElementById('question');

    if (inputQuestion && question){
        inputQuestion.value = question;
    }

    const inputType = document.getElementById('poll-type');
    if (inputType && pollType) {
        inputType.value = pollType;
    }

    const inputAllowMultiple = document.getElementById('allow-multiple');
    if (inputAllowMultiple && allowMultipleVotes) {
        inputAllowMultiple.checked = allowMultipleVotes;
    }
    const container = document.getElementById('options-container');
    const needed = Math.max(2, options.length);

    while (container.children.length < needed) {
        addOption();
    }

    const inputs = container.querySelectorAll('input[name="options[]"]');

    inputs.forEach((input, index) => {
        input.value = options[index] ?? '';
    })

    updateOptionNumbers?.();
    updateAddButtonState?.();

}