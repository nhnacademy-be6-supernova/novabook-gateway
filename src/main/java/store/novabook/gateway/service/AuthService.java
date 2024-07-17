package store.novabook.gateway.service;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;
import store.novabook.gateway.entity.AccessTokenInfo;

@Service
@Transactional
@RequiredArgsConstructor
public class AuthService {

	private final RedisTemplate<String, Object> redisTemplate;
	private final ObjectMapper objectMapper;

	public AccessTokenInfo getAccessToken(String uuid) {
		Object object = redisTemplate.opsForValue().get(uuid);
		if (object == null) {
			return null;
		}
		try {
			String jsonString = objectMapper.writeValueAsString(object);
			return objectMapper.readValue(jsonString, AccessTokenInfo.class);
		} catch (Exception e) {
			throw new IllegalArgumentException("Failed to deserialize access token with uuid: " + uuid, e);
		}
	}

}