package com.springboot.demo.dto.task;

import com.springboot.demo.model.Task;
import java.util.List;

public class GroupItemResponse {
    private Object key;
    private String label;
    private Integer order;
    private Integer count;
    private List<Task> tasks;
    private List<GroupItemResponse> groups;

    public Object getKey() { return key; }
    public void setKey(Object key) { this.key = key; }

    public String getLabel() { return label; }
    public void setLabel(String label) { this.label = label; }

    public Integer getOrder() { return order; }
    public void setOrder(Integer order) { this.order = order; }

    public Integer getCount() { return count; }
    public void setCount(Integer count) { this.count = count; }

    public List<Task> getTasks() { return tasks; }
    public void setTasks(List<Task> tasks) { this.tasks = tasks; }

    public List<GroupItemResponse> getGroups() { return groups; }
    public void setGroups(List<GroupItemResponse> groups) { this.groups = groups; }
}