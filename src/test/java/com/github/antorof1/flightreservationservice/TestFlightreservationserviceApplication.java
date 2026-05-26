package com.github.antorof1.flightreservationservice;

import org.springframework.boot.SpringApplication;

public class TestFlightreservationserviceApplication {

	public static void main(String[] args) {
		SpringApplication.from(FlightreservationserviceApplication::main).with(TestcontainersConfiguration.class).run(args);
	}

}
