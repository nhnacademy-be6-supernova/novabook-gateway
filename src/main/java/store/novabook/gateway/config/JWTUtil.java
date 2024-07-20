package store.novabook.gateway.config;

import java.util.Date;
import java.util.Objects;

import javax.crypto.SecretKey;

import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import store.novabook.gateway.util.KeyManagerUtil;
import store.novabook.gateway.util.dto.JWTConfigDto;

@Component
@RequiredArgsConstructor
public class JWTUtil {
	private final Environment env;
	private JWTConfigDto jwtConfigDto;
	private SecretKey secretKey;

	public String getUUID(String token) {
		if (Objects.isNull(jwtConfigDto)) {
			RestTemplate restTemplate = new RestTemplate();
			jwtConfigDto = KeyManagerUtil.getJWTConfig(env, restTemplate);
			byte[] keyBytes = Decoders.BASE64.decode(jwtConfigDto.secret());
			this.secretKey = Keys.hmacShaKeyFor(keyBytes);
		}

		Claims claims = Jwts
			.parserBuilder()
			.setSigningKey(secretKey)
			.build()
			.parseClaimsJws(token)
			.getBody();

		return claims.get("uuid", String.class);
	}
}
