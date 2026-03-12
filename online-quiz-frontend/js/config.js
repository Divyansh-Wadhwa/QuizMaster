// Centralized API configuration for all services
// Automatically detects environment and uses appropriate URLs

const isDevelopment = window.location.hostname === 'localhost' || 
                     window.location.hostname === '127.0.0.1' ||
                     window.location.port !== '';

let AUTH_API_BASE, QUESTION_API_BASE, RESULT_API_BASE;

if (isDevelopment) {
    // Local development URLs
    const API_HOST = window.API_HOST || 'localhost';
    AUTH_API_BASE = `http://${API_HOST}:8080/api/auth`;
    QUESTION_API_BASE = `http://${API_HOST}:8081/api/questions`;
    RESULT_API_BASE = `http://${API_HOST}:8082/api/results`;
    console.log('🔧 Running in DEVELOPMENT mode');
} else {
    // Production URLs - Use environment variables or defaults
    AUTH_API_BASE = window.PRODUCTION_AUTH_API || 'https://quizmaster-auth.onrender.com/api/auth';
    QUESTION_API_BASE = window.PRODUCTION_QUESTION_API || 'https://quizmaster-0z97.onrender.com/api/questions';
    RESULT_API_BASE = window.PRODUCTION_RESULT_API || 'https://quiz-result-service.onrender.com/api/results';
    
    // Remove console logs in production
    
}

// Create API service object to hide direct URLs
const API = {
    auth: {
        login: `${AUTH_API_BASE}/login`,
        register: `${AUTH_API_BASE}/register`,
        getUsers: `${AUTH_API_BASE}/admin/users`
    },
    questions: {
        getAll: `${QUESTION_API_BASE}/quiz/all`,
        getById: `${QUESTION_API_BASE}/quiz`,
        add: `${QUESTION_API_BASE}/add`
    },
    results: {
        submit: `${RESULT_API_BASE}/submit`,
        getQuizResults: `${RESULT_API_BASE}/quiz`,
        getResult: `${RESULT_API_BASE}`
    }
};

// Expose only the API object, not individual URLs
window.API = API;
