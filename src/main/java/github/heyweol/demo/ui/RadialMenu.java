package github.heyweol.demo.ui;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.almasb.fxgl.entity.Entity;
import github.heyweol.demo.EntityType;
import org.kordamp.ikonli.fontawesome5.FontAwesomeSolid;
import org.kordamp.ikonli.javafx.FontIcon;

import com.almasb.fxgl.dsl.FXGL;

import github.heyweol.demo.utils.SceneManager;
import github.heyweol.demo.components.InteractiveItemComponent;
import github.heyweol.demo.IsometricGrid;

import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceDialog;
import javafx.scene.control.TextInputDialog;
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
  
  public RadialMenu(Runnable screenshotAction, IsometricGrid isometricGrid) {
    this.screenshotAction = screenshotAction;
    this.isometricGrid = isometricGrid;
    
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
    // Clear all entities from the game world
//    FXGL.getGameWorld().getEntities().forEach(entity -> {
//      // Remove the entity from the isometric grid
//      // only TYPE==FLOOR_ITEM and TYPE==WALL_ITEM
//      if (entity.getType() == EntityType.FLOOR_ITEM || entity.getType() == EntityType.WALL_ITEM)
//      {
//        isometricGrid.removeEntity(entity);
//
//        // Remove the entity from the game world
//        entity.removeFromWorld();
//      }
//    });
    
    List<Entity> entitiesToRemove = new ArrayList<>();
    FXGL.getGameWorld().getEntities().forEach(entity -> {
      // Remove the entity from the isometric grid
      // only TYPE==FLOOR_ITEM and TYPE==WALL_ITEM
      if (entity.getType() == EntityType.FLOOR_ITEM || entity.getType() == EntityType.WALL_ITEM) {
        isometricGrid.removeEntity(entity);
        entitiesToRemove.add(entity);
      }
    });
    entitiesToRemove.forEach(Entity::removeFromWorld);
    // Clear the selection
    InteractiveItemComponent.deselectAll();

    // Clear any remaining UI elements (if necessary)
//    FXGL.getGameScene().clearUINodes();

    // Notify listeners that the materials have been updated
    InteractiveItemComponent.addMaterialUpdateListener(() -> {});

    FXGL.getNotificationService().pushNotification("Scene cleared");
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
    Button button = new Button(text, icon);
    button.setShape(new Circle(20));
    button.setMinSize(40, 40);
    button.setMaxSize(40, 40);
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