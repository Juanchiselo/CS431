package Homework4;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.image.Image;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import java.io.*;
import java.nio.file.Paths;
import java.util.*;
import Homework4.GanttChart;

public class Main extends Application
{
    public static Controller controller;
    public static Scene simulatorScene;
    private static GanttChart<Number, String> chart;
    private static int count = 0;

    /**
     * The overridden start() method belonging to the Application class.
     * @param primaryStage - The primary stage of the Application.
     * @throws Exception
     */
    @Override
    public void start(Stage primaryStage) throws Exception
    {
        // Scene setup.
        FXMLLoader loader = new FXMLLoader(
                getClass().getResource("Layouts/ProcessesLayout.fxml"));
        simulatorScene = new Scene(loader.load(), 1280, 720);

        // Saving a controller reference so other threads
        // can modify the GUI components.
        controller = loader.getController();

        // Stage setup.
        primaryStage.getIcons().add(
                new Image(Main.class
                        .getResourceAsStream("Drawable/Icon.png")));
        primaryStage.setTitle("Homework #4 - Jose Juan Sandoval");
        primaryStage.setResizable(true);
        primaryStage.setScene(simulatorScene);
        primaryStage.show();
    }

    public static void main(String[] args)
    {
        launch(args);
        ProcessSimulator.endThread = true;
    }

    /**
     * Creates process data for the simulation.
     * @param processes - The number of process data files to create.
     * @param jobsPerProcess - The maximum number of jobs per process.
     * @param maxJobExecutionTime - The maximum amount of execution time a job can have.
     */
    public static void createProcessData(int processes,
                                         int jobsPerProcess,
                                         int maxJobExecutionTime)
    {
        Platform.runLater(() ->
                controller.setStatus("STATUS", "Creating process files."));

        FileHandler.getInstance().writeProcessFiles(processes, jobsPerProcess, maxJobExecutionTime);

        Platform.runLater(() ->
                controller.setStatus("STATUS", "Finished creating process files."));
    }

    /**
     * Creates Page data for the simulation.
     * @param processes - The number of process data files to create.
     * @param pagesPerProcess - The maximum number of virtual pages per process.
     * @param maxPageExecutionTime - The maximum amount of execution time a virtual page can have.
     * @param ramSize - The size of the simulated physical ram.
     */
    public static void createPageData(int processes,
                                      int pagesPerProcess,
                                      int maxPageExecutionTime,
                                      int ramSize)
    {
        Platform.runLater(() ->
                controller.setStatus("STATUS", "Creating page files."));

        FileHandler.getInstance().writePageFiles(processes, pagesPerProcess,
                maxPageExecutionTime, ramSize);

        Platform.runLater(() ->
                controller.setStatus("STATUS", "Finished creating page files."));
    }

    /**
     * Starts the process simulation. It first reads the data and then
     * passes it to a new thread where the simulation is run.
     * @param quantumTime - The quantum time given to each process.
     */
    public static void startProcessSimulation(int quantumTime)
    {
        ObservableList<Process> lvProcesses = FXCollections.observableArrayList();
        ObservableList<Process> saProcesses = FXCollections.observableArrayList();

        readProcessData(lvProcesses, saProcesses);
        createChart();

        // Starts the Process Simulator in a new thread.
        if(!lvProcesses.isEmpty() && !saProcesses.isEmpty())
            new ProcessSimulator(lvProcesses, saProcesses, quantumTime).start();
    }

    public static void startPageSimulation(int quantumTime)
    {
        ObservableList<Process> lvProcesses = FXCollections.observableArrayList();
        ObservableList<Process> saProcesses = FXCollections.observableArrayList();

        readPageData(lvProcesses, saProcesses);

        // Starts the Process Simulator in a new thread.
        if(!lvProcesses.isEmpty() && !saProcesses.isEmpty())
            new ProcessSimulator(lvProcesses, saProcesses, quantumTime).start();
    }

    /**
     * Reads the data from the files.
     * @param lvProcesses - The processes data for the Lock Variable simulation.
     * @param saProcesses - The processes data for the Strict Alternation simulation.
     */
    private static void readProcessData(ObservableList<Process> lvProcesses,
                                        ObservableList<Process> saProcesses)
    {
        String currentDirectory = System.getProperty("user.dir");
        String filenameFormat = "p[0-9]{2}.txt";
        String nextFileToRead = "p00.txt";
        int nextFileNumber;
        String filename;
        int processID = 0;
        ArrayList<Integer> data = new ArrayList<>();

        Platform.runLater(() ->
                controller.setStatus("STATUS", "Reading process files from "
                        + currentDirectory));

        // Finds all the files that match the name format.
        File directory = new File(currentDirectory);
        File[] foundFiles = directory.listFiles(
                (dir, name) -> name.matches(filenameFormat));

        if(foundFiles.length != 0)
        {
            for (File file : foundFiles)
            {
                filename = file.getName();

                // Executes if the next file to read is missing.
                if(!filename.equals(nextFileToRead))
                {
                    final String missingFile = nextFileToRead;

                    if(missingFile.equals("p00.txt"))
                    {
                        controller.onStopSimulation();
                        Platform.runLater(() ->
                                controller.setStatus("ERROR", "Process file "
                                        + missingFile + " is missing."));
                    }
                    else
                    {
                        Platform.runLater(() ->
                                controller.setStatus("ERROR", "Finished reading process files."
                                        + " File " + missingFile + " is missing so only read up "
                                        + "to the previous file."));
                    }
                    return;
                }

                FileHandler.getInstance().readProcessFiles(data ,currentDirectory + "\\" + filename);

                // Create two separate arrays of process data so each
                // exclusion method can work and modify its own set of data.
                Integer[] lvJobs = new Integer[data.size()];
                Integer[] saJobs = new Integer[data.size()];

                for(int i = 0; i < data.size(); i++)
                    lvJobs[i] = saJobs[i] = data.get(i);

                // Create the processes.
                lvProcesses.add(new Process(String.valueOf(processID), "Ready",
                        String.valueOf(0), String.valueOf(0), lvJobs));

                saProcesses.add(new Process(String.valueOf(processID), "Ready",
                        String.valueOf(0), String.valueOf(0), saJobs));

                // Setup the next file to read.
                nextFileNumber = Integer.valueOf(filename.substring(1, 3)) + 1;
                nextFileToRead = "p" + String.format("%02d", nextFileNumber) + ".txt";
                data.clear();
                processID++;
            }
        }
        else
        {
            controller.onStopSimulation();
            Platform.runLater(() -> {
                controller.setStatus("ERROR", "There were no files to read.");
            });
        }
    }

    private static void readPageData(ObservableList<Process> lvProcesses,
                                        ObservableList<Process> saProcesses)
    {
        String currentDirectory = System.getProperty("user.dir");
        String filenameFormat = "p[0-3].txt";
        String nextFileToRead = "p0.txt";
        int nextFileNumber;
        String filename;
        int processID = 0;
        Map<Integer, Integer> data = new HashMap<>();

        Platform.runLater(() ->
                controller.setStatus("STATUS", "Reading process files from "
                        + currentDirectory));

        // Finds all the files that match the name format.
        File directory = new File(currentDirectory);
        File[] foundFiles = directory.listFiles(
                (dir, name) -> name.matches(filenameFormat));

        if(foundFiles.length != 0)
        {
            for (File file : foundFiles)
            {
                filename = file.getName();

                // Executes if the next file to read is missing.
                if(!filename.equals(nextFileToRead))
                {
                    final String missingFile = nextFileToRead;

                    controller.onStopSimulation();
                    Platform.runLater(() ->
                            controller.setStatus("ERROR", "Process file "
                                    + missingFile + " is missing."));
                    return;
                }

                FileHandler.getInstance().readPageFiles(data ,currentDirectory + "\\" + filename);


                System.out.println(filename);
                Iterator it = data.entrySet().iterator();
                while (it.hasNext())
                {
                    Map.Entry pair = (Map.Entry)it.next();
                    System.out.println(pair.getKey() + " = " + pair.getValue());
                    it.remove(); // avoids a ConcurrentModificationException
                }



//                // Create two separate arrays of process data so each
//                // exclusion method can work and modify its own set of data.
//                Integer[] lvJobs = new Integer[data.size()];
//
//                // Create the processes.
//                lvProcesses.add(new Process(String.valueOf(processID), "Ready",
//                        String.valueOf(0), String.valueOf(0), lvJobs));

                // Setup the next file to read.
                nextFileNumber = Integer.valueOf(filename.substring(1, 2)) + 1;
                nextFileToRead = "p" + String.format("%01d", nextFileNumber) + ".txt";
                data.clear();
                processID++;
            }
        }
        else
        {
            controller.onStopSimulation();
            Platform.runLater(() -> {
                controller.setStatus("ERROR", "There were no files to read.");
            });
        }
    }

    public static void createChart()
    {
        final NumberAxis xAxis = new NumberAxis();
        final CategoryAxis yAxis = new CategoryAxis();
        chart = new GanttChart<>(xAxis, yAxis);
        xAxis.setTickLabelFill(Color.WHITE);
        chart.setId("chart");
        String filename = System.getProperty("user.dir") + "\\ProcessColors.css";
        String uri = Paths.get(filename).toUri().toString();
        simulatorScene.getStylesheets().add(uri);
        xAxis.setMinorTickCount(0);
        yAxis.setTickMarkVisible(false);
        chart.setBlockHeight(30);
        chart.getYAxis().setOpacity(0);
        controller.setChart(chart);
    }

    public static void addProcessToChart(int processID)
    {
        String yAxis = "";
        XYChart.Series series = new XYChart.Series();

        String style = "process" + String.format("%02d", processID);
        series.getData().add(new XYChart.Data(
                count++, yAxis, new GanttChart.ExtraData( 1, style)));
        chart.getData().addAll(series);
    }
}