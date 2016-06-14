/**
 *  Copyright (c) 2015 EMC Corporation
 * All Rights Reserved
 *
 * This software contains the intellectual property of EMC Corporation
 * or is licensed to EMC Corporation from third parties.  Use of this
 * software and the intellectual property contained therein is expressly
 * limited to the terms and conditions of the License Agreement under which
 * it is provided by or on behalf of EMC.
 */
package com.emc.caspian.ccs.esrs.internal.model;

import java.util.ArrayList;
import java.util.List;


public class CaspianDetailsModel {

	private String caspianVersion = null;
	private int numberOfNodes;
	private List<NodeHelper> listOfNodes;
	private int numberOfComponents;
	private List<ComponentHelper> listOfComponents = null;
	private int numberOfAccounts;
	private List<AccountHelper> listOfAccounts;
	private int numberOfApis;
	private List<ApiDetailsHelper> listOfApis;
	private MnrHelper mnrDetail;

	public CaspianDetailsModel() {

		if(null == listOfNodes ) {
			this.listOfNodes = new ArrayList<NodeHelper>();
		}
		if(null == listOfAccounts ) {
			this.listOfAccounts = new ArrayList<AccountHelper>();
		}
		if(null == listOfComponents ) {
			this.listOfComponents = new ArrayList<ComponentHelper>();
		}
		if(null == listOfApis ) {
			this.listOfApis = new ArrayList<ApiDetailsHelper>();
		}
	}

	public String getCaspianVersion() {
		return caspianVersion;
	}

	public void setCaspianVersion(final String caspianVersion) {
		this.caspianVersion = caspianVersion;
	}

	public MnrHelper getMnrDetail() {
		return mnrDetail;
	}

	public void setMnrDetail(MnrHelper mnrDetail) {
		this.mnrDetail = mnrDetail;
	}

	public int getNumberOfNodes() {
		return numberOfNodes;
	}

	public List<ComponentHelper> getListOfComponents() {
		return listOfComponents;
	}

	public void setListOfComponents(final List<ComponentHelper> listOfComponents) {
		this.listOfComponents = listOfComponents;
	}

	public void addComponent(final ComponentHelper compDetail) {
		listOfComponents.add(compDetail);
	}

	public void setNumberOfNodes(final int numberOfNodes) {
		this.numberOfNodes = numberOfNodes;
	}

	public List<NodeHelper> getListOfNodes() {
		return listOfNodes;
	}

	public void addNode(final NodeHelper node) {
		listOfNodes.add(node);
	}

	public List<AccountHelper> getListOfAccounts() {
		return listOfAccounts;
	}

	public void addAccount(final AccountHelper account) {
		listOfAccounts.add(account);
	}

	public int getNumberOfComponents() {
		return numberOfComponents;
	}

	public void setNumberOfComponents(final int numberOfComponents) {
		this.numberOfComponents = numberOfComponents;
	}
	public int getNumberOfAccounts() {
		return numberOfAccounts;
	}

	public void setNumberOfAccounts(final int numberOfAccounts) {
		this.numberOfAccounts = numberOfAccounts;
	}

	public void setListOfNodes(final List<NodeHelper> listOfNodes) {
		this.listOfNodes = listOfNodes;
	}

	public void setListOfAccounts(final List<AccountHelper> listOfAccounts) {
		this.listOfAccounts = listOfAccounts;

	}

	public int getNumberOfApis() {
		return numberOfApis;
	}

	public void setNumberOfApis(int numberOfApis) {
		this.numberOfApis = numberOfApis;
	}

	public List<ApiDetailsHelper> getListOfApis() {
		return listOfApis;
	}

	public void setListOfApis(List<ApiDetailsHelper> listOfApis) {
		this.listOfApis = listOfApis;
	}

	public void addApi(ApiDetailsHelper api) {
		this.listOfApis.add(api);
	}

	public void populateDetailsNumbers() {

		numberOfNodes = listOfNodes.size();
		numberOfComponents = listOfComponents.size();
		numberOfAccounts = listOfAccounts.size();
		numberOfApis = listOfApis.size();

	}

	
}
