package store.novabook.gateway.filter;

import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.core.Ordered;
import org.springframework.web.server.ServerWebExchange;

import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

@Slf4j
public class CustomGlobalFilter implements org.springframework.cloud.gateway.filter.GlobalFilter, Ordered {

    //TODO#2 global filter입니다.
    //특정 url에 상관없이 전역적으로 동작합니다.

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        //TODO#2-1 모든 요청에 대해서 global filter로그가 출력됩니다.
        log.debug("global filter");
        return chain.filter(exchange);
    }

    @Override
    public int getOrder() {
        //TODO#2-2 Ordered interface를 구현함으로써 filter의 우선순위를 설정할 수 있습니다.
        return -1;
    }

}