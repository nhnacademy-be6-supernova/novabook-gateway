package store.novabook.gateway.filter;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.Key;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import jakarta.ws.rs.core.Cookie;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import store.novabook.gateway.config.JWTUtil;
import store.novabook.gateway.service.AuthService;
import store.novabook.gateway.util.KeyManagerUtil;
import store.novabook.gateway.util.dto.JWTConfigDto;

@Component
@Slf4j
public class JwtAuthorizationHeaderFilter extends AbstractGatewayFilterFactory<JwtAuthorizationHeaderFilter.Config> {

	private final JWTUtil jwtUtil;
	private final AuthService authService;
	private final JWTConfigDto jwtConfig;

	public JwtAuthorizationHeaderFilter(JWTUtil jwtUtil, AuthService authService, Environment env) {
		this.jwtUtil = jwtUtil;
		this.authService = authService;
		this.jwtConfig = KeyManagerUtil.getJWTConfig(env);
	}

	public static class Config {
	}

	@Override
	public GatewayFilter apply(Config config) {
		return (exchange, chain) -> {
			ServerHttpRequest request = exchange.getRequest();
			String refreshToken = "";
			if (!request.getHeaders().containsKey(HttpHeaders.AUTHORIZATION) && !request.getHeaders()
				.containsKey("Refresh")) {
				log.error("No Authorization, Refresh header");
			} else {

				Key key = Keys.hmacShaKeyFor(Decoders.BASE64.decode(jwtConfig.secret()));
				try {
					refreshToken = "";
					if (request.getHeaders().containsKey("Refresh")) {
						refreshToken = request.getHeaders().get("Refresh").get(0).replace("Bearer ", "");
						if (refreshToken.equals("null") || refreshToken.isEmpty()) {
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
					// String redirectUrl = "http://localhost:8080/api/v1/front/new-token";


					String encodedJwt = "";
					try {
						encodedJwt = URLEncoder.encode(refreshToken, StandardCharsets.UTF_8.toString());
					} catch (UnsupportedEncodingException ex) {
						throw new RuntimeException("Error encoding JWT", ex);
					}

					// redirectUrl에 PathVariable 값 포함
					String redirectUrl = "http://localhost:8080/api/v1/front/new-token/" + encodedJwt;

					exchange.getResponse().getHeaders().set(HttpHeaders.LOCATION, redirectUrl);
					exchange.getResponse().setStatusCode(HttpStatus.SEE_OTHER);
					return exchange.getResponse().setComplete();

				} catch (JwtException e) {
					// JWT 토큰이 유효하지 않은 경우, 401 Unauthorized 에러를 반환합니다.
					exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);

					log.error("JwtException");
					return exchange.getResponse().setComplete();
				}
			}

			return chain.filter(exchange);
		};
	}
}
