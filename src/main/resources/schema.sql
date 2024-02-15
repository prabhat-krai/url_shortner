CREATE SEQUENCE IF NOT EXISTS url_mapping_id_seq;

CREATE TABLE IF NOT EXISTS "url_mapping" (
    "id" int8 NOT NULL DEFAULT nextval('url_shortner.url_mapping_id_seq'::regclass),
    "url" text NOT NULL,
    "short_key" varchar NOT NULL,
    PRIMARY KEY ("id")
);

CREATE UNIQUE INDEX IF NOT EXISTS "uniq_rows" ON "url_shortner"."url_mapping" USING BTREE ("url","short_key");