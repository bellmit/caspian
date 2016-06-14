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

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.json.simple.JSONObject;

import com.emc.caspian.ccs.db.util.AppLogger;

@Path("/v1")
public class MySQLAPI {

	@POST
	@Path("/databases")
	@Consumes(MediaType.APPLICATION_JSON)
	public Response createDatabase(DatabaseWrapper databases) {
		try {
			if (databases.getDatabase().size() != 1)
				return Response.status(Status.Code.BAD_REQUEST.value())
						.entity("Bad request").build();

			Database database = databases.getDatabase().get(0);
			MySQLResponse response = new MySQLResponse();
			response = MySQLClient.createDatabase(response,
					database.getDatabase_name(), database.getUser_name(),
					database.getPassword());
			String message = "{\"message\": \"" + "Database "
					+ database.getDatabase_name() + " and user "
					+ database.getUser_name() + " successfully created" + "\"}";
			if (response.getReturnCode() == Status.Code.OK.value())
				return Response.status(Status.Code.OK.value()).entity(message)
						.build();
			else
				return Response.status(response.getReturnCode())
						.entity(response.getErrorMessage()).build();

		} catch (Exception e) {
			AppLogger.warn("Error creating database " + e.toString());
			return Response.status(Status.Code.INTERNAL_SERVER_ERROR.value())
					.build();
		}
	}

	@SuppressWarnings("unchecked")
	@GET
	@Path("/databases")
	@Produces(MediaType.APPLICATION_JSON)
	public Response listDatabases() {
	
		try {
			JSONObject responseJSON = new JSONObject();
			MySQLResponse response = new MySQLResponse();
			response = MySQLClient.getDatabases(response);
			if (response.getReturnCode() == Status.Code.OK.value()) {
				responseJSON.put("databases", response.getDatabases());
				return Response.status(Status.Code.OK.value())
						.entity(responseJSON).build();
			} else {
				return Response.status(response.getReturnCode())
						.entity(response.getErrorMessage()).build();
			}

		} catch (Exception e) {
			AppLogger.warn("Error listing databases " + e.toString());
			return Response.status(Status.Code.INTERNAL_SERVER_ERROR.value())
					.build();
		}
	}
	
	@SuppressWarnings("unchecked")
	@GET
	@Path("/monitor")
	@Produces(MediaType.APPLICATION_JSON)
	public Response monitorDatabases() {
		try {
			JSONObject responseJSON = new JSONObject();
			MySQLResponse response = new MySQLResponse();
			response = MySQLClient.monitorDatabases(response);

			if (response.getReturnCode() == Status.Code.OK.value()) {
				responseJSON.put("DB_Monitor", response.getDatabases());
				return Response.status(Status.Code.OK.value())
						.entity(responseJSON).build();
			} else {
				return Response.status(response.getReturnCode())
						.entity(response.getErrorMessage()).build();
			}

		} catch (Exception e) {
			AppLogger.warn("Error monitoring databases " + e.toString());
			return Response.status(Status.Code.INTERNAL_SERVER_ERROR.value())
					.build();
		}
	}

	@DELETE
	@Path("/databases/{database_name}")
	@Consumes(MediaType.APPLICATION_JSON)
	public Response deleteDatabase(@PathParam("database_name") String dbName,
			@HeaderParam("user") String userName,
			@HeaderParam("password") String password) {
		try {
			MySQLResponse response = new MySQLResponse();
			response = MySQLClient.deleteDatabase(response, dbName, userName,
					password);
			String message = "{\"message\": \"" + "Database " + dbName
					+ " and user " + userName + " successfully deleted" + "\"}";
			if (response.getReturnCode() == Status.Code.OK.value())
				return Response.status(Status.Code.OK.value()).entity(message)
						.build();
			else
				return Response.status(response.getReturnCode())
						.entity(response.getErrorMessage()).build();

		} catch (Exception e) {
			AppLogger.warn("Error deleting database " + e.toString());
			return Response.status(Status.Code.INTERNAL_SERVER_ERROR.value())
					.build();
		}
	}
};

