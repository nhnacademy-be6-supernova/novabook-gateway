package store.novabook.gateway.service;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import store.novabook.gateway.entity.Auth;

@Service
@Transactional
@RequiredArgsConstructor
public class AuthService {

	private final RedisTemplate<String, Object> redisTemplate;

	public void saveAuth(Auth auth) {
		if (Boolean.TRUE.equals(redisTemplate.hasKey(auth.getUuid()))) {
			throw new IllegalArgumentException("Auth already exists for this uuid: " + auth.getUuid());
		}
		redisTemplate.opsForValue().set(auth.getUuid(), auth);
	}

	public Auth getAuth(String uuid) {
		Object object = redisTemplate.opsForValue().get(uuid);
		if (object instanceof Auth) {
			return (Auth)object;
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
		if (object instanceof Auth) {
			delete = redisTemplate.delete(uuid);
		} else {
			throw new IllegalArgumentException("No auth found with uuid: " + uuid);
		}
		return delete;
	}
}