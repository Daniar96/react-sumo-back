package com.example.project;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import java.sql.*;


@Path("/database")
public class database {
    static String user = "dab_di20212b_65";
    static String password = "oOeyfYe4wXA3Ipc+";
    static String schema = "?currentSchema=project";

    static String getSimulations =
            "SELECT to_jsonb(array_agg(jsonobj.simdata)) " +
            "FROM project.simulation s, " +
            "LATERAL (SELECT json_build_object('id', s.simulationid, 'name', s.name, 'date', s.uploaddate, 'description', " +
             "s.description, 'steps', count(distinct o.timestep)) simdata from project.output o " +
            "where o.simulationid = s.simulationid) jsonobj; ";

    static String getTimeStemp = "SELECT jsonb_agg(ultimateData.line) " +
            "FROM ( " +
            "SELECT json_build_object('timestamp', timestep, 'vehicles', array_agg(car.data)) as line " +
            "FROM project.output out, " +
            "LATERAL ( SELECT json_build_object('id', vehicle_id, 'x', x, 'y', y, 'angle', angle, 'type', " +
            "type, 'spd', speed, 'pos', pos, 'lane', lane, 'slope', slope) AS data " +
            "FROM project.output " +
            "WHERE out.timestep = timestep) car " +
            "WHERE timestep >= ? " +
            "AND timestep <= ?" +
            "AND simulationid = 1 " +
            "GROUP BY timestep ) as ultimateData ";

    static String getAllNodes =
            "SELECT to_jsonb(array_agg(nodes.data)) " +
                    "FROM project.node, " +
                    "LATERAL ( SELECT jsonb_build_object('id', node_id, 'lon', x, 'lat', y) AS data) nodes " +
                    "WHERE simulationid = 1 ";
    static String getAlledges ="" +
            "SELECT jsonb_agg(ultimateData.line) " +
            "FROM ( " +
            "SELECT json_build_object('id', edge_id, 'start', start, 'finish', finish, " +
                    "'geometry', string_to_array(shape, ' ')) as line " +
                    "FROM project.edges " +
                    "WHERE simulationid = 1 " +
                    "ORDER BY edge_id ) as ultimateData ";



    @Path("simulations")
    @GET
    @Produces("application/json")
    public String simulations() {

        try {
            return getFromDatabase(getSimulations);
        } catch (SQLException e) {
            return "{}";
        }
    }

    @GET
    @Path("time/{from}/{to}")
    @Produces("application/json")
    public String timeStep(@PathParam("from") int from, @PathParam("to") int to) {

        try {
            String[] arr = new String[2];
            arr[0] = String.valueOf(from);
            arr[1] = String.valueOf(to);
            return getFromDatabasePrepared(getTimeStemp, arr);
        } catch (SQLException e) {
            return "{}";
        }
    }


    @Path("nodes")
    @GET
    @Produces("application/json")
    public String nodes() {
        try {
            return getFromDatabase(getAllNodes);
        } catch (SQLException e) {
            return "{}";
        }
    }

    @Path("edges")
    @GET
    @Produces("application/json")
    public String edges() {
        try {
            return getFromDatabase(getAlledges);
        } catch (SQLException e) {
            return "{}";
        }
    }


    public String getFromDatabasePrepared(String Query, String[] param) throws SQLException {
        Connection database = connectToDB();
        ResultSet fin;
        try {
            PreparedStatement pr = database.prepareStatement(Query);
            for (int i = 0; i < param.length; i++) {
                pr.setString(i+1, param[i]);
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

        if(fin.next()) {
            return fin.getString(1);
        } else {
            return "{}";
        }
    }

    public String getFromDatabase(String query) throws SQLException {
        Connection database = connectToDB();
        ResultSet test;
        try {
            test =  database.createStatement().executeQuery(query);
        } catch (SQLException | NullPointerException e) {
            System.out.println(e.getMessage());
            System.out.println("Still not quite there though");
            return null;
        }


        if (test.next()) {
            return test.getString(1);
        } else {
            return "{}";
        }
    };


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
