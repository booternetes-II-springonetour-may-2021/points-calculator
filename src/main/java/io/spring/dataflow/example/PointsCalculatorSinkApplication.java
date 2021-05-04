package io.spring.dataflow.example;

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
import org.springframework.messaging.support.GenericMessage;

@SpringBootApplication
@Import(HttpRequestFunctionConfiguration.class)
public class PointsCalculatorSinkApplication {

	private static final int POINTS_MULTIPLIER = 100;

	private static final Logger logger = LoggerFactory.getLogger(PointsCalculatorSinkApplication.class);

	public static void main(String[] args) {
		SpringApplication.run(PointsCalculatorSinkApplication.class, args);
	}

	@Bean
	Function<Purchase, Points> calculatePoints() {
		return purchase -> new Points(purchase.getUsername(), purchase.getAmount() * POINTS_MULTIPLIER);
	}

	@Bean
	Consumer<Points> postPoints(HttpRequestFunction httpRequestFunction) {
		return points -> httpRequestFunction
				.apply(Flux.just(new GenericMessage<>(points)))
				.doOnError(throwable -> logger.error(throwable.getMessage(), throwable))
				.blockFirst();
	}
}
