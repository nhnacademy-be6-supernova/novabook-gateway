package store.novabook.gateway.filter;

import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpHeaders;
import org.springframework.web.server.ServerWebExchange;

import reactor.core.publisher.Mono;

public class ResponseHeaderLoggingFilter implements GlobalFilter, Ordered {

	@Override
	public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
		return chain.filter(exchange).then(Mono.fromRunnable(() -> {
			HttpHeaders headers = exchange.getResponse().getHeaders();
			String authorizationHeader = headers.getFirst(HttpHeaders.AUTHORIZATION);
			System.out.println("Authorization Header: " + authorizationHeader);
		}));
	}

	@Override
	public int getOrder() {
		return -1;
	}
}