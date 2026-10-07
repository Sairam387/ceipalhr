package com.smarthr.service;

import com.smarthr.entity.User;
import com.smarthr.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public User createUser(User user) {

        if (user == null) {
            throw new IllegalArgumentException("User data is required");
        }

        if (user.getUsername() == null ||
                user.getUsername().isBlank()) {

            throw new IllegalArgumentException(
                    "Username is required"
            );
        }

        if (user.getPassword() == null ||
                user.getPassword().isBlank()) {

            throw new IllegalArgumentException(
                    "Password is required"
            );
        }

        if (userRepository.existsByUsernameIgnoreCase(
                user.getUsername())) {

            throw new IllegalArgumentException(
                    "Username already exists"
            );
        }

        user.setUsername(
                user.getUsername().trim()
        );

        user.setRole(
                normalizeRole(user.getRole())
        );

        if (user.getActive() == null) {
            user.setActive(true);
        }

        return userRepository.save(user);
    }

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public User getUserById(Long id) {

        return userRepository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException(
                                "User not found: " + id
                        )
                );
    }

    public User getUserByUsername(String username) {

        return userRepository
                .findByUsernameIgnoreCase(username)
                .orElseThrow(() ->
                        new RuntimeException(
                                "User not found: " + username
                        )
                );
    }

    public List<User> getUsersByRole(String role) {

        return userRepository
                .findByRoleIgnoreCase(
                        normalizeRole(role)
                );
    }

    public User updateUser(Long id, User request) {

        User existing = getUserById(id);

        if (request.getUsername() != null &&
                !request.getUsername().isBlank() &&
                !request.getUsername()
                        .equalsIgnoreCase(existing.getUsername())) {

            if (userRepository.existsByUsernameIgnoreCase(
                    request.getUsername())) {

                throw new IllegalArgumentException(
                        "Username already exists"
                );
            }

            existing.setUsername(
                    request.getUsername().trim()
            );
        }

        if (request.getPassword() != null &&
                !request.getPassword().isBlank()) {

            existing.setPassword(
                    request.getPassword()
            );
        }

        if (request.getRole() != null &&
                !request.getRole().isBlank()) {

            existing.setRole(
                    normalizeRole(request.getRole())
            );
        }

        if (request.getEmployeeId() != null) {
            existing.setEmployeeId(
                    request.getEmployeeId()
            );
        }

        if (request.getFullName() != null) {
            existing.setFullName(
                    request.getFullName()
            );
        }

        if (request.getEmail() != null) {
            existing.setEmail(
                    request.getEmail()
            );
        }

        if (request.getActive() != null) {
            existing.setActive(
                    request.getActive()
            );
        }

        return userRepository.save(existing);
    }

    public void deleteUser(Long id) {

        if (!userRepository.existsById(id)) {

            throw new RuntimeException(
                    "User not found: " + id
            );
        }

        userRepository.deleteById(id);
    }

    public LoginResult login(
            String username,
            String password) {

        if (username == null ||
                username.isBlank()) {

            throw new IllegalArgumentException(
                    "Username is required"
            );
        }

        if (password == null ||
                password.isBlank()) {

            throw new IllegalArgumentException(
                    "Password is required"
            );
        }

        User user =
                userRepository
                        .findByUsernameIgnoreCase(
                                username.trim()
                        )
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "Invalid username or password"
                                )
                        );

        if (!Boolean.TRUE.equals(user.getActive())) {

            throw new IllegalArgumentException(
                    "User account is inactive"
            );
        }

        /*
         * Temporary Phase 1 authentication.
         *
         * We will replace this plain-text comparison
         * with BCrypt/Spring Security in the next phase.
         */

        if (!password.equals(user.getPassword())) {

            throw new IllegalArgumentException(
                    "Invalid username or password"
            );
        }

        return new LoginResult(
                true,
                user.getId(),
                user.getUsername(),
                user.getFullName(),
                user.getRole(),
                user.getEmployeeId()
        );
    }

    private String normalizeRole(String role) {

        if (role == null ||
                role.isBlank()) {

            return "EMPLOYEE";
        }

        String normalized =
                role.trim().toUpperCase();

        switch (normalized) {

            case "ADMIN":
            case "HR":
            case "IT_SUPPORT":
            case "EMPLOYEE":
                return normalized;

            default:
                throw new IllegalArgumentException(
                        "Invalid role. Allowed roles: " +
                        "ADMIN, HR, IT_SUPPORT, EMPLOYEE"
                );
        }
    }

    public static class LoginResult {

        private boolean success;
        private Long userId;
        private String username;
        private String fullName;
        private String role;
        private String employeeId;

        public LoginResult(
                boolean success,
                Long userId,
                String username,
                String fullName,
                String role,
                String employeeId) {

            this.success = success;
            this.userId = userId;
            this.username = username;
            this.fullName = fullName;
            this.role = role;
            this.employeeId = employeeId;
        }

        public boolean isSuccess() {
            return success;
        }

        public Long getUserId() {
            return userId;
        }

        public String getUsername() {
            return username;
        }

        public String getFullName() {
            return fullName;
        }

        public String getRole() {
            return role;
        }

        public String getEmployeeId() {
            return employeeId;
        }
    }
}