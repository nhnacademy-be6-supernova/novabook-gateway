package store.novabook.gateway.service;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import store.novabook.gateway.entity.AuthenticationInfo;

@Service
@Transactional
@RequiredArgsConstructor
public class AuthService {

	private final RedisTemplate<String, Object> redisTemplate;

	public void saveAuth(AuthenticationInfo authenticationInfo) {
		if (Boolean.TRUE.equals(redisTemplate.hasKey(authenticationInfo.getUuid()))) {
			throw new IllegalArgumentException("Auth already exists for this uuid: " + authenticationInfo.getUuid());
		}
		redisTemplate.opsForValue().set(authenticationInfo.getUuid(), authenticationInfo);
	}

	public AuthenticationInfo getAuth(String uuid) {
		Object object = redisTemplate.opsForValue().get(uuid);
		if (object instanceof AuthenticationInfo) {
			return (AuthenticationInfo)object;
		} else {
			throw new IllegalArgumentException("No auth found with uuid: " + uuid);
		}
	}

	public boolean existsByUuid(String uuid) {
		return Boolean.TRUE.equals(redisTemplate.hasKey(uuid));
	}

	public Boolean deleteAuth(String uuid) {
		Boolean delete;
		Object object = redisTemplate.opsForValue().get(uuid);
		if (object instanceof AuthenticationInfo) {
			delete = redisTemplate.delete(uuid);
		} else {
			throw new IllegalArgumentException("No auth found with uuid: " + uuid);
		}
		return delete;
	}
}