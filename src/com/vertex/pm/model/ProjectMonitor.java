package com.vertex.pm.model;

public class ProjectMonitor {
    private final float overallProgress;
    private final String scheduleStatus;
    private final int tasksCompleted;
    private final int milestonesAchieved;
    private final int delays;

    public ProjectMonitor(float overallProgress, String scheduleStatus, int tasksCompleted, int milestonesAchieved, int delays) {
        this.overallProgress = overallProgress;
        this.scheduleStatus = scheduleStatus;
        this.tasksCompleted = tasksCompleted;
        this.milestonesAchieved = milestonesAchieved;
        this.delays = delays;
    }

    public float getOverallProgress() {
        return overallProgress;
    }

    public String getScheduleStatus() {
        return scheduleStatus;
    }

    public int getTasksCompleted() {
        return tasksCompleted;
    }

    public int getMilestonesAchieved() {
        return milestonesAchieved;
    }

    public int getDelays() {
        return delays;
    }
}
