package Homework2;

import javafx.application.Platform;
import javafx.collections.ObservableList;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Queue;

public class ProcessSimulator extends Thread implements ListenerThread
{
    public volatile static boolean endThread = false;
    private ObservableList<Process> lvProcesses;
    private ObservableList<Process> saProcesses;
    private int quantumTime;
    private long lvCPUtime;
    private long saCPUtime;
    public static boolean lvSimulation = true;
    public static boolean saSimulation = true;
    public volatile static int simulationSpeed = 0;
    public volatile static boolean selectRow = true;
    public volatile static int turn = 0;
    private ArrayList<Thread> threadsList;

    public ProcessSimulator(ObservableList<Process> lvProcesses,
                            ObservableList<Process> saProcesses,
                            int quantumTime)
    {
        super("Process Simulator Thread");
        this.lvProcesses = lvProcesses;
        this.saProcesses = saProcesses;
        this.quantumTime = quantumTime;
        threadsList = new ArrayList<>();
    }

    @Override
    public void run()
    {
        // Set up the tables with the data.
        Platform.runLater(() ->
        {
            Main.controller.setStatus("STATUS", "Simulation is running.");
            Main.controller.setTableView("Lock Variable", lvProcesses);
            Main.controller.setTableView("Strict Alternation", saProcesses);
        });

        if(lvSimulation)
        {
            NotifierThread lockVariableThread =
                    new NotifierThread("Lock Variable Thread")
                    {
                        @Override
                        public void doRun(){lockVariable();}
                    };
            lockVariableThread.addListener(this);
            lockVariableThread.start();
            threadsList.add(lockVariableThread);
        }

        if(saSimulation)
            strictAlternation();
    }

    /**
     * The Lock Variable Mutual Exclusion method simulation.
     */
    private void lockVariable()
    {
        int currentPID;
        int currentJobExecutionTime;
        int quantumTimeLeft;
        Process currentProcess;
        Integer[] currentJobs;
        boolean isCRLocked = false;
        boolean isTurnDone;
        lvCPUtime = 0;
        Queue<Process> processQueue = new LinkedList<>();

        processQueue.addAll(lvProcesses);

        while(!processQueue.isEmpty() && !endThread)
        {
            // Reset variables for next process.
            quantumTimeLeft = quantumTime;
            currentProcess = processQueue.peek();
            currentPID = Integer.parseInt(currentProcess.getProcessID());
            currentJobs = currentProcess.getJobs();
            currentProcess.setState("Running");

            // Selects the row of the process that is currently running.
            if(selectRow)
            {
                final int rowIndex = currentPID;
                Platform.runLater(() ->
                        Main.controller.selectRow("Lock Variable", rowIndex));
            }

            try {
                Thread.sleep(simulationSpeed);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            // Goes through the current process' jobs.
            for(int currentJobID = 0; currentJobID < currentJobs.length; currentJobID++)
            {
                currentJobExecutionTime = currentJobs[currentJobID];

                // Executes if the job is not done.
                if (currentJobExecutionTime != 0)
                {
                    // Executes if the job is Non-Critical.
                    if(currentJobID % 2 == 0)
                    {
                        isTurnDone = lvExecuteJob(currentProcess, currentJobID,
                                currentJobExecutionTime, quantumTimeLeft, processQueue);

                        if(isTurnDone)
                            break;
                    }
                    // Executes if the job is Critical.
                    else
                    {
                        if(!isCRLocked)
                        {
                            isCRLocked = true;
                            currentProcess.setIsInCriticalRegion(true);
                            isTurnDone = lvExecuteJob(currentProcess, currentJobID,
                                    currentJobExecutionTime, quantumTimeLeft, processQueue);

                            // Did not finished its critical region.
                            if(isTurnDone)
                            {
                                currentProcess.setState("Blocked");
                                break;
                            }
                            else
                            {
                                isCRLocked = false;
                                currentProcess.setIsInCriticalRegion(false);
                            }
                        }
                        else
                        {
                            // If the current process is the locker.
                            if(currentProcess.getIsInCriticalRegion())
                            {
                                isTurnDone = lvExecuteJob(currentProcess, currentJobID,
                                        currentJobExecutionTime, quantumTimeLeft, processQueue);

                                if(isTurnDone)
                                {
                                    currentProcess.setState("Ready");
                                    break;
                                }
                                else
                                {
                                    isCRLocked = false;
                                    currentProcess.setIsInCriticalRegion(false);
                                }
                            }
                            // Another process is in its critical region.
                            else
                            {
                                currentProcess.setState("Blocked");
                                processQueue.add(currentProcess);
                                break;
                            }
                        }
                    }

                    if(!isTurnDone)
                        quantumTimeLeft -= currentJobExecutionTime;
                }
            }

            processQueue.remove();
            Platform.runLater(() ->
                    Main.controller.updateTable("Lock Variable", lvProcesses));
        }

    }

    /**
     * Creates the threadsList for the Strict Alternation
     * Mutual Exclusion method.
     */
    private void strictAlternation()
    {
        saCPUtime = 0;
        ArrayList<Process> processes = new ArrayList<>(saProcesses.size());
        processes.addAll(saProcesses);

        for(int i = 0; i < saProcesses.size(); i++)
        {
            final int processID = i;

            NotifierThread strictAlternationThread =
                    new NotifierThread("Strict Alternation Thread #" + i)
            {
                @Override
                public void doRun()
                {
                    strictAlternation(saProcesses.get(processID),
                    processes);
                }
            };
            strictAlternationThread.addListener(this);
            strictAlternationThread.start();
            threadsList.add(strictAlternationThread);
        }
    }

    /**
     * The Strict Alternation Mutual Exclusion method simulation.
     * @param process - The process to simulate.
     * @param processesLeft - The processes that have not finished yet.
     */
    private void strictAlternation(Process process, ArrayList<Process> processesLeft)
    {
        int processID = Integer.parseInt(process.getProcessID());
        Integer[] jobs = process.getJobs();
        boolean isProcessDone = false;

        while(!isProcessDone && !endThread)
        {
            // The Busy Waiting loop.
            // It forces the process to wait for its turn.
            while(processID != turn) {}
            process.setState("Running");

            if(selectRow)
            {
                final int rowIndex = processID;
                Platform.runLater(() ->
                        Main.controller.selectRow("Strict Alternation", rowIndex));
            }

            for(int i = 0; i < jobs.length; i++)
            {
                try {
                    Thread.sleep(simulationSpeed);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                if(jobs[i] != 0)
                {
                    if(i % 2 == 1)
                    {
                        process.setCriticalRegion(true);
                        process.setState("Blocked");
                        saExecuteJob(process, i, jobs[i]);
                        process.setCriticalRegion(false);
                        nextTurn(process, processesLeft);
                        if(i != jobs.length - 1)
                            saExecuteJob(process, i + 1, jobs[i + 1]);
                    }
                    // This only executes the first job,
                    // a Non-Critical job. It's the process setup.
                    else
                    {
                        saExecuteJob(process, i, jobs[i]);
                        nextTurn(process, processesLeft);
                    }
                    break;
                }

                // This executes when all jobs in the
                // process have finished.
                if(i == jobs.length - 1 && jobs[i] == 0)
                {
                    process.setState("Done");
                    isProcessDone = true;
                    process.setCompletionTime(String.valueOf(saCPUtime));
                    nextTurn(process, processesLeft);
                    processesLeft.remove(process);
                }

                Platform.runLater(() ->
                        Main.controller.updateTable("Strict Alternation", saProcesses));
            }
        }
    }

    /**
     * Sets the next turn for the Strict Alternation Mutual Exclusion method.
     * @param process
     * @param processesLeft
     */
    private void nextTurn(Process process, ArrayList<Process> processesLeft)
    {
        if(processesLeft.size() != 1)
        {
            if(turn == Integer.parseInt(
                    processesLeft.get(processesLeft.size() - 1)
                            .getProcessID()))
                turn = Integer.parseInt(
                        processesLeft.get(0).getProcessID());
            else
                turn = Integer.parseInt(processesLeft
                        .get(processesLeft.indexOf(process) + 1)
                        .getProcessID());
        }
    }

    /**
     * Executes the job for the Strict Alternation Mutual Exclusion method.
     * @param process - The process whose job needs to be executed.
     * @param jobID - The ID of the job.
     * @param jobExecutionTime - The job execution time.
     */
    private void saExecuteJob(Process process, int jobID, int jobExecutionTime)
    {
        process.setExecutionTime(jobExecutionTime);
        saCPUtime += jobExecutionTime;
        process.getJobs()[jobID] = 0;
        process.setCompletionPercent(jobID);
        process.setState("Ready");
    }

    /**
     * Executes the job for the Lock Variable Mutual Exclusion method.
     * @param process - The process whose job needs to be executed.
     * @param jobID - The ID of the job.
     * @param jobExecutionTime - The job execution time.
     * @param quantumTimeLeft - The quantum time left.
     * @param processQueue - The processes queue.
     * @return - Returns whether the processes turn is done or not.
     *          AKA whether the process still has CPU/Quantum time.
     */
    private boolean lvExecuteJob(Process process, int jobID,
                                 int jobExecutionTime, int quantumTimeLeft,
                                 Queue<Process> processQueue)
    {
        boolean isTurnDone;

        jobExecutionTime -= quantumTimeLeft;

        // If the job is negative it means
        // the job was finished and we still have
        // some quantum time left.
        if (jobExecutionTime <= 0)
        {
            // TODO: Make this more readable.
            process.setExecutionTime(quantumTimeLeft + jobExecutionTime);
            lvCPUtime += (quantumTimeLeft + jobExecutionTime);
            process.getJobs()[jobID] = 0;
            process.setCompletionPercent(jobID);
            process.setState("Ready");

            // If this was the last job in the process and
            // it is done, then set the state of the process as done.
            if(jobID == process.getJobs().length - 1)
            {
                process.setCompletionTime(String.valueOf(lvCPUtime));
                process.setState("Done");
            }

            isTurnDone = false;
        }
        else
        {
            lvCPUtime += quantumTimeLeft;
            process.setExecutionTime(quantumTimeLeft);
            process.getJobs()[jobID] = jobExecutionTime;
            process.setState("Ready");
            processQueue.add(process);
            isTurnDone = true;
        }

        return isTurnDone;
    }

    /**
     * Notifies the Process Simulator thread when one of
     * its child threads has ended.
     * @param thread - The thread that ended.
     */
    @Override
    public void threadEnded(Thread thread)
    {
        threadsList.remove(thread);

        if(threadsList.isEmpty())
        {
            Platform.runLater(() ->
            {
                Main.controller.setStatus("STATUS",
                        "Simulation has finished.");
                Main.controller.disableButtons(false);
            });
        }
    }
}