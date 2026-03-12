package com.example.questionbank.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import com.example.questionbank.dto.QuizDto;
import com.example.questionbank.model.Question;
import com.example.questionbank.repository.QuestionRepository;
import com.example.questionbank.service.QuestionService;
import com.example.questionbank.util.JwtUtil;

@RestController
@RequestMapping("/api/questions")
public class QuestionController {

    // Get all available quizzes (unique quizIds)
    @GetMapping("/quiz/all")
    public List<String> getAllQuizzes() {
        // Get all questions, extract unique quizIds
        List<Question> questions = service.getAllQuestions();
        return questions.stream()
                .map(Question::getQuizId)
                .filter(qid -> qid != null && !qid.isEmpty())
                .distinct()
                .collect(java.util.stream.Collectors.toList());
    }

    @Autowired
    private QuestionService service;

    @Autowired
    private QuestionRepository questionRepository;

    @Autowired
    private JwtUtil jwtUtil;

    @Value("${result.service.url:http://localhost:8082}")
    private String resultServiceUrl;
    private final RestTemplate restTemplate = new RestTemplate();

    // Get questions by quizId
    @GetMapping("/quiz/{quizId}")
    public List<Question> getQuestions(@PathVariable String quizId) {
        return service.getQuestionsByQuizId(quizId);
    }

    // Get question by ID
    @GetMapping("/{id}")
    public ResponseEntity<Question> getQuestionById(@PathVariable String id) {
        Question question = service.getQuestionById(id);
        if (question != null) {
            return ResponseEntity.ok(question);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    // Add new question
    @PostMapping("/add")
    public ResponseEntity<?> addQuestion(@RequestBody Question question,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        try {
            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                String token = authHeader.substring(7);
                String username = jwtUtil.extractUsername(token);
                question.setHostUsername(username);
            }
            Question saved = questionRepository.save(question);
            return ResponseEntity.ok(saved);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body("Error saving question: " + e.getMessage());
        }
    }

    // Update question
    @org.springframework.web.bind.annotation.PutMapping("/{id}")
    public ResponseEntity<Question> updateQuestion(@PathVariable String id, @RequestBody Question updated) {
        Question q = service.updateQuestion(id, updated);
        if (q != null) {
            return ResponseEntity.ok(q);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    // Delete question
    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteQuestion(@PathVariable String id) {
        boolean exists = service.deleteQuestion(id);
        if (exists) {
            return ResponseEntity.ok("Question deleted successfully");
        } else {
            return ResponseEntity.status(404).body("Question not found");
        }
    }

    // Get quizzes by host
    @GetMapping("/host/{username}")
    public ResponseEntity<?> getQuizzesByHost(@PathVariable String username) {
        List<String> quizIds = service.getDistinctQuizIdsByHostUsername(username);
        return ResponseEntity.ok(quizIds);
    }

    // Delete an entire quiz (all questions and results)
    @DeleteMapping("/quiz/{quizId}")
    public ResponseEntity<String> deleteQuiz(@PathVariable String quizId) {
        // Delete all questions for this quiz
        List<Question> questions = questionRepository.findByQuizId(quizId);
        try {
            questionRepository.deleteAll(questions);
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Failed to delete questions for quiz: " + e.getMessage());
        }

        // Call result-service to delete all results for this quiz
        try {
            String url = resultServiceUrl + "/api/results/quiz/" + quizId;
            restTemplate.delete(url);
        } catch (Exception e) {
            return ResponseEntity.status(500)
                    .body("Questions deleted, but failed to delete results: " + e.getMessage());
        }

        return ResponseEntity.ok("Quiz and all related results deleted successfully");
    }

    // Quiz metadata management endpoints

    // Get all quizzes with metadata
    @GetMapping("/quiz/metadata/all")
    public ResponseEntity<List<QuizDto>> getAllQuizzesWithMetadata() {
        try {
            List<String> quizIds = service.getDistinctQuizIds();
            List<QuizDto> quizzes = new java.util.ArrayList<>();

            for (String quizId : quizIds) {
                QuizDto quizDto = buildQuizMetadata(quizId);
                if (quizDto != null) {
                    quizzes.add(quizDto);
                }
            }

            return ResponseEntity.ok(quizzes);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(null);
        }
    }

    // Get quiz metadata by ID
    @GetMapping("/quiz/metadata/{quizId}")
    public ResponseEntity<QuizDto> getQuizMetadataById(@PathVariable String quizId) {
        try {
            QuizDto quizDto = buildQuizMetadata(quizId);
            if (quizDto != null) {
                return ResponseEntity.ok(quizDto);
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            return ResponseEntity.status(500).body(null);
        }
    }

    // Create quiz metadata (this creates a placeholder quiz entry)
    @PostMapping("/quiz/create")
    public ResponseEntity<?> createQuiz(@RequestBody QuizDto quizDto,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        try {
            // Generate a unique quiz ID if not provided
            String quizId = quizDto.getId();
            if (quizId == null || quizId.isEmpty()) {
                quizId = "quiz_" + UUID.randomUUID().toString().substring(0, 8);
            }

            // Extract username from token
            String username = "admin";
            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                String token = authHeader.substring(7);
                try {
                    username = jwtUtil.extractUsername(token);
                } catch (Exception e) {
                    // Use default username if token is invalid
                }
            }

            // Create a placeholder question for the quiz to establish the quiz metadata
            Question placeholderQuestion = new Question();
            placeholderQuestion.setQuizId(quizId);
            placeholderQuestion.setQuizName(quizDto.getTitle());
            placeholderQuestion.setHostUsername(username);
            placeholderQuestion.setQuestionText("Quiz created - add questions");
            placeholderQuestion.setOptionA("Placeholder");
            placeholderQuestion.setOptionB("Placeholder");
            placeholderQuestion.setOptionC("Placeholder");
            placeholderQuestion.setOptionD("Placeholder");
            placeholderQuestion.setCorrectAnswer("A");

            // Save the placeholder question
            questionRepository.save(placeholderQuestion);

            // Create response with quiz metadata
            Map<String, Object> response = new HashMap<>();
            response.put("quizId", quizId);
            response.put("title", quizDto.getTitle());
            response.put("creator", username);
            response.put("message", "Quiz created successfully. Add questions to complete the quiz.");

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(500)
                    .body(java.util.Collections.singletonMap("error", "Failed to create quiz: " + e.getMessage()));
        }
    }

    // Update quiz metadata
    @PutMapping("/quiz/update/{quizId}")
    public ResponseEntity<?> updateQuiz(@PathVariable String quizId, @RequestBody QuizDto quizDto) {
        try {
            // Update all questions for this quiz with new metadata
            List<Question> questions = questionRepository.findByQuizId(quizId);
            if (questions.isEmpty()) {
                return ResponseEntity.notFound().build();
            }

            for (Question question : questions) {
                question.setQuizName(quizDto.getTitle());
                // We could add more fields to Question model for description, category, etc. if
                // needed
            }

            questionRepository.saveAll(questions);

            Map<String, Object> response = new HashMap<>();
            response.put("quizId", quizId);
            response.put("title", quizDto.getTitle());
            response.put("message", "Quiz updated successfully");

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(500)
                    .body(java.util.Collections.singletonMap("error", "Failed to update quiz: " + e.getMessage()));
        }
    }

    // Helper method to build QuizDto from questions
    private QuizDto buildQuizMetadata(String quizId) {
        try {
            List<Question> questions = questionRepository.findByQuizId(quizId);
            if (questions.isEmpty()) {
                return null;
            }

            Question firstQuestion = questions.get(0);
            QuizDto quizDto = new QuizDto();
            quizDto.setId(quizId);
            quizDto.setTitle(firstQuestion.getQuizName() != null ? firstQuestion.getQuizName() : "Quiz " + quizId);
            quizDto.setCreator(firstQuestion.getHostUsername() != null ? firstQuestion.getHostUsername() : "Unknown");
            quizDto.setQuestionCount(questions.size());
            quizDto.setActive(true); // Default to active
            quizDto.setDescription(""); // Could be added to Question model
            quizDto.setCategory("General"); // Could be added to Question model
            quizDto.setDifficulty("medium"); // Could be added to Question model
            quizDto.setTimeLimit(30); // Could be added to Question model
            quizDto.setCreatedAt(java.time.LocalDateTime.now().toString()); // Could be added to Question model

            // Try to get participant count from results service
            try {
                String url = resultServiceUrl + "/api/results/quiz/" + quizId;
                @SuppressWarnings("unchecked")
                List<Object> results = restTemplate.getForObject(url, List.class);
                quizDto.setParticipants(results != null ? results.size() : 0);
            } catch (Exception e) {
                quizDto.setParticipants(0);
            }

            return quizDto;
        } catch (Exception e) {
            return null;
        }
    }

    @Configuration
    public class WebConfig implements WebMvcConfigurer {
        @Override
        public void addCorsMappings(CorsRegistry registry) {
            registry.addMapping("/**")
                    .allowedOrigins("http://127.0.0.1:3000", "http://localhost:5500", "http://127.0.0.1:5500")
                    .allowedMethods("GET", "POST", "PUT", "DELETE")
                    .allowedHeaders("*")
                    .allowCredentials(true);
        }
    }
}
