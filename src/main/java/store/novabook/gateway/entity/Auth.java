package store.novabook.gateway.entity;

import java.io.Serializable;
import java.time.LocalDateTime;

import org.springframework.data.redis.core.RedisHash;

import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Setter
@RedisHash("auth")
public class Auth implements Serializable {

	@NotNull
	String uuid;

	@NotNull
	long usersId;

	@NotNull
	String role;

	LocalDateTime expirationTime;

	@Builder
	private Auth(String uuid, long usersId, String role, LocalDateTime expirationTime) {
		this.uuid = uuid;
		this.usersId = usersId;
		this.role = role;
		this.expirationTime = expirationTime;
	}

	public static Auth of(String uuid, long usersId, String role, LocalDateTime expirationTime) {
		return Auth.builder().uuid(uuid).usersId(usersId).role(role).expirationTime(LocalDateTime.now()).build();
	}
}