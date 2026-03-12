package com.example.authquiz.repository;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.example.authquiz.model.Student;

public interface StudentRepository extends MongoRepository<Student, String> {
    Student findByUsername(String username);

    Student findByEmail(String email);
}
