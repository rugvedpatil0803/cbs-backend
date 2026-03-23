package com.project.cbsbackend.config;

import com.project.cbsbackend.entity.ApiRoleLink;
import com.project.cbsbackend.repository.ApiRoleLinkRepository;
import com.project.cbsbackend.repository.UserRoleLinkRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final UserRoleLinkRepository userRoleLinkRepository;
    private final ApiRoleLinkRepository apiRoleLinkRepository;

    private static final List<String> PUBLIC_URLS = List.of(
            "/auth/login",
            "/auth/register",
            "/auth/login/email",
            "/auth/request-otp",
            "/auth/reset",
            "/auth/refresh-token"
    );

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        String requestUrl    = request.getRequestURI();
        String requestMethod = request.getMethod();

        // ── 1. Skip public URLs ───────────────────────────────────────
        boolean isPublic = PUBLIC_URLS.stream()
                .anyMatch(url -> url.equals(requestUrl));

        if (isPublic) {
            filterChain.doFilter(request, response);
            return;
        }

        // ── 2. Check Authorization header ────────────────────────────
        String authHeader = request.getHeader("Authorization");

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            sendError(response, HttpServletResponse.SC_UNAUTHORIZED, "Missing or invalid token");
            return;
        }

        String token = authHeader.substring(7);

        // ── 3. Validate token ─────────────────────────────────────────
        if (!jwtUtil.isTokenValid(token)) {
            sendError(response, HttpServletResponse.SC_UNAUTHORIZED, "Invalid or expired token");
            return;
        }

        // ── 4. Extract userId from token ──────────────────────────────
        Long userId = jwtUtil.extractUserId(token);

        // ── 5. Get all role IDs for this user ─────────────────────────
        List<Long> roleIds = userRoleLinkRepository.findByUserId(userId)
                .stream()
                .filter(link -> link.getIsActive() && !link.getIsDeleted())
                .map(link -> link.getRole().getId())
                .collect(Collectors.toList());

        if (roleIds.isEmpty()) {
            sendError(response, HttpServletResponse.SC_FORBIDDEN, "User has no roles assigned");
            return;
        }

        // ── 6. Get all API permissions for those roles ────────────────
        List<ApiRoleLink> apiPermissions = apiRoleLinkRepository.findActiveByRoleIds(roleIds);

        // ── 7. Check if any permission matches this URL + method ──────
        apiPermissions.forEach(permission -> {
            boolean urlMatches    = requestUrl.equals(permission.getApiUrl()) ||
                    requestUrl.startsWith(permission.getApiUrl() + "/");
            boolean methodMatches = List.of(permission.getAllowedActions().split(","))
                    .contains(requestMethod.toUpperCase());
        });

        boolean isAllowed = apiPermissions.stream().anyMatch(permission -> {
            boolean urlMatches    = requestUrl.equals(permission.getApiUrl()) ||
                    requestUrl.startsWith(permission.getApiUrl() + "/");
            boolean methodMatches = List.of(permission.getAllowedActions().split(","))
                    .contains(requestMethod.toUpperCase());
            return urlMatches && methodMatches;
        });


        if (!isAllowed) {
            sendError(response, HttpServletResponse.SC_FORBIDDEN,
                    "You are not authorized to perform this action");
            return;
        }

        // ── 8. Set authentication in Spring Security context ──────────
        String email       = jwtUtil.extractEmail(token);
        List<String> roles = jwtUtil.extractRoles(token);

        List<SimpleGrantedAuthority> authorities = roles.stream()
                .map(role -> new SimpleGrantedAuthority("ROLE_" + role))
                .collect(Collectors.toList());

        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(email, userId, authorities);

        SecurityContextHolder.getContext().setAuthentication(authentication);
        filterChain.doFilter(request, response);
    }

    private void sendError(HttpServletResponse response, int status, String message) throws IOException {
        response.setStatus(status);
        response.setContentType("application/json");
        response.getWriter().write(
                "{\"status\": \"error\", \"message\": \"" + message + "\"}"
        );
    }
}