package store.novabook.gateway.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.time.LocalDateTime;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import com.fasterxml.jackson.databind.ObjectMapper;

import store.novabook.gateway.entity.AccessTokenInfo;

class AuthenticationServiceTest {

	@InjectMocks
	private AuthenticationService authenticationService;

	@Mock
	private RedisTemplate<String, Object> redisTemplate;

	@Mock
	private ValueOperations<String, Object> valueOperations;

	@Mock
	private ObjectMapper objectMapper;

	@BeforeEach
	void setUp() {
		MockitoAnnotations.openMocks(this);
		when(redisTemplate.opsForValue()).thenReturn(valueOperations);
	}

	@Test
	void testGetAccessToken_Success() throws Exception {
		AccessTokenInfo mockAccessTokenInfo = AccessTokenInfo.of(
			"123e4567-e89b-12d3-a456-426614174000",
			"987e6543-e21c-34f2-a423-567817441000",
			1L,
			"USER",
			LocalDateTime.of(2023, 10, 5, 12, 0)
		);

		String jsonString = "{\"uuid\":\"00733359-d21c-4820-8e19-cc9ea07ea9c8\",\"accessTokenUUID\":\"fec0ab86-350b-4415-a015-120c2e0b4c74\",\"membersId\":34,\"role\":\"ROLE_MEMBERS\",\"expirationTime\":[2024,7,20,22,3,21,818000000],\"createdTime\":[2024,7,20,18,43,21,819193000]}";

		when(redisTemplate.opsForValue().get(mockAccessTokenInfo.getUuid())).thenReturn(mockAccessTokenInfo);
		when(objectMapper.writeValueAsString(any())).thenReturn(jsonString);
		when(objectMapper.readValue(jsonString, AccessTokenInfo.class)).thenReturn(mockAccessTokenInfo);

		AccessTokenInfo result = authenticationService.getAccessToken(mockAccessTokenInfo.getUuid());

		assertNotNull(result);
		assertEquals(mockAccessTokenInfo, result);
	}

	@Test
	void testGetAccessToken_NotFound() {
		String uuid = "non-existing-uuid";
		when(redisTemplate.opsForValue().get(uuid)).thenReturn(null);
		AccessTokenInfo result = authenticationService.getAccessToken(uuid);
		assertNull(result);
	}

	@Test
	void testGetAccessToken_DeserializationError() throws Exception {
		String uuid = "test-uuid";
		String jsonString = "{\"invalidJson\"}";

		when(redisTemplate.opsForValue().get(uuid)).thenReturn(jsonString);
		when(objectMapper.writeValueAsString(any()))
			.thenThrow(new IllegalArgumentException("Deserialization error"));
		when(objectMapper.readValue(jsonString, AccessTokenInfo.class))
			.thenThrow(new IllegalArgumentException("Deserialization error"));

		IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class, () -> {
			authenticationService.getAccessToken(uuid);
		});

		assertEquals("Failed to deserialize access token with uuid: " + uuid, thrown.getMessage());
	}
}
