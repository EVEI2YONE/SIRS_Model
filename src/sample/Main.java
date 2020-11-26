package sample;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Main extends Application {
    static Controller controller;

    @Override
    public void start(Stage primaryStage) throws Exception{
        FXMLLoader loader = new FXMLLoader(getClass().getResource("sample.fxml"));
        Parent root = loader.load();
        controller = loader.getController();
        controller.init();
        primaryStage.setScene(new Scene(root));
        primaryStage.sizeToScene();
        primaryStage.show();
    }

    /*
        TODO FEATURES:
            CREATE LAND STRUCTURE/OUTLINE: class model LandGenerator
            CREATE EMPTY SPACE WITH 'PEOPLE': improve SIRSModel logic
            CREATE CONNECTING EDGES BETWEEN LAND TO TRAVEL (DURATION?): class model Edge
            ALLOW 'PEOPLE' TO RANDOMLY(?) MOVE ABOUT: class model MovementLogic
            ADDITIONAL RATES (BIRTH RATE, DEATH RATE):
                3 CASES REGARDING THEIR RATES <, ==, >
                OBVIOUSLY, BUT CONSIDER THEIR IMPLICATIONS ON LATER SIRSModel LOGIC
            SIMULATE
            EL FIN
     */
    public static void main(String[] args) {
        launch(args);
        controller.stop();
    }
}
