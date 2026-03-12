package com.example.questionbank.repository;

import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import com.example.questionbank.model.Question;

public interface QuestionRepository extends MongoRepository<Question, String> {
    List<Question> findByQuizId(String quizId);

    @Query(value = "{ 'hostUsername': ?0 }", fields = "{ 'quizId': 1 }")
    List<Question> findDistinctQuizIdsByHostUsername(String hostUsername);

    @Query(value = "{ 'quizId': { $ne: null } }", fields = "{ 'quizId': 1 }")
    List<Question> findDistinctQuizIds();
}
