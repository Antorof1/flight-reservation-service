package com.github.antorof1.flightreservationservice;

import com.github.antorof1.flightreservationservice.factory.FlightTestDataFactory;
import com.github.antorof1.flightreservationservice.factory.UserTestDataFactory;
import com.github.antorof1.flightreservationservice.flight.FlightRepository;
import com.github.antorof1.flightreservationservice.reservation.ReservationRepository;
import com.github.antorof1.flightreservationservice.seat.SeatRepository;
import com.github.antorof1.flightreservationservice.user.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webtestclient.autoconfigure.AutoConfigureWebTestClient;
import org.springframework.context.annotation.Import;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.test.web.reactive.server.WebTestClient;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebTestClient
@Import(TestcontainersConfiguration.class)
public abstract class AbstractIntegrationTest {
    @Autowired
    protected WebTestClient testClient;

    @Autowired
    protected FlightTestDataFactory flightFactory;

    @Autowired
    protected UserTestDataFactory userFactory;

    @Autowired
    protected UserRepository userRepository;

    @Autowired
    protected FlightRepository flightRepository;

    @Autowired
    protected SeatRepository seatRepository;

    @Autowired
    protected ReservationRepository reservationRepository;

    @Autowired
    protected StringRedisTemplate redisTemplate;

    @AfterEach
    void clearDatabase() {
        reservationRepository.deleteAllInBatch();
        seatRepository.deleteAllInBatch();
        flightRepository.deleteAllInBatch();
        userRepository.deleteAllInBatch();

        redisTemplate.execute((RedisCallback<Void>) connection -> {
            connection.serverCommands().flushAll();
            return null;
        });
    }
}
