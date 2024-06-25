/*
 * +++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
 * + Copyright 2024. NHN Academy Corp. All rights reserved.
 * + * While every precaution has been taken in the preparation of this resource,  assumes no
 * + responsibility for errors or omissions, or for damages resulting from the use of the information
 * + contained herein
 * + No part of this resource may be reproduced, stored in a retrieval system, or transmitted, in any
 * + form or by any means, electronic, mechanical, photocopying, recording, or otherwise, without the
 * + prior written permission.
 * +++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
 */

package store.novabook.gateway.config;

import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import lombok.RequiredArgsConstructor;
import store.novabook.gateway.filter.JwtAuthorizationHeaderFilter;

/**
 * @Author : marco@nhnacademy.com
 * @Date : 25/05/2023
 */
@RequiredArgsConstructor
@Configuration
public class RouteLocatorConfig {

	private final JwtAuthorizationHeaderFilter jwtAuthorizationHeaderFilter;

	@Bean
	public RouteLocator myRoute(RouteLocatorBuilder builder) {
		//TODO#1 router설정, gateway는 모든 요청의 진입점 입니다.

		return builder.routes()
			//TODO#1-1 localhost:8000/api/account/** 요청은 -> localhost:8100/api/account/** 라우팅 됩니다.
			//  .route("account-api", p -> p.path("/api/account/**")
			// 	 .filters(f -> f.filter(jwtAuthorizationHeaderFilter.apply(new JwtAuthorizationHeaderFilter.Config())))
			//  	.uri("http://127.0.0.1:8778")
			//  )
			.route("auth-service", p -> p.path("/auth/**")
				.filters(f -> f.filter(jwtAuthorizationHeaderFilter.apply(new JwtAuthorizationHeaderFilter.Config())))
				.uri("http://127.0.0.1:8778")
			)
			//TODO#1-2 shoppingmall-api 서버는 포트{8200,8300} 라운드로빈 방식으로(50:50 비율로) 로드밸런싱 됩니다.
			// .route("user-service", p -> p.path("/auth/**")
			// 	//TODO#1-4 shoppingmall-api 서버에 jwt 검증이 필요하다면 설정해주세요.
			// 	.and()
			// 	.weight("user-service", 1)
			// 	// .filters(f -> f.filter(jwtAuthorizationHeaderFilter.apply(new JwtAuthorizationHeaderFilter.Config())))
			// 	.uri("http://127.0.0.1:8090")
			// )
			.route("store", p -> p.path("/api/v1/store/**")
				//TODO#1-4 shoppingmall-api 서버에 jwt 검증이 필요하다면 설정해주세요.
				.and()
				.weight("store", 1)
				// .filters(f -> f.filter(jwtAuthorizationHeaderFilter.apply(new JwtAuthorizationHeaderFilter.Config())))
				.uri("http://127.0.0.1:8090")
			)
			// .route("shoppingmall-api", p -> p.path("/api/v1/store/**").
			// 	and()
			// 	.weight("shoppingmall-api", 50)
			// 	.uri("http://localhost:8300"))
			.build();

	}
}
