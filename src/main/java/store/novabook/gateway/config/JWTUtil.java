package store.novabook.gateway.config;

import javax.crypto.SecretKey;

import org.springframework.beans.factory.InitializingBean;
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
public class JWTUtil implements InitializingBean {
	private final Environment env;
	private SecretKey secretKey;

	@Override
	public void afterPropertiesSet() {
		RestTemplate restTemplate = new RestTemplate();
		JWTConfigDto jwtConfigDto = KeyManagerUtil.getJWTConfig(env, restTemplate);
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
