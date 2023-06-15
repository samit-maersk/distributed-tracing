package com.example.springboot2x;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerResponse;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Arrays;
import java.util.stream.Collectors;

@SpringBootApplication
@Slf4j
public class Springboot2xApplication {

	public static void main(String[] args) {
		SpringApplication.run(Springboot2xApplication.class, args);
	}

	@Bean
	RouterFunction routes() {
		return RouterFunctions
				.route()
				.GET("/timezones", request -> {
					/*
					EST - -05:00
					HST - -10:00
					MST - -07:00
					ACT - Australia/Darwin
					AET - Australia/Sydney
					AGT - America/Argentina/Buenos_Aires
					ART - Africa/Cairo
					AST - America/Anchorage
					BET - America/Sao_Paulo
					BST - Asia/Dhaka
					CAT - Africa/Harare
					CNT - America/St_Johns
					CST - America/Chicago
					CTT - Asia/Shanghai
					EAT - Africa/Addis_Ababa
					ECT - Europe/Paris
					IET - America/Indiana/Indianapolis
					IST - Asia/Kolkata
					JST - Asia/Tokyo
					MIT - Pacific/Apia
					NET - Asia/Yerevan
					NST - Pacific/Auckland
					PLT - Asia/Karachi
					PNT - America/Phoenix
					PRT - America/Puerto_Rico
					PST - America/Los_Angeles
					SST - Pacific/Guadalcanal
					VST - Asia/Ho_Chi_Minh
					 */
					var zones =  ZoneId.SHORT_IDS
							.keySet()
							.stream()
							.collect(Collectors.toMap(k -> k, k -> ZoneId.SHORT_IDS.get(k)));
					return ServerResponse.ok().bodyValue(zones);
				})
				.GET("/time", req -> {
					return req.queryParam("tz")
							.map(tz -> {
								return Arrays.stream(tz.split(","))
										.map(String::trim)
										.map(String::toUpperCase)
										.map(tzName -> {
											if(ZoneId.SHORT_IDS.containsKey(tzName)) {
												var zone = ZoneId.SHORT_IDS.get(tzName);
												return new Timestamp(tzName, LocalDateTime.now(ZoneId.of(zone)).toString());
											}
											return new Timestamp(tzName,null);
										})
										.filter(timestamp -> timestamp.time() != null)
										.collect(Collectors.toMap(Timestamp::id, Timestamp::time));
							})
							.map(timestamp -> ServerResponse.ok().bodyValue(timestamp))
							.orElse(ServerResponse.badRequest().build());
				})
				.after((req, res) -> {
					log.info("{} {} {}", req.method(), req.path(), res.statusCode());
					return res;
				})
				.build();
	}
}

record Timestamp(String id, String time) {}
