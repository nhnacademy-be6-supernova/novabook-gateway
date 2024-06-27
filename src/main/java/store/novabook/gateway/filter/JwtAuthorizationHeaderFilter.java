package store.novabook.gateway.filter;

import java.net.URI;
import java.security.Key;
import java.util.Date;
import java.util.Set;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class JwtAuthorizationHeaderFilter extends AbstractGatewayFilterFactory<JwtAuthorizationHeaderFilter.Config> {

	Set<String> tokenBlacklistStore = new java.util.HashSet<>();
	private final String secret;

	public JwtAuthorizationHeaderFilter(@Value("${jwt.secret}") String secret) {
		super(Config.class);
		this.secret = secret;
	}

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
			//TODO#3 JWT 검증 필터입니다.
			ServerHttpRequest request = exchange.getRequest();

			if (!request.getHeaders().containsKey(HttpHeaders.AUTHORIZATION)) {
				//TODO#3-1 Header에 Authorization 존재하지 않는다면 적절한 예외처리를 합니다.
				exchange.getResponse().setStatusCode(HttpStatus.SEE_OTHER);
				exchange.getResponse().getHeaders().setLocation(URI.create("http://127.0.0.1:9000/error"));
				return exchange.getResponse().setComplete();
			} else {

				//TODO#3-2 AccessToken jjwt 라이브러리를 사용하여 검증 구현하기
				//이미 Token이 만료되었는지?
				//Token의 signature 값 검증(HMAC)
				//이미 로그아웃된 Token 인지? - Black List 관리
				//account-api의 JwtProperties를 참고하여 구현합니다.

				String accessToken = request.getHeaders().get(HttpHeaders.AUTHORIZATION).get(0).replace("Bearer ", "");

				try {
					Key key = Keys.hmacShaKeyFor(Decoders.BASE64.decode(secret));

					Claims claims = Jwts
						.parserBuilder()
						.setSigningKey(key)
						.build()
						.parseClaimsJws(accessToken)
						.getBody();

					Date expiration = claims.getExpiration();
					Date now = new Date();

					//exception
					if (now.after(expiration)) {
						log.error("JWT token is expired");
						exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
						return exchange.getResponse().setComplete();
					}

					if (tokenBlacklistStore.contains(accessToken)) {
						exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
						return exchange.getResponse().setComplete();
					}

					exchange.mutate().request(builder -> {
						builder.header("X-USER-ID", claims.get("sub").toString());
					});

				} catch (Exception e) {
					// 토큰이 유효하지 않으면, 이곳에 예외 처리 로직을 추가할 수 있습니다.
					exchange.getResponse().setStatusCode(HttpStatus.SEE_OTHER);
					exchange.getResponse().getHeaders().setLocation(URI.create("http://127.0.0.1:9000/error"));
					return exchange.getResponse().setComplete();
				}

				// String accessToken = request.getHeaders().get(HttpHeaders.AUTHORIZATION).toString();

				//TODO#3-3 검증이 완료되면  Request header에 X-USER-ID를 등록합니다.
				//exchange.getRequest().getHeaders(); <-- imutable 합니다. 즉 수정 할 수 없습니다.
				//exchage.mutate()를 이용해야 합니다. 아래 코드를 참고하세요.
				// exchange.mutate().request(builder -> {
				// 	builder.header("X-USER-ID","nhnacademy");
				// });

			}

			return chain.filter(exchange);
		};
	}

}
