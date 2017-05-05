package Homework2;

import javafx.application.Platform;
import javafx.collections.ObservableList;
import java.util.LinkedList;
import java.util.Queue;

public class ProcessSimulator extends Thread
{
    public volatile static boolean endThread = false;
    private ObservableList<Process> processes;
    private int quantumTime;

    public ProcessSimulator(ObservableList<Process> processes, int quantumTime)
    {
        super("Process Simulator Thread");
        this.processes = processes;
        this.quantumTime = quantumTime;
    }
    @Override
    public void run()
    {
        int currentPID;
        int currentJobExecutionTime;
        int currentQuantumTime;
        Process currentProcess;
        Integer[] currentJobs;
        boolean isCRLocked = false;
        Queue<Process> processQueue = new LinkedList<>();

        for (Process process : processes)
            processQueue.add(process);

        while(!processQueue.isEmpty() && !endThread)
        {
            // Reset variables for next process.
            currentQuantumTime = quantumTime;
            currentProcess = processQueue.peek();
            currentPID = Integer.parseInt(currentProcess.getProcessID());
            currentJobs = currentProcess.getJobs();
            currentProcess.setState("Running");

            // Selects the row of the process that is currently running.
            final int rowIndex = currentPID;
            Platform.runLater(() ->
                Main.controller.selectRow(rowIndex));

            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            // Goes through the current process' jobs.
            for(int i = 0; i < currentJobs.length; i++)
            {
                currentJobExecutionTime = currentJobs[i];

                // Executes if the job is not done.
                if (currentJobExecutionTime != 0)
                {
                    // Executes if the job is Non-Critical.
                    if(i % 2 == 0)
                    {
                        currentJobExecutionTime = executeJob(currentProcess, i,
                                currentJobExecutionTime, currentQuantumTime, processQueue);

                        // If the current job is negative it means
                        // the job was finished and we still have
                        // some quantum time left.
                        if (currentJobExecutionTime <= 0)
                        {
                            // TODO: Change these parameters so it's more readable.
                            currentProcess.setExecutionTime(currentQuantumTime + currentJobExecutionTime);
                            currentQuantumTime = -1 * currentJobExecutionTime;
                            currentJobs[i] = 0;
                            currentProcess.setCompletion(i);
                            currentProcess.setState("Ready");

                            // If this was the last job in the process and
                            // it is done, then set the state of the process as done.
                            if(i == currentJobs.length - 1)
                                currentProcess.setState("Done");
                        }
                        else
                        {
                            currentProcess.setExecutionTime(currentQuantumTime);
                            currentJobs[i] = currentJobExecutionTime;
                            currentProcess.setState("Ready");
                            processQueue.add(currentProcess);
                            break;
                        }
                    }
                    // Executes if the job is Critical.
                    else
                    {
                        if(!isCRLocked)
                        {
                            isCRLocked = true;
                            currentProcess.setCriticalRegion(true);
                            currentJobExecutionTime = executeJob(currentJobExecutionTime, currentQuantumTime);

                            // If the current job is negative it means
                            // the job was finished and we still have
                            // some quantum time left.
                            if (currentJobExecutionTime < 0)
                            {
                                currentProcess.setExecutionTime(currentQuantumTime + currentJobExecutionTime);
                                currentQuantumTime = -1 * currentJobExecutionTime;
                                currentJobs[i] = 0;
                                currentProcess.setCompletion(i);
                                isCRLocked = false;
                                currentProcess.setCriticalRegion(false);
                                currentProcess.setState("Ready");

                                // If this was the last job in the process and
                                // it is done, then set the state of the process as done.
                                if(i == currentJobs.length - 1)
                                    currentProcess.setState("Done");
                            }
                            else
                            {
                                currentProcess.setExecutionTime(currentQuantumTime);
                                currentJobs[i] = currentJobExecutionTime;
                                currentProcess.setState("Blocked");
                                processQueue.add(currentProcess);
                                break;
                            }
                        }
                        else
                        {
                            if(currentProcess.getCriticalRegion())
                            {
                                currentJobExecutionTime = executeJob(currentJobExecutionTime, currentQuantumTime);

                                // If the current job is negative it means
                                // the job was finished and we still have
                                // some quantum time left.
                                if (currentJobExecutionTime < 0)
                                {
                                    currentProcess.setExecutionTime(currentQuantumTime + currentJobExecutionTime);
                                    currentQuantumTime = -1 * currentJobExecutionTime;
                                    currentJobs[i] = 0;
                                    currentProcess.setCompletion(i);
                                    isCRLocked = false;
                                    currentProcess.setCriticalRegion(false);
                                    currentProcess.setState("Ready");

                                    // If this was the last job in the process and
                                    // it is done, then set the state of the process as done.
                                    if(i == currentJobs.length - 1)
                                        currentProcess.setState("Done");
                                }
                                else
                                {
                                    currentProcess.setExecutionTime(currentQuantumTime);
                                    currentJobs[i] = currentJobExecutionTime;
                                    currentProcess.setState("Blocked");
                                    Platform.runLater(() ->
                                            Main.controller.blockRow(rowIndex));
                                    processQueue.add(currentProcess);
                                    break;
                                }
                            }
                            // Another process is in its critical region.
                            else
                            {
                                currentProcess.setState("Ready");
                                processQueue.add(currentProcess);
                                break;
                            }
                        }
                    }

                }
            }

            processQueue.remove();
            Platform.runLater(() ->
                    Main.controller.updateTable(processes));
        }

        Platform.runLater(() ->
                Main.controller.setStatus("Simulation has finished."));
    }

    private boolean executeJob(Process process, int jobID,
                           int job, int quantumTimeLeft,
                           Queue<Process> processQueue)
    {
        boolean isJobDone;

        job -= quantumTimeLeft;

        // If the job is negative it means
        // the job was finished and we still have
        // some quantum time left.
        if (job <= 0)
        {
            process.setExecutionTime(quantumTimeLeft + job);
            quantumTimeLeft = -1 * job;
            process.getJobs()[jobID] = 0;
            process.setCompletion(jobID);
            process.setState("Ready");

            // If this was the last job in the process and
            // it is done, then set the state of the process as done.
            if(jobID == process.getJobs().length - 1)
                process.setState("Done");

            isJobDone = true;
        }
        else
        {
            process.setExecutionTime(quantumTimeLeft);
            process.getJobs()[jobID] = job;
            process.setState("Ready");
            processQueue.add(process);
            isJobDone = false;
        }

        return isJobDone;
    }
}
