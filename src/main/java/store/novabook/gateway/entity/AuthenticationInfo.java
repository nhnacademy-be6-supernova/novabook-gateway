package store.novabook.gateway.entity;

import java.io.Serializable;
import java.time.LocalDateTime;

import org.springframework.data.redis.core.RedisHash;

import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Setter
@RedisHash("auth")
public class AuthenticationInfo implements Serializable {

	@NotNull
	String uuid;

	@NotNull
	long membersId;

	@NotNull
	String role;

	LocalDateTime expirationTime;

}