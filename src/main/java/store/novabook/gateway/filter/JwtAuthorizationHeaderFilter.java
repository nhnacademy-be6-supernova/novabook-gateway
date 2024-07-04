package store.novabook.gateway.filter;

import java.security.Key;
import java.util.List;
import java.util.Set;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;
import store.novabook.gateway.config.JWTUtil;
import store.novabook.gateway.entity.Auth;
import store.novabook.gateway.service.AuthService;

@Component
@Slf4j
@RequiredArgsConstructor
public class JwtAuthorizationHeaderFilter extends AbstractGatewayFilterFactory<JwtAuthorizationHeaderFilter.Config> {

	Set<String> tokenBlacklistStore = new java.util.HashSet<>();
	@Value("${jwt.secret}")
	private String secret;
	private final JWTUtil jwtUtil;
	private final AuthService authService;

	// public JwtAuthorizationHeaderFilter(@Value("${jwt.secret}") String secret, JWTUtil jwtUtil) {
	// 	super(Config.class);
	// 	this.secret = secret;
	// 	this.jwtUtil = jwtUtil;
	// }

	public static class Config {
		// application.properties 파일에서 지정한 filer의 Argument값을 받는 부분
	}

	public void logout(String token) {
		// tokenBlacklistStore는 토큰 블랙리스트를 저장하는 저장소입니다.
		tokenBlacklistStore.add(token);
	}

	@Override
	public GatewayFilter apply(Config config) {
		return (exchange, chain) -> {
			ServerHttpRequest request = exchange.getRequest();

			if (!request.getHeaders().containsKey(HttpHeaders.AUTHORIZATION) && !request.getHeaders()
				.containsKey("Refresh")) {
				log.error("No Authorization, Refresh header");
			} else {

				//이미 로그아웃된 Token 인지? - Black List 관리

				try {
					String accessToken = "";
					if (request.getHeaders().containsKey(HttpHeaders.AUTHORIZATION)) {
						accessToken = request.getHeaders().get(HttpHeaders.AUTHORIZATION).get(0).replace("Bearer ", "");
					} else {
						throw new ExpiredJwtException(null, null, "No Authorization header");
					}
					Key key = Keys.hmacShaKeyFor(Decoders.BASE64.decode(secret));
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
