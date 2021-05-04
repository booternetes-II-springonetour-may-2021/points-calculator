package io.spring.dataflow.example;

import java.io.IOException;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import org.springframework.boot.WebApplicationType;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.cloud.stream.binder.test.InputDestination;
import org.springframework.cloud.stream.binder.test.TestChannelBinderConfiguration;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.http.HttpStatus;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;

import static org.assertj.core.api.Assertions.assertThat;


public class PointsCalculatorSinkTests {

	private MockWebServer server = new MockWebServer();

	@Test
	void worksAsConsumer() throws InterruptedException {
		MockResponse mockResponse = new MockResponse();
		mockResponse.setResponseCode(HttpStatus.OK.value());
		server.enqueue(mockResponse);

		try (ConfigurableApplicationContext context = new SpringApplicationBuilder(PointsCalculatorSinkApplication.class)
				.web(WebApplicationType.NONE).build()
				.run("--http.request.url-expression='" + url() + "/'+payload.username")) {
			Consumer<Points> postPoints = context.getBean(Consumer.class, "postPoints");
			postPoints.accept(new Points("user123", 579));
			RecordedRequest recordedRequest = server.takeRequest(3, TimeUnit.SECONDS);
			assertThat(recordedRequest.getBody()).asString()
					.isEqualTo("[text={\"username\":\"user123\",\"points\":579}]");
			assertThat(recordedRequest.getRequestUrl().encodedPath()).endsWith("/user123");
			assertThat(recordedRequest.getMethod()).isEqualTo("POST");
		}
	}

	@Test
	void worksAsSink() throws InterruptedException {
		MockResponse mockResponse = new MockResponse();
		mockResponse.setResponseCode(HttpStatus.OK.value());
		server.enqueue(mockResponse);
		try (ConfigurableApplicationContext context = new SpringApplicationBuilder(
				TestChannelBinderConfiguration.getCompleteConfiguration(PointsCalculatorSinkApplication.class))
				.web(WebApplicationType.NONE).build()
				.run("--http.request.url-expression='" + url() + "/'+payload.username")) {
			Message<?> message = MessageBuilder.withPayload(new Purchase(5, "user123"))
					.build();
			InputDestination inputDestination = context.getBean(InputDestination.class);
			inputDestination.send(message);
			RecordedRequest recordedRequest = server.takeRequest(3, TimeUnit.SECONDS);
			assertThat(recordedRequest.getRequestUrl().encodedPath()).endsWith("/user123");
			assertThat(recordedRequest.getBody()).asString()
					.isEqualTo("[text={\"username\":\"user123\",\"points\":500}]");
			assertThat(recordedRequest.getMethod()).isEqualTo("POST");
		}
	}

	@AfterEach
	void tearDown() throws IOException {
		server.shutdown();
	}

	private String url() {
		return String.format("http://localhost:%d", server.getPort());
	}
}

