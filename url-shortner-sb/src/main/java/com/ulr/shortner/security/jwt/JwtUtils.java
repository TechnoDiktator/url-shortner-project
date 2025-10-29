package com.ulr.shortner.security.jwt;

import java.security.Key;
import java.util.Date;
import java.util.Base64.Decoder;
import java.util.stream.Collectors;

import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Value;

import com.ulr.shortner.service.UserDetailsImpl;

import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Component;

//What is the use of this class?
/*
 * This class is intended to handle JSON Web Tokens (JWT)
 *  for authentication and authorization purposes in a web application.
 *   It typically includes methods for generating, validating,
 *    and parsing JWTs to ensure secure communication between clients and servers.
 */

@Component
public class JwtUtils {

    @Value("${jwt.secret}")
    private String jwtSecret;

    @Value("${jwt.expiration}")
    private int jwtExpirationMs;


    //Authorization: Bearer <token>
    public String getJwtFromHeader(HttpServletRequest request){

        String bearerToken = request.getHeader("Authorization");
        if(bearerToken != null && bearerToken.startsWith("Bearer ")){
            return bearerToken.substring(7);
        }
        return null;

    }

    // FIXED: Remove dummy implementation
    public String getUserNameFromJwtToken(String token){
        return Jwts.parser()
            .verifyWith((SecretKey)key())
            .build()
            .parseSignedClaims(token)
            .getPayload()
            .getSubject();
    }


    public String generateToken(UserDetailsImpl UserDetails){
        String username = UserDetails.getUsername();
        String roles = UserDetails.getAuthorities().stream()
                .map(authority -> authority.getAuthority())
                .collect(Collectors.joining(","));

                
                return Jwts.builder().subject(username)
                .claim("roles", roles)
                .issuedAt(new Date()).expiration(new Date((new Date().getTime() + 86400000))).signWith(key())
                .compact(); //1 day
    }


    //so the subject is username
    public String getUserNameFromJwtToken1(String token) {
        return Jwts.parser()
        .verifyWith((SecretKey)key())
        .build().parseSignedClaims(token)
        .getPayload().getSubject();
    }


    public boolean validateToken(String authToken){
        try{
            Jwts.parser()
            .verifyWith((SecretKey)key())
            .build()
            .parseSignedClaims(authToken);
            return true;
        }catch(JwtException e){
            throw new RuntimeException("Invalid JWT token");
        }catch(IllegalArgumentException e){
            throw new RuntimeException("JWT claims string is empty: " + e.getMessage());        
        }catch(Exception e){
            throw new RuntimeException("Error validating JWT token: " + e.getMessage());
        }   
    }


    private Key key() {
        // TODO Auto-generated method stub
        return Keys.hmacShaKeyFor(Decoders.BASE64.decode(jwtSecret));
    }

}
