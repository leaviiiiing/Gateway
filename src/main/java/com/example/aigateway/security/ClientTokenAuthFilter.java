package com.example.aigateway.security;

import com.example.aigateway.config.GatewayProperties;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Slf4j
@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 10)
@RequiredArgsConstructor
public class ClientTokenAuthFilter extends OncePerRequestFilter {

    private final GatewayProperties properties;

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        return path.startsWith("/actuator");
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        List<String> tokens = properties.clientTokenList();
        if (tokens.isEmpty()) {
            log.warn("gateway.client-tokens is empty — accepting all requests (dev only)");
            filterChain.doFilter(request, response);
            return;
        }
        String auth = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (auth == null || !auth.startsWith("Bearer ")) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write("{\"error\":{\"message\":\"missing_bearer_token\"}}");
            return;
        }
        String bearer = auth.substring("Bearer ".length()).trim();
        if (!tokens.contains(bearer)) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write("{\"error\":{\"message\":\"invalid_client_token\"}}");
            return;
        }
        filterChain.doFilter(request, response);
    }
}
