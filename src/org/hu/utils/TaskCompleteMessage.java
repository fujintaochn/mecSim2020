package org.hu.utils;

public class TaskCompleteMessage {
    private int messageType;
    private String applicationName;
    private String taskId;
    private String subTaskName;

    public TaskCompleteMessage(int messageType, String applicationName, String taskId) {
        this.messageType = messageType;
        this.applicationName = applicationName;
        this.taskId = taskId;
    }

    public TaskCompleteMessage(int messageType, String applicationName, String taskId, String subTaskName) {
        this.messageType = messageType;
        this.applicationName = applicationName;
        this.taskId = taskId;
        this.subTaskName = subTaskName;
    }

    public int getMessageType() {
        return messageType;
    }

    public void setMessageType(int messageType) {
        this.messageType = messageType;
    }

    public String getApplicationName() {
        return applicationName;
    }

    public void setApplicationName(String applicationName) {
        this.applicationName = applicationName;
    }

    public String getTaskId() {
        return taskId;
    }

    public void setTaskId(String taskId) {
        this.taskId = taskId;
    }

    public String getSubTaskName() {
        return subTaskName;
    }

    public void setSubTaskName(String subTaskName) {
        this.subTaskName = subTaskName;
    }
}
