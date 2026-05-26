package com.github.antorof1.flightreservationservice;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

@Import(TestcontainersConfiguration.class)
@SpringBootTest
class FlightReservationServiceApplicationTests {

	@Test
	void contextLoads() {
	}

}
