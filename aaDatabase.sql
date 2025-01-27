-- Create the database (if it doesn't exist)
CREATE DATABASE audio_alchemists;

-- Connect to the database
\c audio_alchemists;

-- Create the User table
CREATE TABLE "user" (
    id SERIAL PRIMARY KEY,
    username VARCHAR(255) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    email VARCHAR(255) UNIQUE NOT NULL,
    role VARCHAR(20) NOT NULL,  -- e.g., 'USER', 'COMPOSER', 'ADMIN'
    preferences JSONB,  -- Store user preferences as JSON
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);


-- Create the Project table
CREATE TABLE project (
    id SERIAL PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    description TEXT,
    genre VARCHAR(255),
    tempo INTEGER,
    owner_id INTEGER REFERENCES "user"(id) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);


-- Create the Track table
CREATE TABLE track (
    id SERIAL PRIMARY KEY,
    project_id INTEGER REFERENCES project(id) NOT NULL,
    instrument VARCHAR(255),
    musical_sequence JSONB, -- Store musical data as JSON
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- Create the ProjectVersion table
CREATE TABLE project_version (
    id SERIAL PRIMARY KEY,
    project_id INTEGER REFERENCES project(id) NOT NULL,
    timestamp TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    data JSONB -- Store project snapshot as JSON
);

-- Create the TrackChange table
CREATE TABLE track_change (
    id SERIAL PRIMARY KEY,
    track_id INTEGER REFERENCES track(id) NOT NULL,
    user_id INTEGER REFERENCES "user"(id) NOT NULL,
    timestamp TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    change_type VARCHAR(20) NOT NULL, -- 'ADD', 'DELETE', 'MODIFY'
    data JSONB -- Store change details as JSON
);

-- Create the Collaboration table (for many-to-many relationship)
CREATE TABLE collaboration (
    user_id INTEGER REFERENCES "user"(id) NOT NULL,
    project_id INTEGER REFERENCES project(id) NOT NULL,
    role VARCHAR(50),  -- e.g., 'EDITOR', 'VIEWER'
    PRIMARY KEY (user_id, project_id)
);


-- Create the Follow table (for many-to-many relationship)
CREATE TABLE follow (
    follower_id INTEGER REFERENCES "user"(id) NOT NULL,
    following_id INTEGER REFERENCES "user"(id) NOT NULL,
    PRIMARY KEY (follower_id, following_id)
);


-- Create the Comment table
CREATE TABLE comment (
    id SERIAL PRIMARY KEY,
    project_id INTEGER REFERENCES project(id) NOT NULL,
    user_id INTEGER REFERENCES "user"(id) NOT NULL,
    text TEXT NOT NULL,
    timestamp TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);



-- Add updated_at triggers to relevant tables for automatic updates
-- Example for user table:

CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = NOW();
    RETURN NEW;
END;
$$ language 'plpgsql';

CREATE TRIGGER update_user_updated_at
BEFORE UPDATE ON "user"
FOR EACH ROW
EXECUTE PROCEDURE update_updated_at_column();

-- Repeat the trigger creation for project, track

CREATE TRIGGER update_project_updated_at
BEFORE UPDATE ON project
FOR EACH ROW
EXECUTE PROCEDURE update_updated_at_column();

CREATE TRIGGER update_track_updated_at
BEFORE UPDATE ON track
FOR EACH ROW
EXECUTE PROCEDURE update_updated_at_column();
