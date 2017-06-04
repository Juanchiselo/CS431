package Homework4;

import javafx.application.Platform;
import javafx.collections.ObservableList;

public class PageSimulator extends Thread
{
    public PageSimulator(int quantumTime)
    {
        super("Process Simulator Thread");
    }

    @Override
    public void run()
    {
        // Set up the tables with the data.
        Platform.runLater(() ->
        {
            Main.controller.setStatus("STATUS", "Simulation is running.");
        });
    }
}
