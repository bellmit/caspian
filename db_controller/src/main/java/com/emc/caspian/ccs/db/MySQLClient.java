/**
 * Copyright (c) 2015 EMC Corporation
 * All Rights Reserved
 *
 * This software contains the intellectual property of EMC Corporation
 * or is licensed to EMC Corporation from third parties.  Use of this
 * software and the intellectual property contained therein is expressly
 * limited to the terms and conditions of the License Agreement under which
 * it is provided by or on behalf of EMC.
 */

package com.emc.caspian.ccs.db;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import com.emc.caspian.ccs.db.util.AppLogger;
import com.emc.caspian.crs.model.ApplicationException;

public class MySQLClient {

    static {
        AppLogger.info("DB Controller is initializing.");
        InitializeMySQLProperties.initializeMySQLPropertiesFromConfig();

        //load the jdbc driver.
        try {
            Class.forName("com.mysql.jdbc.Driver").newInstance();
        }
        catch (InstantiationException|IllegalAccessException| ClassNotFoundException  e) {
            AppLogger.error("Could not load MySQL driver", e);
            // This is a FATAL error, there is no recovering from this
            System.exit(-1);
        }
    }

    private static String getMySqlConnUrl() {
        for(int i=0; i<3; i++) {
            try {
                AppLogger.info("Getting SQL URL from CRS. Attempt {} of 3.", i+1);
                StringBuilder sb = new StringBuilder("jdbc:");
                sb.append(CRSClient.getMySQLURL());
                return sb.toString() ;
            } catch (ApplicationException e) {
                AppLogger.error("Could not get SQL URL from CRS. {}", e);
            }
        }
        AppLogger.error("Exahausted all attempts to get SQL URL from CRS. {}");
        throw new RuntimeException("Error:Could not find My SQL in CRS");
    }

    private static Connection getDbConnection(String username, String password) throws SQLException {
        String url = getMySqlConnUrl();
        return DriverManager.getConnection(url, username, password);
    }

    private static Connection getAccountDbConnection() throws SQLException {
        String user = InitializeMySQLProperties.getAccountsUser();
        char[] password = InitializeMySQLProperties.getAccountsPassword();
        return getDbConnection(user, String.valueOf(password));
    }

    @SuppressWarnings("unchecked")
    public static MySQLResponse getDatabases(MySQLResponse response) {

        JSONArray databases = new JSONArray();

        try(Connection connection = getAccountDbConnection()){

            DatabaseMetaData dbmd = connection.getMetaData();
            ResultSet catalogs = dbmd.getCatalogs();

            while (catalogs.next()) {
                String databaseName = catalogs.getString(1);
                if (databaseName.equals("information_schema")
                        || databaseName.equals("performance_schema")
                        || databaseName.equals("mysql"))
                    continue;
                JSONObject json = new JSONObject();
                json.put("database_name", databaseName);
                databases.add(json);
            }
            response.setReturnCode(Status.Code.OK.value());
            response.setDatabases(databases);

            connection.close();

            AppLogger.debug("Get DB was successfull");

        } catch (SQLException e) {
            handleInternalError(e, response);
        } 
        return response;
    }

    @SuppressWarnings("unchecked")
    public static MySQLResponse deleteDatabase(MySQLResponse response,
            String dbName, String userName, String password) {

        try {

            MySQLResponse response1 = new MySQLResponse();
            response1 = getDatabases(response1);
            JSONArray array = response1.getDatabases();
            JSONObject json = new JSONObject();
            json.put("database_name", dbName);

            if (!(array.contains(json))) {
                response.setReturnCode(Status.Code.NOT_FOUND.value());
                response.setErrorMessage(Status.DB_NOT_FOUND);
                return response;
            } else {

                try(Connection connection = getDbConnection(userName, password)){
                    String dropDB = "DROP DATABASE " + dbName;
                    try(Statement statement = connection.createStatement()){
                        statement.executeUpdate(dropDB);
                        statement.close();
                    }
                    connection.close();
                }

                try(Connection connection = getAccountDbConnection()){
                    try(Statement statement = connection.createStatement()){
                        String dropUser = "DROP USER '" + userName + "'";
                        statement.executeUpdate(dropUser);
                    }
                }

                AppLogger.debug("Delete DB was successfull");
                response.setReturnCode(Status.Code.OK.value());
            }

        } catch (SQLException e) {

            AppLogger.error(Status.SQL_EXCEPTION, e);
            if (e.getMessage().contains("Access denied for user")) {
                handleError(e, response, Status.WRONG_CREDENTIALS, Status.Code.UNAUTHENTICATED);
            } else if (e.getMessage().contains("database doesn't exist")) {
                handleError(e, response, Status.DB_NOT_FOUND, Status.Code.NOT_FOUND);
            } else {
                handleError(e, response, Status.SQL_EXCEPTION, Status.Code.INTERNAL_SERVER_ERROR);
            }

        } catch (Exception ex) {
            handleInternalError(ex, response);
        } 
        return response;
    }

    public static MySQLResponse monitorDatabases(MySQLResponse response) {

        Map<String, String> monitors = new HashMap<String, String>();

        monitors = populateMonitorQueries(monitors);
        JSONArray results = new JSONArray();

        try(Connection connection = getAccountDbConnection()){

            try (Statement statement = connection.createStatement()){
                for (String queryKey : monitors.keySet()) {
                    ResultSet resultSet = statement.executeQuery(monitors.get(queryKey));
                    while (resultSet.next()) {

                        JSONObject json = new JSONObject();
                        json.put(resultSet.getString(1),resultSet.getString(2));
                        results.add(json);
                    }
                }
                statement.close();
            }
            response.setReturnCode(Status.Code.OK.value());
            response.setDatabases(results);
            connection.close();
            AppLogger.debug("Monitor DB Successfull");

        } catch (SQLException e) {
            handleInternalError(e, response);
            AppLogger.error(Status.SQL_EXCEPTION, e);
        }   
        return response;
    }

    private static Map<String, String> populateMonitorQueries(Map<String, String> monitors){

        //Note: Change this method to add queries. 
        //ToDo: Decide which queries to execute and how to receive list of queries.
        String monitor1 = "SHOW GLOBAL STATUS LIKE 'aborted_connects'";
        String monitor4 = "SHOW GLOBAL VARIABLES LIKE 'max_connections'";
        String monitor5 = "SHOW GLOBAL STATUS LIKE 'max_used_connections'";

        monitors.put("GLOBAL_STATUS",monitor1);
        monitors.put("MAX_CONNECTIONS",monitor4);
        monitors.put("MAX_USED_CONNECTIONS",monitor5);

        return monitors;
    }

    public static MySQLResponse createDatabase(MySQLResponse response,
            String dbName, String userName, String password) {
        String create_db = "CREATE DATABASE " + dbName;
        String drop_db = "DROP DATABASE " + dbName;
        String create_user = "CREATE USER '" + userName
                + "'@'%' IDENTIFIED BY '" + password + "'";
        String grant = "GRANT ALL PRIVILEGES ON " + dbName + ".* TO '"
                + userName + "'@'%'";
        String flush = "FLUSH PRIVILEGES";

        boolean dbCreated = false;
        boolean userCreated = false;

        try {
            try(Connection connection = getAccountDbConnection()) {

                Statement statement = connection.createStatement();
                try {

                    statement.executeUpdate(create_db);
                    //since no exception thrown above, now set dbCreated to true
                    dbCreated = true;
                    statement.executeUpdate(create_user);
                    //since no exception thrown above, now set userCreated to true
                    userCreated = true;
                    statement.executeUpdate(grant);
                    statement.executeUpdate(flush);
                    response.setReturnCode(Status.Code.OK.value());

                    AppLogger.debug("Create DB successfull");
                } catch (SQLException e) {
                    if (e.getMessage().contains("database exists")) {
                        handleError(e, response, Status.DB_EXISTS, Status.Code.CONFLICT);
                    } else if (e.getMessage().contains("Operation CREATE USER failed")) {
                        statement.executeUpdate(drop_db);
                        handleError(e, response, Status.USER_EXISTS, Status.Code.CONFLICT);
                    } else if (e.getMessage().contains("You have an error in your SQL syntax")
                            && dbCreated == false) {
                        handleError(e, response, Status.UNACCEPTABLE_DB_NAME, Status.Code.PRECONDITION_FAILED);
                    } else if (e.getMessage().contains("You have an error in your SQL syntax")
                            && userCreated == false) {
                        statement.executeUpdate(drop_db);
                        handleError(e, response, Status.UNACCEPTABLE_CREDENTIALS, Status.Code.PRECONDITION_FAILED);
                    } else {
                        handleInternalError(e, response);
                    }
                    AppLogger.error(Status.SQL_EXCEPTION, e);
                }  

                statement.close();
                connection.close();
            } 
        } catch(SQLException e) {
            handleInternalError(e, response);
            AppLogger.error("Exception during dropping DB or closing statement/connection: {}", e);
        }

        return response;
    }

    private static void handleError(Exception e, MySQLResponse response, String message, Status.Code code) {
        response.setErrorMessage(message);
        response.setReturnCode(code.value());
        AppLogger.error(message, e);
    }
    private static void handleInternalError(Exception e, MySQLResponse response) {
        handleError(e, response, Status.INTERNAL_ERROR, Status.Code.INTERNAL_SERVER_ERROR);
    }
}  	