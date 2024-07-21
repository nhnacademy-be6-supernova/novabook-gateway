package store.novabook.gateway.filter;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.security.Key;
import java.util.UUID;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.server.ServerWebExchange;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import reactor.core.publisher.Mono;
import store.novabook.gateway.config.JWTUtil;
import store.novabook.gateway.service.AuthenticationService;
import store.novabook.gateway.util.KeyManagerUtil;
import store.novabook.gateway.util.dto.JWTConfigDto;

@ExtendWith(MockitoExtension.class)
class JwtAuthorizationHeaderFilterTest {

	@Mock
	private JWTUtil jwtUtil;

	@Mock
	private AuthenticationService authenticationService;

	@Mock
	private Environment environment;

	@InjectMocks
	private JwtAuthorizationHeaderFilter filter;

	private MockedStatic<KeyManagerUtil> keyManagerUtilMockedStatic;

	private JWTConfigDto jwtConfigDto;

	private Key key;

	@InjectMocks
	private JwtAuthorizationHeaderFilter jwtAuthorizationHeaderFilter;

	@BeforeEach
	void setUp() {
		MockitoAnnotations.openMocks(this);

		jwtConfigDto = new JWTConfigDto("header",
			"JDJhJDEyJHk0RC5jVkM1UzcwMExHUlU4VXdFZmVPTkZ6cWlZclJOamdTMnVxV1F1VXEyRnYybXpFTFhX",
			86400);
		byte[] keyBytes = Decoders.BASE64.decode(jwtConfigDto.secret());
		key = Keys.hmacShaKeyFor(keyBytes);

		keyManagerUtilMockedStatic = mockStatic(KeyManagerUtil.class);
		keyManagerUtilMockedStatic.when(() -> KeyManagerUtil.getJWTConfig(any(Environment.class), any(RestTemplate.class)))
			.thenReturn(jwtConfigDto);

		jwtAuthorizationHeaderFilter.afterPropertiesSet();
	}


	@AfterEach
	void tearDown() {
		if (keyManagerUtilMockedStatic != null) {
			keyManagerUtilMockedStatic.close();
		}
	}

	@Test
	void testNoAuthorizationHeader() {
		ServerWebExchange exchange = mock(ServerWebExchange.class);
		ServerHttpRequest request = mock(ServerHttpRequest.class);
		HttpHeaders headers = new HttpHeaders(); // No Authorization header
		when(exchange.getRequest()).thenReturn(request);
		when(request.getHeaders()).thenReturn(headers);

		GatewayFilterChain chain = mock(GatewayFilterChain.class);
		when(chain.filter(any(ServerWebExchange.class))).thenReturn(Mono.empty());

		Mono<Void> result = jwtAuthorizationHeaderFilter.apply(new JwtAuthorizationHeaderFilter.Config())
			.filter(exchange, chain);

		verify(chain, times(1)).filter(exchange);
		assertEquals(Mono.empty(), result);
	}

	@Test
	void testInvalidToken() {

		String token = Jwts.builder()
			.setHeaderParam("typ", "JWT")
			.claim("UUID", UUID.randomUUID())
			.signWith(key, SignatureAlgorithm.HS256)
			.compact();

		// Mock ServerWebExchange and ServerHttpRequest
		ServerWebExchange exchange = mock(ServerWebExchange.class);
		ServerHttpRequest request = mock(ServerHttpRequest.class);
		ServerHttpResponse response = mock(ServerHttpResponse.class);
		HttpHeaders headers = new HttpHeaders();
		headers.set(HttpHeaders.AUTHORIZATION, "Bearer " + token);

		when(exchange.getRequest()).thenReturn(request);
		when(request.getHeaders()).thenReturn(headers);
		when(exchange.getResponse()).thenReturn(response);
		when(response.setStatusCode(any(HttpStatus.class))).thenReturn(true);

		GatewayFilterChain chain = mock(GatewayFilterChain.class);

		jwtAuthorizationHeaderFilter.apply(new JwtAuthorizationHeaderFilter.Config())
			.filter(exchange, chain);

		verify(response, times(1)).setStatusCode(HttpStatus.SEE_OTHER);
	}


}
