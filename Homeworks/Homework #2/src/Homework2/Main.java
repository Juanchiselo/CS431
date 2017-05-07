package Homework2;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import java.io.*;
import java.util.ArrayList;
import java.util.Random;

public class Main extends Application
{
    public static Controller controller;
    public static Scene processesScene;

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
        processesScene = new Scene(loader.load(), 1280, 720);

        // Saving a controller reference so other threads
        // can modify the GUI components.
        controller = loader.getController();

        // Stage setup.
        primaryStage.getIcons().add(
                new Image(Main.class
                        .getResourceAsStream("Drawable/Icon.png")));
        primaryStage.setTitle("Homework #2 - Jose Juan Sandoval");
        primaryStage.setResizable(true);
        primaryStage.setScene(processesScene);
        primaryStage.show();
    }

    public static void main(String[] args)
    {
        launch(args);
        ProcessSimulator.endThread = true;
    }

    /**
     * Creates process data for the simulation
     * in case it does not exist already.
     * @param processes - The number of process data files
     *                          to create.
     * @param jobsPerProcess - The number of jobs per process.
     * @param maxJobExecutionTime - The maximum amount of execution
     *                            time a job can have.
     */
    public static void createProcessData(int processes,
                                         int jobsPerProcess,
                                         int maxJobExecutionTime)
    {
        Platform.runLater(() ->
                controller.setStatus("STATUS", "Creating process files."));

        Random random = new Random();
        String filename;

        try
        {
            for(int processID = 0; processID < processes; processID++)
            {
                filename = "p" + String.format("%02d", processID) + ".txt";
                PrintWriter writer = new PrintWriter(filename, "UTF-8");

                int size = random.nextInt(jobsPerProcess) + 1;
                for (int i = 0; i < size; i++)
                    writer.println(random.nextInt(maxJobExecutionTime) + 1);
                writer.close();
            }
        } catch (IOException e)
        {
            e.printStackTrace();
        }

        Platform.runLater(() ->
                controller.setStatus("STATUS", "Finished creating process files."));
    }

    /**
     * Starts the process simulation. It first reads the data and then
     * passes it to a new thread where the simulation is run.
     * @param quantumTime - The quantum time given to each process.
     */
    public static void startSimulation(int quantumTime)
    {
        ObservableList<Process> lvProcesses = FXCollections.observableArrayList();
        ObservableList<Process> saProcesses = FXCollections.observableArrayList();

        readData(lvProcesses, saProcesses);

        // Starts the Process Simulator in a new thread.
        if(!lvProcesses.isEmpty() && !saProcesses.isEmpty())
            new ProcessSimulator(lvProcesses, saProcesses, quantumTime).start();
    }

    /**
     * Reads the data from the files.
     * @param lvProcesses - The processes data for the Lock Variable simulation.
     * @param saProcesses - The processes data for the Strict Alternation simulation.
     */
    private static void readData(ObservableList<Process> lvProcesses,
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

                readFile(data ,currentDirectory + "\\" + filename);

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
            Platform.runLater(() ->
                    controller.setStatus("ERROR", "There were no files to read."));
        }
    }

    /**
     * Reads the files.
     * @param data - The arraylist where the data will be saved to.
     * @param fileName - The name of the file to read the data from.
     */
    private static void readFile(ArrayList<Integer> data, String fileName)
    {
        String line;

        try
        {
            FileReader fileReader = new FileReader(fileName);
            BufferedReader bufferedReader = new BufferedReader(fileReader);

            while((line = bufferedReader.readLine()) != null)
            {
                // This line ignores comments in the file.
                if(line.matches("[0-9]+"))
                    data.add(Integer.valueOf(line));
            }

            bufferedReader.close();
            fileReader.close();
        }
        catch(FileNotFoundException ex)
        {
            System.out.println(
                    "Unable to open file '" +
                            fileName + "'");
        }
        catch(IOException ex)
        {
            System.out.println(
                    "Error reading file '"
                            + fileName + "'");
        }
    }
}