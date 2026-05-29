CREATE TABLE users (
    id BIGSERIAL PRIMARY KEY,
    email VARCHAR(255) NOT NULL UNIQUE,
    name VARCHAR(255) NOT NULL
);

CREATE TABLE flights (
    id BIGSERIAL PRIMARY KEY,
    flight_number VARCHAR(50) NOT NULL UNIQUE,
    departure_airport VARCHAR(10) NOT NULL,
    arrival_airport VARCHAR(10) NOT NULL,
    departure_time TIMESTAMP WITH TIME ZONE NOT NULL,
    arrival_time TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE TABLE seats (
    id BIGSERIAL PRIMARY KEY,
    flight_id BIGINT NOT NULL,
    seat_number VARCHAR(10) NOT NULL,
    class VARCHAR(20) NOT NULL,
    price DECIMAL(10, 2) NOT NULL,
    status VARCHAR(10) NOT NULL,
    CONSTRAINT fk_seats_flight FOREIGN KEY (flight_id) REFERENCES flights(id) ON DELETE CASCADE,
    CONSTRAINT uq_flight_seat UNIQUE (flight_id, seat_number)
);

CREATE TABLE reservations (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    seat_id BIGINT NOT NULL,
    status VARCHAR(20) NOT NULL,
    lock_token UUID NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    expires_at TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT fk_reservations_user FOREIGN KEY (user_id) REFERENCES users(id),
    CONSTRAINT fk_reservations_seat FOREIGN KEY (seat_id) REFERENCES seats(id)
);
