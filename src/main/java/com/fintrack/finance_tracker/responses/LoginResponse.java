package com.fintrack.finance_tracker.responses;

public class LoginResponse {
    private String token;
    private Long ExpiresIn;

    public LoginResponse(String token, Long expiresIn) {
        this.token = token;
        ExpiresIn = expiresIn;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public Long getExpiresIn() {
        return ExpiresIn;
    }

    public void setExpiresIn(Long expiresIn) {
        ExpiresIn = expiresIn;
    }
}
