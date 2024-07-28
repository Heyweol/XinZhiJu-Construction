package github.heyweol.demo;

import com.almasb.fxgl.app.GameApplication;
import com.almasb.fxgl.app.GameSettings;
import com.almasb.fxgl.dsl.FXGL;
import com.almasb.fxgl.entity.Entity;
import com.almasb.fxgl.entity.SpawnData;
import com.almasb.fxgl.input.UserAction;

import github.heyweol.demo.components.GridVisualizerComponent;
import github.heyweol.demo.components.InteractiveItemComponent;
import github.heyweol.demo.components.ZIndexComponent;
import github.heyweol.demo.ui.ItemBar;
import github.heyweol.demo.ui.MainGameScene;
import github.heyweol.demo.ui.MaterialSummaryWindow;
import github.heyweol.demo.utils.ResourceManager;
import javafx.geometry.Point2D;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.HBox;
import javafx.util.Duration;
import org.kordamp.ikonli.fontawesome5.FontAwesomeSolid;
import org.kordamp.ikonli.javafx.FontIcon;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.logging.Logger;

import static com.almasb.fxgl.dsl.FXGL.*;

public class MyGameApp extends GameApplication {
  
  private static final Logger LOGGER = Logger.getLogger(MyGameApp.class.getName());
  
  private static final int GAME_WIDTH = 800;
  private static final int GAME_HEIGHT = 600;
  private static final int ITEM_BAR_HEIGHT = 100;
  
  private MainGameScene mainGameScene;
  private ItemBar itemBar;
  private List<Entity> placedItems;
  private HBox currentToolbar = null;
  private IsometricGrid isometricGrid;
  private GridVisualizerComponent gridVisualizerComponent;
  private List<Item> allItems;
  private Map<String, Integer> materialPrices;
  private MaterialSummaryWindow materialSummaryWindow;
  private Map<String, Integer> totalMaterials = new HashMap<>();
  
  @Override
  protected void initSettings(GameSettings settings) {
    settings.setHeight(500);
    settings.setPreserveResizeRatio(true);
    settings.setTitle("心纸居");
    settings.setVersion("v0.1");
  }
  
  @Override
  protected void initGame() {
    isometricGrid = new IsometricGrid(40, 40, 64, 32, 200, 0);
    
    Entity background = entityBuilder()
            .at(200, 0)
            .view(new javafx.scene.shape.Rectangle(500, 500, javafx.scene.paint.Color.TRANSPARENT))
            .buildAndAttach();
    
    gridVisualizerComponent = new GridVisualizerComponent(isometricGrid, 200, 0);
    background.addComponent(gridVisualizerComponent);
    
    FXGL.getGameWorld().addEntityFactory(new MyGameFactory(isometricGrid, gridVisualizerComponent));
    
    Image bgImage = new Image(getClass().getResourceAsStream("/assets/textures/bg.png"));
    ImageView bgView = new ImageView(bgImage);
    bgView.setFitHeight(500);
    bgView.setPreserveRatio(true);
    bgView.setX(200);
    Entity room = entityBuilder()
            .view(bgView)
            .zIndex(-1)
            .buildAndAttach();
    
    itemBar = new ItemBar(200, 500);
    List<Item> allItems = ResourceManager.loadItems();
    
    Map<String, Map<String, List<Item>>> organizedItems = allItems.stream()
            .collect(Collectors.groupingBy(
                    item -> item.getFilename().split("_")[1],
                    Collectors.groupingBy(item -> {
                      String[] parts = item.getFilename().split("_");
                      switch(parts[2]) {
                        case "guajian": return "hanging";
                        case "qiju": return "furniture";
                        case "zhiwu": return "plant";
                        case "zhuangshi": return "decor";
                        default: return "other";
                      }
                    })
            ));
    
    for (Map.Entry<String, Map<String, List<Item>>> characterEntry : organizedItems.entrySet()) {
      String character = characterEntry.getKey();
      for (Map.Entry<String, List<Item>> typeEntry : characterEntry.getValue().entrySet()) {
        String type = typeEntry.getKey();
        List<Item> items = typeEntry.getValue();
        LOGGER.info("Adding " + items.size() + " items for character " + character + " and type " + type);
        itemBar.addItemType(character, type, items);
      }
    }
    
    FXGL.addUINode(itemBar, 0, 0);
    
    materialSummaryWindow = new MaterialSummaryWindow();
    FXGL.addUINode(materialSummaryWindow, FXGL.getAppWidth() - 200, 20);
    materialSummaryWindow.setVisible(true);
    
    InteractiveItemComponent.addGlobalSelectionListener(this::handleGlobalSelection);
    InteractiveItemComponent.addMaterialUpdateListener(this::updateMaterialSummary);
    
    getGameTimer().runAtInterval(this::updateZIndices, Duration.millis(50));
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
          Point2D gridPos = isometricGrid.getGridPosition(mousePos.getX(), mousePos.getY());
          
          if (isometricGrid.canPlaceItem((int) gridPos.getX(), (int) gridPos.getY(), selectedItem.getWidth(), selectedItem.getLength())) {
            Point2D isoPos = isometricGrid.getIsometricPosition((int) gridPos.getX(), (int) gridPos.getY());
            Entity placedItem = spawn("placedItem", new SpawnData(isoPos.getX(), isoPos.getY())
                    .put("item", selectedItem));
            isometricGrid.placeItem(placedItem, (int) gridPos.getX(), (int) gridPos.getY(), selectedItem.getWidth(), selectedItem.getLength());
            updateMaterialSummary();
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
  
  private void addInteractionButtons(Entity placedItem) {
    HBox toolbar = new HBox(5);
    toolbar.setStyle("-fx-background-color: rgba(200, 200, 200, 0.7); -fx-padding: 5; -fx-background-radius: 5;");
    
    Button rotateBtn = createButton("Rotate");
    Button removeBtn = createButton("Remove");
    
    removeBtn.setOnAction(e -> {
      placedItem.removeFromWorld();
      Item item = placedItem.getObject("item");
      FXGL.removeUINode(toolbar);
      updateMaterialSummary();
    });
    
    rotateBtn.setOnAction(e -> {
      placedItem.rotateBy(90);
    });
    
    toolbar.getChildren().addAll(rotateBtn, removeBtn);
    toolbar.setVisible(false);
    
    FXGL.addUINode(toolbar);
    
    placedItem.getComponentOptional(InteractiveItemComponent.class).ifPresent(component -> {
      component.addSelectionListener(() -> {
        boolean isSelected = InteractiveItemComponent.getSelectedEntity() == placedItem;
        toolbar.setVisible(isSelected);
        if (isSelected) {
          updateToolbarPosition(placedItem, toolbar);
        }
      });
    });
    
    placedItem.xProperty().addListener((obs, old, newX) -> updateToolbarPosition(placedItem, toolbar));
    placedItem.yProperty().addListener((obs, old, newY) -> updateToolbarPosition(placedItem, toolbar));
  }
  
  private HBox createToolbar(Entity placedItem) {
    HBox toolbar = new HBox(5);
    toolbar.setStyle("-fx-background-color: rgba(200, 200, 200, 0.7); -fx-padding: 5; -fx-background-radius: 5;");
    
    Button mirrorBtn = createButton("", new FontIcon(FontAwesomeSolid.EXCHANGE_ALT));
    Button removeBtn = createButton("", new FontIcon(FontAwesomeSolid.TRASH));
    
    removeBtn.setOnAction(e -> {
      placedItem.removeFromWorld();
      Item item = placedItem.getObject("item");
      isometricGrid.removeItem(placedItem);
      removeCurrentToolbar();
      updateMaterialSummary();
    });
    
    mirrorBtn.setOnAction(e -> {
      InteractiveItemComponent itemComponent = placedItem.getComponent(InteractiveItemComponent.class);
      itemComponent.mirror();
    });
    
    toolbar.getChildren().addAll(mirrorBtn, removeBtn);
    
    return toolbar;
  }
  
  private Button createButton(String text) {
    Button button = new Button(text);
    button.setStyle("-fx-background-color: white; -fx-text-fill: black; -fx-border-color: black; -fx-border-radius: 3;");
    button.setOnMouseEntered(e -> button.setStyle("-fx-background-color: lightgray; -fx-text-fill: black; -fx-border-color: black; -fx-border-radius: 3;"));
    button.setOnMouseExited(e -> button.setStyle("-fx-background-color: white; -fx-text-fill: black; -fx-border-color: black; -fx-border-radius: 3;"));
    return button;
  }
  
  private Button createButton(String text, FontIcon icon) {
    Button button = new Button(text, icon);
    button.setStyle("-fx-background-color: white; -fx-text-fill: black; -fx-border-color: black; -fx-border-radius: 3;");
    button.setOnMouseEntered(e -> button.setStyle("-fx-background-color: lightgray; -fx-text-fill: black; -fx-border-color: black; -fx-border-radius: 3;"));
    button.setOnMouseExited(e -> button.setStyle("-fx-background-color: white; -fx-text-fill: black; -fx-border-color: black; -fx-border-radius: 3;"));
    return button;
  }
  
  private void updateToolbarPosition(Entity item, HBox toolbar) {
    toolbar.setTranslateX(item.getX() - toolbar.getWidth() / 2 + item.getWidth() / 2);
    toolbar.setTranslateY(item.getY() - toolbar.getHeight() - 10);
  }
  
  private boolean isClickOnItem() {
    return FXGL.getGameWorld().getEntitiesAt(getInput().getMousePositionWorld())
            .stream()
            .anyMatch(entity -> entity.hasComponent(InteractiveItemComponent.class));
  }
  
  private void handleGlobalSelection() {
    removeCurrentToolbar();
    
    Entity selectedEntity = InteractiveItemComponent.getSelectedEntity();
    if (selectedEntity != null) {
      currentToolbar = createToolbar(selectedEntity);
      FXGL.addUINode(currentToolbar);
      updateToolbarPosition(selectedEntity, currentToolbar);
    }
    updateMaterialSummary();
  }
  
  private void updateMaterialSummary() {
    totalMaterials.clear();
    getGameWorld().getEntitiesByType(EntityType.PLACED_ITEM).forEach(entity -> {
      Item item = entity.getObject("item");
      for (Map.Entry<String, Integer> entry : item.getMaterialList().entrySet()) {
        totalMaterials.merge(entry.getKey(), entry.getValue(), Integer::sum);
      }
    });
    materialSummaryWindow.updateMaterials(totalMaterials);
  }
  
  private void removeCurrentToolbar() {
    if (currentToolbar != null) {
      FXGL.removeUINode(currentToolbar);
      currentToolbar = null;
    }
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
}