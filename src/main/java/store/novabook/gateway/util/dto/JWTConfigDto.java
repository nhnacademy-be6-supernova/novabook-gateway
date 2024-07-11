package store.novabook.gateway.util.dto;

public record JWTConfigDto (
	String header,
	String secret,
	int tokenValidityInSeconds
) {
}
