package store.novabook.gateway.service;

import java.util.HashMap;
import java.util.Map;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.extern.slf4j.Slf4j;
import store.novabook.gateway.entity.AccessTokenInfo;
import store.novabook.gateway.entity.JWTTokenInfo;
import store.novabook.gateway.entity.MembersInfo;
import store.novabook.gateway.entity.RefreshTokenInfo;

@Slf4j
@Service
public class JWTTokenService {

	private final RedisTemplate<String, Object> redisTemplate;
	private final ObjectMapper objectMapper;
	private static final String MEMBERS_INFO = "membersInfo";
	private static final String ACCESS_TOKEN_INFO = "accessTokenInfo";
	private static final String REFRESH_TOKEN_INFO = "refreshTokenInfo";

	public JWTTokenService(RedisTemplate<String, Object> redisTemplate, ObjectMapper objectMapper) {
		this.redisTemplate = redisTemplate;
		this.objectMapper = objectMapper;
	}

	public void saveJWTTokenInfo(String key, JWTTokenInfo jwtTokenInfo) throws JsonProcessingException {
		Map<String, String> jwtTokenInfoMap = serializeJWTTokenInfo(jwtTokenInfo);
		redisTemplate.opsForHash().putAll(key, jwtTokenInfoMap);
	}

	public JWTTokenInfo getJWTTokenInfo(String key) {
		try {
			Map<Object, Object> entries = redisTemplate.opsForHash().entries(key);
			String membersInfoJson = (String)entries.get(MEMBERS_INFO);
			String accessTokenInfoJson = (String)entries.get(ACCESS_TOKEN_INFO);
			String refreshTokenInfoJson = (String)entries.get(REFRESH_TOKEN_INFO);

			MembersInfo membersInfo = objectMapper.readValue(membersInfoJson, MembersInfo.class);
			AccessTokenInfo accessTokenInfo = objectMapper.readValue(accessTokenInfoJson, AccessTokenInfo.class);
			RefreshTokenInfo refreshTokenInfo = objectMapper.readValue(refreshTokenInfoJson, RefreshTokenInfo.class);

			return new JWTTokenInfo(membersInfo, accessTokenInfo, refreshTokenInfo);
		} catch (JsonProcessingException e) {
			log.error("Error while getting members info from redis", e);
			throw new RuntimeException(e);
		}
	}

	public MembersInfo getMembersInfo(String key) {
		try {
			Map<Object, Object> entries = redisTemplate.opsForHash().entries(key);
			String membersInfoJson = (String)entries.get(MEMBERS_INFO);

			if (membersInfoJson != null) {
				return objectMapper.readValue(membersInfoJson, MembersInfo.class);
			} else {
				return null;
			}
		} catch (JsonProcessingException e) {
			log.error("Error while getting members info from redis", e);
			throw new RuntimeException(e);
		}
	}

	public AccessTokenInfo getAccessTokenInfo(String key) {
		try {
			Map<Object, Object> entries = redisTemplate.opsForHash().entries(key);

			String accessTokenInfoJson = (String)entries.get(ACCESS_TOKEN_INFO);

			if (accessTokenInfoJson != null) {
				return objectMapper.readValue(accessTokenInfoJson, AccessTokenInfo.class);
			} else {
				return null;
			}
		} catch (JsonProcessingException e) {
			log.error("Error while getting members info from redis", e);
			throw new RuntimeException(e);
		}

	}

	public RefreshTokenInfo getRefreshTokenInfo(String key) {
		try {
			Map<Object, Object> entries = redisTemplate.opsForHash().entries(key);

			String refreshTokenInfoJson = (String)entries.get(REFRESH_TOKEN_INFO);

			if (refreshTokenInfoJson != null) {
				return objectMapper.readValue(refreshTokenInfoJson, RefreshTokenInfo.class);
			} else {
				return null;
			}
		} catch (JsonProcessingException e) {
			log.error("Error while getting members info from redis", e);
			throw new RuntimeException(e);
		}
	}

	public boolean existsByUuid(String uuid) {
		return Boolean.TRUE.equals(redisTemplate.hasKey(uuid));
	}

	private Map<String, String> serializeJWTTokenInfo(JWTTokenInfo jwtTokenInfo) throws JsonProcessingException {
		Map<String, String> map = new HashMap<>();
		map.put(MEMBERS_INFO, objectMapper.writeValueAsString(jwtTokenInfo.getMembersInfo()));
		map.put(ACCESS_TOKEN_INFO, objectMapper.writeValueAsString(jwtTokenInfo.getAccessTokenInfo()));
		map.put(REFRESH_TOKEN_INFO, objectMapper.writeValueAsString(jwtTokenInfo.getRefreshTokenInfo()));
		return map;
	}
}
