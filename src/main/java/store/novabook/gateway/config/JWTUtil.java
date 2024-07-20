package store.novabook.gateway.config;

import javax.crypto.SecretKey;

import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import store.novabook.gateway.util.KeyManagerUtil;
import store.novabook.gateway.util.dto.JWTConfigDto;

@Component
public class JWTUtil {
	private final SecretKey secretKey;

	public JWTUtil(Environment env) {
		JWTConfigDto jwtConfigDto = KeyManagerUtil.getJWTConfig(env);
		byte[] keyBytes = Decoders.BASE64.decode(jwtConfigDto.secret());
		this.secretKey = Keys.hmacShaKeyFor(keyBytes);
	}

	public String getUUID(String token) {

		Claims claims = Jwts
			.parserBuilder()
			.setSigningKey(secretKey)
			.build()
			.parseClaimsJws(token)
			.getBody();

		return claims.get("uuid", String.class);
	}
}
