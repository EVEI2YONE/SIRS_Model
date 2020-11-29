import controllers.Controller;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Main extends Application {
    static Controller controller;

    @Override
    public void start(Stage primaryStage) throws Exception{
        FXMLLoader loader = new FXMLLoader(getClass().getResource("views/sample.fxml"));
        Parent root = loader.load();
        controller = loader.getController();
        primaryStage.setScene(new Scene(root));
        primaryStage.sizeToScene();
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
        controller.stop();
    }

    public static double fade(double ratio) { return 6*Math.pow(ratio, 5)-15*Math.pow(ratio,4)+10*Math.pow(ratio,3); };
}
