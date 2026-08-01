package com.url.shortener.security.jwt;

import com.url.shortener.service.UserDetailsImpl;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.security.Key;
import java.util.Date;
import java.util.stream.Collectors;

@Component
public class JwtUtils {

    @Value("${jwt.secret}")
    private String jwtSecret;

    @Value("${jwt.expirationMs}")
    private int jwtExpirationMs;
    //Authorization Header -> Bearer <Token>

    /**
     * Extracts the JWT token from the Authorization header of the incoming HTTP request.
     *
     * @param request the incoming HTTP request
     * @return the extracted JWT string without the "Bearer " prefix, or null if missing/invalid
     */
    public String getJwtFromHeader(HttpServletRequest request){
        String bearerToken = request.getHeader("Authorization");
        if(bearerToken!=null && bearerToken.startsWith("Bearer ")){
            return bearerToken.substring(7);
        }
        return null;
    }

    /**
     * Generates a JWT token based on the specified user's details and authorities.
     *
     * @param userDetails object containing username and assigned roles
     * @return the generated JWT token string
     */
    public String generateToken(UserDetailsImpl userDetails){
        String username = userDetails.getUsername();
        String roles = userDetails.getAuthorities().stream()
                .map(authority -> authority.getAuthority())
                .collect(Collectors.joining(","));
        return Jwts.builder()
                .subject(username)
                .claim("roles", roles)
                .issuedAt(new Date())
                .expiration(new Date(new Date().getTime() + jwtExpirationMs))
                .signWith(key())
                .compact();
    }

    /**
     * Parses the JWT to extract the subject containing the user's username.
     *
     * @param token the valid JWT token
     * @return the embedded username string
     */
    public String getUserNameFromJwtToken(String token){
        return Jwts.parser()
                .verifyWith((SecretKey) key())
                .build().parseSignedClaims(token)
                .getPayload().getSubject();

    }
    
    /**
     * Retrieves the SecretKey used for cryptographic signing of the token.
     *
     * @return the decoded Key object
     */
    private Key key(){
       return Keys.hmacShaKeyFor(Decoders.BASE64.decode(jwtSecret));
    }

    /**
     * Validates if the given JWT token string aligns with parsing and signature protocols securely.
     *
     * @param authToken the raw token to validate
     * @return true if valid, otherwise throws specific runtime exceptions
     */
    public boolean validateToken(String authToken){
        try{
            Jwts.parser().verifyWith((SecretKey) key())
                    .build().parseSignedClaims(authToken);
            return true;
        } catch(JwtException e){
            throw new RuntimeException(e);
        } catch(IllegalArgumentException e){
            throw new RuntimeException(e);
        } catch(Exception e){
            throw new RuntimeException(e);
        }
    }
}
