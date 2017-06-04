package Homework4;

import javafx.scene.paint.Color;

import java.io.*;
import java.util.ArrayList;
import java.util.Map;
import java.util.Random;

public class FileHandler
{
    private static FileHandler instance = null;
    private String line;
    private String fileName;
    private FileReader fileReader;
    private BufferedReader bufferedReader;

    public static FileHandler getInstance()
    {
        if(instance == null)
            instance = new FileHandler();
        return instance;
    }

    private FileHandler()
    {
    }

    private void openFile()
    {
        try {
            fileReader = new FileReader(fileName);
            bufferedReader = new BufferedReader(fileReader);
        } catch(FileNotFoundException e) {
            System.err.println("ERROR: Unable to open file '" + fileName + "'.");
        }
    }

    private void closeFile()
    {
        try {
            bufferedReader.close();
            fileReader.close();
        } catch(IOException e) {
            System.err.println("ERROR: Unable to close file '" + fileName + "'.");
        }
    }

    public void writeProcessFiles(int processes,
                                  int jobsPerProcess,
                                  int maxJobExecutionTime)
    {
        Random random = new Random();
        String filename = "";
        String twoDigitProcessID;
        writeColorStyles(processes);

        try
        {
            for(int processID = 0; processID < processes; processID++)
            {
                twoDigitProcessID = String.format("%02d", processID);
                filename = "p" + twoDigitProcessID + ".txt";
                PrintWriter writer = new PrintWriter(filename, "UTF-8");
                writer.println("# Process #" + twoDigitProcessID);

                int size = random.nextInt(jobsPerProcess) + 1;
                for (int i = 0; i < size; i++)
                    writer.println(random.nextInt(maxJobExecutionTime) + 1);
                writer.println("# End of Process #" + twoDigitProcessID);
                writer.close();
            }
        } catch (IOException e) {
            System.err.println("ERROR: Could not write to file '" + filename + "'.");
        }
    }

    /**
     * Reads the Process files from Homeworks #2 and #3.
     * @param data - The ArrayList where the data will be saved to.
     * @param fileName - The name of the file to read the data from.
     */
    public void readProcessFiles(ArrayList<Integer> data, String fileName)
    {
        this.fileName = fileName;
        openFile();
        try {
            while((line = bufferedReader.readLine()) != null)
                if(line.matches("[0-9]+"))
                    data.add(Integer.valueOf(line));
        } catch (IOException e) {
            System.err.println("ERROR: Unable to read from file '" + fileName + "'.");
        } finally {
            closeFile();
        }
    }

    public void writePageFiles(int processes,
                               int jobsPerProcess,
                               int maxJobExecutionTime,
                               int ramSize)
    {
        Random random = new Random();
        String filename = "";
        int pageNumber;

        try
        {
            for(int processID = 0; processID < processes; processID++)
            {
                filename = "p" + String.format("%01d", processID) + ".txt";
                PrintWriter writer = new PrintWriter(filename, "UTF-8");
                writer.println("# Process #" + processID);

                int size = random.nextInt(jobsPerProcess) + 1;
                for (int i = 0; i < size; i++)
                {
                    pageNumber = random.nextInt(255);
                    line = String.valueOf(pageNumber) + " " +
                            String.valueOf((random.nextInt(maxJobExecutionTime) + 1));
                    writer.println(line);
                }
                writer.println("# End of Process #" + processID);
                writer.close();
            }
        } catch (IOException e) {
            System.err.println("ERROR: Could not write to file '" + filename + "'.");
        }
    }

    /**
     * Reads the Page files from Homework #4.
     * @param data - The Map where the data will be saved to.
     * @param fileName - The name of the file to read the data from.
     */
    public void readPageFiles(Map<Integer, Integer> data, String fileName)
    {
        this.fileName = fileName;
        openFile();
        try {
            while((line = bufferedReader.readLine()) != null)
                if(line.matches("[0-9]+[ ][0-9]+"))
                {
                    String[] values = line.split("[ ]");
                    data.put(Integer.valueOf(values[0]), Integer.valueOf(values[1]));
                }
        } catch (IOException e) {
            System.err.println("ERROR: Unable to read from file '" + fileName + "'.");
        } finally {
            closeFile();
        }
    }

    public void writeColorStyles(int processes)
    {
        String filename = "ProcessColors.css";
        String twoDigitProcessID;

        try
        {
            PrintWriter writer = new PrintWriter(filename, "UTF-8");
            for(int processID = 0; processID < processes; processID++)
            {
                twoDigitProcessID = String.format("%02d", processID);
                Color color = Color.color(Math.random(), Math.random(), Math.random());

                String hexColor = String.format( "#%02X%02X%02X",
                        (int)( color.getRed() * 255 ),
                        (int)( color.getGreen() * 255 ),
                        (int)( color.getBlue() * 255 ) );
                String style = "-fx-background-color: " + hexColor;

                writer.println(".process" + twoDigitProcessID + " {\n\t"
                    + "-fx-background-color: " + hexColor + ";"
                    + "\n}\n");
            }
            writer.close();
        } catch (IOException e) {
            System.err.println("ERROR: Could not write to file '" + filename + "'.");
        }
    }
}
