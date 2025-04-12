CREATE TABLE title_inverted_index (
    id BIGINT PRIMARY KEY,
    term VARCHAR(255) NOT NULL UNIQUE,
    max_tf BIGINT NOT NULL,
    documents JSONB NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE body_inverted_index (
    id BIGINT PRIMARY KEY,
    term VARCHAR(255) NOT NULL UNIQUE,
    max_tf BIGINT NOT NULL,
    documents JSONB NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);