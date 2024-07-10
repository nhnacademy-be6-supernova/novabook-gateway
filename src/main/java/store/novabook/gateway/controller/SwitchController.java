package store.novabook.gateway.controller;

import java.net.URI;
import java.util.Collections;
import java.util.Map;

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
public class SwitchController {

	private final RouteDefinitionLocator routeDefinitionLocator;
	private final RouteDefinitionWriter routeDefinitionWriter;
	private final ApplicationEventPublisher publisher;

	private final Map<String, String> servicePorts = Map.of(
		"coupon-blue", "8070",
		"coupon-green", "8071",
		"store-blue", "8090",
		"store-green", "8091",
		"auth-blue", "8778",
		"auth-green", "8779"
	);

	@PostMapping("/switch")
	public Mono<ResponseEntity<String>> switchRoutes(@RequestParam("service") String service, @RequestParam("target") String target) {
		String targetId = service + "-" + target;
		String port = servicePorts.get(targetId);

		if (port == null) {
			return Mono.just(ResponseEntity.badRequest().body("Invalid service or target"));
		}

		return routeDefinitionLocator.getRouteDefinitions().collectList().flatMap(definitions -> {
			Mono<Void> deleteRoutes = Mono.empty();
			for (RouteDefinition route : definitions) {
				if (route.getId().startsWith(service)) {
					deleteRoutes = deleteRoutes.then(
						routeDefinitionWriter.delete(Mono.just(route.getId())).onErrorResume(e -> Mono.empty()));
				}
			}

			return deleteRoutes.then(Mono.defer(() -> {
				RouteDefinition newRoute = new RouteDefinition();
				newRoute.setId(targetId);
				newRoute.setUri(URI.create("http://127.0.0.1:" + port));
				newRoute.setPredicates(Collections.singletonList(new PredicateDefinition("Path=/api/v1/" + service + "/**")));
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
