package io.spring.dataflow.example;

import java.math.BigDecimal;
import java.util.function.Consumer;
import java.util.function.Function;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Flux;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.fn.http.request.HttpRequestFunctionConfiguration;
import org.springframework.cloud.fn.http.request.HttpRequestFunctionConfiguration.HttpRequestFunction;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.GenericMessage;
import org.springframework.messaging.support.MessageBuilder;

@SpringBootApplication
@Import(HttpRequestFunctionConfiguration.class)
public class PointsCalculatorSinkApplication {

	private static final int POINTS_MULTIPLIER = 100;

	private static final Logger logger = LoggerFactory.getLogger(PointsCalculatorSinkApplication.class);

	public static void main(String[] args) {
		SpringApplication.run(PointsCalculatorSinkApplication.class, args);
	}

	@Bean
	Function<Purchase, Message<Points>> calculatePoints() {
		return purchase -> MessageBuilder
				.withPayload(new Points(BigDecimal.valueOf(purchase.getAmount())
					.setScale(2)
					.multiply(BigDecimal.valueOf(POINTS_MULTIPLIER))
					.intValue()))
				.setHeader("username", purchase.getUsername())
				.build();
	}

	@Bean
	Consumer<Message<Points>> postPoints(HttpRequestFunction httpRequestFunction) {
		return pointsMessage -> {
			httpRequestFunction
					.apply(Flux.just(pointsMessage))
					.doOnError(throwable -> logger.error(throwable.getMessage(), throwable))
					.blockFirst();
		};
	}
}
