package com.example.authquiz.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.authquiz.model.Student;
import com.example.authquiz.repository.StudentRepository;

@Service
public class StudentService {
    @Autowired
    private StudentRepository repo;

    public Student save(Student student) {
        return repo.save(student);
    }

    public Student findByUsername(String username) {
        return repo.findByUsername(username);
    }

    public Student findByEmail(String email) {
        return repo.findByEmail(email);
    }

    public List<Student> findAll() {
        return repo.findAll();
    }

    public long count() {
        return repo.count();
    }

    public Student findById(String id) {
        return repo.findById(id).orElse(null);
    }

    public Student update(Student updatedStudent) {
        // First get the existing student
        Student existingStudent = repo.findById(updatedStudent.getId()).orElse(null);
        if (existingStudent == null) {
            return null;
        }

        // Only update fields that are provided (not null)
        if (updatedStudent.getUsername() != null && !updatedStudent.getUsername().trim().isEmpty()) {
            existingStudent.setUsername(updatedStudent.getUsername());
        }

        if (updatedStudent.getPassword() != null && !updatedStudent.getPassword().trim().isEmpty()) {
            existingStudent.setPassword(updatedStudent.getPassword());
        }

        if (updatedStudent.getEmail() != null && !updatedStudent.getEmail().trim().isEmpty()) {
            existingStudent.setEmail(updatedStudent.getEmail());
        }

        if (updatedStudent.getStatus() != null && !updatedStudent.getStatus().trim().isEmpty()) {
            existingStudent.setStatus(updatedStudent.getStatus());
        }

        if (updatedStudent.getRole() != null && !updatedStudent.getRole().trim().isEmpty()) {
            existingStudent.setRole(updatedStudent.getRole());
        }

        if (updatedStudent.getIsBlocked() != null) {
            existingStudent.setIsBlocked(updatedStudent.getIsBlocked());
        }

        if (updatedStudent.getLastLoginTime() != null) {
            existingStudent.setLastLoginTime(updatedStudent.getLastLoginTime());
        }

        if (updatedStudent.getLastActivityTime() != null) {
            existingStudent.setLastActivityTime(updatedStudent.getLastActivityTime());
        }

        if (updatedStudent.getOnlineStatus() != null && !updatedStudent.getOnlineStatus().trim().isEmpty()) {
            existingStudent.setOnlineStatus(updatedStudent.getOnlineStatus());
        }

        return repo.save(existingStudent);
    }

    public void deleteById(String id) {
        repo.deleteById(id);
    }

    public boolean existsById(String id) {
        return repo.existsById(id);
    }
}
