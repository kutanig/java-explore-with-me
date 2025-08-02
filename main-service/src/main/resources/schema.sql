CREATE TABLE IF NOT EXISTS users (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(250) NOT NULL,
    email VARCHAR(254) NOT NULL UNIQUE
);

CREATE TABLE IF NOT EXISTS categories (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(50) NOT NULL UNIQUE
);

CREATE TABLE IF NOT EXISTS events (
    id BIGSERIAL PRIMARY KEY,
    annotation VARCHAR(2000) NOT NULL,
    category_id BIGINT NOT NULL,
    description VARCHAR(7000) NOT NULL,
    event_date TIMESTAMP NOT NULL,
    initiator_id BIGINT NOT NULL,
    lat FLOAT,
    lon FLOAT,
    paid BOOLEAN NOT NULL,
    participant_limit INTEGER NOT NULL,
    request_moderation BOOLEAN NOT NULL,
    title VARCHAR(120) NOT NULL,
    state VARCHAR(20) NOT NULL,
    created_on TIMESTAMP,
    published_on TIMESTAMP,
    CONSTRAINT fk_events_category FOREIGN KEY (category_id) REFERENCES categories(id),
    CONSTRAINT fk_events_initiator FOREIGN KEY (initiator_id) REFERENCES users(id)
);

CREATE TABLE IF NOT EXISTS participation_requests (
    id BIGSERIAL PRIMARY KEY,
    created TIMESTAMP NOT NULL,
    event_id BIGINT NOT NULL,
    requester_id BIGINT NOT NULL,
    status VARCHAR(20),
    CONSTRAINT fk_requests_event FOREIGN KEY (event_id) REFERENCES events(id),
    CONSTRAINT fk_requests_requester FOREIGN KEY (requester_id) REFERENCES users(id)
);

CREATE TABLE IF NOT EXISTS compilations (
    id BIGSERIAL PRIMARY KEY,
    title VARCHAR(50) NOT NULL,
    pinned BOOLEAN NOT NULL
);

CREATE TABLE IF NOT EXISTS compilation_events (
    compilation_id BIGINT NOT NULL,
    event_id BIGINT NOT NULL,
    PRIMARY KEY (compilation_id, event_id),
    CONSTRAINT fk_compilation_events_compilation FOREIGN KEY (compilation_id) REFERENCES compilations(id),
    CONSTRAINT fk_compilation_events_event FOREIGN KEY (event_id) REFERENCES events(id)
);