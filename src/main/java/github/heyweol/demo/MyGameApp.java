package github.heyweol.demo;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import javax.imageio.ImageIO;

import com.almasb.fxgl.app.GameApplication;
import com.almasb.fxgl.app.GameSettings;
import com.almasb.fxgl.dsl.FXGL;
import static com.almasb.fxgl.dsl.FXGL.entityBuilder;
import static com.almasb.fxgl.dsl.FXGL.getGameTimer;
import static com.almasb.fxgl.dsl.FXGL.getGameWorld;
import static com.almasb.fxgl.dsl.FXGL.getInput;
import static com.almasb.fxgl.dsl.FXGL.spawn;
import com.almasb.fxgl.entity.Entity;
import com.almasb.fxgl.entity.SpawnData;
import com.almasb.fxgl.entity.components.BoundingBoxComponent;
import com.almasb.fxgl.input.UserAction;

import github.heyweol.demo.components.GridVisualizerComponent;
import github.heyweol.demo.components.InteractiveItemComponent;
import github.heyweol.demo.components.ZIndexComponent;
import github.heyweol.demo.ui.ItemBar;
import github.heyweol.demo.ui.MainGameScene;
import github.heyweol.demo.ui.MaterialSummaryWindow;
import github.heyweol.demo.ui.RadialMenu;
import github.heyweol.demo.utils.ResourceManager;
import github.heyweol.demo.utils.SceneManager;
import javafx.geometry.Point2D;
import javafx.geometry.Rectangle2D;
import javafx.scene.Node;
import javafx.scene.SnapshotParameters;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextInputDialog;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.PixelReader;
import javafx.scene.image.WritableImage;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.scene.transform.Transform;
import javafx.util.Duration;

public class MyGameApp extends GameApplication {
  
  private static final Logger LOGGER = Logger.getLogger(MyGameApp.class.getName());
  
  
  
  private static final int ITEM_BAR_WIDTH = 260;
  private static final int BG_WIDTH = 1063;
  private static final int BG_HEIGHT = 1111;
 
  
  private MainGameScene mainGameScene;
  private ItemBar itemBar;
  private List<Entity> placedItems;
  private IsometricGrid isometricGrid;
  private GridVisualizerComponent gridVisualizerComponent;
  private List<Item> allItems;
  private Map<String, Integer> materialPrices;
  private MaterialSummaryWindow materialSummaryWindow;
  private final Map<String, Integer> totalMaterials = new HashMap<>();
  // Add these as class variables
  private WallGrid leftWallGrid;
  private WallGrid rightWallGrid;
  private ComboBox<String> saveSelector;
  
  private static final int game_height = 600;
  
  private static final double scale_factor = (double) game_height / BG_HEIGHT;
  private static final int bg_height = game_height;
  private static final int bg_width = (int) (BG_WIDTH * scale_factor);
  private static final int game_width = bg_width + ITEM_BAR_WIDTH;
  
  private static final int GRID_TOP_X = ITEM_BAR_WIDTH + bg_width / 2; // 528
  private static final int GRID_TOP_Y = (int) (451*scale_factor);
  
  private RadialMenu radialMenu;
  
  @Override
  protected void initSettings(GameSettings settings) {
    settings.setHeight(game_height);
    settings.setWidth(game_width);
    settings.setTitle("心纸居");
    settings.setVersion("0.1");
    
    settings.getCSSList().add("radial-menu.css");
  }
  
  @Override
  protected void initGame() {
    // Register a scene load listener to update the material list
    SceneManager.addSceneLoadListener(this::updateMaterialSummary);
    
    // Create the isometric grid with the new parameters
    isometricGrid = new IsometricGrid(15, 15, 34, 17, GRID_TOP_X, GRID_TOP_Y);
    leftWallGrid = new WallGrid(15, 5, 17, 17, GRID_TOP_X , GRID_TOP_Y - 95, true);
    rightWallGrid = new WallGrid(15, 5, 17, 17, GRID_TOP_X , GRID_TOP_Y - 95, false);
    radialMenu = new RadialMenu(this::takeCustomScreenshot, isometricGrid);
    FXGL.addUINode(radialMenu, 300, 100);
    
    Entity background = entityBuilder()
            .at(ITEM_BAR_WIDTH, 0)
            .view(new javafx.scene.shape.Rectangle(bg_width, bg_height, javafx.scene.paint.Color.TRANSPARENT))
            .buildAndAttach();
    
    gridVisualizerComponent = new GridVisualizerComponent(isometricGrid, leftWallGrid, rightWallGrid, ITEM_BAR_WIDTH, 0);
    background.addComponent(gridVisualizerComponent);
    
    FXGL.getGameWorld().addEntityFactory(new MyGameFactory(isometricGrid, leftWallGrid, rightWallGrid, gridVisualizerComponent));
    
    Image bgImage = new Image(getClass().getResourceAsStream("/assets/textures/bg15.jpg"));
    ImageView bgView = new ImageView(bgImage);
    bgView.setFitHeight(game_height);
    bgView.setPreserveRatio(true);
    bgView.setX(ITEM_BAR_WIDTH);
    Entity room = entityBuilder()
            .view(bgView)
            .zIndex(-1)
            .buildAndAttach();
    
    itemBar = new ItemBar(ITEM_BAR_WIDTH , bg_height);
    ResourceManager.initialize();
    List<Item> allItems = ResourceManager.getAllItems();
    
//    Map<String, Map<String, List<Item>>> organizedItems = allItems.stream()
//            .collect(Collectors.groupingBy(
//                    item -> item.getFilename().split("_")[1],
//                    Collectors.groupingBy(item -> {
//                      String filename = item.getFilename();
//                      if (filename.contains("guajian")) return "挂件";
//                      if (filename.contains("qiju")) return "起居";
//                      if (filename.contains("zhiwu")) return "植物";
//                      if (filename.contains("zhuangshi")) return "装饰";
//                      return "其他";
//                    })
//            ));
    
    Map<String, Map<String, Map<String, List<Item>>>> organizedItems = allItems.stream()
            .collect(Collectors.groupingBy(
                    item -> item.getFilename().split("_")[0], // Season
                    Collectors.groupingBy(
                            item -> item.getFilename().split("_")[1], // Character
                            Collectors.groupingBy(item -> {
                              String filename = item.getFilename();
                              if (filename.contains("guajian")) return "挂件";
                              if (filename.contains("qiju")) return "起居";
                              if (filename.contains("zhiwu")) return "植物";
                              if (filename.contains("zhuangshi")) return "装饰";
                              return "其他";
                            })
                    )
            ));
    
    for (Map.Entry<String, Map<String, Map<String, List<Item>>>> seasonEntry : organizedItems.entrySet()) {
      String season = seasonEntry.getKey();
      for (Map.Entry<String, Map<String, List<Item>>> characterEntry : seasonEntry.getValue().entrySet()) {
        String character = characterEntry.getKey();
        for (Map.Entry<String, List<Item>> typeEntry : characterEntry.getValue().entrySet()) {
          String type = typeEntry.getKey();
          List<Item> items = typeEntry.getValue();
          itemBar.addItemType(season, character, type, items);
        }
      }
    }
    
//    for (Map.Entry<String, Map<String, List<Item>>> characterEntry : organizedItems.entrySet()) {
//      String character = characterEntry.getKey();
//      for (Map.Entry<String, List<Item>> typeEntry : characterEntry.getValue().entrySet()) {
//        String type = typeEntry.getKey();
//        List<Item> items = typeEntry.getValue();
////        LOGGER.info("Adding " + items.size() + " items for character " + character + " and type " + type);
//        itemBar.addItemType(character, type, items);
//      }
//    }
    
    FXGL.addUINode(itemBar, 0, 0);
    
    materialSummaryWindow = new MaterialSummaryWindow();
    FXGL.addUINode(materialSummaryWindow, FXGL.getAppWidth() - 200, 20);
    materialSummaryWindow.setVisible(true);
    
    InteractiveItemComponent.addGlobalSelectionListener(this::handleGlobalSelection);
    InteractiveItemComponent.addMaterialUpdateListener(this::updateMaterialSummary);
    
    getGameTimer().runAtInterval(this::updateZIndices, Duration.millis(50));
    
    // Save button for debugging use only
    Button saveButton = new Button("Save Items to JSON");
    saveButton.setOnAction(event -> {
      String savePath = "src/main/resources/assets/data/updated_items.json";
      ResourceManager.saveItemsToJson(savePath);
    });
    FXGL.addUINode(saveButton, 300, 10);
    
    Text positionText = new Text();
    positionText.setTranslateX(500);
    positionText.setTranslateY(20);
    FXGL.addUINode(positionText);
    // Add mouse move listener
    FXGL.getInput().addEventHandler(javafx.scene.input.MouseEvent.MOUSE_MOVED, event -> {
      Point2D mousePos = new Point2D(event.getX(), event.getY());
      Point2D gridPos = isometricGrid.getGridPosition(mousePos.getX(), mousePos.getY());
      positionText.setText(String.format("Mouse: (%.2f, %.2f) Grid: (%.0f, %.0f)",
              mousePos.getX(), mousePos.getY(),
              gridPos.getX(), gridPos.getY()));
    });
  }
  
  @Override
  protected void initInput() {
    getInput().addAction(new UserAction("Place Item or Deselect") {
      @Override
      protected void onActionBegin() {
        if (itemBar.getSelectedItem() != null) {
          gridVisualizerComponent.show();
          InteractiveItemComponent.deselectAll();
          Item selectedItem = itemBar.getSelectedItem();
          Point2D mousePos = getInput().getMousePositionWorld();
          
          // Determine which grid to use based on the selected item type
          if (selectedItem.getFilename().contains("guajian")) {
            // For hanging items, check both wall grids
            Point2D leftGridPos = leftWallGrid.getGridPosition(mousePos.getX(), mousePos.getY());
            Point2D rightGridPos = rightWallGrid.getGridPosition(mousePos.getX(), mousePos.getY());
            
            if (leftWallGrid.canPlaceItem((int) leftGridPos.getX(), (int) leftGridPos.getY(), selectedItem.getWidth(), selectedItem.getLength())) {
              Point2D wallPos = leftWallGrid.getWallPosition((int) leftGridPos.getX(), (int) leftGridPos.getY());
              spawnItem(selectedItem, wallPos, EntityType.WALL_ITEM);
            } else if (rightWallGrid.canPlaceItem((int) rightGridPos.getX(), (int) rightGridPos.getY(), selectedItem.getWidth(), selectedItem.getLength())) {
              Point2D wallPos = rightWallGrid.getWallPosition((int) rightGridPos.getX(), (int) rightGridPos.getY());
              spawnItem(selectedItem, wallPos, EntityType.WALL_ITEM);
            }
          } else {
            // For other items, use the floor grid
            Point2D gridPos = isometricGrid.getGridPosition(mousePos.getX(), mousePos.getY());
            System.out.println("gridPos: " + gridPos);
            System.out.println("Can place item: " + isometricGrid.canPlaceItem((int) gridPos.getX(), (int) gridPos.getY(), selectedItem.getWidth(), selectedItem.getLength()));
            if (isometricGrid.canPlaceItem((int) gridPos.getX(), (int) gridPos.getY(), selectedItem.getWidth(), selectedItem.getLength())) {
              Point2D isoPos = isometricGrid.getIsometricPosition((int) gridPos.getX(), (int) gridPos.getY());
              spawnItem(selectedItem, isoPos, EntityType.FLOOR_ITEM);
            }
          }
          
          itemBar.deselectItem();
        } else {
          if (getInput().getMousePositionWorld().getX() > 0 &&
                  getInput().getMousePositionWorld().getY() > 0) {
            
            Entity clickedEntity = getEntityAtMouse();
            if (clickedEntity == null || !clickedEntity.hasComponent(InteractiveItemComponent.class)) {
              InteractiveItemComponent.deselectAll();
            }
          }
        }
      }
      
      @Override
      protected void onAction() {
        Point2D mousePos = getInput().getMousePositionWorld();
        Point2D gridPos = isometricGrid.getGridPosition(mousePos.getX(), mousePos.getY());
      }
      
      @Override
      protected void onActionEnd() {
        gridVisualizerComponent.hide();
      }
    }, MouseButton.PRIMARY);
  }
  
  @Override
  protected void initPhysics() {
  }
  
  @Override
  protected void initGameVars(Map<String, Object> vars) {
    super.initGameVars(vars);
  }
  
  @Override
  protected void onUpdate(double tpf) {
    super.onUpdate(tpf);
  }
  
  public static void main(String[] args) {
    launch(args);
  }
  
//  private boolean isClickOnItem() {
//    return FXGL.getGameWorld().getEntitiesAt(getInput().getMousePositionWorld())
//            .stream()
//            .anyMatch(entity -> entity.hasComponent(InteractiveItemComponent.class));
//  }
  
  private void handleGlobalSelection() {
    Entity selectedEntity = InteractiveItemComponent.getSelectedEntity();
    updateMaterialSummary();
  }
  
  private void updateMaterialSummary() {
    totalMaterials.clear();
    getGameWorld().getEntitiesByType(EntityType.FLOOR_ITEM).forEach(entity -> {
      Item item = entity.getObject("item");
      for (Map.Entry<String, Integer> entry : item.getMaterialList().entrySet()) {
        totalMaterials.merge(entry.getKey(), entry.getValue(), Integer::sum);
      }
    });
    materialSummaryWindow.updateMaterials(totalMaterials);
  }
  
  private Entity getEntityAtMouse() {
    return FXGL.getGameWorld().getEntitiesAt(getInput().getMousePositionWorld())
            .stream()
            .filter(entity -> entity.hasComponent(InteractiveItemComponent.class))
            .findFirst()
            .orElse(null);
  }
  
  private void updateZIndices() {
    getGameWorld().getEntitiesCopy().stream()
            .filter(e -> e.hasComponent(InteractiveItemComponent.class))
            .forEach(e -> e.setZIndex((int) (e.getY() * 100)));
  }
  
  /**
   *
   * @param selectedItem Item object to spawn
   * @param position Mouse position
   * @param type
   */
  private void spawnItem(Item selectedItem, Point2D position, EntityType type) {
    String entityType = (type == EntityType.WALL_ITEM) ? "wallItem" : "floorItem";
    Point2D gridPos = isometricGrid.getGridPosition(position.getX(), position.getY());
    if (!isometricGrid.canPlaceItem((int) gridPos.getX(), (int) gridPos.getY(), selectedItem.getNumTileWidth(), selectedItem.getNumTileHeight())) {
      return;
    }
    Entity placedItem = spawn(entityType, new SpawnData(position.getX(), position.getY())
            .put("item", selectedItem)
            .put("type", type));
    
//    // Calculate and apply the offset
//    Point2D offset = calculateOffset(placedItem);
//    placedItem.translate(offset);
//
//    // Update the grid with the new position
//    Point2D finalPos = new Point2D(placedItem.getX(), placedItem.getY());
//    Point2D finalGridPos = isometricGrid.getGridPosition(finalPos.getX(), finalPos.getY());
//    isometricGrid.placeEntity(placedItem, (int) finalGridPos.getX(), (int) finalGridPos.getY(),
//            selectedItem.getNumTileWidth(), selectedItem.getNumTileHeight());
//
    updateMaterialSummary();
  }
  
  private Point2D calculateOffset(Entity entity) {
    BoundingBoxComponent bbox = entity.getBoundingBoxComponent();
    double height = bbox.getHeight();
    return new Point2D(0, -height);
  }
  
//  private void initSaveModule() {
//    saveSelector = new ComboBox<>();
//    updateSaveList();
//
//    Button saveButton = new Button("Save Scene");
//    saveButton.setOnAction(e -> {
//      TextInputDialog dialog = new TextInputDialog();
//      dialog.setTitle("Save Scene");
//      dialog.setHeaderText("Enter a name for your save:");
//      dialog.setContentText("Save name:");
//
//      dialog.showAndWait().ifPresent(saveName -> {
//        SceneManager.saveScene(saveName);
//        updateSaveList();
//      });
//    });
//
//    Button loadButton = new Button("Load Scene");
//    loadButton.setOnAction(e -> {
//      String selectedSave = saveSelector.getValue();
//      if (selectedSave != null) {
//        SceneManager.loadScene(selectedSave);
//      }
//    });
//
//    Button deleteButton = new Button("Delete Save");
//    deleteButton.setOnAction(e -> {
//      String selectedSave = saveSelector.getValue();
//      if (selectedSave != null) {
//        SceneManager.deleteSave(selectedSave);
//        updateSaveList();
//      }
//    });
//
//    HBox buttonBox = new HBox(10, saveButton, loadButton, deleteButton);
//    VBox saveBox = new VBox(10, saveSelector, buttonBox);
//    saveBox.setTranslateX(ITEM_BAR_WIDTH);
//    saveBox.setTranslateY(10);
//
//    FXGL.addUINode(saveBox);
//  }
  
  private void updateSaveList() {
    List<String> saveFiles = SceneManager.getSaveFiles();
    saveSelector.getItems().setAll(saveFiles);
    if (!saveFiles.isEmpty()) {
      saveSelector.setValue(saveFiles.get(0));
    }
  }
  
//  private void adjustItemPosition(Entity item, double dx, double dy, double scale) {
//    // Adjust position
//    item.translateX(dx);
//    item.translateY(dy);
//
//    // Adjust scale
//    item.setScaleX(scale);
//    item.setScaleY(scale);
//
//    // Update grid position if necessary
//    if (item.getType() == EntityType.FLOOR_ITEM) {
//      Point2D gridPos = isometricGrid.getGridPosition(item.getX()                                                                                                      , item.getY());
//      isometricGrid.placeEntity(item, (int) gridPos.getX(), (int) gridPos.getY(),
//              item.getInt("itemWidth"), item.getInt("itemLength"));
//    }
//
//    // Update z-index
//    item.getComponent(ZIndexComponent.class).onUpdate(0);
//  }
  
  
  private void takeCustomScreenshot() {
    radialMenu.setVisible(false);
    // Get the main game scene without UI elements
    Node gameView = FXGL.getGameScene().getContentRoot();
    
    // Calculate the scale factor
    double scaleX = BG_WIDTH / (double)bg_width;
    double scaleY = BG_HEIGHT / (double)bg_height;
    
    // Create a new WritableImage to store the screenshot at full resolution
    WritableImage screenshot = new WritableImage((int)(bg_width * scaleX), (int)(bg_height * scaleY));
    
    // Take the snapshot of the game view
    SnapshotParameters params = new SnapshotParameters();
    params.setViewport(new Rectangle2D(ITEM_BAR_WIDTH * scaleX, 0, bg_width * scaleX, bg_height * scaleY));
    params.setTransform(Transform.scale(scaleX, scaleY));
    gameView.snapshot(params, screenshot);
    
    
    radialMenu.setVisible(true);
    // Save the screenshot
    String fileName = "screenshot_" + System.currentTimeMillis() + ".png";
    String filePath = System.getProperty("user.home") + "/Desktop/" + fileName;
    File file = new File(filePath);
    
    try {
      // Use JavaFX's built-in functionality to save the image
      BufferedImage bufferedImage = new BufferedImage((int)(bg_width * scaleX), (int)(bg_height * scaleY), BufferedImage.TYPE_INT_ARGB);
      PixelReader pr = screenshot.getPixelReader();
      for (int x = 0; x < bg_width * scaleX; x++) {
        for (int y = 0; y < bg_height * scaleY; y++) {
          bufferedImage.setRGB(x, y, pr.getArgb(x, y));
        }
      }
      ImageIO.write(bufferedImage, "png", file);
      
      FXGL.getNotificationService().pushNotification("截图已保存至桌面😊 ");
    } catch (IOException e) {
      e.printStackTrace();
      FXGL.getNotificationService().pushNotification("截图失败😫");
    }
  }
}