module com.example._2048_expectimax {
    requires javafx.controls;
    requires javafx.fxml;


    opens com.example._2048_expectimax to javafx.fxml;
    exports com.example._2048_expectimax;
}