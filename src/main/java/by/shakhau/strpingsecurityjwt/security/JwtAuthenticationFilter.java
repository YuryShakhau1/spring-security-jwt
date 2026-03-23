package by.shakhau.strpingsecurityjwt.security;

import lombok.AllArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

@AllArgsConstructor
@Component
public class JwtAuthenticationFilter implements WebFilter {

    private JwtService jwtService;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        var bearer = "Bearer ";
        var authHeader = exchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION);

        if (authHeader != null && authHeader.startsWith(bearer)) {
            String token = authHeader.substring(bearer.length());
            if (jwtService.validateToken(token)) {
                String userName = jwtService.getUserNameFromToken(token);
                var auth = new UsernamePasswordAuthenticationToken(userName, null, null);
                return chain.filter(exchange).contextWrite(ReactiveSecurityContextHolder.withAuthentication(auth));
            }
        }

        return chain.filter(exchange);
    }
}
