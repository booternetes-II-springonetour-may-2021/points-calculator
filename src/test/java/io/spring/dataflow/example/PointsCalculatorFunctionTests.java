package io.spring.dataflow.example;

import java.util.function.Function;

import org.junit.jupiter.api.Test;
import reactor.core.publisher.Flux;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

@SpringBootTest(properties = "http.request.url-expression='https://example.com'")
class PointsCalculatorFunctionTests {

	@Autowired
	Function<Double, Integer> pointsCalculator;

	@Test
	void functionWorks() {
		assertThat(pointsCalculator.apply(4.79)).isEqualTo(479);
		assertThat(pointsCalculator.apply(0.0)).isEqualTo(0);
	}

}
