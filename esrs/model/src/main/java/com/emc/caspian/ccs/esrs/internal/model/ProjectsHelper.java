package com.emc.caspian.ccs.esrs.internal.model;

public class ProjectsHelper{
	private String projId;
	private int numberOfUsers;

	public ProjectsHelper(String projId) {
		this.projId = projId;
	}

	public String getProjId() {
		return projId;
	}

	public void setProjId(String projId) {
		this.projId = projId;
	}

	public int getNumberOfUsers() {
		return numberOfUsers;
	}

	public void setNumberOfUsers(int numberOfUsers) {
		this.numberOfUsers = numberOfUsers;
	}
}