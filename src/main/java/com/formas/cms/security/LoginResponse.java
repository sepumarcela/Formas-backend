package com.formas.cms.security;

public record LoginResponse(String token, String email, long expiresAt) {
}
