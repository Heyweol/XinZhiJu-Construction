package github.heyweol.demo.ui;

import com.almasb.fxgl.dsl.FXGL;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.util.Duration;

import java.util.List;

public class TutorialWindow extends VBox {
  private List<Image> tutorialImages;
  private int currentImageIndex = 0;
  private ImageView imageView;
  
  public TutorialWindow() {
    setStyle("-fx-background-color: rgba(0, 0, 0, 0.8); -fx-padding: 20;");
    setVisible(false);
    
    // Load tutorial images
    tutorialImages = List.of(
            new Image(getClass().getResourceAsStream("/assets/tutorial/tutorial1.png")),
            new Image(getClass().getResourceAsStream("/assets/tutorial/tutorial2.png")),
            new Image(getClass().getResourceAsStream("/assets/tutorial/tutorial3.png")),
            new Image(getClass().getResourceAsStream("/assets/tutorial/tutorial4.png")),
            new Image(getClass().getResourceAsStream("/assets/tutorial/tutorial5.png"))
    );
    
    imageView = new ImageView(tutorialImages.get(0));
//    imageView.setFitHeight(380);
    imageView.setPreserveRatio(true);
    imageView.setFitWidth(800);
    
    Button prevButton = new Button("Previous");
    Button nextButton = new Button("Next");
    Button closeButton = new Button("Close");
    
    prevButton.setOnAction(e -> showPreviousImage());
    nextButton.setOnAction(e -> showNextImage());
    closeButton.setOnAction(e -> setVisible(false));
    
    HBox buttonBox = new HBox(10, prevButton, nextButton, closeButton);
    buttonBox.setStyle("-fx-alignment: center;");
    
    getChildren().addAll(imageView, buttonBox);
  }
  
  private void showPreviousImage() {
    if (currentImageIndex > 0) {
      currentImageIndex--;
      imageView.setImage(tutorialImages.get(currentImageIndex));
    }
  }
  
  private void showNextImage() {
    if (currentImageIndex < tutorialImages.size() - 1) {
      currentImageIndex++;
      imageView.setImage(tutorialImages.get(currentImageIndex));
    }
  }
  
  public void show() {
    currentImageIndex = 0;
    imageView.setImage(tutorialImages.get(currentImageIndex));
    setVisible(true);
    
    // Center the window after it's visible
    FXGL.runOnce(this::centerWindow, Duration.millis(0.1));
  }
  
  private void centerWindow() {
//    double screenWidth = FXGL.getAppWidth();
//    double screenHeight = FXGL.getAppHeight();
//    double windowWidth = getWidth();
//    double windowHeight = getHeight();
//
//    setLayoutX((screenWidth - windowWidth) / 2 -200);
//    setLayoutY((screenHeight - windowHeight) / 2 - 200);
    setLayoutX(0);
    setLayoutY(50);
  }
}
