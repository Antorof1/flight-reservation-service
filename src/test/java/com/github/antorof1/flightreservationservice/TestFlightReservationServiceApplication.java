package com.github.antorof1.flightreservationservice;

import org.springframework.boot.SpringApplication;

public class TestFlightReservationServiceApplication {

    public static void main(String[] args) {
        SpringApplication.from(FlightReservationServiceApplication::main).with(TestcontainersConfiguration.class)
            .run(args);
    }

}
