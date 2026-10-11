CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

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
    location point NOT NULL,
    creation_date timestamp NOT NULL DEFAULT now(),
    created_by uuid NOT NULL,
    PRIMARY KEY (id)
);

INSERT INTO meshes(label, blob)
    VALUES ('cube', pg_read_file('/etc/resolv.conf')::bytea)
