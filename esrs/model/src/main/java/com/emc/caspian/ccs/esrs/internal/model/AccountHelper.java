package com.emc.caspian.ccs.esrs.internal.model;

import java.util.ArrayList;
import java.util.List;

import com.emc.caspian.ccs.esrs.internal.model.DomainHelper;

public class AccountHelper{
	private String accountName;
	private int numOfDomains;
	private List<DomainHelper> listOfDomains;


	public AccountHelper() {
		if(null == listOfDomains ) {
			this.listOfDomains = new ArrayList<DomainHelper>();
		}
	}
	public String getAccountName() {
		return accountName;
	}
	public void setAccountName(String accountName) {
		this.accountName = accountName;
	}
	public int getNumOfDomains() {
		return numOfDomains;
	}
	public void setNumOfDomains(int numOfDomains) {
		this.numOfDomains = numOfDomains;
	}
	public List<DomainHelper> getListOfDomains() {
		return listOfDomains;
	}
	public void setListOfDomains(List<DomainHelper> listOfDomains) {
		this.listOfDomains = listOfDomains;
	}
	public void addDomain(DomainHelper domain) {
		this.listOfDomains.add(domain);
	}
}
