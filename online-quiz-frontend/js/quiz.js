const API_BASE = window.QUESTION_API_BASE;
const token = localStorage.getItem("token");

// Try to get quiz ID from localStorage first, then from URL parameter
let quizId = localStorage.getItem("currentQuizId");

// If not in localStorage, check URL parameters
if (!quizId) {
    const urlParams = new URLSearchParams(window.location.search);
    quizId = urlParams.get('quizId');
    // If found in URL, store it in localStorage for consistency
    if (quizId) {
        localStorage.setItem("currentQuizId", quizId);
    }
}

console.log('Quiz ID for this session:', quizId);

if (!token || !quizId) {
    console.error('Missing token or quizId:', { token: !!token, quizId });
    alert('Missing authentication token or quiz ID. Redirecting to login.');
    window.location.href = "index.html";
}

let questions = [];
let currentQuestion = 0;
const userAnswers = {};

const headers = {
    "Content-Type": "application/json",
    "Authorization": `Bearer ${token}`,
};


async function loadQuestions() {
    // Try to extract username from JWT (if present)
    let studentUsername = "anonymous";
    try {
        const tokenPayload = JSON.parse(atob(token.split('.')[1]));
        if (tokenPayload && tokenPayload.sub) {
            studentUsername = tokenPayload.sub;
        }
    } catch (e) { }

    // Check if user has already attempted this quiz
    try {
        const attemptRes = await fetch(`${window.RESULT_API_BASE}/attempted/${quizId}/${studentUsername}`, { headers });
        if (attemptRes.ok) {
            const attempted = await attemptRes.json();
            if (attempted) {
                document.getElementById("question-container").innerHTML = `<p>You have already attempted this quiz. Re-attempt is not allowed.</p>`;
                document.getElementById("prev-btn").style.display = "none";
                document.getElementById("next-btn").style.display = "none";
                document.getElementById("submit-btn").style.display = "none";
                return;
            }
        }
    } catch (e) { /* ignore, allow attempt if check fails */ }

    try {
        const res = await fetch(`${API_BASE}/quiz/${quizId}`, { headers });
        if (!res.ok) throw new Error("Failed to load questions");
        questions = await res.json();
        if (!questions || questions.length === 0) {
            document.getElementById("question-container").innerHTML = `<p>No questions available for this quiz.</p>`;
            document.getElementById("prev-btn").style.display = "none";
            document.getElementById("next-btn").style.display = "none";
            document.getElementById("submit-btn").style.display = "none";
            return;
        }
        showQuestion(currentQuestion);
    } catch (err) {
        document.getElementById("question-container").innerHTML = `<p>${err.message}</p>`;
    }
}

function showQuestion(index) {
    const q = questions[index];
    const container = document.getElementById("question-container");
    container.innerHTML = `
        <div class="quiz-question-card">
            <div class="quiz-question-header">Question ${index + 1}</div>
            <div class="quiz-question-content">
                <h3 class="quiz-question-text">${q.questionText}</h3>
                <div class="quiz-options-container">
                    ${[
            { key: "optionA", label: q.optionA, letter: "A" },
            { key: "optionB", label: q.optionB, letter: "B" },
            { key: "optionC", label: q.optionC, letter: "C" },
            { key: "optionD", label: q.optionD, letter: "D" }
        ].map(opt => `
                        <label class="quiz-option">
                            <input type="radio" name="option" value="${opt.letter}" data-option-text="${opt.label}" class="quiz-option-radio" ${userAnswers[q.id] === opt.letter ? 'checked' : ''}>
                            <span class="quiz-option-text">${opt.label}</span>
                        </label>
                    `).join("")}
                </div>
            </div>
            <div class="quiz-navigation">
                <button id="prev-btn" class="quiz-nav-btn" style="display:${index > 0 ? 'inline-block' : 'none'};">Previous</button>
                <button id="next-btn" class="quiz-nav-btn" style="display:${index < questions.length - 1 ? 'inline-block' : 'none'};">Next</button>
                <button id="submit-btn" style="display:${index === questions.length - 1 ? 'inline-block' : 'none'};" class="quiz-submit-btn">Submit</button>
            </div>
        </div>
    `;

    // Event listener to capture selection - now stores the letter (A, B, C, D)
    document.querySelectorAll('input[name="option"]').forEach((radio) => {
        radio.addEventListener("change", (e) => {
            userAnswers[q.id] = e.target.value; // This is now the letter (A, B, C, D)
            console.log(`Question ${q.id}: Selected answer ${e.target.value}`);
            
            // Update visual selection state
            document.querySelectorAll('.quiz-option').forEach(option => {
                option.classList.remove('selected');
            });
            e.target.closest('.quiz-option').classList.add('selected');
        });
        
        // Set initial selected state if answer exists
        if (userAnswers[q.id] === radio.value) {
            radio.closest('.quiz-option').classList.add('selected');
        }
    });

    // Attach navigation button listeners after rendering
    const prevBtn = document.getElementById("prev-btn");
    const nextBtn = document.getElementById("next-btn");
    const submitBtn = document.getElementById("submit-btn");

    if (prevBtn) {
        prevBtn.onclick = () => {
            if (currentQuestion > 0) {
                currentQuestion--;
                showQuestion(currentQuestion);
            }
        };
    }
    if (nextBtn) {
        nextBtn.onclick = () => {
            if (currentQuestion < questions.length - 1) {
                currentQuestion++;
                showQuestion(currentQuestion);
            }
        };
    }
    if (submitBtn) {
        submitBtn.onclick = async () => {
            try {
                const answersArray = Object.keys(userAnswers).map((questionId) => ({
                    questionId,
                    selectedAnswer: userAnswers[questionId],
                }));
                // Try to extract username from JWT (if present)
                let studentUsername = "anonymous";
                try {
                    const tokenPayload = JSON.parse(atob(token.split('.')[1]));
                    if (tokenPayload && tokenPayload.sub) {
                        studentUsername = tokenPayload.sub;
                    }
                } catch (e) { }
                const resultRes = await fetch(`${window.RESULT_API_BASE}/submit`, {
                    method: "POST",
                    headers,
                    body: JSON.stringify({
                        quizId,
                        answers: answersArray,
                        studentUsername
                    }),
                });
                if (!resultRes.ok) throw new Error("Failed to submit quiz");

                const result = await resultRes.json();
                console.log("Quiz submission result:", result);
                console.log("Score received:", result.score);
                console.log("Correct answers:", result.correctAnswers);
                console.log("Total questions:", result.totalQuestions);
                localStorage.setItem("lastResult", JSON.stringify(result));
                window.location.href = "result.html";
            } catch (err) {
                alert(err.message);
            }
        };
    }

    document.getElementById("prev-btn").style.display = index > 0 ? "inline-block" : "none";
    document.getElementById("next-btn").style.display = index < questions.length - 1 ? "inline-block" : "none";
    document.getElementById("submit-btn").style.display = index === questions.length - 1 ? "inline-block" : "none";
}

loadQuestions();
