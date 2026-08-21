/*
 * The game loop from GameService, in the browser.
 *
 * Keep the two in step: ten points for a correct answer, nothing subtracted for a wrong one,
 * answers compared ignoring case. If GameService changes, change this.
 */
const POINTS_PER_CORRECT_ANSWER = 10;

const el = (id) => document.getElementById(id);
const startView = el('start');
const roundView = el('round');
const resultView = el('result');

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
    }));
}

function show(view) {
    for (const section of [startView, roundView, resultView]) {
        section.hidden = section !== view;
    }
}

function startGame() {
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

    const options = el('options');
    options.replaceChildren();
    question.options.forEach((option, i) => {
        const label = document.createElement('label');
        const input = document.createElement('input');
        input.type = 'radio';
        input.name = 'answer';
        input.value = option;
        input.required = true;
        if (i === 0) {
            input.autofocus = true;
        }
        const text = document.createElement('span');
        // textContent, not innerHTML: the questions come from a file anyone can edit.
        text.textContent = option;
        label.append(input, text);
        options.append(label);
    });
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
        feedback.textContent = `Not quite — the answer was ${question.answer}`;
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
    el('tally').textContent = `${right} right, ${wrong} wrong`;
    show(resultView);
    el('again').focus();
}

async function load() {
    try {
        const response = await fetch('questions.json');
        if (!response.ok) {
            throw new Error(`questions.json responded ${response.status}`);
        }
        questions = parse(await response.json());
        if (questions.length === 0) {
            throw new Error('questions.json has no questions in it');
        }
        el('intro').textContent =
            `${questions.length} questions, ${POINTS_PER_CORRECT_ANSWER} points each. A wrong answer costs nothing.`;
        el('begin').disabled = false;
        el('begin').focus();
    } catch (error) {
        el('intro').textContent = `Could not load the questions: ${error.message}`;
    }
}

el('begin').addEventListener('click', startGame);
el('again').addEventListener('click', startGame);
el('answer-form').addEventListener('submit', submit);
load();
