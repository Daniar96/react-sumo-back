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

    public static final String GET_TIME_STAMP = "SELECT jsonb_agg(data) \n" +
            "FROM (\n" +
            "SELECT time_step, jsonb_agg(json_build_object('id', vehicle_id, 'x', x, 'y', y, 'angle', angle, 'type', type, 'spd', speed, 'pos', pos, 'lane', lane, 'slope', slope)) as vehicles\n" +
            "FROM geo_sumo.output out\n" +
            "WHERE out.sim_id = ?\n" +
            "AND out.time_step >= ?\n" +
            "AND out.time_step <= ?\n" +
            "GROUP BY time_step ) as data";

    public static final String GET_ALL_NODES =
            "SELECT to_jsonb(array_agg(nodes.data)) " +
                    "FROM node, " +
                    "     LATERAL ( SELECT jsonb_build_object('id', node_id, 'lon', x, 'lat', y) AS data) nodes " +
                    "WHERE sim_id = ?";

    public static final String getAllEdges =
            "SELECT jsonb_agg(ultimateData.line) " +
                    "FROM ( " +
                    "         SELECT json_build_object('id', e.edge_id, 'start', e.start, 'finish', e.finish, " +
                    "                                  'geometry', COALESCE(string_to_array(e.shape, ' '), "+
                    " string_to_array(CONCAT(CONCAT(nst.x,',',nst.y), ' ', CONCAT(nfin.x,',',nfin.y)),' '))) AS line " +
                    "         FROM node nst, edge e, node nfin" +
                    "         WHERE e.sim_id = ? AND e.start = nst.node_id AND e.finish = nfin.node_id" +
                    "         ORDER BY e.edge_id) AS ultimateData;";


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
