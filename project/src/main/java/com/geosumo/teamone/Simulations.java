package com.geosumo.teamone;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.sql.*;

import static com.geosumo.teamone.Helpers.*;
import static com.geosumo.teamone.queries.DataAggregationQueries.*;

@Path("/simulations")
public class Simulations {
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public String simulations() {
        try {
            return getFromDatabasePrepared(GET_SIMULATIONS);
        } catch (SQLException e) {
            return "{}";
        }
    }

    @GET
    @Path("{simulation_id}/metadata")
    @Produces(MediaType.APPLICATION_JSON)
    public String simulation(@PathParam("simulation_id") int id) {
        try {
            return getFromDatabasePrepared(GET_SIMULATION_BY_ID, id, id);
        } catch (SQLException ignore) {
            return "{}";
        }
    }

    @GET
    @Path("{simulation_id}/vehicles")
    @Produces(MediaType.APPLICATION_JSON)
    public String timeStep(@PathParam("simulation_id") int id,
                           @QueryParam("from") int from,
                           @QueryParam("to") int to) {

        try {
            return getFromDatabasePrepared(GET_TIME_STAMP, id, from, to);
        } catch (SQLException e) {
            return "{}";
        }
    }

    @Path("{simulation_id}/nodes")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public String nodes(@PathParam("simulation_id") int id) {
        try {
            return getFromDatabasePrepared(GET_ALL_NODES, id);
        } catch (SQLException e) {
            return "{}";
        }
    }

    @Path("{simulation_id}/edges")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public String edges(@PathParam("simulation_id") int id) {
        try {
            return getFromDatabasePrepared(getAllEdges, id);
        } catch (SQLException e) {
            return "{}";
        }
    }

    @Path("{simulation_id}/graphs/static")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public String staticGraphs(@PathParam("simulation_id") int id){
        try {
            return "{ count: " + getFromDatabasePrepared(VEHICLE_PER_TIME_STEP, id)+ "," +
                    " speed: " + getFromDatabasePrepared(SPEED_PER_TIME_STEP, id) + "}";
        } catch (SQLException e) {
            return "{}";
        }
    }



    @Path("{simulation_id}/metaData")
    @POST
    @Consumes(MediaType.TEXT_PLAIN)
    public void ChangeMetaData(String input){
        System.out.println(input);
    }

}
