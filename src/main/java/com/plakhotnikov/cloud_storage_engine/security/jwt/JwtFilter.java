package com.plakhotnikov.cloud_storage_engine.security.jwt;

import com.plakhotnikov.cloud_storage_engine.security.entity.UserEntity;
import com.plakhotnikov.cloud_storage_engine.security.services.CustomUserDetailsService;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;



/**
 * Фильтр для проверки JWT-токена в HTTP-запросах.
 * Обрабатывает заголовок Authorization и устанавливает пользователя в SecurityContext.
 *
 * @see JwtService
 * @see CustomUserDetailsService
 */
@Component
@RequiredArgsConstructor
public class JwtFilter extends OncePerRequestFilter {
    private final JwtService jwtService;
    private final RequestMatcher ignoredPaths = new AntPathRequestMatcher("/auth/refresh");
    private final CustomUserDetailsService customUserDetailsService;


    /**
     * Фильтрует входящий HTTP-запрос, проверяя JWT-токен.
     *
     * @param request HTTP-запрос.
     * @param response HTTP-ответ.
     * @param filterChain Цепочка фильтров.
     * @throws ServletException если произошла ошибка сервлета.
     * @throws IOException если произошла ошибка ввода-вывода.
     */
    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain filterChain) throws ServletException, IOException {
        if (this.ignoredPaths.matches(request)) {
            filterChain.doFilter(request, response);
            return;
        }
        try {
            String accessToken = getTokenFromRequest(request);
            if (accessToken != null && jwtService.validateAccessToken(accessToken)) {
                var userEmail = jwtService.getUsernameFromAccessClaims(accessToken);
                UserEntity userEntity = (UserEntity) customUserDetailsService.loadUserByUsername(userEmail);
                if (userEntity.getLastResetTime().isAfter(jwtService.getIssuedAtFromAccessClaims(accessToken))) {
                    throw new JwtException("password changed");
                }
                var authentication = new UsernamePasswordAuthenticationToken(
                        userEntity.getUsername(),
                        null,
                        userEntity.getAuthorities()
                );
                authentication.setDetails(
                        new WebAuthenticationDetailsSource().buildDetails(request)
                );
                SecurityContextHolder.getContext().setAuthentication(authentication);
            }
            filterChain.doFilter(request, response);
        }
        catch (Exception e) {
            throw new RuntimeException(e);
        }

    }


    /**
     * Извлекает токен из заголовка Authorization в HTTP-запросе.
     *
     * @param request HTTP-запрос.
     * @return JWT-токен или null, если он отсутствует.
     */
    private String getTokenFromRequest(HttpServletRequest request) {
        final String bearer = request.getHeader("Authorization");
        if (StringUtils.hasText(bearer) && bearer.startsWith("Bearer ")) {
            return bearer.substring(7);
        }
        return null;
    }
}
