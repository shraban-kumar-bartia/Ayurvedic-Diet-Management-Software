package com.sih.ayurvedic.services;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;

@Service
public class JwtService {

    @Value("${security.jwt.secret-key}")
    private String secretKey;

    @Value("${security.jwt.expiration-ms}")
    private long expirationMs;

    @Value("${security.jwt.issuer}")
    private String issuer;

    public String generateToken(String email, Integer userId) {
        long now = System.currentTimeMillis();
        Date issuedAt = new Date(now);
        Date expiry = new Date(now + expirationMs);

        Key key = Keys.hmacShaKeyFor(secretKey.getBytes(StandardCharsets.UTF_8));

        return Jwts.builder()
                .setSubject(email)
                .setIssuer(issuer)
                .setIssuedAt(issuedAt)
                .setExpiration(expiry)
                .claim("id", userId)
                .compact()
                // signWith is done by JJWT builder automatically when key is provided:
                // using .signWith(key, SignatureAlgorithm.HS256) required for older API;
                // but here we will sign explicitly:
                ;
    }

    // alternate explicit signing that avoids warnings:
    public String generateSignedToken(String email, Integer userId) {
        long now = System.currentTimeMillis();
        Date issuedAt = new Date(now);
        Date expiry = new Date(now + expirationMs);

        Key key = Keys.hmacShaKeyFor(secretKey.getBytes(StandardCharsets.UTF_8));

        return Jwts.builder()
                .setSubject(email)
                .setIssuer(issuer)
                .setIssuedAt(issuedAt)
                .setExpiration(expiry)
                .claim("id", userId)
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }
}
