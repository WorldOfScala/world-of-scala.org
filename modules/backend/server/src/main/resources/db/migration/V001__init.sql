CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

CREATE TABLE "users"(
    id uuid NOT NULL DEFAULT uuid_generate_v4(),
    firstname text NOT NULL,
    lastname text NOT NULL,
    email text NOT NULL UNIQUE,
    hashed_password text NOT NULL,
    creation_date timestamp NOT NULL DEFAULT now(),
    PRIMARY KEY (id)
);

CREATE TABLE meshes(
    id uuid NOT NULL DEFAULT uuid_generate_v4(),
    label text NOT NULL,
    blob bytea,
    thumbnail text,
    PRIMARY KEY (id)
);

CREATE TABLE organisations(
    id uuid NOT NULL DEFAULT uuid_generate_v4(),
    name text NOT NULL,
    mesh_id uuid REFERENCES meshes(id),
    location point,
    created_by uuid REFERENCES users(id),
    creation_date timestamp NOT NULL DEFAULT now(),
    PRIMARY KEY (id)
);

