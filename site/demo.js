const POINTS_PER_CORRECT_ANSWER = 10;
const ROUND_LIMIT = 5;

const el = (id) => document.getElementById(id);
const startView = el('start');
const roundView = el('round');
const resultView = el('result');

let bank = [];
let questions = [];
let index = 0;
let score = 0;
let right = 0;
let wrong = 0;

/** questions.json is the app's own export shape: a header row, then one row per question. */
function parse(rows) {
    return rows.slice(1).map((row) => ({
        text: row[0],
        options: row.slice(1, 5),
        answer: row[5],
        category: row[6]?.trim() || 'Uncategorized',
        difficulty: row[7]?.trim().toUpperCase() || 'MEDIUM',
    }));
}

function show(view) {
    for (const section of [startView, roundView, resultView]) {
        section.hidden = section !== view;
    }
}

function matchingQuestions() {
    return bank.filter((question) =>
        (!el('category').value || question.category === el('category').value)
        && (!el('difficulty').value || question.difficulty === el('difficulty').value));
}

function updateSelection() {
    const count = matchingQuestions().length;
    const size = Math.min(ROUND_LIMIT, count);
    el('selection-count').textContent = count === 0
        ? 'No questions match. Change your selection to start.'
        : `${count} matching ${count === 1 ? 'question' : 'questions'}. This round: ${size} ${size === 1 ? 'question' : 'questions'}.${size < ROUND_LIMIT ? ' Fewer than five match, so this will be a shorter round.' : ''}`;
    el('begin').disabled = count === 0;
}

function shuffle(items) {
    for (let i = items.length - 1; i > 0; i -= 1) {
        const j = Math.floor(Math.random() * (i + 1));
        [items[i], items[j]] = [items[j], items[i]];
    }
}

function startGame() {
    questions = matchingQuestions();
    if (questions.length === 0) {
        updateSelection();
        return;
    }
    shuffle(questions);
    questions = questions.slice(0, ROUND_LIMIT).map((question) => {
        const options = [...question.options];
        shuffle(options);
        return { ...question, options };
    });
    index = 0;
    score = 0;
    right = 0;
    wrong = 0;
    el('feedback').textContent = '';
    el('feedback').className = 'feedback';
    show(roundView);
    render();
}

function render() {
    const question = questions[index];
    el('progress').textContent = `Question ${index + 1} of ${questions.length}`;
    el('score').textContent = `Score: ${score}`;
    el('question').textContent = question.text;

    const track = el('progress-track');
    track.setAttribute('aria-valuemax', String(questions.length));
    track.setAttribute('aria-valuenow', String(index + 1));
    el('progress-fill').style.width = `${((index + 1) / questions.length) * 100}%`;

    const options = el('options');
    options.replaceChildren();
    question.options.forEach((option) => {
        const label = document.createElement('label');
        const input = document.createElement('input');
        input.type = 'radio';
        input.name = 'answer';
        input.value = option;
        input.required = true;
        const text = document.createElement('span');
        // textContent, not innerHTML: the questions come from a file anyone can edit.
        text.textContent = option;
        label.append(input, text);
        options.append(label);
    });
    options.querySelector('input').focus();
}

function submit(event) {
    event.preventDefault();
    const chosen = roundView.querySelector('input[name="answer"]:checked');
    if (!chosen) {
        return;
    }

    const question = questions[index];
    const correct = chosen.value.toLowerCase() === question.answer.toLowerCase();
    const feedback = el('feedback');

    if (correct) {
        score += POINTS_PER_CORRECT_ANSWER;
        right += 1;
        feedback.textContent = 'Correct';
        feedback.className = 'feedback right';
    } else {
        wrong += 1;
        feedback.textContent = `Not quite. The answer was ${question.answer}.`;
        feedback.className = 'feedback wrong';
    }

    index += 1;
    if (index < questions.length) {
        render();
    } else {
        finish();
    }
}

function finish() {
    el('final-score').textContent = score;
    el('tally-right').textContent = right;
    el('tally-wrong').textContent = wrong;
    const answered = right + wrong;
    el('result-ring').style.setProperty('--result-share',
        `${answered > 0 ? (right / answered) * 100 : 0}%`);
    show(resultView);
    el('result-heading').focus();
}

async function load() {
    try {
        const response = await fetch('questions.json');
        if (!response.ok) {
            throw new Error(`questions.json responded ${response.status}`);
        }
        bank = parse(await response.json());
        if (bank.length === 0) {
            throw new Error('questions.json has no questions in it');
        }
        el('intro').textContent =
            `Choose a category and difficulty from ${bank.length} questions. Play up to five, ${POINTS_PER_CORRECT_ANSWER} points each. A wrong answer costs nothing. Questions never repeat within a round.`;
        for (const category of [...new Set(bank.map((question) => question.category))].sort()) {
            const option = document.createElement('option');
            option.value = category;
            option.textContent = category;
            el('category').append(option);
        }
        el('category').disabled = false;
        el('difficulty').disabled = false;
        updateSelection();
        el('begin').focus();
    } catch (error) {
        el('intro').textContent = `Could not load the questions: ${error.message}`;
    }
}

el('begin').addEventListener('click', startGame);
el('again').addEventListener('click', () => {
    show(startView);
    updateSelection();
    el('category').focus();
});
el('category').addEventListener('change', updateSelection);
el('difficulty').addEventListener('change', updateSelection);
el('answer-form').addEventListener('submit', submit);
load();
