package com.geosumo.teamone.queries;

public class DataInsertionQueries {
    public static final String SWITCH_SCHEMA = "SET search_path = geo_sumo;";
    public static final String INSERT = "INSERT INTO files (nodes) VALUES ('') RETURNING id;";
    public static final String UPDATE_NODES = "UPDATE files SET nodes = coalesce(nodes, '') || ? WHERE id = ?;";
    public static final String UPDATE_EDGES = "UPDATE files SET edges = coalesce(edges, '') || ? WHERE id = ?;";
    public static final String UPDATE_ROUTES = "UPDATE files SET routes = coalesce(routes, '') || ? WHERE id = ?;";
    public static final String UPDATE_OUTPUT = "UPDATE files SET output = coalesce(output, '') || ? WHERE id = ?;";

    public static final String INSERT_ROUNDABOUT = "INSERT INTO roundabout (sim_id, roundabout_id, edges, nodes) " +
            "    (SELECT ?, xml.roundabout_id, xml.edges, xml.nodes " +
            "     FROM files, " +
            "          xmltable('/edges/roundabout' PASSING xmlparse(DOCUMENT files.edges) COLUMNS " +
            "              roundabout_id FOR ORDINALITY, " +
            "              edges TEXT PATH './@edges' NOT NULL, " +
            "              nodes TEXT PATH './@nodes' " +
            "              ) AS xml " +
            "        WHERE id = ?);";

    public static final String INSERT_ROUTE = "INSERT INTO route (sim_id, vehicle_id, depart, edges) " +
            "    (SELECT ?, xml.id, xml.depart, xml.edges " +
            "     FROM files, " +
            "          xmltable('/routes/vehicle' PASSING xmlparse(DOCUMENT routes) COLUMNS " +
            "              id INT PATH './@id' NOT NULL, " +
            "              depart NUMERIC PATH './@depart' NOT NULL, " +
            "              edges TEXT PATH './route/@edges' NOT NULL " +
            "              ) AS xml " +
            "        WHERE files.id = ?);";

    public static final String INSERT_OUTPUT = "INSERT INTO output (sim_id, time_step, vehicle_id, x, y, angle, type, speed, pos, slope, lane) " +
            "    (SELECT ?, " +
            "            xml.time, xml.vehicle_id, xml.x, xml.y, xml.angle, xml.col_type, xml.speed, xml.pos, xml.slope, xml.lane " +
            "     FROM files, " +
            "          xmltable('/fcd-export/timestep/vehicle' PASSING xmlparse(DOCUMENT output) COLUMNS " +
            "              time NUMERIC PATH './../@time' NOT NULL, " +
            "              vehicle_id NUMERIC PATH './@id' NOT NULL, " +
            "              type TEXT PATH './@type' NOT NULL, " +
            "              x NUMERIC PATH './@x' NOT NULL, " +
            "              y NUMERIC PATH './@y' NOT NULL, " +
            "              angle NUMERIC PATH './@angle' NOT NULL, " +
            "              col_type TEXT PATH './@type' NOT NULL," +
            "              speed NUMERIC PATH './@speed' NOT NULL, " +
            "              pos NUMERIC PATH './@pos' NOT NULL, " +
            "              lane TEXT PATH './@lane' NOT NULL, " +
            "              slope NUMERIC PATH './@slope' NOT NULL " +
            "              ) AS xml " +
            "        WHERE id = ?);";

    private static final String INSERT_NODE = "CREATE VIEW node_view AS " +
            "SELECT xml.* " +
            "FROM files, " +
            "     xmltable('/nodes/node' PASSING xmlparse(DOCUMENT nodes) COLUMNS " +
            "         id TEXT PATH './@id' NOT NULL, " +
            "         x NUMERIC PATH './@x', " +
            "         y NUMERIC PATH './@y', " +
            "         type TEXT PATH './@type', " +
            "         tl TEXT PATH './@tl', " +
            "         key TEXT PATH './param/@key', " +
            "         value TEXT PATH './param/@value' " +
            "         ) AS xml " +
            "WHERE files.id = %d; " +
            " " +
            "INSERT INTO node (sim_id, node_id, x, y, type) " +
            "        (SELECT ?, nv.id, nv.x, nv.y, nv.type FROM node_view nv); " +
            " " +
            "INSERT INTO node_tl (sim_id, node_id, tl_id) " +
            "    (SELECT ?, nv.id, nv.tl " +
            "     FROM node_view nv " +
            "     WHERE tl IS NOT NULL); " +
            " " +
            "INSERT INTO node_data (sim_id, node_id, key, value) " +
            "    (SELECT ?, nv.id, nv.key, nv.value " +
            "     FROM node_view nv " +
            "     WHERE nv.key IS NOT NULL " +
            "        OR nv.value IS NOT NULL); " +
            " " +
            "DROP VIEW IF EXISTS node_view;";

    private static final String INSERT_EDGE = "CREATE VIEW edge_view AS " +
            "SELECT xml.* " +
            "FROM files, " +
            "     xmltable('/edges/edge' PASSING xmlparse(DOCUMENT edges) COLUMNS " +
            "         edge_id TEXT PATH './@id' NOT NULL, " +
            "         start NUMERIC PATH './@from' NOT NULL, " +
            "         finish NUMERIC PATH './@to' NOT NULL, " +
            "         priority NUMERIC PATH './@priority', " +
            "         type TEXT PATH './@type' NOT NULL, " +
            "         num_lanes NUMERIC PATH './@numLanes' NOT NULL, " +
            "         speed NUMERIC PATH './@speed' NOT NULL, " +
            "         shape TEXT PATH './@shape', " +
            "         spread_type TEXT PATH './@spreadType', " +
            "         length NUMERIC PATH './@length', " +
            "         width NUMERIC PATH './@width', " +
            "         name TEXT PATH './@name', " +
            "         allow TEXT PATH './@allow', " +
            "         disallow TEXT PATH './@disallow', " +
            "         key TEXT PATH './param/@key', " +
            "         value TEXT PATH './param/@value' " +
            "         ) AS xml " +
            "WHERE id = %d; " +
            " " +
            "INSERT INTO edge (sim_id, edge_id, start, finish, priority, type, num_lanes, speed, shape, spread_type, length, width, " +
            "                  name, allow, disallow) " +
            "    (SELECT ?, " +
            "            ev.edge_id, ev.start, ev.finish, ev.priority, ev.type, ev.num_lanes, ev.speed,  " +
            "            ev.shape, ev.spread_type, ev.length, ev.width, ev.name, ev.allow, ev.disallow " +
            "     FROM edge_view ev); " +
            " " +
            "INSERT INTO edge_data (sim_id, edge_id, key, value) " +
            "    (SELECT ?, ev.edge_id, ev.key, ev.value " +
            "     FROM edge_view ev " +
            "     WHERE ev.key IS NOT NULL " +
            "        OR ev.value IS NOT NULL); " +
            " " +
            "INSERT INTO edge_lane " +
            "    (SELECT ?, xml.edge_id, xml.line_id, xml.acceleration,  " +
            "            xml.allow, xml.disallow, xml.speed, xml.width " +
            "     FROM files, " +
            "          xmltable('//lane' PASSING xmlparse(DOCUMENT files.edges) COLUMNS " +
            "              edge_id TEXT PATH './../@id' NOT NULL, " +
            "              line_id INT PATH './@index' NOT NULL, " +
            "              acceleration INT PATH './@acceleration', " +
            "              allow TEXT PATH './@allow', " +
            "              disallow TEXT PATH './@disallow', " +
            "              speed NUMERIC PATH './@speed', " +
            "              width NUMERIC PATH './@width' " +
            "              ) AS xml " +
            "        WHERE files.id = ?); " +
            " " +
            "DROP VIEW IF EXISTS edge_view;";

    public static final String INSERT_SIMULATION =
            "INSERT INTO simulation (name, description) " +
                    "VALUES (?, ?) " +
                    "RETURNING id;";

    public static final String DROP_VIEWS =
            "DROP VIEW IF EXISTS node_view; " +
                    "DROP VIEW IF EXISTS edge_view;";

    public static final String ERASE_FILES = "DELETE FROM files;";

    public static String getInsertNode(int fileIndex) {
        return String.format(INSERT_NODE, fileIndex);
    }

    public static String getInsertEdge(int fileIndex) {
        return String.format(INSERT_EDGE, fileIndex);
    }
}
