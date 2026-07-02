package cl.techstore.api.security;

import java.security.Key;
import java.util.Date;

import org.springframework.stereotype.Component;

import io.jsonwebtoken.Jwts;

@Component
public class JwtUtil {
    
    
    private static final String SECRET_STRING = "EstaEsMiClaveSecretaSuperSeguraParaTechStore2026!";
    private static final Key SECRET_KEY = io.jsonwebtoken.security.Keys.hmacShaKeyFor(SECRET_STRING.getBytes());
    
    private static final long EXPIRATION_TIME = 3600000; // 1 hora
   
    public String generarToken(String username) {
        return Jwts.builder()
                .setSubject(username)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME))
                .signWith(SECRET_KEY)
                .compact();
    }

    
    public String extraerUsername(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(SECRET_KEY)
                .build()
                .parseClaimsJws(token)
                .getBody()
                .getSubject();
    }


    public boolean validarToken(String token) {
        try {
            Jwts.parserBuilder().setSigningKey(SECRET_KEY).build().parseClaimsJws(token);
            return true;
        } catch (io.jsonwebtoken.ExpiredJwtException | 
                 io.jsonwebtoken.UnsupportedJwtException | 
                 io.jsonwebtoken.MalformedJwtException | 
                 io.jsonwebtoken.security.SignatureException | 
                 IllegalArgumentException e) {
            
            
            System.out.println("Token inválido: " + e.getMessage());
            return false;
        }
    }
}