package store.novabook.gateway.filter;

import java.security.Key;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import store.novabook.gateway.config.JWTUtil;
import store.novabook.gateway.service.AuthService;

@Component
@Slf4j
@RequiredArgsConstructor
public class JwtAuthorizationHeaderFilter extends AbstractGatewayFilterFactory<JwtAuthorizationHeaderFilter.Config> {

	@Value("${jwt.secret}")
	private String secret;
	private final JWTUtil jwtUtil;
	private final AuthService authService;

	public static class Config {
	}

	@Override
	public GatewayFilter apply(Config config) {
		return (exchange, chain) -> {
			ServerHttpRequest request = exchange.getRequest();

			if (!request.getHeaders().containsKey(HttpHeaders.AUTHORIZATION) && !request.getHeaders()
				.containsKey("Refresh")) {
				log.error("No Authorization, Refresh header");
			} else {

				Key key = Keys.hmacShaKeyFor(Decoders.BASE64.decode(secret));
				try {
					String refreshToken = "";
					if (request.getHeaders().containsKey("Refresh")) {
						refreshToken = request.getHeaders().get("Refresh").get(0).replace("Bearer ", "");
						if(refreshToken.equals("null") || refreshToken.isEmpty()) {
							throw new ExpiredJwtException(null, null, "Refresh token is null");
						}
					} else {
						throw new ExpiredJwtException(null, null, "No Refresh header");
					}
					Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(refreshToken);
				} catch (ExpiredJwtException e) {
					exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
					return exchange.getResponse().setComplete();
				}

				try {
					String accessToken = "";
					if (request.getHeaders().containsKey(HttpHeaders.AUTHORIZATION)) {
						accessToken = request.getHeaders().get(HttpHeaders.AUTHORIZATION).get(0).replace("Bearer ", "");
					} else {
						throw new ExpiredJwtException(null, null, "No Authorization header");
					}

					Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(accessToken);

					String username = jwtUtil.getUsername(accessToken);
					String role = jwtUtil.getRole(accessToken);

					if (!authService.existsByUuid(username)) {
						String redirectUrl = "http://localhost:8080/login";

						exchange.getResponse().setStatusCode(HttpStatus.SEE_OTHER);
						exchange.getResponse().getHeaders().set(HttpHeaders.LOCATION, redirectUrl);
						return exchange.getResponse().setComplete();
					}

					exchange.mutate().request(builder -> {
						builder.header("X-USER-ID", username);
						builder.header("X-USER-ROLE", role);
					});

				} catch (ExpiredJwtException e) {
					String redirectUrl = "http://localhost:8080/api/v1/front/new-token";

					exchange.getResponse().setStatusCode(HttpStatus.SEE_OTHER);
					exchange.getResponse().getHeaders().set(HttpHeaders.LOCATION, redirectUrl);
					return exchange.getResponse().setComplete();

				} catch (JwtException e) {
					// JWT 토큰이 유효하지 않은 경우, 401 Unauthorized 에러를 반환합니다.
					exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
					return exchange.getResponse().setComplete();
				}
			}

			return chain.filter(exchange);
		};
	}
}
