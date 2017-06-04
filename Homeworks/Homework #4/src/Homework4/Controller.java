package Homework4;

import javafx.collections.ObservableList;
import javafx.css.PseudoClass;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.util.Callback;

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
    @FXML private RadioMenuItem FCFSRadioMenuItem;
    @FXML private RadioMenuItem RRRadioMenuItem;
    @FXML private RadioMenuItem SJFRadioMenuItem;
    @FXML private CheckMenuItem disableUpdatesCheckMenu;
    @FXML private Label processSimulatorLabel;
    @FXML private Label pageSimulatorLabel;
    private final PseudoClass selectedClass = PseudoClass.getPseudoClass("selected");
    @FXML private TabPane simulatorTabPane;
    @FXML private Tab processSimulatorTab;
    @FXML private Tab pageSimulatorTab;
    @FXML private GridPane lvViewGridPane;
    @FXML private GridPane saViewGridPane;
    @FXML private GridPane ganttChart;

    public void initialize()
    {
        ToggleGroup toggleGroup = new ToggleGroup();
        FCFSRadioMenuItem.setToggleGroup(toggleGroup);
        RRRadioMenuItem.setToggleGroup(toggleGroup);
        SJFRadioMenuItem.setToggleGroup(toggleGroup);
        processSimulatorLabel.pseudoClassStateChanged(selectedClass, true);

        EventHandler<ActionEvent> checkMenuEvent = checkMenuHandler();
        lvCheckMenu.setOnAction(checkMenuEvent);
        saCheckMenu.setOnAction(checkMenuEvent);
        selectProcessCheckMenu.setOnAction(checkMenuEvent);
        disableUpdatesCheckMenu.setOnAction(checkMenuEvent);
    }

    public void setChart(GanttChart chart)
    {
        ganttChart.getChildren().clear();
        ganttChart.add(chart, 0, 0);
    }

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

    private EventHandler<ActionEvent> checkMenuHandler()
    {
        return event ->
        {
            CheckMenuItem checkMenuItem = (CheckMenuItem) event.getSource();
            Boolean enable = checkMenuItem.isSelected();
            String id = checkMenuItem.getId();

            switch (id)
            {
                case "lvCheckMenu":
                    lvViewGridPane.setVisible(enable);
                    ProcessSimulator.lvSimulation = enable;
                    break;
                case "saCheckMenu":
                    saViewGridPane.setVisible(enable);
                    ProcessSimulator.saSimulation = enable;
                    break;
                case "selectProcessCheckMenu":
                    ProcessSimulator.selectRow = enable;
                    break;
                case "disableUpdatesCheckMenu":
                    selectProcessCheckMenu.setSelected(!enable);
                    selectProcessCheckMenu.setDisable(enable);
                    ProcessSimulator.selectRow = !enable;
                    ProcessSimulator.disableGraphicUpdates = enable;
                    break;
            }

            // Extra Settings for Mutual Exclusion Method Check Menu Items.
            if(id.equals("lvCheckMenu") || id.equals("saCheckMenu"))
            {
                if(lvViewGridPane.isVisible() && saViewGridPane.isVisible())
                {
                    GridPane.setColumnIndex(saViewGridPane, 1);
                    GridPane.setColumnSpan(lvViewGridPane, 1);
                    GridPane.setColumnSpan(saViewGridPane, 1);
                }
                else
                {
                    if(lvViewGridPane.isVisible())
                        GridPane.setColumnSpan(lvViewGridPane, GridPane.REMAINING);
                    if(saViewGridPane.isVisible())
                    {
                        GridPane.setColumnIndex(saViewGridPane, 0);
                        GridPane.setColumnSpan(saViewGridPane, GridPane.REMAINING);
                    }
                }
            }
        };
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
            {
                if(simulatorTabPane.getSelectionModel().getSelectedItem() == processSimulatorTab)
                    Main.startProcessSimulation(quantumTime);
                else
                    Main.startPageSimulation(quantumTime);
            }
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
        disableButtons(false);
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
    public void onCreateSimulationData()
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

            if(simulatorTabPane.getSelectionModel().getSelectedItem() == processSimulatorTab)
            {
                if(numberOfProcesses >= 0 && numberOfProcesses <= 100)
                    Main.createProcessData(numberOfProcesses,
                            jobsPerProcess, maxJobExecutionTime);
                else
                    displayError("Number of Processes",
                            "You can only simulate a max of 100 processes for the Process simulator.");
            }
            else
            {
                if(numberOfProcesses >= 0 && numberOfProcesses <= 4)
                    Main.createPageData(numberOfProcesses,
                            jobsPerProcess, maxJobExecutionTime, 64);
                else
                    displayError("Number of Processes",
                            "You can only simulate a max of 4 processes for the Page simulator.");
            }

        }
        else
            displayError("Invalid Input",
                    "Number of processes/jobs/execution time "
                    + "should be a positive integer.");
    }

    public void onSimulateProcesses()
    {
        simulatorTabPane.getSelectionModel().select(processSimulatorTab);
        jobsTextField.setPromptText("# Jobs");
        processSimulatorLabel.pseudoClassStateChanged(selectedClass, true);
        pageSimulatorLabel.pseudoClassStateChanged(selectedClass, false);
    }

    public void onSimulatePages()
    {
        simulatorTabPane.getSelectionModel().select(pageSimulatorTab);
        jobsTextField.setPromptText("# Pages");
        pageSimulatorLabel.pseudoClassStateChanged(selectedClass, true);
        processSimulatorLabel.pseudoClassStateChanged(selectedClass, false);
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