package com.url.shortener.security.jwt;

/**
 * Data Transfer Object representing the payload sent to the client upon successful authentication.
 */
public class JwtAuthenticationResponse {
    private String token ;

    /**
     * Constructs a new authentication response.
     *
     * @param token the generated JWT string to be sent
     */
    public JwtAuthenticationResponse(String token) {
        this.token = token;
    }

    /**
     * @return the generated JWT token string
     */
    public String getToken() {
        return token;
    }
}
