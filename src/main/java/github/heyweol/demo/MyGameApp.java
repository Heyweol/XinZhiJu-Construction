package github.heyweol.demo;

import com.almasb.fxgl.app.GameApplication;
import com.almasb.fxgl.app.GameSettings;
import com.almasb.fxgl.dsl.FXGL;
import com.almasb.fxgl.entity.Entity;
import com.almasb.fxgl.entity.SpawnData;
import com.almasb.fxgl.input.UserAction;
import com.almasb.fxgl.physics.CollisionHandler;
import github.heyweol.demo.components.DraggableComponent;
import github.heyweol.demo.ui.ItemBar;
import github.heyweol.demo.ui.MainGameScene;
import github.heyweol.demo.utils.FileUtils;
import javafx.geometry.Point2D;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static com.almasb.fxgl.dsl.FXGL.*;

public class MyGameApp extends GameApplication {
  
  private static final int GAME_WIDTH = 800;
  private static final int GAME_HEIGHT = 600;
  private static final int ITEM_BAR_HEIGHT = 100;
  
  private MainGameScene mainGameScene;
  private ItemBar itemBar;
  private List<Entity> placedItems;
  
  @Override
  protected void initSettings(GameSettings settings) {
//    settings.setWidth(800);
    settings.setHeight(500);
    settings.setPreserveResizeRatio(true);
    settings.setTitle("心纸居");
    settings.setVersion("v0.1");
  }
  

  
  @Override
  protected void initGame() {
    FXGL.getGameWorld().addEntityFactory(new MyGameFactory());
    
//    FXGL.getGameScene().setBackgroundColor(Color.GRAY);
    Image bgImage = new Image(getClass().getResourceAsStream("/assets/textures/bg.png"));
    ImageView bgView = new ImageView(bgImage);
//    bgView.setFitWidth(800); // Adjust the width as needed
    bgView.setFitHeight(500); // Adjust the height as needed
    bgView.setPreserveRatio(true);
    bgView.setX(200);
    System.out.println("bgView: " + bgView.getFitWidth() + " " + bgView.getFitHeight());
    Entity room = entityBuilder()
            .view(bgView)
            .zIndex(-1)
            .buildAndAttach();
    
//    mainGameScene = new MainGameScene();
//    FXGL.getGameScene().addUINode(mainGameScene);
    
    itemBar = new ItemBar(200, 500);
    Map<String, List<Item>> organizedItems = FileUtils.scanAndOrganizeItems();

    
    System.out.println("Organized items size: " + organizedItems.size());
    
    for (Map.Entry<String, List<Item>> entry : organizedItems.entrySet()) {
      System.out.println("Adding item type: " + entry.getKey());
      itemBar.addItemType(entry.getKey(), entry.getValue());
    }
    
    
    FXGL.addUINode(itemBar, 0, 0);
    

  }
  
  @Override
  protected void initInput() {
    getInput().addAction(new UserAction("Place Item") {
      @Override
      protected void onActionBegin() {
        if (itemBar.getSelectedItem() != null) {
          Item selectedItem = itemBar.getSelectedItem();
          double mouseX = getInput().getMouseXWorld();
          double mouseY = getInput().getMouseYWorld();
          
          Entity placedItem = spawn("placedItem", new SpawnData(mouseX, mouseY)
                  .put("item", selectedItem));
          
          
          // Add remove button to the placed item
          addRemoveButton(placedItem);
          
          // Deselect the item in the item bar
          itemBar.deselectItem();
        }
      }
    }, MouseButton.PRIMARY);
  }
  
  private void addRemoveButton(Entity placedItem) {
    var removeBtn = getUIFactoryService().newButton("Remove");
    removeBtn.setOnAction(e -> {
      // Remove the item
      placedItem.removeFromWorld();
      
      // Update total cost
      Item item = placedItem.getObject("item");

      
      // Remove the button itself
      FXGL.removeUINode(removeBtn);
    });
    
    // Position the button near the placed item
    removeBtn.setTranslateX(placedItem.getX());
    removeBtn.setTranslateY(placedItem.getY() - 30);
    
    FXGL.addUINode(removeBtn);
    
    // Make the button visible only when the item is clicked
    removeBtn.setVisible(false);
    placedItem.getViewComponent().addEventHandler(MouseEvent.MOUSE_CLICKED, e -> {
      removeBtn.setVisible(!removeBtn.isVisible());
    });
  }
  
  @Override
  protected void initPhysics() {
  
  }
  
  
  public static void main(String[] args) {
    launch(args);
  }
}
