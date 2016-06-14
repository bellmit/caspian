package com.emc.caspian.ccs.esrs.internal.model;

import java.util.ArrayList;
import java.util.List;

import com.emc.caspian.ccs.esrs.internal.model.ProjectsHelper;

public class DomainHelper {
	private String domainName;
	private int numberOfProjects;
	private List<ProjectsHelper> listOfProjects = new ArrayList<ProjectsHelper>();

	public int getNumberOfProjects() {
		return numberOfProjects;
	}

	public void setNumberOfProjects(int numberOfProjects) {
		this.numberOfProjects = numberOfProjects;
	}

	public String getDomainName() {
		return domainName;
	}

	public void setDomainName(String domainName) {
		this.domainName = domainName;
	}

	public List<ProjectsHelper> getListOfProjects() {
		return listOfProjects;
	}

	public void setListOfProjects(List<ProjectsHelper> listProjects) {
		this.listOfProjects = listProjects;
	}

	public void addProjects(ProjectsHelper projects) {
		this.listOfProjects.add(projects);
	}
}