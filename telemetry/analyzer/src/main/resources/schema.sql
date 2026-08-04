-- Таблица истории взаимодействий пользователей
CREATE TABLE IF NOT EXISTS user_actions
(
    id          BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    user_id     BIGINT                      NOT NULL,
    event_id    BIGINT                      NOT NULL,
    action_type VARCHAR(20)                 NOT NULL,
    weight      DOUBLE PRECISION            NOT NULL,
    timestamp   TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    created_at  TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    UNIQUE (user_id, event_id, action_type)
);

-- Таблица коэффициентов сходства мероприятий
CREATE TABLE IF NOT EXISTS event_similarity
(
    id         BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    event_a_id BIGINT                      NOT NULL,
    event_b_id BIGINT                      NOT NULL,
    score      DOUBLE PRECISION            NOT NULL,
    timestamp  TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    UNIQUE (event_a_id, event_b_id)
);

-- Таблица для хранения максимальных весов действий пользователя
CREATE TABLE IF NOT EXISTS user_event_weights
(
    user_id               BIGINT                      NOT NULL,
    event_id              BIGINT                      NOT NULL,
    max_weight            DOUBLE PRECISION            NOT NULL,
    last_action_timestamp TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    PRIMARY KEY (user_id, event_id)
);