package Homework2;

import javafx.beans.property.SimpleStringProperty;

public class Process
{
    private final SimpleStringProperty processID;
    private final SimpleStringProperty state;
    private final SimpleStringProperty completion;
    private final SimpleStringProperty executionTime;
    private boolean isInCriticalRegion = false;
    private Integer[] jobs;

    public Process(String processID, String state,
                   String completion, String executionTime,
                   Integer[] jobs)
    {
        this.processID = new SimpleStringProperty(processID);
        this.state = new SimpleStringProperty(state);
        this.completion = new SimpleStringProperty(completion);
        this.executionTime = new SimpleStringProperty(executionTime);
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

    public String getCompletion()
    {
        return completion.get();
    }

    public void setCompletion(int lastJobCompleted)
    {
        float completionPercentage = (((float)lastJobCompleted + 1) / jobs.length) * 100;
        this.completion.set(String.valueOf((int)completionPercentage) + "%");
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

    public boolean getCriticalRegion()
    {
        return isInCriticalRegion;
    }

    public void setCriticalRegion(boolean isInCriticalRegion)
    {
        this.isInCriticalRegion = isInCriticalRegion;
    }

}