package store.novabook.gateway.filter;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import store.novabook.gateway.config.JWTUtil;
import store.novabook.gateway.entity.AccessTokenInfo;
import store.novabook.gateway.service.AuthenticationService;
import store.novabook.gateway.util.KeyManagerUtil;
import store.novabook.gateway.util.dto.JWTConfigDto;

import java.security.Key;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class JwtAuthorizationHeaderFilterTest {

	@Mock
	private JWTUtil jwtUtil;

	@Mock
	private AuthenticationService authenticationService;

	@Mock
	private Environment environment;

	@Mock
	private GatewayFilterChain filterChain;

	@InjectMocks
	private JwtAuthorizationHeaderFilter filter;

	private JWTConfigDto jwtConfig;

	@BeforeEach
	void setUp() {
		when(environment.getProperty("jwt.secret")).thenReturn("YourSecretKeyForJWT");

		jwtConfig = KeyManagerUtil.getJWTConfig(environment);
		filter = new JwtAuthorizationHeaderFilter(jwtUtil, authenticationService, environment);
	}

	@Test
	void testApply_Success() {
		// Given
		ServerWebExchange exchange = mock(ServerWebExchange.class);
		ServerHttpRequest request = mock(ServerHttpRequest.class);
		ServerHttpResponse response = mock(ServerHttpResponse.class);

		String validToken = "valid-jwt-token";
		String uuid = "test-uuid";

		when(exchange.getRequest()).thenReturn(request);
		when(exchange.getResponse()).thenReturn(response);
		when(request.getHeaders()).thenReturn(HttpHeaders.EMPTY);
		when(jwtUtil.getUUID(validToken)).thenReturn(uuid);

		AccessTokenInfo accessTokenInfo = AccessTokenInfo.of(
			"123e4567-e89b-12d3-a456-426614174000",
			"987e6543-e21c-34f2-a423-567817441000",
			1L,
			"USER",
			LocalDateTime.of(2023, 10, 5, 12, 0)
		);

		when(authenticationService.getAccessToken(uuid)).thenReturn(accessTokenInfo);

		// When
		JwtAuthorizationHeaderFilter.Config config = new JwtAuthorizationHeaderFilter.Config();
		GatewayFilter gatewayFilter = filter.apply(config);
		Mono<Void> result = gatewayFilter.filter(exchange, filterChain);

		// Then
		assertNotNull(result);
		verify(authenticationService, times(1)).getAccessToken(uuid);
	}

	@Test
	void testApply_ExpiredJwtException() {
		// Given
		ServerWebExchange exchange = mock(ServerWebExchange.class);
		ServerHttpRequest request = mock(ServerHttpRequest.class);
		ServerHttpResponse response = mock(ServerHttpResponse.class);

		String expiredToken = "expired-jwt-token";

		when(exchange.getRequest()).thenReturn(request);
		when(exchange.getResponse()).thenReturn(response);
		when(request.getHeaders()).thenReturn(HttpHeaders.EMPTY);
		when(jwtUtil.getUUID(expiredToken)).thenThrow(new ExpiredJwtException(null, null, "Token expired"));

		// When
		JwtAuthorizationHeaderFilter.Config config = new JwtAuthorizationHeaderFilter.Config();
		GatewayFilter gatewayFilter = filter.apply(config);
		Mono<Void> result = gatewayFilter.filter(exchange, filterChain);

		// Then
		assertNotNull(result);
		verify(exchange.getResponse()).setStatusCode(HttpStatus.UNAUTHORIZED);
	}

	@Test
	void testApply_JwtException() {
		// Given
		ServerWebExchange exchange = mock(ServerWebExchange.class);
		ServerHttpRequest request = mock(ServerHttpRequest.class);
		ServerHttpResponse response = mock(ServerHttpResponse.class);

		String invalidToken = "invalid-jwt-token";

		when(exchange.getRequest()).thenReturn(request);
		when(exchange.getResponse()).thenReturn(response);
		when(request.getHeaders()).thenReturn(HttpHeaders.EMPTY);
		when(jwtUtil.getUUID(invalidToken)).thenThrow(new JwtException("Token invalid"));

		// When
		JwtAuthorizationHeaderFilter.Config config = new JwtAuthorizationHeaderFilter.Config();
		GatewayFilter gatewayFilter = filter.apply(config);
		Mono<Void> result = gatewayFilter.filter(exchange, filterChain);

		// Then
		assertNotNull(result);
		verify(exchange.getResponse()).setStatusCode(HttpStatus.UNAUTHORIZED);
	}

	// Additional tests can be written to cover more scenarios
}
