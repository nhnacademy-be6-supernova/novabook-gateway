package store.novabook.gateway.util;

import java.util.Map;

import org.springframework.core.ParameterizedTypeReference;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.validation.constraints.NotNull;
import lombok.extern.slf4j.Slf4j;
import store.novabook.gateway.exception.KeyManagerException;
import store.novabook.gateway.util.dto.JWTConfigDto;
import store.novabook.gateway.util.dto.RedisConfigDto;
@Slf4j
public class KeyManagerUtil {
	private static final ObjectMapper objectMapper = new ObjectMapper();

	private KeyManagerUtil() {
	}

	private static String getDataSource(Environment environment, String keyid) {

		String appkey = environment.getProperty("nhn.cloud.keyManager.appkey");
		String userId = environment.getProperty("nhn.cloud.keyManager.userAccessKey");
		String secretKey = environment.getProperty("nhn.cloud.keyManager.secretAccessKey");

		RestTemplate restTemplate = new RestTemplate();
		String baseUrl = "https://api-keymanager.nhncloudservice.com/keymanager/v1.2/appkey/{appkey}/secrets/{keyid}";
		String url = baseUrl.replace("{appkey}", appkey).replace("{keyid}", keyid);
		HttpHeaders headers = new HttpHeaders();
		headers.set("X-TC-AUTHENTICATION-ID", userId);
		headers.set("X-TC-AUTHENTICATION-SECRET", secretKey);

		HttpEntity<String> entity = new HttpEntity<>(headers);

		ResponseEntity<Map<String, Object>> response = restTemplate.exchange(url, HttpMethod.GET, entity,
			new ParameterizedTypeReference<>() {
			});

		var body = getStringObjectMap(response);

		String result = (String)body.get("secret");
		if (result.isEmpty()) {
			log.error("\"secret\" key is missing in responsxcle body");
			log.error("{}", body);
			throw new KeyManagerException("\"secret\" key is missing in responsxcle body");
		}

		return result;
	}

	private static @NotNull Map<String, Object> getStringObjectMap(ResponseEntity<Map<String, Object>> response) {
		if (response.getBody() == null) {
			throw new KeyManagerException("response.getBody() is null");
		}
		Object bodyObj = response.getBody().get("body");

		Map<String, Object> body;
		try {
			body = TypeUtil.castMap(bodyObj, String.class, Object.class);
		} catch (ClassCastException e) {
			throw new KeyManagerException(e.getMessage());
		}

		return body;
	}

	public static RedisConfigDto getRedisConfig(Environment environment) {
		try {
			String keyid = environment.getProperty("nhn.cloud.keyManager.redisKey");
			return objectMapper.readValue(getDataSource(environment, keyid), RedisConfigDto.class);
		} catch (JsonProcessingException e) {
			//오류처리
			throw new KeyManagerException(e.getMessage());
		}
	}

	public static JWTConfigDto getJWTConfig(Environment environment) {
		try {
			String keyid = environment.getProperty("nhn.cloud.keyManager.jwtKey");
			return objectMapper.readValue(getDataSource(environment, keyid), JWTConfigDto.class);
		} catch (JsonProcessingException e) {
			//오류처리
			throw new KeyManagerException(e.getMessage());
		}
	}

}
