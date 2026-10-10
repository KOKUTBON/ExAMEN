-- Examen database schema.
-- Timestamps are stored as UTC (TIMESTAMP WITHOUT TIME ZONE); the application runs in UTC.
-- Every row that belongs to a user is removed with that user (ON DELETE CASCADE).

CREATE TABLE users (
    id        BIGSERIAL PRIMARY KEY,
    email     VARCHAR(255) NOT NULL UNIQUE,
    full_name VARCHAR(255),
    password  VARCHAR(255),
    role      VARCHAR(255) CHECK (role IN ('USER', 'ADMIN'))
);

CREATE TABLE user_settings (
    id                      BIGSERIAL PRIMARY KEY,
    user_id                 BIGINT NOT NULL UNIQUE REFERENCES users(id) ON DELETE CASCADE,
    time_zone               VARCHAR(255),
    reminder_time           TIME(0),
    theme                   VARCHAR(255),
    email_reminder          BOOLEAN DEFAULT FALSE,
    in_app_reminder         BOOLEAN DEFAULT FALSE,
    last_reminder_sent_date DATE
);

CREATE TABLE password_reset_tokens (
    id         BIGSERIAL PRIMARY KEY,
    token      VARCHAR(100) NOT NULL UNIQUE,
    user_id    BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    created_at TIMESTAMP(6) NOT NULL DEFAULT NOW(),
    expires_at TIMESTAMP(6) NOT NULL,
    used       BOOLEAN NOT NULL DEFAULT FALSE
);
CREATE INDEX idx_reset_token_user ON password_reset_tokens(user_id);
CREATE INDEX idx_reset_token_user_created_at ON password_reset_tokens(user_id, created_at);

-- Prompt library: rows without an owner are the shared defaults seeded at startup.
CREATE TABLE categories (
    id          BIGSERIAL PRIMARY KEY,
    name        VARCHAR(100) NOT NULL,
    description VARCHAR(500),
    owner_id    BIGINT REFERENCES users(id) ON DELETE CASCADE
);
CREATE INDEX idx_categories_owner ON categories(owner_id);

CREATE TABLE questions (
    id            BIGSERIAL PRIMARY KEY,
    text          VARCHAR(500) NOT NULL,
    order_number  INTEGER,
    response_type VARCHAR(255) CHECK (response_type IN ('YES_NO', 'SCALE_1_5')),
    difficulty    VARCHAR(255),
    category_id   BIGINT NOT NULL REFERENCES categories(id) ON DELETE CASCADE,
    owner_id      BIGINT REFERENCES users(id) ON DELETE CASCADE,
    custom        BOOLEAN NOT NULL DEFAULT FALSE,
    archived      BOOLEAN NOT NULL DEFAULT FALSE
);
CREATE INDEX idx_question_category ON questions(category_id);
CREATE INDEX idx_question_owner ON questions(owner_id);

CREATE TABLE examination_sessions (
    id           BIGSERIAL PRIMARY KEY,
    user_id      BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    started_at   TIMESTAMP(6),
    completed_at TIMESTAMP(6),
    mood_score   INTEGER,
    notes        VARCHAR(1000)
);
CREATE INDEX idx_session_user_completed ON examination_sessions(user_id, completed_at);

CREATE TABLE answers (
    id              BIGSERIAL PRIMARY KEY,
    session_id      BIGINT NOT NULL REFERENCES examination_sessions(id) ON DELETE CASCADE,
    question_id     BIGINT NOT NULL REFERENCES questions(id) ON DELETE CASCADE,
    answer_text     VARCHAR(255) NOT NULL,
    correct         BOOLEAN NOT NULL DEFAULT FALSE,
    reflection_text VARCHAR(2000),
    feeling_score   INTEGER
);
CREATE INDEX idx_answers_session ON answers(session_id);
CREATE INDEX idx_answers_question ON answers(question_id);

CREATE TABLE daily_examinations (
    id         BIGSERIAL PRIMARY KEY,
    user_id    BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    session_id BIGINT UNIQUE REFERENCES examination_sessions(id) ON DELETE SET NULL,
    exam_date  DATE NOT NULL,
    mood_score INTEGER,
    notes      VARCHAR(1000),
    CONSTRAINT uq_daily_exam_user_date UNIQUE (user_id, exam_date)
);

CREATE TABLE journal_entries (
    id         BIGSERIAL PRIMARY KEY,
    user_id    BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    content    VARCHAR(2000) NOT NULL,
    created_at TIMESTAMP(6) NOT NULL
);
CREATE INDEX idx_journal_user_created ON journal_entries(user_id, created_at);

CREATE TABLE gratitude_entries (
    id         BIGSERIAL PRIMARY KEY,
    user_id    BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    content    VARCHAR(1000) NOT NULL,
    created_at TIMESTAMP(6) DEFAULT NOW()
);
CREATE INDEX idx_gratitude_user_created ON gratitude_entries(user_id, created_at DESC);

CREATE TABLE habit_scores (
    id         BIGSERIAL PRIMARY KEY,
    user_id    BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    habit      VARCHAR(255) NOT NULL,
    score      INTEGER NOT NULL,
    score_date DATE NOT NULL
);
CREATE INDEX idx_habit_scores_user_date ON habit_scores(user_id, score_date DESC);

CREATE TABLE todo_items (
    id         BIGSERIAL PRIMARY KEY,
    user_id    BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    title      VARCHAR(255) NOT NULL,
    completed  BOOLEAN NOT NULL DEFAULT FALSE,
    due_at     TIMESTAMP(6),
    updated_at TIMESTAMP(6) DEFAULT NOW()
);
CREATE INDEX idx_todo_user ON todo_items(user_id);

CREATE TABLE user_metrics (
    id                 BIGSERIAL PRIMARY KEY,
    user_id            BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    metric_date        DATE NOT NULL,
    sessions_completed INTEGER,
    average_mood       DOUBLE PRECISION,
    average_score      DOUBLE PRECISION,
    created_at         TIMESTAMP(6) DEFAULT NOW(),
    CONSTRAINT uk_user_metric_date UNIQUE (user_id, metric_date)
);

CREATE TABLE user_notifications (
    id                BIGSERIAL PRIMARY KEY,
    user_id           BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    title             VARCHAR(140) NOT NULL,
    message           VARCHAR(500) NOT NULL,
    notification_type VARCHAR(40) NOT NULL,
    created_at        TIMESTAMP(6) NOT NULL DEFAULT NOW(),
    read_at           TIMESTAMP(6)
);
CREATE INDEX idx_notifications_user_created_at ON user_notifications(user_id, created_at);
CREATE INDEX idx_notifications_user_unread ON user_notifications(user_id, read_at);

CREATE TABLE semantic_tags (
    id         BIGSERIAL PRIMARY KEY,
    user_id    BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    session_id BIGINT REFERENCES examination_sessions(id) ON DELETE CASCADE,
    answer_id  BIGINT REFERENCES answers(id) ON DELETE CASCADE,
    category   VARCHAR(32) NOT NULL CHECK (category IN ('VIRTUE', 'STRUGGLE', 'GRATITUDE', 'STRESSOR')),
    tag        VARCHAR(128) NOT NULL,
    confidence DOUBLE PRECISION DEFAULT 1.0,
    created_at TIMESTAMP(6) NOT NULL DEFAULT NOW()
);
CREATE INDEX idx_semantic_tags_user ON semantic_tags(user_id, created_at);
CREATE INDEX idx_semantic_tags_session ON semantic_tags(session_id);
CREATE INDEX idx_semantic_tags_category ON semantic_tags(category);

CREATE TABLE insight_feedbacks (
    id          BIGSERIAL PRIMARY KEY,
    user_id     BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    target_type VARCHAR(64) NOT NULL,
    target_id   VARCHAR(128),
    is_helpful  BOOLEAN NOT NULL,
    comments    TEXT,
    created_at  TIMESTAMP(6) NOT NULL DEFAULT NOW()
);
CREATE INDEX idx_insight_feedback_user ON insight_feedbacks(user_id, created_at);
CREATE INDEX idx_insight_feedback_target ON insight_feedbacks(target_type, target_id);
