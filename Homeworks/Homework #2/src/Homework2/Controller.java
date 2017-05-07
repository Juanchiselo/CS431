package Homework2;

import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

public class Controller
{
    @FXML private Label statusLabel;
    @FXML private Button startSimulationButton;
    @FXML private Button stopSimulationButton;
    @FXML private Button newProcessDataButton;
    @FXML private TextField processesTextField;
    @FXML private TextField jobsTextField;
    @FXML private TextField jobTimeTextField;
    @FXML private TextField quantumTimeTextField;
    @FXML private Slider speedSlider;
    @FXML private TableView<Process> lockVariableTableView;
    @FXML private TableColumn<Process, String> LVprocessIDColumn;
    @FXML private TableColumn<Process, String> LVcrColumn;
    @FXML private TableColumn<Process, String> LVstateColumn;
    @FXML private TableColumn<Process, String> LVcompletionPercentColumn;
    @FXML private TableColumn<Process, String> LVcompletionTimeColumn;
    @FXML private TableColumn<Process, String> LVexecutionTimeColumn;
    @FXML private TableView<Process> strictAlternationTableView;
    @FXML private TableColumn<Process, String> SAprocessIDColumn;
    @FXML private TableColumn<Process, String> SAcrColumn;
    @FXML private TableColumn<Process, String> SAstateColumn;
    @FXML private TableColumn<Process, String> SAcompletionPercentColumn;
    @FXML private TableColumn<Process, String> SAcompletionTimeColumn;
    @FXML private TableColumn<Process, String> SAexecutionTimeColumn;
    @FXML private CheckMenuItem lvCheckMenu;
    @FXML private CheckMenuItem saCheckMenu;
    @FXML private CheckMenuItem selectProcessCheckMenu;
    private TableView<Process> table;

    /**
     * Sets the data model for the TableViews.
     * @param tableName - The name of the table to setup.
     * @param data - The data to insert in the table.
     */
    public void setTableView(String tableName, ObservableList<Process> data)
    {
        if(tableName.equals("Lock Variable") && lvCheckMenu.isSelected())
        {
            LVprocessIDColumn.setCellValueFactory(
                    new PropertyValueFactory<>("processID"));

            LVcrColumn.setCellValueFactory(
                    new PropertyValueFactory<>("criticalRegion"));

            LVstateColumn.setCellValueFactory(
                    new PropertyValueFactory<>("state"));

            LVcompletionPercentColumn.setCellValueFactory(
                    new PropertyValueFactory<>("completionPercent"));

            LVcompletionTimeColumn.setCellValueFactory(
                    new PropertyValueFactory<>("completionTime"));

            LVexecutionTimeColumn.setCellValueFactory(
                    new PropertyValueFactory<>("executionTime"));

            lockVariableTableView.setItems(data);
        }
        else if(tableName.equals("Strict Alternation") && saCheckMenu.isSelected())
        {
            SAprocessIDColumn.setCellValueFactory(
                    new PropertyValueFactory<>("processID"));

            SAcrColumn.setCellValueFactory(
                    new PropertyValueFactory<>("criticalRegion"));

            SAstateColumn.setCellValueFactory(
                    new PropertyValueFactory<>("state"));

            SAcompletionPercentColumn.setCellValueFactory(
                    new PropertyValueFactory<>("completionPercent"));

            SAcompletionTimeColumn.setCellValueFactory(
                    new PropertyValueFactory<>("completionTime"));

            SAexecutionTimeColumn.setCellValueFactory(
                    new PropertyValueFactory<>("executionTime"));

            strictAlternationTableView.setItems(data);
        }
    }

    /**
     * Selects the row specified.
     * @param tableName - The name of the table where
     *                  the row will be selected.
     * @param rowIndex - The index of the row to be selected.
     */
    public void selectRow(String tableName, int rowIndex)
    {
        if(tableName.equals("Lock Variable"))
            table = lockVariableTableView;
        else
            table = strictAlternationTableView;

        table.getSelectionModel().select(rowIndex);
    }

    /**
     * Updates the table with new data.
     * @param tableName - The name of the table to update.
     * @param data - The new data to update the table with.
     */
    public void updateTable(String tableName, ObservableList<Process> data)
    {
        if(tableName.equals("Lock Variable"))
            table = lockVariableTableView;
        else
            table = strictAlternationTableView;

        table.setItems(data);
        table.refresh();
    }

    /**
     * Event handler for the Lock Variable CheckMenu component.
     */
    public void onLockVariable()
    {
        if(lvCheckMenu.isSelected())
        {
            lockVariableTableView.setVisible(true);
            ProcessSimulator.lvSimulation = true;
        }
        else
        {
            lockVariableTableView.setVisible(false);
            ProcessSimulator.lvSimulation = false;
        }
    }

    /**
     * Event handler for the Strict Alternation CheckMenu component.
     */
    public void onStrictAlternation()
    {
        if(saCheckMenu.isSelected())
        {
            strictAlternationTableView.setVisible(true);
            ProcessSimulator.saSimulation = true;
        }
        else
        {
            strictAlternationTableView.setVisible(false);
            ProcessSimulator.saSimulation = false;
        }
    }

    /**
     * Event handler for the Select Running Process CheckMenu component.
     */
    public void onSelectRunningProcess()
    {
        if(selectProcessCheckMenu.isSelected())
            ProcessSimulator.selectRow = true;
        else
            ProcessSimulator.selectRow = false;
    }

    /**
     * Event handler for the "Start Simulation" button.
     */
    public void onStartSimulation()
    {
        ProcessSimulator.endThread = false;
        disableButtons(true);
        String quantumTimeString = quantumTimeTextField.getText();

        if (isInputValid(quantumTimeString, "INTEGERS"))
        {
            int quantumTime = Integer.parseInt(quantumTimeString);

            if(quantumTime > 0)
                Main.startSimulation(quantumTime);
            else
                displayError("Invalid Input",
                    "CPU time slice should be a positive integer greater than 0.");
        }
        else
            displayError("Invalid Input",
                    "CPU time slice should be a positive integer greater than 0.");
    }

    /**
     * The event handler for the "Stop Simulation" button.
     */
    public void onStopSimulation()
    {
        setStatus("STATUS", "Stopping simulation.");
        ProcessSimulator.endThread = true;
    }

    /**
     * Disables or enables certain parts of the GUI.
     * @param disable - Whether to disable or enable
     *                the GUI components.
     */
    public void disableButtons(boolean disable)
    {
        newProcessDataButton.setDisable(disable);
        startSimulationButton.setDisable(disable);
        stopSimulationButton.setDisable(!disable);
        processesTextField.setDisable(disable);
        jobsTextField.setDisable(disable);
        jobTimeTextField.setDisable(disable);
        quantumTimeTextField.setDisable(disable);
    }

    /**
     * The event handler for the "Speed" slider.
     * It updates the speed of the simulation in realtime.
     */
    public void onSliderDragged()
    {
        ProcessSimulator.simulationSpeed = (int)speedSlider.getValue();
    }

    /**
     * The event handler for "New Process Data" button.
     * It creates new process data files.
     */
    public void onCreateProcessData()
    {
        String processesString = processesTextField.getText();
        String jobsString = jobsTextField.getText();
        String jobTimeString = jobTimeTextField.getText();

        if (isInputValid(processesString, "INTEGERS")
                && isInputValid(jobsString, "INTEGERS")
                && isInputValid(jobTimeString, "INTEGERS"))
        {
            int numberOfProcesses = Integer.parseInt(processesString);
            int jobsPerProcess = Integer.parseInt(jobsString);
            int maxJobExecutionTime = Integer.parseInt(jobTimeString);

            if(numberOfProcesses >= 0 && numberOfProcesses <= 100)
                Main.createProcessData(numberOfProcesses,
                        jobsPerProcess, maxJobExecutionTime);
            else
                displayError("Number of Processes",
                        "You can only simulate a max of 100 processes.");
        }
        else
            displayError("Invalid Input",
                    "Number of processes/jobs/execution time "
                    + "should be a positive integer.");
    }

    /**
     * Checks if a user given input is valid.
     * @param input - The input given by the user.
     * @param type - The type the input should be.
     * @return - Returns whether is valid or not.
     */
    private boolean isInputValid(String input, String type)
    {
        String regex = "";

        switch (type.toUpperCase())
        {
            case "INTEGERS":
                regex = "[0-9]+";
                break;
        }

        return input.matches(regex);
    }

    /**
     * Displays error messages in the form of alerts.
     * @param title - The title of the alert.
     * @param errorMessage - The error message.
     */
    public void displayError(String title, String errorMessage)
    {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("ERROR: " + title);
        alert.setHeaderText("ERROR: " + title);
        alert.setContentText(errorMessage);
        alert.showAndWait();
    }

    /**
     * Displays the status messages located in the status bar.
     * @param type - The type of the status message.
     * @param message - The message to display.
     */
    public void setStatus(String type, String message)
    {
        if(type.equals("ERROR"))
            statusLabel.setStyle("-fx-text-fill: red");
        else
            statusLabel.setStyle("-fx-text-fill: white");

        statusLabel.setText(type + ": " + message);
    }
}