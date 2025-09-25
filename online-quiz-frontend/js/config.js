// Centralized API configuration for all services
// Change the base IP/host here to match your backend deployment

const API_HOST = window.API_HOST || window.location.hostname;
const AUTH_API_BASE = `http://${API_HOST}:8080/api/auth`;
const QUESTION_API_BASE = `http://${API_HOST}:8081/api/questions`;
const RESULT_API_BASE = `http://${API_HOST}:8082/api/results`;

// Optionally, expose these globally for all scripts
window.AUTH_API_BASE = AUTH_API_BASE;
window.QUESTION_API_BASE = QUESTION_API_BASE;
window.RESULT_API_BASE = RESULT_API_BASE;
