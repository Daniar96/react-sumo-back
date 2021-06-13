package com.geosumo.teamone.queries;

public class DataAggregationQueries {
    public static final String GET_SIMULATIONS =
            "SELECT to_jsonb(array_agg(jsonobj.simdata)) " +
                    "FROM simulation s, " +
                    "     LATERAL (SELECT json_build_object('id', s.id, 'name', s.name, 'date', s.upload_date, 'description', " +
                    "                                       s.description, 'steps', count(DISTINCT o.time_step)) simdata " +
                    "              FROM output o " +
                    "              WHERE o.sim_id = s.id) jsonobj;";

    public static final String GET_SIMULATION_BY_ID =
            "SELECT jsonb_build_object('id', s.id, " +
                    "                          'name', s.name, " +
                    "                          'date', s.upload_date, " +
                    "                          'description', s.description, " +
                    "                          'steps', count(DISTINCT o.time_step)) " +
                    "FROM simulation s, " +
                    "     output o " +
                    "WHERE s.id = ? " +
                    "  AND o.sim_id = ? " +
                    "GROUP BY s.id;";

    public static final String GET_TIME_STAMP = "SELECT jsonb_agg(ultimateData.line) " +
            "FROM ( " +
            "         SELECT json_build_object('timestamp', time_step, 'vehicles', array_agg(car.data)) AS line " +
            "         FROM output out, " +
            "              LATERAL ( SELECT json_build_object('id', vehicle_id, 'x', x, 'y', y, 'angle', angle, 'type', " +
            "                                                 type, 'spd', speed, 'pos', pos, 'lane', lane, 'slope', slope) AS data " +
            "                        FROM output " +
            "                        WHERE out.time_step = time_step) car " +
            "         WHERE sim_id = ? " +
            "           AND out.time_step >= ? " +
            "           AND out.time_step <= ? " +
            "         GROUP BY time_step) AS ultimateData;";

    public static final String GET_ALL_NODES =
            "SELECT to_jsonb(array_agg(nodes.data)) " +
                    "FROM node, " +
                    "     LATERAL ( SELECT jsonb_build_object('id', node_id, 'lon', x, 'lat', y) AS data) nodes " +
                    "WHERE sim_id = ?";

    public static final String getAllEdges =
            "SELECT jsonb_agg(ultimateData.line) " +
                    "FROM ( " +
                    "         SELECT json_build_object('id', edge_id, 'start', start, 'finish', finish, " +
                    "                                  'geometry', string_to_array(shape, ' ')) AS line " +
                    "         FROM edge " +
                    "         WHERE sim_id = ? " +
                    "         ORDER BY edge_id) AS ultimateData;";

    public static final String VEHICLE_PER_TIME_STEP = "SELECT jsonb_agg(data) " +
            "            FROM ( SELECT time_step, count(*) AS vehicles " +
            "                    FROM output " +
            "                    WHERE sim_id = ? " +
            "                    GROUP BY time_step) AS data;";

    public static final String SPEED_PER_TIME_STEP = "SELECT jsonb_agg(data) " +
            "                    FROM ( " +
            "                    SELECT time_step, AVG(speed) as averagespeed " +
            "                    FROM output " +
            "                    WHERE sim_id = ? " +
            "                    GROUP BY time_step) as data;";

    public static final String SLOWEST_VEHICLE_PER_TIME_STEP = "SELECT jsonb_agg(data) " +
            "            FROM ( SELECT vehicle_id AS ID, speed " +
            "            FROM output " +
            "            WHERE sim_id = ? " +
            "            AND time_step = ? " +
            "            ORDER BY speed " +
            "            LIMIT 10 ) AS data;";
}
