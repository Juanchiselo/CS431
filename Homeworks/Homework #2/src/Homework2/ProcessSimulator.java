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
    private long cpuTime;

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
        int quantumTimeLeft;
        Process currentProcess;
        Integer[] currentJobs;
        boolean isCRLocked = false;
        boolean isTurnDone = false;
        cpuTime = 0;
        Queue<Process> processQueue = new LinkedList<>();

        for (Process process : processes)
            processQueue.add(process);

        Platform.runLater(() ->
                Main.controller.setStatus("Simulation is running."));

        while(!processQueue.isEmpty() && !endThread)
        {
            // Reset variables for next process.
            quantumTimeLeft = quantumTime;
            currentProcess = processQueue.peek();
            currentPID = Integer.parseInt(currentProcess.getProcessID());
            currentJobs = currentProcess.getJobs();
            currentProcess.setState("Running");

            // Selects the row of the process that is currently running.
            final int rowIndex = currentPID;
            Platform.runLater(() ->
                Main.controller.selectRow(rowIndex));

            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            //System.out.println("\nPID: " + currentPID);

            // Goes through the current process' jobs.
            for(int i = 0; i < currentJobs.length; i++)
            {
                //System.out.println("Job: " + i);
                //System.out.println("Quantum time left: " + quantumTimeLeft);

                currentJobExecutionTime = currentJobs[i];

                // Executes if the job is not done.
                if (currentJobExecutionTime != 0)
                {
                    // Executes if the job is Non-Critical.
                    if(i % 2 == 0)
                    {
                        isTurnDone = executeJob(currentProcess, i,
                                currentJobExecutionTime, quantumTimeLeft, processQueue);

                        if(isTurnDone)
                            break;
                        else
                            quantumTimeLeft -= currentJobExecutionTime;
                    }
                    // Executes if the job is Critical.
                    else
                    {
                        if(!isCRLocked)
                        {
                            isCRLocked = true;
                            currentProcess.setIsInCriticalRegion(true);
                            isTurnDone = executeJob(currentProcess, i,
                                    currentJobExecutionTime, quantumTimeLeft, processQueue);

                            if(isTurnDone)
                            {
                                currentProcess.setState("Blocked");
                                break;
                            }
                            else
                            {
                                isCRLocked = false;
                                currentProcess.setIsInCriticalRegion(false);
                                quantumTimeLeft -= currentJobExecutionTime;
                            }
                        }
                        else
                        {
                            // If the current process is the locker.
                            if(currentProcess.getIsInCriticalRegion())
                            {
                                isTurnDone = executeJob(currentProcess, i,
                                        currentJobExecutionTime, quantumTimeLeft, processQueue);

                                if(isTurnDone)
                                {
                                    currentProcess.setState("Blocked");
                                    break;
                                }
                                else
                                {
                                    isCRLocked = false;
                                    currentProcess.setIsInCriticalRegion(false);
                                    quantumTimeLeft -= currentJobExecutionTime;
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
        boolean isTurnDone;

        //System.out.print(job + " - " + quantumTimeLeft);
        job -= quantumTimeLeft;
        //System.out.println(" = " + job);

        // If the job is negative it means
        // the job was finished and we still have
        // some quantum time left.
        if (job <= 0)
        {
            process.setExecutionTime(quantumTimeLeft + job);
            cpuTime += (quantumTimeLeft + job);
            quantumTimeLeft = -1 * job;
            process.getJobs()[jobID] = 0;
            process.setCompletionPercent(jobID);
            process.setState("Ready");

            // If this was the last job in the process and
            // it is done, then set the state of the process as done.
            if(jobID == process.getJobs().length - 1)
            {
                process.setCompletionTime(String.valueOf(cpuTime));
                process.setState("Done");
            }

            isTurnDone = false;
        }
        else
        {
            cpuTime += quantumTimeLeft;
            process.setExecutionTime(quantumTimeLeft);
            process.getJobs()[jobID] = job;
            process.setState("Ready");
            processQueue.add(process);
            isTurnDone = true;
        }

        return isTurnDone;
    }
}
