package com.geosumo.teamone;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.sql.*;

import static com.geosumo.teamone.queries.DataInsertionQueries.*;
import static com.geosumo.teamone.queries.DataCreationQueries.*;

public class Helpers {
    private static final int CHUNK_LEN = 20_000_000;
    private static final String url = "jdbc:postgresql://bronto.ewi.utwente.nl/";
    private static final String user = "dab_di20212b_167";
    private static final String password = "i449stWjyfgclDCm";
    private static final String schema = "?currentSchema=geo_sumo";
    private static final Connection connection = connectToDB();

    public static void processFiles(String simulationName, String simulationDesc, String... files) throws SQLException, NullPointerException {
        try {
            // Insert the raw data into the database
            int fileIndex = sendFiles(files);
            // Parse the data and fill the tables
            processData(simulationName, simulationDesc, fileIndex);
            // Clean leftovers
            cleanup(false);
        } catch (SQLException | NullPointerException ex) {
            System.out.println(ex.getMessage());
            cleanup(true);
        }
    }

    /**
     * Takes data from the database and returns it
     *
     * @param query sql query to execute
     * @param param parameters for the query
     */
    public static String getFromDatabasePrepared(String query, int... param) throws SQLException {
        try {
            if (connection == null) throw new SQLException("Not connected to a database");

            PreparedStatement pr = connection.prepareStatement(query);
            for (int i = 0; i < param.length; i++) {
                System.out.println(param[i]);
                pr.setInt(i + 1, param[i]);
            }

            ResultSet fin = pr.executeQuery();
            if (fin.next()) {
                return fin.getString(1);
            } else {
                return "{}";
            }
        } catch (SQLException e) {
            System.out.println("The given query was wrong!");
            e.printStackTrace();
            return null;
        } catch (NullPointerException e) {
            System.out.println("Could not connect to the database");
            return null;
        }
    }

    /**
     * Returns a new database connection. Uses predefined url, user and password
     */
    private static Connection connectToDB() {
        try {
            Class.forName("org.postgresql.Driver");
            Connection ret = DriverManager.getConnection(url + user + schema, user, password);
            System.out.println("You have connected to database!");
            if (ret == null) return null;

            // Make sure we have the tables ready
            var statement = ret.prepareStatement(CREATE);
            statement.executeUpdate();

            return ret;
        } catch (SQLException e) {
            System.out.println("Can't connect to db: " + e.getMessage());
            return null;
        } catch (ClassNotFoundException ignore) {
            return null;
        }
    }

    /**
     * Sends the given data to a database in chunks
     *
     * @param data  xml data to send
     * @param query query to send the data with
     * @param index index at which to put the data
     */
    private static void sendData(String data, String query, int index) throws SQLException {
        if (connection == null) throw new SQLException("Not connected to a database");

        PreparedStatement statement = connection.prepareStatement(query);
        int len = data.length();

        for (int start = 0; start < len; start += CHUNK_LEN) {
            int end = Math.min(start + CHUNK_LEN, len);
            statement.setString(1, data.substring(start, end));
            statement.setInt(2, index);
            statement.executeUpdate();
        }
    }

    /**
     * Returns the name of the top-level element in an xml document
     *
     * @param xmlString xml file's contents
     */
    private static String getTopElement(String xmlString) {
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();

            ByteArrayInputStream input = new ByteArrayInputStream(xmlString.getBytes(StandardCharsets.UTF_8));
            Document doc = builder.parse(input);
            Element root = doc.getDocumentElement();
            return root.getTagName();
        } catch (Exception ignore) {
            return null;
        }
    }

    /**
     * Returns the appropriate query based on the column we want to modify
     */
    private static String getQuery(String column) {
        if (FileType.EDGES.getColumn().equals(column)) {
            return UPDATE_EDGES;
        } else if (FileType.NODES.getColumn().equals(column)) {
            return UPDATE_NODES;
        } else if (FileType.ROUTES.getColumn().equals(column)) {
            return UPDATE_ROUTES;
        } else if (FileType.OUTPUT.getColumn().equals(column)) {
            return UPDATE_OUTPUT;
        } else {
            return null;
        }
    }

    /**
     * Sends the files to the database for faster processing
     *
     * @param files stringified xml files to send
     */
    private static int sendFiles(String[] files) throws SQLException, NullPointerException {
        if (files == null) throw new NullPointerException("Data was null");
        if (connection == null) throw new SQLException("Not connected to a database");

        PreparedStatement initial = connection.prepareStatement(SWITCH_SCHEMA);
        initial.executeUpdate();

        initial = connection.prepareStatement(INSERT);
        initial.execute();
        ResultSet result = initial.getResultSet();
        result.next();
        int index = result.getInt(1);

        for (String data : files) {
            FileType type = FileType.getType(getTopElement(data));
            if (type == null) throw new NullPointerException("Not a valid simulation file");

            String column = type.getColumn();
            sendData(data, getQuery(column), index);
        }

        return index;
    }

    private static void processData(String name, String desc, int fileIndex) throws SQLException {
        if (connection == null) throw new SQLException();
        // Put data into the simulation table
        PreparedStatement createSim = connection.prepareStatement(INSERT_SIMULATION);
        createSim.setString(1, name);
        createSim.setString(2, desc);
        createSim.execute();

        // Get the simulationId
        ResultSet result = createSim.getResultSet();
        if(!result.next()) throw new SQLException();
        int simId = result.getInt(1);

        // Fill roundabout table
        PreparedStatement fillRoundabout = connection.prepareStatement(INSERT_ROUNDABOUT);
        fillRoundabout.setInt(1, simId);
        fillRoundabout.setInt(2, fileIndex);
        fillRoundabout.executeUpdate();

        // Fill route table
        PreparedStatement fillRoute = connection.prepareStatement(INSERT_ROUTE);
        fillRoute.setInt(2, fileIndex);
        fillRoute.setInt(1, simId);
        fillRoute.executeUpdate();

        // Fill output table
        PreparedStatement fillOutput = connection.prepareStatement(INSERT_OUTPUT);
        fillOutput.setInt(1, simId);
        fillOutput.setInt(2, fileIndex);
        fillOutput.executeUpdate();

        // Fill node table and all related tables
        PreparedStatement fillNode = connection.prepareStatement(getInsertNode(fileIndex));
        fillNode.setInt(1, simId);
        fillNode.setInt(2, simId);
        fillNode.setInt(3, simId);
        fillNode.executeUpdate();

        // Fill edge table and all related tables
        PreparedStatement fillEdge = connection.prepareStatement(getInsertEdge(fileIndex));
        fillEdge.setInt(1, simId);
        fillEdge.setInt(2, simId);
        fillEdge.setInt(3, simId);
        fillEdge.setInt(4, fileIndex);
        fillEdge.executeUpdate();
    }

    private static void cleanup(boolean fullCleanup) throws SQLException {
        if (connection == null) throw new SQLException("Connection was null");
        // DROP views created while populating the database
        PreparedStatement removeTempData = connection.prepareStatement(DROP_VIEWS);
        removeTempData.executeUpdate();
        // Erase the files table
        removeTempData = connection.prepareStatement(ERASE_FILES);
        removeTempData.executeUpdate();

        if (fullCleanup) {
            PreparedStatement reset = connection.prepareStatement(DROP);
            reset.executeUpdate();
            reset = connection.prepareStatement(CREATE);
            reset.executeUpdate();
        }
    }

    public static void main(String[] args) throws SQLException {
        processData("hello", "world", 2);
    }
}
