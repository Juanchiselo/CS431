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
    public static Stage stage;

    /**
     * The overridden start() method belonging to the Application class.
     * @param primaryStage
     * @throws Exception
     */
    @Override
    public void start(Stage primaryStage) throws Exception
    {
        // Loads the FXML for the Processes Scene and creates the Scene.
        FXMLLoader loader = new FXMLLoader(
                getClass().getResource("Layouts/ProcessesLayout.fxml"));
        processesScene = new Scene(loader.load(), 1280, 720);

        // Saves a reference of the Controller object.
        controller = loader.getController();

        // Saves a reference of the Stage object
        // so the Controller class can access it and sets the Stage.
        stage = primaryStage;
        stage.getIcons().add(new Image(Main.class.getResourceAsStream("Drawable/Icon.png")));
        stage.setTitle("Homework #2 - Jose Juan Sandoval");
        stage.setResizable(true);
        stage.setScene(processesScene);
        stage.show();
    }

    public static void main(String[] args)
    {
        launch(args);
        ProcessSimulator.endThread = true;
    }

    public static void createProcessData(int numberOfProcesses)
    {
        Platform.runLater(() ->
                controller.setStatus("Creating process files."));

        Random random = new Random();
        String filename;

        try
        {
            for(int processID = 0; processID < numberOfProcesses; processID++)
            {
                filename = "p" + String.format("%02d", processID) + ".txt";
                PrintWriter writer = new PrintWriter(filename, "UTF-8");

                int size = random.nextInt(100) + 1;
                for (int i = 0; i < size; i++)
                    writer.println(random.nextInt(2000));
                writer.close();
            }
        } catch (IOException e)
        {
            e.printStackTrace();
        }

        Platform.runLater(() ->
                controller.setStatus("Finished creating process files."));
    }

    public static void startSimulation(int cpuTimeSlice)
    {
        //Read files.
        String currentDirectory = System.getProperty("user.dir");

        Platform.runLater(() ->
                controller.setStatus("Reading process files from "
                + currentDirectory));

        File dir = new File(currentDirectory);
        File[] foundFiles = dir.listFiles(
                (dir1, name) -> name.matches("p[0-9]{2}.txt"));

        String nextFileToRead = "p00.txt";
        int nextFileNumber;
        String currentFile;
        ArrayList<Integer> data = new ArrayList<>();
        ArrayList<Integer[]> processes = new ArrayList<>(foundFiles.length);
        ObservableList<Process> tableData = FXCollections.observableArrayList();

        for (File file : foundFiles)
        {
            currentFile = file.getName();

            if(!currentFile.equals(nextFileToRead))
            {
                // All variables inside runLater have to be final.
                final String missingFile = nextFileToRead;

                if(missingFile.equals("p00.txt"))
                {
                    Platform.runLater(() ->
                            controller.setStatus("ERROR: There were no files to read. "
                                + "Process file " + missingFile + " is missing."));
                }
                else
                {
                    Platform.runLater(() ->
                            controller.setStatus("Finished reading process files."
                                    + " File " + missingFile + " is missing so only read up "
                                    + "to the previous file."));
                }
                return;
            }

            readFile(data ,currentDirectory + "\\" + currentFile);

            Integer[] tempArray = new Integer[data.size()];

            for(int i = 0; i < data.size(); i++)
                tempArray[i] = data.get(i);

            processes.add(tempArray);


            nextFileNumber = Integer.valueOf(currentFile.substring(1, 3)) + 1;
            nextFileToRead = "p" + String.format("%02d", nextFileNumber) + ".txt";
            data.clear();
        }

        for(int i = 0; i < processes.size(); i++)
        {
            tableData.add(new Process(String.valueOf(i), "Ready",
                    String.valueOf(0), String.valueOf(0), processes.get(i)));
        }

        controller.setTableView(tableData);

        Platform.runLater(() ->
                controller.setStatus("Finished reading process files."));

        if(!processes.isEmpty())
            new ProcessSimulator(tableData, cpuTimeSlice).start();
    }

    private static void readFile(ArrayList<Integer> data, String fileName)
    {
        String line;

        try
        {
            FileReader fileReader = new FileReader(fileName);
            BufferedReader bufferedReader = new BufferedReader(fileReader);

            while((line = bufferedReader.readLine()) != null)
            {
                if(line.matches("[0-9]+"))
                    data.add(Integer.valueOf(line));
            }

            bufferedReader.close();
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