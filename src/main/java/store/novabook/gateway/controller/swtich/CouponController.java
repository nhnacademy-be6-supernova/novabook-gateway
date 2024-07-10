package store.novabook.gateway.controller.swtich;

import java.net.URI;
import java.util.Collections;

import org.springframework.cloud.gateway.event.RefreshRoutesEvent;
import org.springframework.cloud.gateway.filter.FilterDefinition;
import org.springframework.cloud.gateway.handler.predicate.PredicateDefinition;
import org.springframework.cloud.gateway.route.RouteDefinition;
import org.springframework.cloud.gateway.route.RouteDefinitionLocator;
import org.springframework.cloud.gateway.route.RouteDefinitionWriter;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/v1/gateway")
@RequiredArgsConstructor
public class CouponController {

	private final RouteDefinitionLocator routeDefinitionLocator;
	private final RouteDefinitionWriter routeDefinitionWriter;
	private final ApplicationEventPublisher publisher;

	@PostMapping("/coupon/switch")
	public Mono<ResponseEntity<String>> switchRoutes(@RequestParam("target") String target) {
		return routeDefinitionLocator.getRouteDefinitions().collectList().flatMap(definitions -> {
			Mono<Void> deleteRoutes = Mono.empty();
			for (RouteDefinition route : definitions) {
				if (route.getId().equals("coupon-blue") || route.getId().equals("coupon-green")) {
					deleteRoutes = deleteRoutes.then(
						routeDefinitionWriter.delete(Mono.just(route.getId())).onErrorResume(e -> Mono.empty()));
				}
			}

			return deleteRoutes.then(Mono.defer(() -> {
				RouteDefinition newRoute = new RouteDefinition();
				if ("green".equals(target)) {
					newRoute.setId("coupon-green");
					newRoute.setUri(URI.create("http://127.0.0.1:8071"));
				} else {
					newRoute.setId("coupon-blue");
					newRoute.setUri(URI.create("http://127.0.0.1:8070"));
				}
				newRoute.setPredicates(Collections.singletonList(new PredicateDefinition("Path=/api/v1/coupon/**")));
				newRoute.setFilters(Collections.singletonList(new FilterDefinition("StripPrefix=1")));

				return routeDefinitionWriter.save(Mono.just(newRoute))
					.then(Mono.fromRunnable(() -> publisher.publishEvent(new RefreshRoutesEvent(this))))
					.thenReturn(ResponseEntity.ok("Routes switched to " + target));
			})).onErrorResume(e -> {
				e.printStackTrace();
				return Mono.just(ResponseEntity.status(500).body("Failed to switch routes: " + e.getMessage()));
			});
		});
	}
}
