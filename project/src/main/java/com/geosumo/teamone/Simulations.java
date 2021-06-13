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
            return getFromDatabasePrepared(getSimulations);
        } catch (SQLException e) {
            return "{}";
        }
    }

    @GET
    @Path("{simulation_id}/metadata")
    @Produces(MediaType.APPLICATION_JSON)
    public String simulation(@PathParam("simulation_id") int id) {
        try {
            return getFromDatabasePrepared(getSimulationById, id, id);
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
            return getFromDatabasePrepared(getTimeStamp, id, from, to);
        } catch (SQLException e) {
            return "{}";
        }
    }

    @Path("{simulation_id}/nodes")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public String nodes(@PathParam("simulation_id") int id) {
        try {
            return getFromDatabasePrepared(getAllNodes, id);
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
}
