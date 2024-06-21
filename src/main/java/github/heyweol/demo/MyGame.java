package github.heyweol.demo;


import com.almasb.fxgl.app.GameApplication;
import com.almasb.fxgl.app.GameSettings;
import com.almasb.fxgl.dsl.FXGL;
import javafx.scene.control.Button;
import javafx.scene.control.TitledPane;
import javafx.scene.image.Image;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.atomic.AtomicBoolean;
import javafx.scene.image.ImageView;


public class MyGame extends GameApplication {
  
  private VBox sidePane;
  private Text mousePositionText;
  
  @Override
  protected void initSettings(GameSettings settings) {
//    settings.setWidth(600);
//    settings.setHeight(600);
    settings.setTitle("XinZhiJu Construction");
    settings.setVersion("0.1");
    settings.setManualResizeEnabled(true);
    settings.setPreserveResizeRatio(true);

  }
  
  
  @Override
  protected void initGame() {
    Image backgroundImage = FXGL.image("bg.png");
    
    var screenWidth = FXGL.getAppWidth();
    var screenHeight = FXGL.getAppHeight();
    
    // Create an ImageView with the background image
    ImageView backgroundView = new ImageView(backgroundImage);
    
    // Set the ImageView size to match the screen
    backgroundView.setFitWidth(screenWidth);
    backgroundView.setFitHeight(screenHeight);
    
    // Preserve image aspect ratio or not, depending on your need
    backgroundView.setPreserveRatio(true);
    
    FXGL.entityBuilder()
            .at(0, 0)
            .view(backgroundView) // set the rectangle as the view of the entity
            .zIndex(-1) // ensure it is rendered behind all other entities
            .buildAndAttach();
  }
  
  @Override
  protected void initInput() {
  }
  
  @Override
  protected void initUI() {

    sidePane = createSidePane();
    sidePane.setTranslateX(0);
    sidePane.setPrefWidth(80);
    sidePane.setStyle("-fx-background-color: white; -fx-border-color: gray; -fx-border-width: 0 1 0 0;");
    
    FXGL.addUINode(sidePane);
    
    mousePositionText = new Text("Mouse: (0, 0)");
    mousePositionText.setTranslateX(FXGL.getAppWidth() - 150);
    mousePositionText.setTranslateY(20);
    FXGL.addUINode(mousePositionText);
    
    // Add mouse move listener to update the text
    FXGL.getGameScene().getContentRoot().setOnMouseMoved(e -> {
      updateMousePositionText(e.getX(), e.getY());
    });
    
    FXGL.getGameScene().getContentRoot().setOnMouseDragged(e -> {
      updateMousePositionText(e.getX(), e.getY());
    });
    
  }
  
  private void updateMousePositionText(double x, double y) {
    mousePositionText.setText(String.format("Mouse: (%.0f, %.0f)", x, y));
  }
  
  @Override
  protected void initGameVars(Map<String, Object> vars) {
    vars.put("pixelsMoved", 0);
  }
  
  private VBox createSidePane() {
    VBox pane = new VBox();
    pane.setPrefWidth(120);
    String [] titles = {"Wall", "Floor", "Furniture", "Decoration", "Character"};
    for (int i = 1; i <= 5; i++) {
      TitledPane titledPane = createTitledPane(titles[i-1]);
      pane.getChildren().add(titledPane);
    }
    
    return pane;
  }
  
  private TitledPane createTitledPane(String title) {
    VBox content = new VBox(5);
    for (int i = 0; i < 3; i++) {
      Rectangle item = createDraggableItem(content);
      content.getChildren().add(item);
    }
    
    TitledPane titledPane = new TitledPane(title, content);
    titledPane.setCollapsible(true);
    return titledPane;
  }
  
  private Rectangle createDraggableItem(VBox parentContainer) {
    Rectangle item = new Rectangle(30, 30);
    item.setFill(getRandomColor());
    
    final Delta dragDelta = new Delta();
    final AtomicBoolean isDragging = new AtomicBoolean(false);
    
    item.setOnMousePressed(e -> {
      dragDelta.x = item.getTranslateX() - e.getSceneX();
      dragDelta.y = item.getTranslateY() - e.getSceneY();
      item.setMouseTransparent(false);
      FXGL.getGameScene().setCursor(javafx.scene.Cursor.CLOSED_HAND);

      if (FXGL.getGameScene().getContentRoot().getChildren().contains(item)) {
        parentContainer.getChildren().remove(item);
        FXGL.addUINode(item);
        addClickHandler(item, parentContainer);
      }
    });
    
//    item.setOnMousePressed(e -> {
//      dragDelta.x = item.getTranslateX() - e.getSceneX();
//      dragDelta.y = item.getTranslateY() - e.getSceneY();
//      item.setMouseTransparent(false);
//      FXGL.getGameScene().setCursor(javafx.scene.Cursor.CLOSED_HAND);
//
//      if (FXGL.getGameScene().getContentRoot().getChildren().contains(item)) {
//        parentContainer.getChildren().remove(item);
//        FXGL.addUINode(item);
//        addClickHandler(item, parentContainer);
//      }
//    });
    
    item.setOnMouseDragged(e -> {
      item.setTranslateX(e.getSceneX() + dragDelta.x);
      item.setTranslateY(e.getSceneY() + dragDelta.y);
    });
    
    item.setOnMouseReleased(e -> {
      item.setMouseTransparent(false);
      FXGL.getGameScene().setCursor(javafx.scene.Cursor.DEFAULT);
      
      if (e.getSceneX() > sidePane.getWidth()) {
        // Remove from side pane and add to game world
        parentContainer.getChildren().remove(item);
        FXGL.addUINode(item);
        
        // Set the item's position to the mouse release coordinates
        item.setTranslateX(e.getSceneX() + dragDelta.x);
        item.setTranslateY(e.getSceneY() + dragDelta.y);
        
        // Add recycling behavior
        item.setOnMouseReleased(recycleEvent -> {
          if (recycleEvent.getSceneX() < sidePane.getWidth()) {
            FXGL.removeUINode(item);
            item.setTranslateX(0);
            item.setTranslateY(0);
            parentContainer.getChildren().add(item);
            item.setOnMouseReleased(e2 -> item.fireEvent(e)); // Reset to original behavior
          }
        });
      } else{
        // Reset position if not dragged out
        item.setTranslateX(0);
        item.setTranslateY(0);
      }
    });
    
    return item;
  }
  
  private void addClickHandler(Rectangle item, VBox parentContainer) {
    item.setOnMouseClicked(e -> {
      if (e.getSceneX() > sidePane.getWidth()) {
        showItemMenu(item, parentContainer);
      }
    });
  }
  
  private void showItemMenu(Rectangle item, VBox parentContainer) {
    VBox menu = new VBox(5);
    menu.setStyle("-fx-background-color: white; -fx-border-color: black;");
    
    Button recycleButton = new Button("Recycle");
    recycleButton.setOnAction(e -> {
      FXGL.removeUINode(item);
      item.setTranslateX(0);
      item.setTranslateY(0);
      parentContainer.getChildren().add(item);
      FXGL.removeUINode(menu);
    });
    
    Button changeColorButton = new Button("Change Color");
    changeColorButton.setOnAction(e -> {
      item.setFill(getRandomColor());
      FXGL.removeUINode(menu);
    });
    
    menu.getChildren().addAll(recycleButton, changeColorButton);
    menu.setTranslateX(item.getTranslateX() + 35);
    menu.setTranslateY(item.getTranslateY() + 35);
    
    FXGL.addUINode(menu);
  }
  
  
  private Color getRandomColor() {
    Random random = new Random();
    return Color.rgb(random.nextInt(256), random.nextInt(256), random.nextInt(256));
  }
  
  // Helper class to track drag delta
  private static class Delta {
    double x, y;
  }
  
  
  public static void main(String[] args) {
    launch(args);
  }
  
  
}