package store.novabook.gateway.controller.swtich;

import java.net.URI;
import java.util.Collections;
import java.util.List;

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
	public ResponseEntity<String> switchRoutes(@RequestParam("target") String target) {
		List<RouteDefinition> definitions = routeDefinitionLocator.getRouteDefinitions().collectList().block();
		if (definitions != null) {
			definitions.forEach(route -> {
				if (route.getId().equals("coupon-blue") || route.getId().equals("coupon-green")) {
					try {
						routeDefinitionWriter.delete(Mono.just(route.getId())).block();
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			});
		}

		if ("green".equals(target)) {
			RouteDefinition greenRoute = new RouteDefinition();
			greenRoute.setId("coupon-green");
			greenRoute.setUri(URI.create("http://127.0.0.1:8071"));
			greenRoute.setPredicates(Collections.singletonList(new PredicateDefinition("Path=/api/v1/coupon/**")));
			greenRoute.setFilters(Collections.singletonList(new FilterDefinition("StripPrefix=1")));
			routeDefinitionWriter.save(Mono.just(greenRoute)).block();
		} else {
			RouteDefinition blueRoute = new RouteDefinition();
			blueRoute.setId("coupon-blue");
			blueRoute.setUri(URI.create("http://127.0.0.1:8070"));
			blueRoute.setPredicates(Collections.singletonList(new PredicateDefinition("Path=/api/v1/coupon/**")));
			blueRoute.setFilters(Collections.singletonList(new FilterDefinition("StripPrefix=1")));
			routeDefinitionWriter.save(Mono.just(blueRoute)).block();
		}

		publisher.publishEvent(new RefreshRoutesEvent(this));
		return ResponseEntity.ok("Routes switched to " + target);
	}
}
