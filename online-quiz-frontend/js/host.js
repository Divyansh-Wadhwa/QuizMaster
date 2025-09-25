
let questionCount = 0;
// Use the new utility for pre-filled blocks
// createQuestionBlockWithData is imported from host-utils.js
function createQuestionBlock(idx) {
    return createQuestionBlockWithData(idx, {});
}

document.addEventListener("DOMContentLoaded", function () {
    const quizIdSection = document.getElementById("quiz-id-section");
    const startQuestionsBtn = document.getElementById("start-questions-btn");
    const quizForm = document.getElementById("quiz-form");
    const questionsSection = document.getElementById("questions-section");
    const addBtn = document.getElementById("add-question-btn");
    let quizId = "";


    startQuestionsBtn.onclick = function () {
        quizId = document.getElementById("quiz-id").value.trim();
        if (!quizId) {
            alert("Please enter a quiz ID.");
            return;
        }
        window.location.href = `admin.html?quizId=${encodeURIComponent(quizId)}`;
    };


    function addQuestion() {
        // Remove 'no questions' message if present
        const msg = questionsSection.querySelector('p');
        if (msg) msg.remove();
        questionCount++;
        questionsSection.insertAdjacentHTML('beforeend', createQuestionBlock(questionCount));
    }
    addBtn.onclick = addQuestion;

    questionsSection.addEventListener('click', function (e) {
        if (e.target.classList.contains('remove-question-btn')) {
            e.target.closest('.question-block').remove();
        }
    });

    quizForm.onsubmit = async function (e) {
        e.preventDefault();
        const questionBlocks = document.querySelectorAll('.question-block');
        if (!quizId || questionBlocks.length === 0) {
            alert("Quiz ID and at least one question are required.");
            return;
        }
        const questions = [];
        for (const block of questionBlocks) {
            const questionText = block.querySelector('.question-text').value.trim();
            const optionA = block.querySelector('.optionA').value.trim();
            const optionB = block.querySelector('.optionB').value.trim();
            const optionC = block.querySelector('.optionC').value.trim();
            const optionD = block.querySelector('.optionD').value.trim();
            const correct = block.querySelector('.correct-answer').value;
            if (!questionText || !optionA || !optionB || !optionC || !optionD) {
                alert("All fields are required for each question.");
                return;
            }
            questions.push({
                questionText,
                optionA,
                optionB,
                optionC,
                optionD,
                correctAnswer: {
                    A: optionA,
                    B: optionB,
                    C: optionC,
                    D: optionD
                }[correct],
                quizId
            });
        }
        // Submit each question to backend
        let success = true;
        for (const q of questions) {
            const res = await fetch(`${window.QUESTION_API_BASE}/add`, {
                method: "POST",
                headers: { "Content-Type": "application/json" },
                body: JSON.stringify(q)
            });
            if (!res.ok) success = false;
        }
        if (success) {
            alert("Quiz hosted successfully!");
            window.location.href = `admin.html?quizId=${encodeURIComponent(quizId)}`;
        } else {
            alert("Some questions could not be added. Please try again.");
        }
    };
});

// Import the utility for pre-filled question blocks
// <script src="js/host-utils.js"></script> should be added to host.html before host.js
