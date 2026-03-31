package codex.mmxxvi.services.impl;

import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.crypto.SecretKey;

import org.springframework.stereotype.Service;

import codex.mmxxvi.config.JwtProperties;
import codex.mmxxvi.services.JwtService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class JwtServiceImpl implements JwtService {
    private  final JwtProperties jwtProperties;

    private SecretKey getSigningKey(){
        return Keys.hmacShaKeyFor(jwtProperties.getSecret().getBytes(StandardCharsets.UTF_8));
    }
    private String generateToken(String username,long expiration,String tokenType){
        Map<String, Object> claims = new HashMap<>();
        claims.put("type", tokenType);
        return Jwts.builder()
                .setClaims(claims)
                .setSubject(username)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis()  + expiration))
                .signWith(getSigningKey())
                .compact();


    }

    private Claims extraAllClaims(String token){
        return Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    @Override
    public String generateAccessToken(String username) {
        return generateToken(username,jwtProperties.getAccessExpiration(),"access");
    }

    @Override
    public String generateRefreshToken(String username) {
        return generateToken(username,jwtProperties.getRefreshExpiration(),"refresh");
    }

    @Override
    public String extractUsername(String token) {
        return extraAllClaims(token).getSubject();
    }

    @Override
    public String extractTokenType(String token) {
        return extraAllClaims(token).get("type",String.class);
    }

    @Override
    public boolean isValidToken(String token, String username) {
        Claims claims = extraAllClaims(token);
        return claims.getSubject().equals(username)
                && claims.getExpiration().after(new Date());
    }

    @Override
    public boolean isRefreshToken(String token) {
        return "refresh".equals(extractTokenType(token));
    }
}
