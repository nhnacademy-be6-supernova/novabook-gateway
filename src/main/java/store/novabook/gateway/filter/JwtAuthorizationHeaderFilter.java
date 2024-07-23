package store.novabook.gateway.filter;

import java.security.Key;
import java.util.Objects;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import store.novabook.gateway.config.JWTUtil;
import store.novabook.gateway.entity.AccessTokenInfo;
import store.novabook.gateway.service.AuthenticationService;
import store.novabook.gateway.util.KeyManagerUtil;
import store.novabook.gateway.util.dto.JWTConfigDto;

@Component
@Slf4j
@RequiredArgsConstructor
public class JwtAuthorizationHeaderFilter extends AbstractGatewayFilterFactory<JwtAuthorizationHeaderFilter.Config>
	implements
	InitializingBean {

	private final AuthenticationService authenticationService;
	private final JWTUtil jwtUtil;
	private final Environment env;
	private JWTConfigDto jwtConfig;
	private Key key;

	@Override
	public void afterPropertiesSet() {
		RestTemplate restTemplate = new RestTemplate();
		this.jwtConfig = KeyManagerUtil.getJWTConfig(env, restTemplate);
		key = Keys.hmacShaKeyFor(Decoders.BASE64.decode(jwtConfig.secret()));
	}

	public static class Config {
		public void init() {
			//nothing
		}
	}

	@Override
	public GatewayFilter apply(Config config) {
		return (exchange, chain) -> {
			ServerHttpRequest request = exchange.getRequest();
			if (!request.getHeaders().containsKey(HttpHeaders.AUTHORIZATION)) {
				log.info("인증 정보가 없습니다");
				return chain.filter(exchange);
			}
			try {
				String accessToken = "";
				if (request.getHeaders().containsKey(HttpHeaders.AUTHORIZATION)) {
					accessToken = Objects.requireNonNull(request.getHeaders().get(HttpHeaders.AUTHORIZATION))
						.getFirst().replace("Bearer ", "");
				} else {
					log.info("액세스 토큰이 없습니다");
					throw new ExpiredJwtException(null, null, "No Authorization");
				}

				Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(accessToken);
				String uuid = jwtUtil.getUUID(accessToken);
				AccessTokenInfo accessTokenInfo = authenticationService.getAccessToken(uuid);

				if (accessTokenInfo == null) {
					log.info("레디스에 토큰 정보가 없습니다");
					exchange.getResponse().setStatusCode(HttpStatus.SEE_OTHER);
					return exchange.getResponse().setComplete();
				}

				log.info("인증 UUID: {}, 회원 아이디: {}, 회원 권한: {}", accessTokenInfo.getUuid(),
					accessTokenInfo.getMembersId(), accessTokenInfo.getRole());
				exchange.mutate().request(builder -> {
					builder.header("X-USER-ID", Long.toString(accessTokenInfo.getMembersId()));
					builder.header("X-USER-ROLE", accessTokenInfo.getRole());
				});

			} catch (ExpiredJwtException e) {
				log.info("액세스토큰 만료");
				exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
				return exchange.getResponse().setComplete();
			} catch (JwtException e) {
				log.info("올바른 JWT 토큰이 아닙니다");
				exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
				return exchange.getResponse().setComplete();
			}

			return chain.filter(exchange);
		};
	}
}
