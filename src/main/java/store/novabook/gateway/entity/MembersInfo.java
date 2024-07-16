package store.novabook.gateway.entity;

import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Setter
public class MembersInfo {
	@NotNull
	long membersId;

	@NotNull
	String role;

	public MembersInfo(long membersId, String role) {
		this.membersId = membersId;
		this.role = role;
	}

	public static MembersInfo of(long membersId, String role) {
		return new MembersInfo(membersId, role);
	}
}
