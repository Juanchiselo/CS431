package Homework2;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

import java.util.ArrayList;

public class Controller
{
    @FXML private Label statusLabel;
    @FXML private TextField processesTextField;
    @FXML private TextField timeSliceTextField;
    @FXML private TableView processInfoTableView;
    @FXML private TableColumn processIDColumn;
    @FXML private TableColumn crColumn;
    @FXML private TableColumn stateColumn;
    @FXML private TableColumn completionPercentColumn;
    @FXML private TableColumn completionTimeColumn;
    @FXML private TableColumn executionTimeColumn;
    @FXML private Slider speedSlider;


//    /**
//     * Switches between the Login and Chat windows.
//     */
//    public void switchWindows()
//    {
//        if(ChatClient.stage.getScene().equals(ChatClient.chatLoginScene))
//        {
//            ChatClient.stage.setScene(ChatClient.chatWindowScene);
//            ChatClient.stage.setResizable(true);
//        }
//        else
//        {
//            ChatClient.stage.setScene(ChatClient.chatLoginScene);
//            ChatClient.stage.setResizable(false);
//        }
//        ChatClient.stage.centerOnScreen();
//    }

    public void setTableView(ObservableList<Process> data)
    {
        processIDColumn.setCellValueFactory(
                new PropertyValueFactory<>("processID"));

        crColumn.setCellValueFactory(
                new PropertyValueFactory<>("criticalRegion"));

        stateColumn.setCellValueFactory(
                new PropertyValueFactory<>("state"));

        completionPercentColumn.setCellValueFactory(
                new PropertyValueFactory<>("completionPercent"));

        completionTimeColumn.setCellValueFactory(
                new PropertyValueFactory<>("completionTime"));

        executionTimeColumn.setCellValueFactory(
                new PropertyValueFactory<>("executionTime"));

        processInfoTableView.setItems(data);
    }

    public void selectRow(int rowIndex)
    {
        processInfoTableView.requestFocus();
        processInfoTableView.getSelectionModel().select(rowIndex);
        processInfoTableView.getFocusModel().focus(rowIndex);
    }

    public void blockRow(int rowIndex)
    {
//        Object blockedRow = processInfoTableView.getItems().get(rowIndex);
//        blockedRow.setStyle("-fx-background-color");
    }

    public void updateTable(ObservableList<Process> processData)
    {
        processInfoTableView.setItems(processData);
        processInfoTableView.refresh();
    }

    public void onStartSimulation()
    {
        String cpuTimeSliceString = timeSliceTextField.getText();

        if (isInputValid(cpuTimeSliceString, "INTEGERS"))
        {
            int cpuTimeSlice = Integer.parseInt(cpuTimeSliceString);

            if(cpuTimeSlice > 0)
                Main.startSimulation(cpuTimeSlice);
            else
                displayError("Invalid Input",
                    "CPU time slice should be a positive integer greater than 0.");
        }
        else
            displayError("Invalid Input",
                    "CPU time slice should be a positive integer greater than 0.");
    }

    public void onSliderDragged()
    {
        System.out.println(speedSlider.getValue());
    }

    public void onCreateProcessData()
    {
        String processesString = processesTextField.getText();

        if (isInputValid(processesString, "INTEGERS"))
        {
            int numberOfProcesses = Integer.parseInt(processesString);

            if(numberOfProcesses >= 0 && numberOfProcesses <= 100)
                Main.createProcessData(numberOfProcesses);
            else
                displayError("Number of Processes",
                        "You can only simulate a max of 100 processes.");
        }
        else
            displayError("Invalid Input",
                    "Number of processes should be a positive integer.");
    }

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
     * @param status - The status message.
     */
    public void setStatus(String status)
    {
        statusLabel.setText("Status: " + status);
    }
}