package com.example.questionbank.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.questionbank.model.Question;
import com.example.questionbank.repository.QuestionRepository;

@Service
public class QuestionService {

    @Autowired
    private QuestionRepository repository;

    public List<Question> getAllQuestions() {
        return repository.findAll();
    }

    public Question addQuestion(Question question) {
        return repository.save(question);
    }

    public Question getQuestionById(String id) {
        return repository.findById(id).orElse(null);
    }

    public Question updateQuestion(String id, Question updated) {
        if (repository.existsById(id)) {
            updated.setId(id); // Ensure ID stays the same
            return repository.save(updated);
        }
        return null;
    }

    public boolean deleteQuestion(String id) {
        if (repository.existsById(id)) {
            repository.deleteById(id);
            return true;
        }
        return false;
    }

    public List<Question> getQuestionsByQuizId(String quizId) {
        return repository.findByQuizId(quizId);
    }

    public List<String> getDistinctQuizIdsByHostUsername(String hostUsername) {
        List<Question> questions = repository.findDistinctQuizIdsByHostUsername(hostUsername);
        return questions.stream()
                .map(Question::getQuizId)
                .distinct()
                .collect(java.util.stream.Collectors.toList());
    }

    public List<String> getDistinctQuizIds() {
        List<Question> questions = repository.findDistinctQuizIds();
        return questions.stream()
                .map(Question::getQuizId)
                .distinct()
                .collect(java.util.stream.Collectors.toList());
    }

}
