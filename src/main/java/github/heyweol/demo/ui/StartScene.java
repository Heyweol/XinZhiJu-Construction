// src/main/java/github/heyweol/demo/ui/StartScene.java
package github.heyweol.demo.ui;

import com.almasb.fxgl.scene.SubScene;

import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.Stop;
import javafx.scene.shape.Rectangle;

public class StartScene extends SubScene {
    private Runnable onStartAction;

    public StartScene(int width, int height) {
        // Create main layout
        StackPane root = new StackPane();
        
        // Create gradient background
        LinearGradient gradient = new LinearGradient(
            0, 0,    // start point
            0, 1,    // end point
            true,    // proportional
            CycleMethod.NO_CYCLE,
            new Stop(0, javafx.scene.paint.Color.web("#b4c8df")),   // top color
            new Stop(1, javafx.scene.paint.Color.web("#f4f6fc"))    // bottom color
        );
        
        Rectangle background = new Rectangle(width, height, gradient);
        
        // Load and set background image
        Image loadingImage = new Image(getClass().getResourceAsStream("/assets/textures/loading.png"));
        ImageView imageView = new ImageView(loadingImage);
        imageView.setFitWidth(width);
        imageView.setFitHeight(height);
        imageView.setPreserveRatio(true);

        // Create start button with image
        Image buttonImage = new Image(getClass().getResourceAsStream("/assets/textures/start_button.png"));
        ImageView buttonImageView = new ImageView(buttonImage);
        buttonImageView.setFitHeight(100);  // Changed from 50 to 100 to make it 2x larger
        buttonImageView.setPreserveRatio(true);
        
        Button startButton = new Button();
        startButton.setGraphic(buttonImageView);
        startButton.setStyle("-fx-background-color: transparent;"); // Makes button background transparent
        
        // Add hover effect
        startButton.setOnMouseEntered(e -> buttonImageView.setOpacity(0.8));
        startButton.setOnMouseExited(e -> buttonImageView.setOpacity(1.0));
        
        startButton.setOnAction(e -> {
            if (onStartAction != null) {
                onStartAction.run();
            }
        });

        // Add all elements to root (background first, then other elements)
        root.getChildren().addAll(background, imageView, startButton);
        
        getContentRoot().getChildren().add(root);
    }

    public void setOnStartAction(Runnable action) {
        this.onStartAction = action;
    }
}