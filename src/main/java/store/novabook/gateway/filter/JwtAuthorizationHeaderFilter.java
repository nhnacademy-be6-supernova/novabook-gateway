package store.novabook.gateway.filter;

import java.security.Key;

import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import store.novabook.gateway.config.JWTUtil;
import store.novabook.gateway.service.AuthService;
import store.novabook.gateway.service.JWTTokenService;
import store.novabook.gateway.util.KeyManagerUtil;
import store.novabook.gateway.util.dto.JWTConfigDto;

@Component
@Slf4j
public class JwtAuthorizationHeaderFilter extends AbstractGatewayFilterFactory<JwtAuthorizationHeaderFilter.Config> {

	private final JWTUtil jwtUtil;
	private final JWTTokenService jwtTokenService;
	private final AuthService authService;
	private final JWTConfigDto jwtConfig;

	public JwtAuthorizationHeaderFilter(JWTUtil jwtUtil, JWTTokenService jwtTokenService, AuthService authService, Environment env) {
		this.jwtUtil = jwtUtil;
		this.jwtTokenService = jwtTokenService;
		this.authService = authService;
		this.jwtConfig = KeyManagerUtil.getJWTConfig(env);
	}

	public static class Config {
	}

	@Override
	public GatewayFilter apply(Config config) {
		return (exchange, chain) -> {

			// ServerHttpRequest request = exchange.getRequest();
			// if (!request.getHeaders().containsKey(HttpHeaders.AUTHORIZATION) && !request.getHeaders()
			// 	.containsKey("Refresh")) {
			// 	log.debug("No Authorization, Refresh header");
			// } else {
			//
			// 	Key key = Keys.hmacShaKeyFor(Decoders.BASE64.decode(jwtConfig.secret()));
			//
			// 	try {
			// 		String accessToken = "";
			// 		if (request.getHeaders().containsKey(HttpHeaders.AUTHORIZATION)) {
			// 			accessToken = request.getHeaders().get(HttpHeaders.AUTHORIZATION).get(0).replace("Bearer ", "");
			// 		} else {
			// 			throw new ExpiredJwtException(null, null, "No Authorization header");
			// 		}
			//
			// 		Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(accessToken);
			//
			// 		String username = jwtUtil.getUsername(accessToken);
			// 		String role = jwtUtil.getRole(accessToken);
			//
			// 		if (!jwtTokenService.existsByUuid(username)) {
			// 			exchange.getResponse().setStatusCode(HttpStatus.SEE_OTHER);
			// 			return exchange.getResponse().setComplete();
			// 		}
			//
			// 		exchange.mutate().request(builder -> {
			// 			builder.header("X-USER-ID", username);
			// 			builder.header("X-USER-ROLE", role);
			// 		});
			//
			// 	} catch (ExpiredJwtException e) {
			//
			// 		exchange.getResponse().setStatusCode(HttpStatus.SEE_OTHER);
			// 		return exchange.getResponse().setComplete();
			//
			// 	} catch (JwtException e) {
			// 		exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
			//
			// 		log.error("JwtException");
			// 		return exchange.getResponse().setComplete();
			// 	}
			// }
			//
			// return chain.filter(exchange);





			ServerHttpRequest request = exchange.getRequest();
			if (!request.getHeaders().containsKey(HttpHeaders.AUTHORIZATION) && !request.getHeaders()
				.containsKey("Refresh")) {
				log.debug("No Authorization, Refresh header");
			} else {

				Key key = Keys.hmacShaKeyFor(Decoders.BASE64.decode(jwtConfig.secret()));

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
						exchange.getResponse().setStatusCode(HttpStatus.SEE_OTHER);
						return exchange.getResponse().setComplete();
					}

					exchange.mutate().request(builder -> {
						builder.header("X-USER-ID", username);
						builder.header("X-USER-ROLE", role);
					});

				} catch (ExpiredJwtException e) {

					exchange.getResponse().setStatusCode(HttpStatus.SEE_OTHER);
					return exchange.getResponse().setComplete();

				} catch (JwtException e) {
					exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);

					log.error("JwtException");
					return exchange.getResponse().setComplete();
				}
			}

			return chain.filter(exchange);
		};
	}
}
