package com.searchDev.SearchDev.Config;

import com.searchDev.SearchDev.Service.AuthService.JWTservice;
import com.searchDev.SearchDev.Service.AuthService.MyUserDetailsService;
import com.searchDev.SearchDev.Service.AuthService.TokenBlacklistService;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class JwtFilter extends OncePerRequestFilter {

    @Autowired
    private JWTservice jwTservice;

    @Autowired
    private MyUserDetailsService myUserDetailsService;

    @Autowired
    private TokenBlacklistService tokenBlacklistService;

    private boolean isPublicPath(HttpServletRequest request) {
        String path = request.getRequestURI();
        return path.equals("/login") ||
               path.equals("/register") ||
               path.equals("/refresh") ||
               path.equals("/forget-password") ||
               path.startsWith("/reset-password");
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        // 1. Skip JWT validation for public routes
        if (isPublicPath(request)) {
            filterChain.doFilter(request, response);
            return;
        }

        // 2. Extract access token from cookies
        String token = null;
        String email = null;

        if (request.getCookies() != null) {
            for (Cookie cookie : request.getCookies()) {
                if ("access_Token".equals(cookie.getName())) {
                    token = cookie.getValue();
                }
            }
        }

        // 3. Missing token
        if (token == null) {
            writeError(response, 401, "NO_ACCESS_TOKEN");
            return;
        }

        // 4. Token blacklisted
        if (tokenBlacklistService.istokenBlackListed(token)) {
            writeError(response, 401, "TOKEN_BLACKLISTED");
            return;
        }

        // 5. Extract and validate token
        try {
            email = jwTservice.extractUserEmail(token);

        } catch (ExpiredJwtException e) {
            writeError(response, 401, "ACCESS_TOKEN_EXPIRED");
            return;

        } catch (MalformedJwtException e) {
            writeError(response, 401, "INVALID_ACCESS_TOKEN");
            return;

        } catch (Exception e) {
            writeError(response, 401, "TOKEN_ERROR");
            return;
        }

        // 6. Validate user and set authentication
        if (email != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            UserDetails userDetails = myUserDetailsService.loadUserByUsername(email);

            try {
                if (jwTservice.validateToken(token, userDetails, true)) {
                    UsernamePasswordAuthenticationToken auth =
                            new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
                    auth.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(auth);
                }
            } catch (Exception e) {
                writeError(response, 401, "INVALID_ACCESS_TOKEN");
                return;
            }
        }

        filterChain.doFilter(request, response);
    }

    private void writeError(HttpServletResponse response, int status, String message) throws IOException {
        response.setStatus(status);
        response.setContentType("application/json");
        response.getWriter().write("{\"status\":" + status + ",\"message\":\"" + message + "\"}");
    }
}

