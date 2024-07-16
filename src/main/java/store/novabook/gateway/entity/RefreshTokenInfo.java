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
public class RefreshTokenInfo implements Serializable {
	@NotNull
	String uuid;

	@NotNull
	LocalDateTime expirationTime;

	@NotNull
	LocalDateTime createdTime;

	public RefreshTokenInfo(String uuid, LocalDateTime expirationTime, LocalDateTime createdTime) {
		this.uuid = uuid;
		this.expirationTime = expirationTime;
		this.createdTime = createdTime;
	}

	public static RefreshTokenInfo of(String uuid, LocalDateTime expirationTime) {
		return new RefreshTokenInfo(uuid, expirationTime, LocalDateTime.now());
	}
}
