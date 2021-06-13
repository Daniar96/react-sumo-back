package com.geosumo.teamone.queries;

public class DataCreationQueries {
    public static final String CREATE = "CREATE SCHEMA IF NOT EXISTS geo_sumo; " +
            "SET search_path = geo_sumo; " +
            " " +
            "CREATE TABLE IF NOT EXISTS simulation " +
            "( " +
            "    id          SERIAL PRIMARY KEY, " +
            "    name        TEXT NOT NULL, " +
            "    upload_date TIMESTAMP DEFAULT current_timestamp, " +
            "    description TEXT " +
            "); " +
            " " +
            "CREATE TABLE IF NOT EXISTS node " +
            "( " +
            "    sim_id  SERIAL REFERENCES simulation (id) ON DELETE CASCADE NOT NULL, " +
            "    node_id TEXT UNIQUE                                         NOT NULL, " +
            "    x       NUMERIC                                             NOT NULL, " +
            "    y       NUMERIC                                             NOT NULL, " +
            "    type    TEXT, " +
            "    PRIMARY KEY (sim_id, node_id) " +
            "); " +
            " " +
            "CREATE TABLE IF NOT EXISTS node_data " +
            "( " +
            "    sim_id  SERIAL REFERENCES simulation (id) ON DELETE CASCADE NOT NULL, " +
            "    node_id TEXT REFERENCES node (node_id) ON DELETE CASCADE    NOT NULL, " +
            "    key     TEXT                                                NOT NULL, " +
            "    value   TEXT                                                NOT NULL, " +
            "    PRIMARY KEY (sim_id, node_id) " +
            "); " +
            " " +
            "CREATE TABLE IF NOT EXISTS node_tl " +
            "( " +
            "    sim_id  SERIAL REFERENCES simulation (id) ON DELETE CASCADE NOT NULL, " +
            "    node_id TEXT REFERENCES node (node_id) ON DELETE CASCADE    NOT NULL, " +
            "    tl_id   TEXT                                                NOT NULL, " +
            "    PRIMARY KEY (sim_id, node_id) " +
            "); " +
            " " +
            "CREATE TABLE IF NOT EXISTS route " +
            "( " +
            "    sim_id     SERIAL REFERENCES simulation (id) ON DELETE CASCADE NOT NULL, " +
            "    vehicle_id INT                                                 NOT NULL, " +
            "    depart     NUMERIC                                             NOT NULL, " +
            "    edges      TEXT                                                NOT NULL, " +
            "    PRIMARY KEY (sim_id, vehicle_id) " +
            "); " +
            " " +
            "CREATE TABLE IF NOT EXISTS edge " +
            "( " +
            "    sim_id      SERIAL REFERENCES simulation (id) ON DELETE CASCADE NOT NULL, " +
            "    edge_id     TEXT UNIQUE                                         NOT NULL, " +
            "    start       TEXT REFERENCES node (node_id) ON DELETE CASCADE    NOT NULL, " +
            "    finish      TEXT REFERENCES node (node_id) ON DELETE CASCADE    NOT NULL, " +
            "    priority    NUMERIC, " +
            "    type        TEXT                                                NOT NULL, " +
            "    num_lanes   NUMERIC                                             NOT NULL, " +
            "    speed       NUMERIC                                             NOT NULL, " +
            "    shape       TEXT, " +
            "    spread_type TEXT, " +
            "    length      NUMERIC, " +
            "    width       NUMERIC, " +
            "    name        TEXT, " +
            "    allow       TEXT, " +
            "    disallow    TEXT, " +
            "    PRIMARY KEY (sim_id, edge_id) " +
            "); " +
            " " +
            "CREATE TABLE IF NOT EXISTS edge_lane " +
            "( " +
            "    sim_id       SERIAL REFERENCES simulation (id) ON DELETE CASCADE NOT NULL, " +
            "    edge_id      TEXT REFERENCES edge (edge_id) ON DELETE CASCADE    NOT NULL, " +
            "    line_id      INT, " +
            "    acceleration INT, " +
            "    allow        TEXT, " +
            "    disallow     TEXT, " +
            "    speed        NUMERIC, " +
            "    width        NUMERIC, " +
            "    PRIMARY KEY (sim_id, edge_id, line_id) " +
            "); " +
            " " +
            "CREATE TABLE IF NOT EXISTS edge_data " +
            "( " +
            "    sim_id  SERIAL REFERENCES simulation (id) ON DELETE CASCADE NOT NULL, " +
            "    edge_id TEXT REFERENCES edge (edge_id) ON DELETE CASCADE    NOT NULL, " +
            "    key     TEXT                                                NOT NULL, " +
            "    value   TEXT                                                NOT NULL, " +
            "    PRIMARY KEY (sim_id, edge_id) " +
            "); " +
            " " +
            "CREATE TABLE IF NOT EXISTS output " +
            "( " +
            "    sim_id     SERIAL REFERENCES simulation (id) ON DELETE CASCADE NOT NULL, " +
            "    time_step  INT                                                 NOT NULL, " +
            "    vehicle_id INT                                                 NOT NULL, " +
            "    x          NUMERIC                                             NOT NULL, " +
            "    y          NUMERIC                                             NOT NULL, " +
            "    angle      NUMERIC                                             NOT NULL, " +
            "    speed      NUMERIC                                             NOT NULL, " +
            "    pos        NUMERIC                                             NOT NULL, " +
            "    slope      NUMERIC                                             NOT NULL, " +
            "    lane       TEXT                                                NOT NULL, " +
            "    PRIMARY KEY (sim_id, time_step, vehicle_id) " +
            "); " +
            " " +
            "CREATE TABLE IF NOT EXISTS roundabout " +
            "( " +
            "    sim_id        SERIAL REFERENCES simulation (id) ON DELETE CASCADE NOT NULL, " +
            "    roundabout_id INT                                                 NOT NULL, " +
            "    edges         TEXT                                                NOT NULL, " +
            "    nodes         TEXT, " +
            "    PRIMARY KEY (sim_id, roundabout_id) " +
            "); " +
            " " +
            "CREATE TABLE IF NOT EXISTS files " +
            "( " +
            "    id     SERIAL PRIMARY KEY, " +
            "    output TEXT, " +
            "    routes TEXT, " +
            "    edges  TEXT, " +
            "    nodes  TEXT " +
            ");";

    public static final String DROP = "DROP TABLE IF EXISTS simulation CASCADE; " +
            "DROP TABLE IF EXISTS node CASCADE; " +
            "DROP TABLE IF EXISTS node_tl CASCADE; " +
            "DROP TABLE IF EXISTS node_data CASCADE; " +
            "DROP TABLE IF EXISTS roundabout CASCADE; " +
            "DROP TABLE IF EXISTS route CASCADE; " +
            "DROP TABLE IF EXISTS edge CASCADE; " +
            "DROP TABLE IF EXISTS edge_allow CASCADE; " +
            "DROP TABLE IF EXISTS edge_disallow CASCADE; " +
            "DROP TABLE IF EXISTS edge_data CASCADE; " +
            "DROP TABLE IF EXISTS edge_lane CASCADE; " +
            "DROP TABLE IF EXISTS output CASCADE; " +
            "DROP TABLE IF EXISTS files CASCADE;";
}
