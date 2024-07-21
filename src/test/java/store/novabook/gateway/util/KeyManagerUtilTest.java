package store.novabook.gateway.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.*;
import static org.mockito.Mockito.when;

import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import store.novabook.gateway.exception.KeyManagerException;
import store.novabook.gateway.util.dto.JWTConfigDto;
import store.novabook.gateway.util.dto.RedisConfigDto;

class KeyManagerUtilTest {

	@Mock
	private Environment environment;

	@Mock
	private RestTemplate restTemplate;

	@BeforeEach
	void setUp() {
		MockitoAnnotations.openMocks(this);
	}

	@Test
	void testGetRedisConfig() {
		// Mock environment properties
		when(environment.getProperty("nhn.cloud.keyManager.appkey")).thenReturn("appkey");
		when(environment.getProperty("nhn.cloud.keyManager.userAccessKey")).thenReturn("userId");
		when(environment.getProperty("nhn.cloud.keyManager.secretAccessKey")).thenReturn("secretKey");
		when(environment.getProperty("nhn.cloud.keyManager.redisKey")).thenReturn("redisKey");

		// Mock RestTemplate response
		Map<String, Object> responseBody = new HashMap<>();
		responseBody.put("body", Map.of("secret", "{\"host\":\"localhost\",\"database\":0,\"password\":\"password\",\"port\":6379}"));
		ResponseEntity<Map<String, Object>> responseEntity = new ResponseEntity<>(responseBody, HttpStatus.OK);
		when(restTemplate.exchange(any(String.class), any(HttpMethod.class), any(HttpEntity.class), any(
			ParameterizedTypeReference.class)))
			.thenReturn(responseEntity);

		// Test getRedisConfig
		RedisConfigDto expectedDto = new RedisConfigDto("localhost", 0, "password", 6379);
		RedisConfigDto actualDto = KeyManagerUtil.getRedisConfig(environment, restTemplate);

		assertEquals(expectedDto, actualDto);
	}

	@Test
	void testGetJWTConfig() {
		// Mock environment properties
		when(environment.getProperty("nhn.cloud.keyManager.appkey")).thenReturn("appkey");
		when(environment.getProperty("nhn.cloud.keyManager.userAccessKey")).thenReturn("userId");
		when(environment.getProperty("nhn.cloud.keyManager.secretAccessKey")).thenReturn("secretKey");
		when(environment.getProperty("nhn.cloud.keyManager.jwtKey")).thenReturn("jwtKey");

		// Mock RestTemplate response
		Map<String, Object> responseBody = new HashMap<>();
		responseBody.put("body", Map.of("secret", "{\"header\":\"Authorization\",\"secret\":\"jwtSecret\",\"tokenValidityInSeconds\":3600}"));
		ResponseEntity<Map<String, Object>> responseEntity = new ResponseEntity<>(responseBody, HttpStatus.OK);
		when(restTemplate.exchange(any(String.class), any(HttpMethod.class), any(HttpEntity.class), any(ParameterizedTypeReference.class)))
			.thenReturn(responseEntity);

		// Test getJWTConfig
		JWTConfigDto expectedDto = new JWTConfigDto("Authorization", "jwtSecret", 3600);
		JWTConfigDto actualDto = KeyManagerUtil.getJWTConfig(environment, restTemplate);

		assertEquals(expectedDto, actualDto);
	}

	@Test
	void testGetRedisConfig_ThrowsException() {
		// Mock environment properties
		when(environment.getProperty("nhn.cloud.keyManager.appkey")).thenReturn("appkey");
		when(environment.getProperty("nhn.cloud.keyManager.userAccessKey")).thenReturn("userId");
		when(environment.getProperty("nhn.cloud.keyManager.secretAccessKey")).thenReturn("secretKey");
		when(environment.getProperty("nhn.cloud.keyManager.redisKey")).thenReturn("redisKey");

		// Mock RestTemplate response with invalid data
		Map<String, Object> responseBody = new HashMap<>();
		responseBody.put("body", null);
		ResponseEntity<Map<String, Object>> responseEntity = new ResponseEntity<>(responseBody, HttpStatus.OK);
		when(restTemplate.exchange(any(String.class), any(HttpMethod.class), any(HttpEntity.class), any(ParameterizedTypeReference.class)))
			.thenReturn(responseEntity);

		// Test getRedisConfig exception
		assertThrows(KeyManagerException.class, () -> KeyManagerUtil.getRedisConfig(environment, restTemplate));
	}
	
	@Test
	void testGetJWTConfig_ThrowsException() {
		// Mock environment properties
		when(environment.getProperty("nhn.cloud.keyManager.appkey")).thenReturn("appkey");
		when(environment.getProperty("nhn.cloud.keyManager.userAccessKey")).thenReturn("userId");
		when(environment.getProperty("nhn.cloud.keyManager.secretAccessKey")).thenReturn("secretKey");
		when(environment.getProperty("nhn.cloud.keyManager.jwtKey")).thenReturn("jwtKey");

		// Mock RestTemplate response with invalid data
		Map<String, Object> responseBody = new HashMap<>();
		responseBody.put("body", Map.of("invalidKey", "invalidValue"));
		ResponseEntity<Map<String, Object>> responseEntity = new ResponseEntity<>(responseBody, HttpStatus.OK);
		when(restTemplate.exchange(any(String.class), any(HttpMethod.class), any(HttpEntity.class), any(ParameterizedTypeReference.class)))
			.thenReturn(responseEntity);

		// Test getJWTConfig exception
		assertThrows(KeyManagerException.class, () -> KeyManagerUtil.getJWTConfig(environment, restTemplate));
	}

	@Test
	void testGetRedisConfig_NullResponseBody() {
		// Mock environment properties
		when(environment.getProperty("nhn.cloud.keyManager.appkey")).thenReturn("appkey");
		when(environment.getProperty("nhn.cloud.keyManager.userAccessKey")).thenReturn("userId");
		when(environment.getProperty("nhn.cloud.keyManager.secretAccessKey")).thenReturn("secretKey");
		when(environment.getProperty("nhn.cloud.keyManager.redisKey")).thenReturn("redisKey");

		// Mock RestTemplate response with null body
		ResponseEntity<Map<String, Object>> responseEntity = new ResponseEntity<>(null, HttpStatus.OK);
		when(restTemplate.exchange(any(String.class), any(HttpMethod.class), any(HttpEntity.class), any(ParameterizedTypeReference.class)))
			.thenReturn(responseEntity);

		// Test getRedisConfig exception
		assertThrows(KeyManagerException.class, () -> KeyManagerUtil.getRedisConfig(environment, restTemplate));
	}

	@Test
	void testGetJWTConfig_NullResponseBody() {
		// Mock environment properties
		when(environment.getProperty("nhn.cloud.keyManager.appkey")).thenReturn("appkey");
		when(environment.getProperty("nhn.cloud.keyManager.userAccessKey")).thenReturn("userId");
		when(environment.getProperty("nhn.cloud.keyManager.secretAccessKey")).thenReturn("secretKey");
		when(environment.getProperty("nhn.cloud.keyManager.jwtKey")).thenReturn("jwtKey");

		// Mock RestTemplate response with null body
		ResponseEntity<Map<String, Object>> responseEntity = new ResponseEntity<>(null, HttpStatus.OK);
		when(restTemplate.exchange(any(String.class), any(HttpMethod.class), any(HttpEntity.class), any(ParameterizedTypeReference.class)))
			.thenReturn(responseEntity);

		// Test getJWTConfig exception
		assertThrows(KeyManagerException.class, () -> KeyManagerUtil.getJWTConfig(environment, restTemplate));
	}

	@Test
	void getRedisConfig_jsonProcessingException_throwsException() {
		// given
		String redisKey = "testRedisKey";
		given(environment.getProperty("nhn.cloud.keyManager.redisKey")).willReturn(redisKey);
		when(environment.getProperty("nhn.cloud.keyManager.appkey")).thenReturn("appkey");

		// 가짜 응답 데이터 설정 - 잘못된 JSON 형식
		Map<String, Object> bodyMap = new HashMap<>();
		bodyMap.put("body", Map.of("secret", "{\"host\":\"localhost\",\"port\":\"invalid_port\",\"username\":\"user\",\"password\":\"password\"}"));
		ResponseEntity<Map<String, Object>> responseEntity = new ResponseEntity<>(bodyMap, HttpStatus.OK);
		given(restTemplate.exchange(any(String.class), eq(HttpMethod.GET), any(HttpEntity.class),
			eq(new ParameterizedTypeReference<Map<String, Object>>() {})))
			.willReturn(responseEntity);

		// when, then
		assertThrows(KeyManagerException.class, () -> KeyManagerUtil.getRedisConfig(environment, restTemplate));
	}

	@Test
	void getJWTConfig_jsonProcessingException_throwsException() {
		// given
		String jwtConfig = "testjwtKey";
		given(environment.getProperty("nhn.cloud.keyManager.jwtKey")).willReturn(jwtConfig);
		when(environment.getProperty("nhn.cloud.keyManager.appkey")).thenReturn("appkey");

		// 가짜 응답 데이터 설정 - 잘못된 JSON 형식
		Map<String, Object> bodyMap = new HashMap<>();
		bodyMap.put("body", Map.of("secret", "{\"host\":\"localhost\",\"port\":\"invalid_port\",\"username\":\"user\",\"password\":\"password\"}"));
		ResponseEntity<Map<String, Object>> responseEntity = new ResponseEntity<>(bodyMap, HttpStatus.OK);
		given(restTemplate.exchange(any(String.class), eq(HttpMethod.GET), any(HttpEntity.class),
			eq(new ParameterizedTypeReference<Map<String, Object>>() {})))
			.willReturn(responseEntity);

		// when, then
		assertThrows(KeyManagerException.class, () -> KeyManagerUtil.getJWTConfig(environment, restTemplate));
	}


}
