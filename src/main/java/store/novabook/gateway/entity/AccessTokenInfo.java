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
	String refreshTokenUUID;

	@NotNull
	long membersId;

	@NotNull
	private String role;

	@NotNull
	LocalDateTime expirationTime;

	@NotNull
	LocalDateTime createdTime;

	public AccessTokenInfo(String uuid, String refreshTokenUUID, long membersId, String role,
		LocalDateTime expirationTime, LocalDateTime createdTime) {
		this.uuid = uuid;
		this.refreshTokenUUID = refreshTokenUUID;
		this.membersId = membersId;
		this.role = role;
		this.expirationTime = expirationTime;
		this.createdTime = createdTime;
	}

	public static AccessTokenInfo of(String uuid, String refreshTokenUUID, long membersId, String role,
		LocalDateTime expirationTime) {
		return new AccessTokenInfo(uuid, refreshTokenUUID, membersId, role, expirationTime, LocalDateTime.now());
	}
}
