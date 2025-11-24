package com.springboot.demo.dto.task;

import java.util.List;

public class GroupedTasksResponse {
    private Integer totalCount;
    private List<GroupItemResponse> groups;

    public GroupedTasksResponse() {}
    public GroupedTasksResponse(Integer totalCount, List<GroupItemResponse> groups) {
        this.totalCount = totalCount;
        this.groups = groups;
    }

    public Integer getTotalCount() { return totalCount; }
    public void setTotalCount(Integer totalCount) { this.totalCount = totalCount; }

    public List<GroupItemResponse> getGroups() { return groups; }
    public void setGroups(List<GroupItemResponse> groups) { this.groups = groups; }
}