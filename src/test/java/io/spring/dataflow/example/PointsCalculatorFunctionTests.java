package io.spring.dataflow.example;

import java.util.function.Function;

import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

@SpringBootTest(properties = "http.request.url-expression='https://example.com/points/'+payload.username")
class PointsCalculatorFunctionTests {

	@Autowired
	Function<Purchase, Points> pointsCalculator;

	@Test
	void functionWorks() {
		Points points = pointsCalculator.apply(new Purchase(4, "user"));
		assertThat(points).isEqualTo(new Points("user", 400));
		assertThat(pointsCalculator.apply(new Purchase(0, "user")))
				.isEqualTo(new Points("user", 0));
	}

}
