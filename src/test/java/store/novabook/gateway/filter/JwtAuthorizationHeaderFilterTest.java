package store.novabook.gateway.filter;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.security.Key;
import java.time.LocalDateTime;
import java.util.UUID;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.server.ServerWebExchange;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import reactor.core.publisher.Mono;
import store.novabook.gateway.config.JWTUtil;
import store.novabook.gateway.entity.AccessTokenInfo;
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
		jwtConfigDto = new JWTConfigDto("header",
			"JDJhJDEyJHk0RC5jVkM1UzcwMExHUlU4VXdFZmVPTkZ6cWlZclJOamdTMnVxV1F1VXEyRnYybXpFTFhX",
			86400);
		byte[] keyBytes = Decoders.BASE64.decode(jwtConfigDto.secret());
		key = Keys.hmacShaKeyFor(keyBytes);

		filter = new JwtAuthorizationHeaderFilter(authenticationService, jwtUtil, environment);

		keyManagerUtilMockedStatic = mockStatic(KeyManagerUtil.class);
		keyManagerUtilMockedStatic.when(
				() -> KeyManagerUtil.getJWTConfig(any(Environment.class), any(RestTemplate.class)))
			.thenReturn(jwtConfigDto);

	}

	@AfterEach
	void tearDown() {
		if (keyManagerUtilMockedStatic != null) {
			keyManagerUtilMockedStatic.close();
		}
	}

	@Test
	void testNoAuthorizationHeader() {
		// Create mock ServerWebExchange and ServerHttpRequest
		ServerWebExchange exchange = mock(ServerWebExchange.class);
		ServerHttpRequest request = mock(ServerHttpRequest.class);
		HttpHeaders headers = new HttpHeaders(); // No Authorization header
		when(exchange.getRequest()).thenReturn(request);
		when(request.getHeaders()).thenReturn(headers);

		// Create a mock GatewayFilterChain
		GatewayFilterChain chain = mock(GatewayFilterChain.class);
		when(chain.filter(any(ServerWebExchange.class))).thenReturn(Mono.empty());

		// Apply the filter
		Mono<Void> result = jwtAuthorizationHeaderFilter.apply(new JwtAuthorizationHeaderFilter.Config())
			.filter(exchange, chain);

		// Verify that the chain was called
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

		// Mock JWTUtil's behavior
		// when(jwtUtil.getUUID(anyString())).thenThrow(new JwtException("Invalid token"));

		// Create a mock GatewayFilterChain
		GatewayFilterChain chain = mock(GatewayFilterChain.class);
		// when(chain.filter(any(ServerWebExchange.class))).thenReturn(Mono.empty());

		// Apply the filter
		Mono<Void> result = jwtAuthorizationHeaderFilter.apply(new JwtAuthorizationHeaderFilter.Config())
			.filter(exchange, chain);

		// Verify that the response status is set to UNAUTHORIZED
		verify(response, times(1)).setStatusCode(HttpStatus.SEE_OTHER);
	}

	@Test
	void testExpiredJwtException() {

		String token = Jwts.builder()
			.setHeaderParam("typ", "JWT")
			.claim("UUID", UUID.randomUUID())
			.signWith(key, SignatureAlgorithm.HS256)
			.compact();
		// Mock the necessary components
		ServerWebExchange exchange = mock(ServerWebExchange.class);
		ServerHttpRequest request = mock(ServerHttpRequest.class);
		ServerHttpResponse response = mock(ServerHttpResponse.class);
		GatewayFilterChain chain = mock(GatewayFilterChain.class);
		HttpHeaders headers = new HttpHeaders();
		headers.set(HttpHeaders.AUTHORIZATION, "Bearer " + token);

		// Setup the behavior for the mock objects
		when(exchange.getRequest()).thenReturn(request);
		when(request.getHeaders()).thenReturn(headers);
		when(exchange.getResponse()).thenReturn(response);
		when(chain.filter(any(ServerWebExchange.class))).thenReturn(Mono.empty());

		// Create the filter and config
		// JwtAuthorizationHeaderFilter filter = new JwtAuthorizationHeaderFilter(null, null, null);

		// Simulate the ExpiredJwtException
		// doThrow(new ExpiredJwtException(null, null, "Expired JWT")).when(Jwts.parserBuilder().setSigningKey(key).build()).parseClaimsJws(anyString());

		// Apply the filter
		// Mono<Void> result = filter.apply(new JwtAuthorizationHeaderFilter.Config()).filter(exchange, chain);
		Mono<Void> result = jwtAuthorizationHeaderFilter.apply(new JwtAuthorizationHeaderFilter.Config())
			.filter(exchange, chain);


		// Verify that the response status is UNAUTHORIZED and the response is complete
		verify(response).setStatusCode(HttpStatus.UNAUTHORIZED);
		verify(response).setComplete();
		verify(chain, never()).filter(any(ServerWebExchange.class));
	}

	@Test
	void testValidTokenWithAccessTokenInfo() {

		// Mock the ServerWebExchange and ServerHttpRequest
		ServerWebExchange exchange = mock(ServerWebExchange.class);
		ServerHttpRequest originalRequest = mock(ServerHttpRequest.class);
		ServerHttpRequest.Builder requestBuilder = mock(ServerHttpRequest.Builder.class, RETURNS_SELF);
		ServerHttpRequest mutatedRequest = mock(ServerHttpRequest.class);
		HttpHeaders httpHeaders = new HttpHeaders();

		// Setup the behavior for the original request
		when(exchange.getRequest()).thenReturn(originalRequest);
		when(originalRequest.getHeaders()).thenReturn(httpHeaders);

		// Setup the behavior for the request builder
		when(originalRequest.mutate()).thenReturn(requestBuilder);
		when(requestBuilder.header(anyString(), anyString())).thenReturn(requestBuilder);
		when(requestBuilder.build()).thenReturn(mutatedRequest);

		// Mock the ServerWebExchange.Builder
		ServerWebExchange.Builder exchangeBuilder = mock(ServerWebExchange.Builder.class);
		when(exchange.mutate()).thenReturn(exchangeBuilder);
		when(exchangeBuilder.request(mutatedRequest)).thenReturn(exchangeBuilder);
		when(exchangeBuilder.build()).thenReturn(exchange);

		String token = Jwts.builder()
			.setHeaderParam("typ", "JWT")
			.claim("UUID", "123e4567-e89b-12d3-a456-426614174000")
			.signWith(key, SignatureAlgorithm.HS256)
			.compact();

		HttpHeaders headers = new HttpHeaders();
		headers.set(HttpHeaders.AUTHORIZATION, "Bearer " + token);

		when(originalRequest.getHeaders()).thenReturn(headers);

		// Mock AuthenticationService to return valid AccessTokenInfo
		AccessTokenInfo accessTokenInfo = AccessTokenInfo.of(
			"123e4567-e89b-12d3-a456-426614174000", // uuid
			"987e6543-e21c-34f2-a423-567817441000", // refreshTokenUUID
			1L, // membersId
			"ROLE_MEMBERS", // role
			LocalDateTime.of(2023, 10, 5, 12, 0) // expirationTime
		);
		when(authenticationService.getAccessToken(anyString())).thenReturn(accessTokenInfo);

		// Mock JWTUtil's behavior
		when(jwtUtil.getUUID(anyString())).thenReturn("123e4567-e89b-12d3-a456-426614174000");

		// Create a mock GatewayFilterChain
		GatewayFilterChain chain = mock(GatewayFilterChain.class);
		when(chain.filter(any(ServerWebExchange.class))).thenReturn(Mono.empty());

		// Apply the filter
		Mono<Void> result = filter.apply(new JwtAuthorizationHeaderFilter.Config()).filter(exchange, chain);

		// Verify that headers were set correctly and the chain filter was called
		verify(requestBuilder, times(1)).header("X-USER-ID", "1");
		verify(requestBuilder, times(1)).header("X-USER-ROLE", "ROLE_MEMBERS");
		verify(requestBuilder, times(1)).build();
		verify(chain, times(1)).filter(exchange);
		assertEquals(Mono.empty(), result);
	}

}

// package store.novabook.gateway.filter;
//
// import static org.junit.jupiter.api.Assertions.*;
// import static org.mockito.Mockito.*;
//
// import java.security.Key;
// import java.time.LocalDateTime;
// import java.util.UUID;
//
// import org.junit.jupiter.api.AfterEach;
// import org.junit.jupiter.api.BeforeEach;
// import org.junit.jupiter.api.Test;
// import org.junit.jupiter.api.extension.ExtendWith;
// import org.mockito.InjectMocks;
// import org.mockito.Mock;
// import org.mockito.MockedStatic;
// import org.mockito.junit.jupiter.MockitoExtension;
// import org.springframework.cloud.gateway.filter.GatewayFilterChain;
// import org.springframework.core.env.Environment;
// import org.springframework.http.HttpHeaders;
// import org.springframework.http.HttpStatus;
// import org.springframework.http.server.reactive.ServerHttpRequest;
// import org.springframework.http.server.reactive.ServerHttpResponse;
// import org.springframework.web.client.RestTemplate;
// import org.springframework.web.server.ServerWebExchange;
// import org.springframework.http.server.reactive.ServerHttpRequest.Builder;
// import org.springframework.web.server.ServerWebExchangeDecorator;
//
// import io.jsonwebtoken.Jwts;
// import io.jsonwebtoken.SignatureAlgorithm;
// import io.jsonwebtoken.io.Decoders;
// import io.jsonwebtoken.security.Keys;
// import reactor.core.publisher.Mono;
// import store.novabook.gateway.config.JWTUtil;
// import store.novabook.gateway.entity.AccessTokenInfo;
// import store.novabook.gateway.service.AuthenticationService;
// import store.novabook.gateway.util.KeyManagerUtil;
// import store.novabook.gateway.util.dto.JWTConfigDto;
//
// @ExtendWith(MockitoExtension.class)
// class JwtAuthorizationHeaderFilterTest {
//
// 	@Mock
// 	private JWTUtil jwtUtil;
//
// 	@Mock
// 	private AuthenticationService authenticationService;
//
// 	@Mock
// 	private Environment environment;
//
// 	@Mock
// 	private GatewayFilterChain filterChain;
//
// 	@InjectMocks
// 	private JwtAuthorizationHeaderFilter filter;
//
// 	private MockedStatic<KeyManagerUtil> keyManagerUtilMockedStatic;
//
// 	private JWTConfigDto jwtConfigDto;
//
// 	@InjectMocks
// 	private JwtAuthorizationHeaderFilter jwtAuthorizationHeaderFilter;
// 	private Key key;
//
//
//
// 	@BeforeEach
// 	void setUp() {
// 		jwtConfigDto = new JWTConfigDto("header",
// 			"JDJhJDEyJHk0RC5jVkM1UzcwMExHUlU4VXdFZmVPTkZ6cWlZclJOamdTMnVxV1F1VXEyRnYybXpFTFhX",
// 			86400);
// 		byte[] keyBytes = Decoders.BASE64.decode(jwtConfigDto.secret());
// 		key = Keys.hmacShaKeyFor(keyBytes);
//
// 		filter = new JwtAuthorizationHeaderFilter(authenticationService, jwtUtil, environment);
//
// 		keyManagerUtilMockedStatic = mockStatic(KeyManagerUtil.class);
// 		keyManagerUtilMockedStatic.when(() -> KeyManagerUtil.getJWTConfig(any(Environment.class), any(RestTemplate.class)))
// 			.thenReturn(jwtConfigDto);
// 	}
//
// 	@AfterEach
// 	void tearDown() {
// 		if (keyManagerUtilMockedStatic != null) {
// 			keyManagerUtilMockedStatic.close();
// 		}
// 	}
//
//
// 	@Test
// 	void testNoAuthorizationHeader() {
// 		// Create mock ServerWebExchange and ServerHttpRequest
// 		ServerWebExchange exchange = mock(ServerWebExchange.class);
// 		ServerHttpRequest request = mock(ServerHttpRequest.class);
// 		HttpHeaders headers = new HttpHeaders(); // No Authorization header
// 		when(exchange.getRequest()).thenReturn(request);
// 		when(request.getHeaders()).thenReturn(headers);
//
// 		// Create a mock GatewayFilterChain
// 		GatewayFilterChain chain = mock(GatewayFilterChain.class);
// 		when(chain.filter(any(ServerWebExchange.class))).thenReturn(Mono.empty());
//
// 		// Apply the filter
// 		Mono<Void> result = jwtAuthorizationHeaderFilter.apply(new JwtAuthorizationHeaderFilter.Config()).filter(exchange, chain);
//
// 		// Verify that the chain was called
// 		verify(chain, times(1)).filter(exchange);
// 		assertEquals(Mono.empty(), result);
// 	}
//
//
// 	@Test
// 	void testInvalidToken() {
//
// 		String token = Jwts.builder()
// 			.setHeaderParam("typ", "JWT")
// 			.claim("UUID", UUID.randomUUID())
// 			.signWith(key, SignatureAlgorithm.HS256)
// 			.compact();
//
// 		// Mock ServerWebExchange and ServerHttpRequest
// 		ServerWebExchange exchange = mock(ServerWebExchange.class);
// 		ServerHttpRequest request = mock(ServerHttpRequest.class);
// 		ServerHttpResponse response = mock(ServerHttpResponse.class);
// 		HttpHeaders headers = new HttpHeaders();
// 		headers.set(HttpHeaders.AUTHORIZATION, "Bearer " + token);
//
// 		when(exchange.getRequest()).thenReturn(request);
// 		when(request.getHeaders()).thenReturn(headers);
// 		when(exchange.getResponse()).thenReturn(response);
// 		when(response.setStatusCode(any(HttpStatus.class))).thenReturn(true);
//
// 		// Mock JWTUtil's behavior
// 		// when(jwtUtil.getUUID(anyString())).thenThrow(new JwtException("Invalid token"));
//
// 		// Create a mock GatewayFilterChain
// 		GatewayFilterChain chain = mock(GatewayFilterChain.class);
// 		// when(chain.filter(any(ServerWebExchange.class))).thenReturn(Mono.empty());
//
// 		// Apply the filter
// 		Mono<Void> result = jwtAuthorizationHeaderFilter.apply(new JwtAuthorizationHeaderFilter.Config()).filter(exchange, chain);
//
// 		// Verify that the response status is set to UNAUTHORIZED
// 		verify(response, times(1)).setStatusCode(HttpStatus.SEE_OTHER);
// 	}
//
//
// 	@Test
// 	void testValidTokenWithAccessTokenInfo() {
//
// 		// Mock the ServerWebExchange and ServerHttpRequest
// 		ServerWebExchange exchange = mock(ServerWebExchange.class);
// 		ServerHttpRequest originalRequest = mock(ServerHttpRequest.class);
// 		ServerHttpRequest.Builder requestBuilder = mock(ServerHttpRequest.Builder.class, RETURNS_SELF);
// 		ServerHttpRequest mutatedRequest = mock(ServerHttpRequest.class);
// 		HttpHeaders httpHeaders = new HttpHeaders();
//
// 		// Setup the behavior for the original request
// 		when(exchange.getRequest()).thenReturn(originalRequest);
// 		when(originalRequest.getHeaders()).thenReturn(httpHeaders);
//
// 		// Setup the behavior for the request builder
// 		when(originalRequest.mutate()).thenReturn(requestBuilder);
// 		when(requestBuilder.header(anyString(), anyString())).thenReturn(requestBuilder);
// 		when(requestBuilder.build()).thenReturn(mutatedRequest);
//
// 		// Setup the behavior for adding headers in the mutated request
// 		when(mutatedRequest.getHeaders()).thenReturn(httpHeaders);
//
// 		// Mock the mutation process to simulate header addition
// 		when(exchange.mutate()).thenAnswer(invocation -> {
// 			httpHeaders.add("X-USER-ID", Long.toString(1L)); // Example values
// 			httpHeaders.add("X-USER-ROLE", "ROLE_MEMBERS");
// 			return new ServerWebExchangeDecorator(exchange) {
// 				@Override
// 				public ServerHttpRequest getRequest() {
// 					return mutatedRequest;
// 				}
// 			};
// 		});
//
//
// 		String token = Jwts.builder()
// 			.setHeaderParam("typ", "JWT")
// 			.claim("UUID", "123e4567-e89b-12d3-a456-426614174000")
// 			.signWith(key, SignatureAlgorithm.HS256)
// 			.compact();
//
// 		ServerHttpRequest request = mock(ServerHttpRequest.class);
// 		HttpHeaders headers = new HttpHeaders();
// 		headers.set(HttpHeaders.AUTHORIZATION, "Bearer " + token);
//
// 		when(exchange.getRequest()).thenReturn(request);
// 		when(request.getHeaders()).thenReturn(headers);
//
// 		when(requestBuilder.build()).thenReturn(request);
//
// 		// Mock AuthenticationService to return valid AccessTokenInfo
// 		AccessTokenInfo accessTokenInfo = AccessTokenInfo.of(
// 			"123e4567-e89b-12d3-a456-426614174000", // uuid
// 			"987e6543-e21c-34f2-a423-567817441000", // refreshTokenUUID
// 			1L, // membersId
// 			"ROLE_MEMBERS", // role
// 			LocalDateTime.of(2023, 10, 5, 12, 0) // expirationTime
// 		);
// 		when(authenticationService.getAccessToken(anyString())).thenReturn(accessTokenInfo);
//
// 		// Mock JWTUtil's behavior
// 		when(jwtUtil.getUUID(anyString())).thenReturn("123e4567-e89b-12d3-a456-426614174000");
//
// 		// Create a mock GatewayFilterChain
// 		GatewayFilterChain chain = mock(GatewayFilterChain.class);
// 		when(chain.filter(any(ServerWebExchange.class))).thenReturn(Mono.empty());
//
// 		// Apply the filter
// 		Mono<Void> result = jwtAuthorizationHeaderFilter.apply(new JwtAuthorizationHeaderFilter.Config()).filter(exchange, chain);
//
// 		// Verify that headers were set correctly and the chain filter was called
// 		verify(requestBuilder, times(1)).header("X-USER-ID", "1");
// 		verify(requestBuilder, times(1)).header("X-USER-ROLE", "ROLE_MEMBERS");
// 		verify(requestBuilder, times(1)).build();
// 		verify(chain, times(1)).filter(exchange);
// 		assertEquals(Mono.empty(), result);
// 	}
// }
