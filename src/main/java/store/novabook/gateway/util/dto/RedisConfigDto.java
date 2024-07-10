package store.novabook.gateway.util.dto;

public record RedisConfigDto(
	String host,
	int database,
	String password,
	int port
) {
}
