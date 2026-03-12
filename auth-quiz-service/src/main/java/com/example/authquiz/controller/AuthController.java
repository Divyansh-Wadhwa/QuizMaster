package com.example.authquiz.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import com.example.authquiz.model.Student;
import com.example.authquiz.security.JwtUtil;
import com.example.authquiz.service.StudentService;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private StudentService service;

    @Autowired
    private JwtUtil jwtUtil;

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody Student student) {
        try {
            // Check if username already exists
            if (service.findByUsername(student.getUsername()) != null) {
                return ResponseEntity.status(409)
                        .body(java.util.Collections.singletonMap("error", "Username already exists"));
            }

            // Check if email already exists
            if (service.findByEmail(student.getEmail()) != null) {
                return ResponseEntity.status(409)
                        .body(java.util.Collections.singletonMap("error", "Email already exists"));
            }

            Student saved = service.save(student);
            return ResponseEntity.ok(java.util.Collections.singletonMap("message", "Registration successful"));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(java.util.Collections.singletonMap("error", "Registration failed"));
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Student student) {
        Student dbStudent = service.findByUsername(student.getUsername());
        if (dbStudent != null && dbStudent.getPassword() != null
                && dbStudent.getPassword().equals(student.getPassword())) {
            
            // Check if user is blocked
            if (dbStudent.getIsBlocked() != null && dbStudent.getIsBlocked()) {
                return ResponseEntity.status(403)
                        .body(java.util.Collections.singletonMap("error", "Your account has been blocked. Please contact administrator."));
            }
            
            // Check status field as well
            if ("blocked".equalsIgnoreCase(dbStudent.getStatus())) {
                return ResponseEntity.status(403)
                        .body(java.util.Collections.singletonMap("error", "Your account has been blocked. Please contact administrator."));
            }
            
            // Set user as online and update timestamps
            long currentTime = System.currentTimeMillis();
            dbStudent.setOnlineStatus("online");
            dbStudent.setLastLoginTime(currentTime);
            dbStudent.setLastActivityTime(currentTime);
            service.update(dbStudent);
            
            String token = jwtUtil.generateToken(dbStudent.getUsername());
            return ResponseEntity.ok(java.util.Collections.singletonMap("token", token));
        }
        return ResponseEntity.status(401).body(java.util.Collections.singletonMap("error", "Invalid credentials"));
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(@RequestBody Map<String, String> request) {
        try {
            String username = request.get("username");
            System.out.println("[LOGOUT] Received logout request for user: " + username);
            
            if (username != null) {
                Student student = service.findByUsername(username);
                if (student != null) {
                    System.out.println("[LOGOUT] Found user: " + username + ", setting offline");
                    student.setOnlineStatus("offline");
                    student.setLastActivityTime(0L);
                    Student updated = service.update(student);
                    System.out.println("[LOGOUT] User updated, onlineStatus: " + updated.getOnlineStatus());
                } else {
                    System.out.println("[LOGOUT] User not found: " + username);
                }
            } else {
                System.out.println("[LOGOUT] No username provided in request");
            }
            return ResponseEntity.ok(java.util.Collections.singletonMap("message", "Logged out successfully"));
        } catch (Exception e) {
            System.out.println("[LOGOUT] Error: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.ok(java.util.Collections.singletonMap("message", "Logged out"));
        }
    }

    @PostMapping("/heartbeat")
    public ResponseEntity<?> heartbeat(@RequestBody Map<String, String> request) {
        try {
            String username = request.get("username");
            if (username != null) {
                Student student = service.findByUsername(username);
                if (student != null) {
                    long currentTime = System.currentTimeMillis();
                    student.setLastActivityTime(currentTime);
                    student.setOnlineStatus("online");
                    service.update(student);
                    return ResponseEntity.ok(java.util.Collections.singletonMap("status", "online"));
                }
            }
            return ResponseEntity.status(404).body(java.util.Collections.singletonMap("error", "User not found"));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(java.util.Collections.singletonMap("error", "Heartbeat failed"));
        }
    }

    // Admin endpoints for user management
    @GetMapping("/admin/users")
    public ResponseEntity<?> getAllUsers() {
        try {
            List<Student> users = service.findAll();
            long currentTime = System.currentTimeMillis();
            long onlineThreshold = 15000; // 15 seconds
            long inactiveThreshold = 300000; // 5 minutes
            
            // Calculate status based on last activity
            for (Student user : users) {
                Long lastActivity = user.getLastActivityTime();
                // If lastActivityTime is explicitly 0, user has logged out - keep offline
                if (lastActivity != null && lastActivity == 0L) {
                    user.setOnlineStatus("offline");
                } else if (lastActivity != null && lastActivity > 0) {
                    long timeSinceActivity = currentTime - lastActivity;
                    if (timeSinceActivity < onlineThreshold) {
                        user.setOnlineStatus("online");
                    } else if (timeSinceActivity < inactiveThreshold) {
                        user.setOnlineStatus("inactive");
                    } else {
                        user.setOnlineStatus("offline");
                    }
                } else {
                    user.setOnlineStatus("offline");
                }
            }
            
            return ResponseEntity.ok(users);
        } catch (Exception e) {
            return ResponseEntity.status(500)
                    .body(java.util.Collections.singletonMap("error", "Failed to fetch users"));
        }
    }

    @GetMapping("/admin/users/count")
    public ResponseEntity<?> getUserCount() {
        try {
            Map<String, Object> response = new HashMap<>();
            response.put("count", service.count());
            response.put("total", service.count());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(500)
                    .body(java.util.Collections.singletonMap("error", "Failed to get user count"));
        }
    }

    // Get specific user by ID
    @GetMapping("/admin/users/{id}")
    public ResponseEntity<?> getUserById(@PathVariable String id) {
        try {
            Student user = service.findById(id);
            if (user != null) {
                return ResponseEntity.ok(user);
            } else {
                return ResponseEntity.status(404).body(java.util.Collections.singletonMap("error", "User not found"));
            }
        } catch (Exception e) {
            return ResponseEntity.status(500).body(java.util.Collections.singletonMap("error", "Failed to fetch user"));
        }
    }

    // Update user
    @PutMapping("/admin/users/{id}")
    public ResponseEntity<?> updateUser(@PathVariable String id, @RequestBody Student updatedUser) {
        try {
            Student existingUser = service.findById(id);
            if (existingUser == null) {
                return ResponseEntity.status(404).body(java.util.Collections.singletonMap("error", "User not found"));
            }

            // Protect admin account from modifications
            if ("admin".equalsIgnoreCase(existingUser.getUsername())) {
                return ResponseEntity.status(403).body(java.util.Collections.singletonMap("error", "Cannot modify the admin account"));
            }

            updatedUser.setId(id); // Ensure the ID is set
            Student savedUser = service.update(updatedUser);
            return ResponseEntity.ok(savedUser);
        } catch (Exception e) {
            return ResponseEntity.status(500)
                    .body(java.util.Collections.singletonMap("error", "Failed to update user"));
        }
    }

    // Delete user
    @DeleteMapping("/admin/users/{id}")
    public ResponseEntity<?> deleteUser(@PathVariable String id) {
        try {
            Student user = service.findById(id);
            if (user == null) {
                return ResponseEntity.status(404).body(java.util.Collections.singletonMap("error", "User not found"));
            }

            // Protect admin account from deletion
            if ("admin".equalsIgnoreCase(user.getUsername())) {
                return ResponseEntity.status(403).body(java.util.Collections.singletonMap("error", "Cannot delete the admin account"));
            }

            service.deleteById(id);
            return ResponseEntity.ok(java.util.Collections.singletonMap("message", "User deleted successfully"));
        } catch (Exception e) {
            return ResponseEntity.status(500)
                    .body(java.util.Collections.singletonMap("error", "Failed to delete user"));
        }
    }

    // Toggle user status (block/unblock)
    @PutMapping("/admin/users/{id}/toggle-status")
    public ResponseEntity<?> toggleUserStatus(@PathVariable String id) {
        try {
            Student user = service.findById(id);
            if (user == null) {
                return ResponseEntity.status(404).body(java.util.Collections.singletonMap("error", "User not found"));
            }

            // Protect admin account from being blocked
            if ("admin".equalsIgnoreCase(user.getUsername())) {
                return ResponseEntity.status(403).body(java.util.Collections.singletonMap("error", "Cannot block the admin account"));
            }

            // Toggle blocked status
            boolean currentlyBlocked = user.getIsBlocked() != null && user.getIsBlocked();
            user.setIsBlocked(!currentlyBlocked);
            user.setStatus(currentlyBlocked ? "active" : "blocked");
            
            service.update(user);
            
            Map<String, Object> response = new HashMap<>();
            response.put("message", "User status updated");
            response.put("isBlocked", user.getIsBlocked());
            response.put("status", user.getStatus());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(500)
                    .body(java.util.Collections.singletonMap("error", "Failed to update user status"));
        }
    }

    // Promote user to admin
    @PutMapping("/admin/users/{id}/promote")
    public ResponseEntity<?> promoteToAdmin(@PathVariable String id) {
        try {
            Student user = service.findById(id);
            if (user == null) {
                return ResponseEntity.status(404).body(java.util.Collections.singletonMap("error", "User not found"));
            }

            user.setRole("admin");
            service.update(user);
            
            return ResponseEntity.ok(java.util.Collections.singletonMap("message", "User promoted to admin"));
        } catch (Exception e) {
            return ResponseEntity.status(500)
                    .body(java.util.Collections.singletonMap("error", "Failed to promote user"));
        }
    }

    // Demote admin to user
    @PutMapping("/admin/users/{id}/demote")
    public ResponseEntity<?> demoteFromAdmin(@PathVariable String id) {
        try {
            Student user = service.findById(id);
            if (user == null) {
                return ResponseEntity.status(404).body(java.util.Collections.singletonMap("error", "User not found"));
            }

            // Protect admin account from being demoted
            if ("admin".equalsIgnoreCase(user.getUsername())) {
                return ResponseEntity.status(403).body(java.util.Collections.singletonMap("error", "Cannot demote the admin account"));
            }

            user.setRole("user");
            service.update(user);
            
            return ResponseEntity.ok(java.util.Collections.singletonMap("message", "Admin privileges removed"));
        } catch (Exception e) {
            return ResponseEntity.status(500)
                    .body(java.util.Collections.singletonMap("error", "Failed to demote user"));
        }
    }

    }
