package io.spring.dataflow.example;

import java.util.function.Function;

import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.messaging.Message;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

@SpringBootTest(properties = "http.request.url-expression='https://example.com/' + headers['username']")
class PointsCalculatorFunctionTests {

	@Autowired
	Function<Purchase, Message<Points>> pointsCalculator;

	@Test
	void functionWorks() {
		Message<Points> message = pointsCalculator.apply(new Purchase(4.79, "user"));
		assertThat(message.getPayload()).isEqualTo(new Points(479));
		assertThat(message.getHeaders().get("username")).isEqualTo("user");
		assertThat(pointsCalculator.apply(new Purchase(0.0, "user")).getPayload()).isEqualTo(new Points(0));
	}

}
