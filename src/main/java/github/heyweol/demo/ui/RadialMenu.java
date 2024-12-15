package github.heyweol.demo.ui;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;

import org.kordamp.ikonli.fontawesome5.FontAwesomeSolid;
import org.kordamp.ikonli.javafx.FontIcon;

import com.almasb.fxgl.dsl.FXGL;
import com.almasb.fxgl.entity.Entity;

import github.heyweol.demo.EntityType;
import github.heyweol.demo.IsometricGrid;
import github.heyweol.demo.components.InteractiveItemComponent;
import github.heyweol.demo.utils.MachineIdentifier;
import github.heyweol.demo.utils.SceneManager;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ChoiceDialog;
import javafx.scene.control.TextInputDialog;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import javafx.scene.shape.Circle;
import javafx.util.Duration;

public class RadialMenu extends Pane {
  private final Button centerButton;
  private final List<Button> menuButtons = new ArrayList<>();
  private boolean isExpanded = false;
  private final double radius = 50;
  private double dragStartX, dragStartY;
  private Runnable screenshotAction;
  private IsometricGrid isometricGrid;
  private final Supplier<Map<String, Integer>> materialListSupplier;
  private final Supplier<File> screenshotSupplier;
  public RadialMenu(Runnable screenshotAction,
                    IsometricGrid isometricGrid,
                    Supplier<Map<String, Integer>> materialListSupplier,
                    Supplier<File> screenshotSupplier) {
    this.screenshotAction = screenshotAction;
    this.isometricGrid = isometricGrid;
    this.materialListSupplier = materialListSupplier;
    this.screenshotSupplier = screenshotSupplier;
    centerButton = createButton("", new FontIcon(FontAwesomeSolid.BARS));
    centerButton.getStyleClass().add("radial-menu-center");
    centerButton.setOnAction(e -> {
      toggleMenu();
      e.consume(); // Prevent the event from bubbling up
    });
    
    getChildren().add(centerButton);
    
    addMenuItem("File", FontAwesomeSolid.FILE, this::openFileSubmenu);
    addMenuItem("Screenshot", FontAwesomeSolid.CAMERA, this::takeScreenshot);
    addMenuItem("Clear", FontAwesomeSolid.TRASH, this::clearScene);
    addMenuItem("Screenshot & Share", FontAwesomeSolid.SHARE, this::screenshotAndShare);
    
    setMaxSize(USE_PREF_SIZE, USE_PREF_SIZE);
    
    // Enable dragging only on the center button
    centerButton.setOnMousePressed(e -> {
      dragStartX = e.getSceneX() - getLayoutX();
      dragStartY = e.getSceneY() - getLayoutY();
      e.consume();
    });
    
    centerButton.setOnMouseDragged(e -> {
      setLayoutX(e.getSceneX() - dragStartX);
      setLayoutY(e.getSceneY() - dragStartY);
      e.consume();
    });
    
    for (Button button : menuButtons) {
      button.setDisable(true);
      button.setOpacity(0);
    }
    
  }
  
  private void takeScreenshot() {
    if (screenshotAction != null) {
      screenshotAction.run();
    }
  }
  
  private void screenshotAndShare() {
    // Call the existing screenshot method and get the File
    File screenshotFile = screenshotSupplier.get();
    
    if (screenshotFile != null && screenshotFile.exists()) {
      // Show the share dialog
      ShareDialog dialog = new ShareDialog();
      dialog.showAndWait().ifPresent(shareData -> {
        String machineId = MachineIdentifier.getUniqueIdentifier();
        Map<String, Integer> materials = materialListSupplier.get();
        
        try {
//          ApiClient.getInstance().shareScreenshot(
//                  screenshotFile,
//                  shareData.nickname,
//                  shareData.description,
//                  machineId,
//                  materials,
//                  response -> {
//                    if (response.isSuccess()) {
//                      FXGL.getNotificationService().pushNotification("Screenshot shared successfully!");
//                    } else {
//                      FXGL.getNotificationService().pushNotification("Failed to share screenshot: " + response.getErrorMessage());
//                      System.out.println("Failed to share screenshot 1: " + response.getErrorMessage());
//                    }
//                  }
//          );
        } catch (Exception e) {
          FXGL.getNotificationService().pushNotification("Failed to share screenshot 2: " + e.getMessage());
          System.out.println("Failed to share screenshot 3: " + e.getMessage());
        }
      });
    } else {
      FXGL.getNotificationService().pushNotification("Failed to take screenshot.");
      System.out.println("Failed to take screenshot.");
    }
  }
  
  private void openFileSubmenu() {
    List<String> choices = List.of("Save", "Load", "Delete");
    ChoiceDialog<String> dialog = new ChoiceDialog<>("Save", choices);
    dialog.setTitle("File Operations");
    dialog.setHeaderText("Choose an operation:");
    dialog.setContentText("Operation:");
    
    Optional<String> result = dialog.showAndWait();
    result.ifPresent(operation -> {
      switch (operation) {
        case "Save":
          saveScene();
          break;
        case "Load":
          loadScene();
          break;
        case "Delete":
          deleteScene();
          break;
      }
    });
  }

  private void clearScene() {
    // Show confirmation dialog
    Alert alert = new Alert(AlertType.CONFIRMATION);
    alert.setTitle("Clear Scene");
    alert.setHeaderText("Are you sure you want to clear the scene?");
    alert.setContentText("This action cannot be undone.");

    Optional<ButtonType> result = alert.showAndWait();
    if (result.isPresent() && result.get() == ButtonType.OK) {
        // Proceed with clearing only if user confirms
        List<Entity> entitiesToRemove = new ArrayList<>();
        FXGL.getGameWorld().getEntities().forEach(entity -> {
            if (entity.getType() == EntityType.FLOOR_ITEM || entity.getType() == EntityType.WALL_ITEM) {
                isometricGrid.removeEntity(entity);
                entitiesToRemove.add(entity);
            }
        });
        entitiesToRemove.forEach(Entity::removeFromWorld);
        
        // Clear the selection
        InteractiveItemComponent.deselectAll();

        // Notify listeners that the materials have been updated
        InteractiveItemComponent.addMaterialUpdateListener(() -> {});

        FXGL.getNotificationService().pushNotification("Scene cleared");
    }
  }

  private void addMenuItem(String text, FontAwesomeSolid icon, Runnable action) {
    Button button = createButton(text, new FontIcon(icon));
    button.getStyleClass().add("radial-menu-item");
    button.setOnAction(e -> {
      action.run();
      toggleMenu();
      e.consume(); // Prevent the event from bubbling up
    });
    menuButtons.add(button);
    getChildren().add(button);
  }
  
  private Button createButton(String text, FontIcon icon) {
    Button button = new Button();
    
    // For the center button, use your custom image
    if (text.isEmpty()) {  // This is the center button
        Image buttonImage = new Image(getClass().getResourceAsStream("/assets/textures/radial-menu-button.png"));
        ImageView buttonImageView = new ImageView(buttonImage);
        buttonImageView.setFitHeight(50);  // Adjust size as needed
        buttonImageView.setFitWidth(50);   // Adjust size as needed
        buttonImageView.setPreserveRatio(true);
        button.setGraphic(buttonImageView);
    } else {
        // For other menu items, keep the icon
        button.setGraphic(icon);
    }
    
    button.setStyle("-fx-background-color: transparent;"); // Makes button background transparent
    button.setShape(new Circle(20));
    button.setMinSize(40, 40);
    button.setMaxSize(40, 40);
    
    // Add hover effect
    button.setOnMouseEntered(e -> button.setOpacity(0.8));
    button.setOnMouseExited(e -> button.setOpacity(1.0));
    
    return button;
  }
  
  private void toggleMenu() {
    isExpanded = !isExpanded;
    for (Button button : menuButtons) {
      button.setDisable(!isExpanded);
    }
    animateMenu();
  }
  
  private void animateMenu() {
    Timeline timeline = new Timeline();
    double angleStep = 360.0 / menuButtons.size();
    
    for (int i = 0; i < menuButtons.size(); i++) {
      Button button = menuButtons.get(i);
      double angle = i * angleStep;
      double targetX = isExpanded ? radius * Math.cos(Math.toRadians(angle)) : 0;
      double targetY = isExpanded ? radius * Math.sin(Math.toRadians(angle)) : 0;
      
      KeyValue kvX = new KeyValue(button.layoutXProperty(), targetX);
      KeyValue kvY = new KeyValue(button.layoutYProperty(), targetY);
      KeyValue kvOpacity = new KeyValue(button.opacityProperty(), isExpanded ? 1 : 0);
      
      KeyFrame kf = new KeyFrame(Duration.millis(200), kvX, kvY, kvOpacity);
      timeline.getKeyFrames().add(kf);
    }
    
    if (!isExpanded) {
      // Add a small delay before hiding menu items when closing
      timeline.setDelay(Duration.millis(50));
    }
    
    timeline.play();
  }
  
  private void saveScene() {
    TextInputDialog dialog = new TextInputDialog();
    dialog.setTitle("Save Scene");
    dialog.setHeaderText("Enter a name for your save:");
    dialog.setContentText("Save name:");
    
    Optional<String> result = dialog.showAndWait();
    result.ifPresent(saveName -> {
      SceneManager.saveScene(saveName);
      FXGL.getNotificationService().pushNotification("Scene saved: " + saveName);
    });
  }
  
  private void loadScene() {
    List<String> saves = SceneManager.getSaveFiles();
    if (saves.isEmpty()) {
      FXGL.getNotificationService().pushNotification("No saved scenes available.");
      return;
    }
    ChoiceDialog<String> dialog = new ChoiceDialog<>(saves.get(0), saves);
    dialog.setTitle("Load Scene");
    dialog.setHeaderText("Choose a scene to load:");
    dialog.setContentText("Saved scene:");
    
    Optional<String> result = dialog.showAndWait();
    result.ifPresent(sceneName -> {
      SceneManager.loadScene(sceneName);
      FXGL.getNotificationService().pushNotification("Scene loaded: " + sceneName);
    });
  }
  
  private void editScene() {
    loadScene(); // For now, edit is the same as load
  }
  
  private void deleteScene() {
    List<String> saves = SceneManager.getSaveFiles();
    if (saves.isEmpty()) {
      FXGL.getNotificationService().pushNotification("No saved scenes available.");
      return;
    }
    ChoiceDialog<String> dialog = new ChoiceDialog<>(saves.get(0), saves);
    dialog.setTitle("Delete Saved Scene");
    dialog.setHeaderText("Choose a scene to delete:");
    dialog.setContentText("Saved scene:");
    
    Optional<String> result = dialog.showAndWait();
    result.ifPresent(sceneName -> {
      SceneManager.deleteSave(sceneName);
      FXGL.getNotificationService().pushNotification("Scene deleted: " + sceneName);
    });
  }
  
  public void collapseMenu() {
    if (isExpanded) {
      toggleMenu();
    }
  }
  
  public boolean isExpanded() {
    return isExpanded;
  }
}