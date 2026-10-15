package com.rei.examenbackend.dto.auth;

public class AuthResponse {

    private Long userId;
    private String fullName;
    private String email;
    private String token;

    public AuthResponse() {}

    public AuthResponse(Long userId, String fullName, String email, String token) {
        this.userId = userId;
        this.fullName = fullName;
        this.email = email;
        this.token = token;
    }

    public static Builder builder() {
        return new Builder();
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public static class Builder {
        private Long userId;
        private String fullName;
        private String email;
        private String token;

        public Builder userId(Long userId) {
            this.userId = userId;
            return this;
        }

        public Builder fullName(String fullName) {
            this.fullName = fullName;
            return this;
        }

        public Builder email(String email) {
            this.email = email;
            return this;
        }

        public Builder token(String token) {
            this.token = token;
            return this;
        }

        public AuthResponse build() {
            return new AuthResponse(userId, fullName, email, token);
        }
    }
}
