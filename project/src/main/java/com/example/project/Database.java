package com.example.project;

import javax.ws.rs.*;
import java.sql.*;

@Path("/simulations")
public class Database {
    static String user = "dab_di20212b_65";
    static String password = "oOeyfYe4wXA3Ipc+";
    static String schema = "?currentSchema=project";

    static String getSimulations =
            "SELECT to_jsonb(array_agg(jsonobj.simdata)) " +
                    "FROM project.simulation s, " +
                    "LATERAL (SELECT json_build_object('id', s.simulationid, 'name', s.name, 'date', s.uploaddate, 'description', " +
                    "s.description, 'steps', count(distinct o.timestep)) simdata from project.output o " +
                    "where o.simulationid = s.simulationid) jsonobj; ";

    static String getSimulationById =
            "SELECT jsonb_build_object('id', s.simulationid," +
            "                          'name', s.name," +
            "                          'date', s.uploaddate," +
            "                          'description', s.description," +
            "                          'steps', count(DISTINCT o.timestep))" +
            "FROM project.simulation s," +
            "     project.output o" +
            "WHERE s.simulationid = ?" +
            "  AND o.simulationid = ?" +
            "GROUP BY s.simulationid;";

    static String getTimeStamp = "SELECT jsonb_agg(ultimateData.line) " +
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

    static String getAllNodes =
            "SELECT to_jsonb(array_agg(nodes.data)) " +
                    "FROM project.node, " +
                    "LATERAL ( SELECT jsonb_build_object('id', node_id, 'lon', x, 'lat', y) AS data) nodes " +
                    "WHERE simulationid = ? ";
    static String getAllEdges = "" +
            "SELECT jsonb_agg(ultimateData.line) " +
            "FROM ( " +
            "SELECT json_build_object('id', edge_id, 'start', start, 'finish', finish, " +
            "'geometry', string_to_array(shape, ' ')) as line " +
            "FROM project.edges " +
            "WHERE simulationid = ? " +
            "ORDER BY edge_id ) as ultimateData ";


    @GET
    @Produces("application/json")
    public String simulations() {
        try {
            return getFromDatabasePrepared(getSimulations, new int[0]);
        } catch (SQLException e) {
            return "{}";
        }
    }

    @GET
    @Path("{simulation_id}/metadata")
    @Produces("application/json")
    public String simulation(@PathParam("simulation_id") int id) {
        try {
            int[] arr = new int[2];
            arr[0] = id;
            arr[1] = id;
            return getFromDatabasePrepared(getSimulationById, arr);
        } catch (SQLException ignore) {
            return "{}";
        }
    }

    @GET
    @Path("{simulation_id}/vehicles")
    @Produces("application/json")
    public String timeStep(@PathParam("simulation_id") int id,
                           @QueryParam("from") int from,
                           @QueryParam("to") int to) {

        try {
            int[] arr = new int[3];
            arr[0] = id;
            arr[1] = from;
            arr[2] = to;

            return getFromDatabasePrepared(getTimeStamp, arr);
        } catch (SQLException e) {
            return "{}";
        }
    }


    @Path("{simulation_id}/nodes")
    @GET
    @Produces("application/json")
    public String nodes(@PathParam("simulation_id") int id) {
        int[] arr = new int[1];
        arr[0] = id;
        try {
            return getFromDatabasePrepared(getAllNodes, arr);
        } catch (SQLException e) {
            return "{}";
        }
    }

    @Path("{simulation_id}/edges")
    @GET
    @Produces("application/json")
    public String edges(@PathParam("simulation_id") int id) {
        int[] arr = new int[1];
        arr[0] = id;
        try {
            return getFromDatabasePrepared(getAllEdges, arr);
        } catch (SQLException e) {
            return "{}";
        }
    }


    public String getFromDatabasePrepared(String Query, int[] param) throws SQLException {
        Connection database = connectToDB();
        ResultSet fin;
        try {
            PreparedStatement pr = database.prepareStatement(Query);
            for (int i = 0; i < param.length; i++) {
                System.out.println(param[i]);
                pr.setInt(i + 1, param[i]);
            }
            fin = pr.executeQuery();

        } catch (SQLException e) {
            System.out.println("the given query was wrong!");
            e.printStackTrace();
            return null;
        } catch (NullPointerException e) {
            System.out.println("Could not connect to database properly");
            return null;
        }

        if (fin.next()) {
            return fin.getString(1);
        } else {
            return "{}";
        }
    }

    public Connection connectToDB() {
        Connection ret;
        try {
            Class.forName("org.postgresql.Driver");
        } catch (ClassNotFoundException ex) {
            System.err.println("Driver not found: " + ex.getMessage());
        }
        try {
            ret = DriverManager.getConnection("jdbc:postgresql://bronto.ewi.utwente.nl/" + user + schema, user,
                    password);

            System.out.println("You have connected to database!");
            return ret;
        } catch (SQLException e) {
            return null;
        }
    }
}
