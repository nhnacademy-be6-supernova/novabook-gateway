package store.novabook.gateway.entity;

import java.io.Serializable;

import org.springframework.data.redis.core.RedisHash;

import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Setter
@RedisHash("JWTToken")
public class JWTTokenInfo implements Serializable {

	@NotNull
	private transient MembersInfo membersInfo;

	@NotNull
	private transient AccessTokenInfo accessTokenInfo;

	@NotNull
	private transient RefreshTokenInfo refreshTokenInfo;

	public JWTTokenInfo(MembersInfo membersInfo, AccessTokenInfo accessTokenInfo, RefreshTokenInfo refreshTokenInfo) {
		this.membersInfo = membersInfo;
		this.accessTokenInfo = accessTokenInfo;
		this.refreshTokenInfo = refreshTokenInfo;
	}

	public static JWTTokenInfo of(MembersInfo membersInfo, AccessTokenInfo accessTokenInfo,
		RefreshTokenInfo refreshTokenInfo) {
		return new JWTTokenInfo(membersInfo, accessTokenInfo, refreshTokenInfo);
	}
}
