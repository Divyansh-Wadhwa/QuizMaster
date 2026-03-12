package com.example.result.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import com.example.result.model.Result;

public interface ResultRepository extends MongoRepository<Result, String> {
	java.util.List<Result> findByQuizId(String quizId);

	boolean existsByQuizIdAndStudentUsername(String quizId, String studentUsername);

	void deleteByQuizId(String quizId);

	void deleteByQuizIdAndStudentUsername(String quizId, String studentUsername);
}
