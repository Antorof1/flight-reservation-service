package com.github.antorof1.flightreservationservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class FlightReservationServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(FlightReservationServiceApplication.class, args);
    }

}
