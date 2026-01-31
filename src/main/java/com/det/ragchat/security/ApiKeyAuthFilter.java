package com.det.ragchat.security;

import com.det.ragchat.config.AppProperties;
import com.det.ragchat.domain.AppUser;
import com.det.ragchat.service.AppUserService;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.*;

@Component
public class ApiKeyAuthFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(ApiKeyAuthFilter.class);

    public static final String API_KEY_HEADER = "X-API-Key";

    private final AppProperties props;
    private final AppUserService userService;

    public ApiKeyAuthFilter(AppProperties props, AppUserService userService) {
        this.props = props;
        this.userService = userService;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) return true;
        return props.getSecurity().getMode() != AppProperties.AuthMode.API_KEY;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String provided = request.getHeader(API_KEY_HEADER);
        String expected = props.getSecurity().getApiKey();

        if (provided == null || expected == null || !Objects.equals(provided, expected)) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            response.getWriter().write("""
                    {"code":"UNAUTHORIZED","message":"Invalid or missing API key"}
                    """);
            return;
        }

        // Require user id header to scope data.
        String userIdHeader = props.getSecurity().getUserIdHeader();
        String userId = request.getHeader(userIdHeader);
        if (userId == null || userId.isBlank()) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            response.getWriter().write(("""
                    {"code":"MISSING_USER_ID","message":"Missing required header: %s"}
                    """).formatted(userIdHeader));
            return;
        }

        // Resolve roles from DB (if enabled) for real-time authorization.
        AppUser dbUser = null;
        if (props.getSecurity().isResolveRolesFromDb()) {
            dbUser = userService.getActiveByUserIdOrNull(userId);
            if (dbUser == null && props.getSecurity().isRequireUserInDb()) {
                response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                response.setContentType(MediaType.APPLICATION_JSON_VALUE);
                response.getWriter().write("""
                        {"code":"FORBIDDEN","message":"Unknown or inactive user"}
                        """);
                return;
            }
        }

        MDC.put("userId", userId);

        var authorities = buildAuthorities(dbUser);
        if (log.isDebugEnabled()) {
            log.debug("AUTH user={} roles={}", userId, authorities.stream().map(GrantedAuthority::getAuthority).toList());
        }

        var auth = new ApiKeyAuthenticationToken(userId, authorities);
        SecurityContextHolder.getContext().setAuthentication(auth);

        filterChain.doFilter(request, response);
    }

    private List<GrantedAuthority> buildAuthorities(AppUser dbUser) {
        Set<String> rolesToGrant = new LinkedHashSet<>();

        // DB roles (preferred)
        if (props.getSecurity().isResolveRolesFromDb() && dbUser != null && dbUser.getRoles() != null) {
            rolesToGrant.addAll(dbUser.getRoles());
        } else {
            // Fallback to configured roles (useful for local dev or when DB-role resolution is disabled)
            if (props.getSecurity().getRoles() != null) {
                rolesToGrant.addAll(props.getSecurity().getRoles());
            }
        }

        // Backwards compatibility role.
        if (props.getSecurity().isGrantApiRole()) {
            rolesToGrant.add("API");
        }

        List<GrantedAuthority> authorities = new ArrayList<>();
        for (String role : rolesToGrant) {
            if (role == null) continue;
            String r = role.trim();
            if (r.isEmpty()) continue;

            // Normalize to ROLE_ prefix so hasRole/hasAnyRole works.
            if (!r.startsWith("ROLE_")) {
                r = "ROLE_" + r;
            }
            authorities.add(new SimpleGrantedAuthority(r));
        }

        return authorities.stream().distinct().toList();
    }

    static class ApiKeyAuthenticationToken extends AbstractAuthenticationToken {
        private final String userId;

        ApiKeyAuthenticationToken(String userId, List<? extends GrantedAuthority> authorities) {
            super(authorities);
            this.userId = userId;
            setAuthenticated(true);
        }

        @Override public Object getCredentials() { return ""; }
        @Override public Object getPrincipal() { return userId; }
        @Override public String getName() { return userId; }
    }
}
