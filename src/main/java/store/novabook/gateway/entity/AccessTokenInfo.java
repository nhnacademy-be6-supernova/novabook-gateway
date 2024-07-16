package store.novabook.gateway.entity;

import java.io.Serializable;
import java.time.LocalDateTime;

import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Setter
public class AccessTokenInfo implements Serializable {

	@NotNull
	String uuid;

	@NotNull
	LocalDateTime expirationTime;

	@NotNull
	LocalDateTime createdTime;

	public AccessTokenInfo(String uuid, LocalDateTime expirationTime, LocalDateTime createdTime) {
		this.uuid = uuid;
		this.expirationTime = expirationTime;
		this.createdTime = createdTime;
	}

	public static AccessTokenInfo of(String uuid, LocalDateTime expirationTime) {
		return new AccessTokenInfo(uuid, expirationTime, LocalDateTime.now());
	}
}
