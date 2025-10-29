package com.searchDev.SearchDev.Service.AuthService;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import io.jsonwebtoken.security.Keys;
import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.function.Function;

@Service
public class JWTservice {
  @Value("${jwt.secret}")
  private String secretKey;

  private SecretKey key;

  @PostConstruct
  public void init(){
      this.key = Keys.hmacShaKeyFor(secretKey.getBytes(StandardCharsets.UTF_8));
  }
  //access token generation
    public String generateAccessToken(Authentication authentication) {
        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();
        String email = userPrincipal.getUsername();

        return Jwts.builder()
                .subject(email)
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis()+1000 *60 *30))
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    //refresh token generation
    public String generateRefreshToken(Authentication authentication) {
              UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();
              String email = userPrincipal.getUsername();

              return Jwts.builder()
                      .subject(email)
                      .issuedAt(new Date(System.currentTimeMillis()))
                      .expiration(new Date(System.currentTimeMillis()+1000L * 60 * 60 *24 * 7))
                      .signWith(key, SignatureAlgorithm.HS256)
                      .compact();
    }


    //login by tokens

    //extract email from token
    public String extractUserEmail(String token) {
      return extractClaim(token, Claims::getSubject);
    }

    private <T> T extractClaim(String token, Function<Claims,T> claimResolver) {
        final Claims claims = extractAllClaims(token);
        return claimResolver.apply(claims);
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public boolean validateToken(String token, UserDetails userDetails) {
      final String email = extractUserEmail(token);
      return (email.equals(userDetails.getUsername()) && !isTokenExpired(token));
    }

    private boolean isTokenExpired(String token){
        return extractExpiration(token).before(new Date());
    }
    private Date extractExpiration(String token){
        return extractClaim(token,Claims::getExpiration);
    }


}
