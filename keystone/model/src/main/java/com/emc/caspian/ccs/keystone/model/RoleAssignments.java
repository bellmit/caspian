package com.emc.caspian.ccs.keystone.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/**
* Created by gulavb on 4/24/2015.
*/
public class RoleAssignments {

    public List<RoleAssignment> getRoleAssignmentList() {
        return roleAssignmentList;
    }

    public void setRoleAssignmentList(List<RoleAssignment> roleAssignmentList) {
        this.roleAssignmentList = roleAssignmentList;
    }

    @JsonProperty("role_assignments")
    List<RoleAssignment> roleAssignmentList;

}
