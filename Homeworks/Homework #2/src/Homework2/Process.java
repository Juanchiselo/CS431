package Homework2;

import javafx.beans.property.SimpleStringProperty;

public class Process
{
    private final SimpleStringProperty processID;
    private final SimpleStringProperty state;
    private final SimpleStringProperty completionPercent;
    private final SimpleStringProperty executionTime;
    private final SimpleStringProperty completionTime;
    private final SimpleStringProperty criticalRegion;
    private boolean isInCriticalRegion = false;
    private Integer[] jobs;

    public Process(String processID, String state,
                   String completion, String executionTime,
                   Integer[] jobs)
    {
        this.processID = new SimpleStringProperty(processID);
        this.state = new SimpleStringProperty(state);
        this.completionPercent = new SimpleStringProperty(completion);
        this.executionTime = new SimpleStringProperty(executionTime);
        this.completionTime = new SimpleStringProperty(String.valueOf(0));
        this.criticalRegion = new SimpleStringProperty("");
        this.jobs = jobs;
    }

    public String getState()
    {
        return state.get();
    }

    public void setState(String state)
    {
        this.state.set(state);
    }

    public String getCompletionPercent()
    {
        return completionPercent.get();
    }

    public void setCompletionPercent(int lastJobCompleted)
    {
        float completionPercentage = (((float)lastJobCompleted + 1) / jobs.length) * 100;
        this.completionPercent.set(String.valueOf((int)completionPercentage) + "%");
    }

    public String getExecutionTime()
    {
        return executionTime.get();
    }

    public void setExecutionTime(int executionTime)
    {
        int executionTimeInt = Integer.parseInt(this.executionTime.get());
        executionTimeInt += executionTime;
        this.executionTime.set(String.valueOf(executionTimeInt));
    }

    public String getProcessID()
    {
        return processID.get();
    }

    public void setProcessID(String processID)
    {
        this.processID.set(processID);
    }

    public Integer[] getJobs()
    {
        return jobs;
    }

    public void setJobs(Integer[] jobs)
    {
        this.jobs = jobs;
    }

    public boolean getIsInCriticalRegion()
    {
        return isInCriticalRegion;
    }

    public void setIsInCriticalRegion(boolean isInCriticalRegion)
    {
        setCriticalRegion(isInCriticalRegion);
        this.isInCriticalRegion = isInCriticalRegion;
    }

    public String getCriticalRegion()
    {
        return criticalRegion.get();
    }

    public void setCriticalRegion(boolean isInCriticalRegion)
    {
        if(isInCriticalRegion)
            criticalRegion.set("*");
        else
            criticalRegion.set("");

    }

    public String getCompletionTime()
    {
        return completionTime.get();
    }

    public void setCompletionTime(String completionTime)
    {
        this.completionTime.set(completionTime);
    }
}