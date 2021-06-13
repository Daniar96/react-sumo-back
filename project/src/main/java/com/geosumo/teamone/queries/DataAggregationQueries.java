package com.geosumo.teamone.queries;

public class DataAggregationQueries {
    public static final String getSimulations =
            "SELECT to_jsonb(array_agg(jsonobj.simdata)) " +
                    "FROM project.simulation s, " +
                    "LATERAL (SELECT json_build_object('id', s.simulationid, 'name', s.name, 'date', s.uploaddate, 'description', " +
                    "s.description, 'steps', count(distinct o.timestep)) simdata from project.output o " +
                    "where o.simulationid = s.simulationid) jsonobj; ";

    public static final String getSimulationById =
            "SELECT jsonb_build_object('id', s.simulationid," +
                    "                          'name', s.name," +
                    "                          'date', s.uploaddate," +
                    "                          'description', s.description," +
                    "                          'steps', count(DISTINCT o.timestep)) " +
                    "FROM project.simulation s, " +
                    "     project.output o " +
                    "WHERE s.simulationid = ? " +
                    "  AND o.simulationid = ? " +
                    "GROUP BY s.simulationid;";

    public static final String getTimeStamp = "SELECT jsonb_agg(ultimateData.line) " +
            "FROM ( " +
            "SELECT json_build_object('timestamp', timestep, 'vehicles', array_agg(car.data)) as line " +
            "FROM project.output out, " +
            "LATERAL ( SELECT json_build_object('id', vehicle_id, 'x', x, 'y', y, 'angle', angle, 'type', " +
            "type, 'spd', speed, 'pos', pos, 'lane', lane, 'slope', slope) AS data " +
            "FROM project.output " +
            "WHERE out.timestep = timestep) car " +
            "WHERE simulationid = ? " +
            "AND timestep >= ? " +
            "AND timestep <= ? " +
            "GROUP BY timestep ) as ultimateData ";

    public static final String getAllNodes =
            "SELECT to_jsonb(array_agg(nodes.data)) " +
                    "FROM project.node, " +
                    "LATERAL ( SELECT jsonb_build_object('id', node_id, 'lon', x, 'lat', y) AS data) nodes " +
                    "WHERE simulationid = ? ";
    public static final String getAllEdges = "" +
            "SELECT jsonb_agg(ultimateData.line) " +
            "FROM ( " +
            "SELECT json_build_object('id', edge_id, 'start', start, 'finish', finish, " +
            "'geometry', string_to_array(shape, ' ')) as line " +
            "FROM project.edges " +
            "WHERE simulationid = ? " +
            "ORDER BY edge_id ) as ultimateData ";
}
