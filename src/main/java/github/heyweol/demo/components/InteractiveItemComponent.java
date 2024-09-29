package github.heyweol.demo.components;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.kordamp.ikonli.fontawesome5.FontAwesomeSolid;
import org.kordamp.ikonli.javafx.FontIcon;

import com.almasb.fxgl.dsl.FXGL;
import com.almasb.fxgl.entity.Entity;
import com.almasb.fxgl.entity.component.Component;
import com.almasb.fxgl.texture.Texture;

import github.heyweol.demo.EntityType;
import github.heyweol.demo.IsometricGrid;
import github.heyweol.demo.Item;
import github.heyweol.demo.WallGrid;
import github.heyweol.demo.ui.ItemAdjustmentDialog;
import github.heyweol.demo.utils.ResourceManager;
import javafx.geometry.Point2D;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;

public class InteractiveItemComponent extends Component {
  private static Entity selectedEntity = null;
  private static DropShadow selectionEffect;
  private Point2D dragOffset;
  private boolean isDragging = false;
  
  private static List<Runnable> globalSelectionListeners = new ArrayList<>();
  private static List<Runnable> materialUpdateListeners = new ArrayList<>();
  private List<Runnable> selectionListeners = new ArrayList<>();
  
  private IsometricGrid isometricGrid;
  private GridVisualizerComponent gridVisualizerComponent;
  private boolean isMirrored = false;
  
  private static HBox toolbar;
  private WallGrid leftWallGrid;
  private WallGrid rightWallGrid;
  
  private int baseOffsetX = 0;
  private int baseOffsetY = 0;
  
  private Point2D currentDisplayOffset = new Point2D(0, 0);
  private Point2D lastGridPos = new Point2D(0, 0);
  private double xOffset = 0;
  private double yOffset = 0;
  
  
  static {
    selectionEffect = new DropShadow();
    selectionEffect.setColor(Color.BLUE);
    selectionEffect.setRadius(10);
  }
  
  public InteractiveItemComponent(IsometricGrid isometricGrid, WallGrid leftWallGrid, WallGrid rightWallGrid, GridVisualizerComponent gridVisualizerComponent) {
    this.isometricGrid = isometricGrid;
    this.leftWallGrid = leftWallGrid;
    this.rightWallGrid = rightWallGrid;
    this.gridVisualizerComponent = gridVisualizerComponent;
  }
  
  @Override
  public void onAdded() {
    entity.getViewComponent().addEventHandler(MouseEvent.MOUSE_PRESSED, this::onMousePressed);
    entity.getViewComponent().addEventHandler(MouseEvent.MOUSE_DRAGGED, this::onMouseDragged);
    entity.getViewComponent().addEventHandler(MouseEvent.MOUSE_RELEASED, this::onMouseReleased);
    
  }
  
  private void onMousePressed(MouseEvent e) {
//    dragOffset = new Point2D(e.getSceneX() - entity.getX(), e.getSceneY() - entity.getY());
    // we don't need offset
    dragOffset = new Point2D(0, 0);
    // remember drag start position
    
    e.consume();
  }
  
  private void onMouseDragged(MouseEvent e) {
    double dragDistance = new Point2D(e.getSceneX() - (entity.getX() + dragOffset.getX()),
            e.getSceneY() - (entity.getY() + dragOffset.getY())).magnitude();
    if (dragDistance > 5 || isDragging) {
      if (!isDragging) {
        EntityType type = (EntityType) entity.getType();
        if (type == EntityType.WALL_ITEM) {
          entity.setProperty("isDragging", true);
        } else {
          isometricGrid.removeItem(entity);
        }
      }
      isDragging = true;
      hideToolbar();
      gridVisualizerComponent.show();
      gridVisualizerComponent.showAllOccupiedGrids();
    }
    
    if (isDragging) {
      gridVisualizerComponent.showAllOccupiedGrids();
      double newX = e.getSceneX() - dragOffset.getX();
      double newY = e.getSceneY() - dragOffset.getY();
      
      Item item = entity.getObject("item");
      EntityType type = (EntityType) entity.getType();
      if (type == EntityType.WALL_ITEM) {
        // Handle hanging items (wall placement)
        handleHangingItemDrag(newX, newY, item);
      } else {
        // Handle floor items
        handleFloorItemDrag(newX, newY, item);
      }
      
      entity.getComponent(ZIndexComponent.class).onUpdate(0);
    }
    e.consume();
  }
  
  private void handleHangingItemDrag(double newX, double newY, Item item) {
    Point2D leftGridPos = leftWallGrid.getGridPosition(newX, newY);
    Point2D rightGridPos = rightWallGrid.getGridPosition(newX, newY);
    
    if (leftWallGrid.canPlaceItem((int) leftGridPos.getX() + item.getBaseOffsetX(), (int) leftGridPos.getY() + item.getBaseOffsetY(),
            item.getNumTileWidth(), item.getNumTileHeight())) {
      Point2D wallPos = leftWallGrid.getWallPosition((int) leftGridPos.getX() + item.getBaseOffsetX(), (int) leftGridPos.getY() + item.getBaseOffsetY());
      entity.setPosition(wallPos);
      gridVisualizerComponent.showItemBase(item, (int) leftGridPos.getX() + item.getBaseOffsetX(), (int) leftGridPos.getY() + item.getBaseOffsetY(), true, true);
    } else if (rightWallGrid.canPlaceItem((int) rightGridPos.getX() + item.getBaseOffsetX(), (int) rightGridPos.getY() + item.getBaseOffsetY(),
            item.getNumTileWidth(), item.getNumTileHeight())) {
      Point2D wallPos = rightWallGrid.getWallPosition((int) rightGridPos.getX() + item.getBaseOffsetX(), (int) rightGridPos.getY() + item.getBaseOffsetY());
      entity.setPosition(wallPos);
      gridVisualizerComponent.showItemBase(item, (int) rightGridPos.getX() + item.getBaseOffsetX(), (int) rightGridPos.getY() + item.getBaseOffsetY(), true, false);
    }
  }
  
  private void handleFloorItemDrag(double newX, double newY, Item item) {
    Point2D gridPos = isometricGrid.getGridPosition(newX, newY);
    
    if (isometricGrid.canPlaceItem((int) gridPos.getX(), (int) gridPos.getY(),
            item.getNumTileWidth(), item.getNumTileHeight())) {
      Point2D isoPos = isometricGrid.getIsometricPosition((int) gridPos.getX(), (int) gridPos.getY());
      
      double displayWidth = item.getNumTileWidth() * isometricGrid.getTileWidth();
      currentDisplayOffset = new Point2D(-displayWidth/4, -displayWidth * item.getRatio()/2);
      isoPos = isoPos.add(currentDisplayOffset);
      
      isoPos = isoPos.add(item.getXOffset(), item.getYOffset());
      lastGridPos = gridPos;
      
      entity.setPosition(isoPos);
      gridVisualizerComponent.showItemBase(item, (int) gridPos.getX() + item.getBaseOffsetX(), (int) gridPos.getY() + item.getBaseOffsetY(), false, false);
    }
  }
  
  private void onMouseReleased(MouseEvent e) {
    gridVisualizerComponent.hide();
    gridVisualizerComponent.hideItemBase();
    gridVisualizerComponent.HideAllOccupiedGrids();
    
    if (isDragging) {
      EntityType type = (EntityType) entity.getType();
      if (type == EntityType.WALL_ITEM) {
        // For wall items, determine which wall it's on and update its position
        Point2D leftGridPos = leftWallGrid.getGridPosition(entity.getX(), entity.getY());
        Point2D rightGridPos = rightWallGrid.getGridPosition(entity.getX(), entity.getY());
        
        if (leftWallGrid.canPlaceItem((int) leftGridPos.getX(), (int) leftGridPos.getY(),
                entity.getInt("itemWidth"), entity.getInt("itemLength"))) {
          Point2D wallPos = leftWallGrid.getWallPosition((int) leftGridPos.getX(), (int) leftGridPos.getY());
          entity.setPosition(wallPos);
        } else if (rightWallGrid.canPlaceItem((int) rightGridPos.getX(), (int) rightGridPos.getY(),
                entity.getInt("itemWidth"), entity.getInt("itemLength"))) {
          Point2D wallPos = rightWallGrid.getWallPosition((int) rightGridPos.getX(), (int) rightGridPos.getY());
          entity.setPosition(wallPos);
        }
        entity.setProperty("isDragging", false);
      } else {
        // For floor items (existing code)
        Point2D gridPos = isometricGrid.getGridPosition(entity.getX(), entity.getY());
        
        Item item = entity.getObject("item");
        
        double displayWidth = item.getNumTileWidth()* isometricGrid.getTileWidth() ;
        
        Point2D isoPos = isometricGrid.getIsometricPosition((int) lastGridPos.getX(), (int) lastGridPos.getY());
        isoPos = isoPos.add(item.getXOffset(), item.getYOffset());
        isometricGrid.placeEntity(entity, (int) lastGridPos.getX(), (int) lastGridPos.getY(),
                entity.getInt("itemWidth"), entity.getInt("itemLength"),currentDisplayOffset);
        
        
      }
      notifyMaterialUpdateListeners();
    } else {
      if (selectedEntity != entity) {
        deselectCurrent();
        selectThis();
      } else {
        deselectCurrent();
      }
    }
    
    isDragging = false;
    e.consume();
  }
  
  private Button createButton(String text, FontIcon icon) {
    Button button = new Button(text, icon);
    button.setStyle("-fx-background-color: white; -fx-text-fill: black; -fx-border-color: black; -fx-border-radius: 3;");
    button.setOnMouseEntered(e -> button.setStyle("-fx-background-color: lightgray; -fx-text-fill: black; -fx-border-color: black; -fx-border-radius: 3;"));
    button.setOnMouseExited(e -> button.setStyle("-fx-background-color: white; -fx-text-fill: black; -fx-border-color: black; -fx-border-radius: 3;"));
    return button;
  }
  
  private void showToolbar() {
    hideToolbar(); // Ensure any existing toolbar is removed
    
    toolbar = new HBox(5);
    toolbar.setStyle("-fx-background-color: rgba(200, 200, 200, 0.7); -fx-padding: 5; -fx-background-radius: 5;");
    
    Item currentItem = entity.getObject("item");
    List<Item> variants = getItemVariants(currentItem);
    
    for (Item variant : variants) {
      if (!variant.getFilename().equals(currentItem.getFilename())) {
        Button variantBtn = createVariantButton(variant);
        toolbar.getChildren().add(variantBtn);
      }
    }
    
    Button mirrorBtn = createButton("", new FontIcon(FontAwesomeSolid.EXCHANGE_ALT));
    Button removeBtn = createButton("", new FontIcon(FontAwesomeSolid.TRASH));
    Button adjustBtn = createButton("", new FontIcon(FontAwesomeSolid.SLIDERS_H));
    
    removeBtn.setOnAction(e -> {
      entity.removeFromWorld();
      isometricGrid.removeItem(entity);
      hideToolbar();
      notifyMaterialUpdateListeners();
    });
    
    mirrorBtn.setOnAction(e -> {
      mirror();
    });
    
    adjustBtn.setOnAction(e -> {
      ItemAdjustmentDialog dialog = new ItemAdjustmentDialog(entity, gridVisualizerComponent,isometricGrid);
      Optional<ButtonType> result = dialog.showAndWait();
      if (result.isPresent() && result.get() == ButtonType.OK) {
        updateItemPosition();
      }
    });
    
    toolbar.getChildren().addAll(mirrorBtn, adjustBtn, removeBtn);
    
    Button adjustBaseBtn = createButton("", new FontIcon(FontAwesomeSolid.ARROWS_ALT));
    adjustBaseBtn.setOnAction(e -> {
      showBaseAdjustmentDialog();
    });
    
    toolbar.getChildren().add(adjustBaseBtn);
    
    updateToolbarPosition();
    FXGL.addUINode(toolbar);
  }
  
  private void showBaseAdjustmentDialog() {
    ItemAdjustmentDialog dialog = new ItemAdjustmentDialog(entity, gridVisualizerComponent, isometricGrid);
    Optional<ButtonType> result = dialog.showAndWait();
    if (result.isPresent() && result.get() == ButtonType.OK) {
      Item item = entity.getObject("item");
      item.setXOffset(dialog.getXOffset());
      item.setYOffset(dialog.getYOffset());
      updateItemPosition();
    }
  }
  
  private void updateItemPosition() {
    if (entity.getType() == EntityType.FLOOR_ITEM) {
      Item item = entity.getObject("item");
      Point2D gridPos = isometricGrid.getGridPosition(entity.getX(), entity.getY());
      Point2D isoPos = isometricGrid.getIsometricPosition((int) gridPos.getX() , (int) gridPos.getY() );
      isoPos = isoPos.add(item.getXOffset(), item.getYOffset());
      isometricGrid.placeEntity(entity, (int) gridPos.getX(), (int) gridPos.getY());
    } else if (entity.getType() == EntityType.WALL_ITEM) {
      Point2D leftGridPos = leftWallGrid.getGridPosition(entity.getX(), entity.getY());
      Point2D rightGridPos = rightWallGrid.getGridPosition(entity.getX(), entity.getY());
      
      if (leftWallGrid.canPlaceItem((int) leftGridPos.getX() + baseOffsetX, (int) leftGridPos.getY() + baseOffsetY,
              entity.getInt("itemWidth"), entity.getInt("itemLength"))) {
        Point2D wallPos = leftWallGrid.getWallPosition((int) leftGridPos.getX() + baseOffsetX, (int) leftGridPos.getY() + baseOffsetY);
        entity.setPosition(wallPos);
      } else if (rightWallGrid.canPlaceItem((int) rightGridPos.getX() + baseOffsetX, (int) rightGridPos.getY() + baseOffsetY,
              entity.getInt("itemWidth"), entity.getInt("itemLength"))) {
        Point2D wallPos = rightWallGrid.getWallPosition((int) rightGridPos.getX() + baseOffsetX, (int) rightGridPos.getY() + baseOffsetY);
        entity.setPosition(wallPos);
      }
    }
    entity.getComponent(ZIndexComponent.class).onUpdate(0);
  }
  
  private Button createVariantButton(Item variant) {
    ImageView imageView = new ImageView(ResourceManager.getImage(variant.getImageName()));
    imageView.setFitWidth(20);
    imageView.setFitHeight(20);
    imageView.setPreserveRatio(true);
    
    Button variantBtn = new Button("", imageView);
    variantBtn.setStyle("-fx-background-color: white; -fx-padding: 2;");
    variantBtn.setOnAction(e -> {
      changeVariant(variant);
    });
    
    return variantBtn;
  }
  
  private List<Item> getItemVariants(Item currentItem) {
    String baseName = currentItem.getName().split("·")[0];
    return ResourceManager.getItemsByBaseName(baseName);
  }
  
  public void changeVariant(Item newVariant) {
    Item currentItem = entity.getObject("item");
    entity.setProperty("item", newVariant);
    
    Texture currentTexture = (Texture) entity.getViewComponent().getChildren().get(0);
    double entityWidth = entity.getWidth();
    double entityHeight = entity.getHeight();
    
    Texture newTexture = new Texture(ResourceManager.getImage(newVariant.getImageName()));
    newTexture.setFitWidth(entityWidth);
    newTexture.setFitHeight(entityHeight);
    newTexture.setPreserveRatio(false);  // This ensures the image fits exactly to the specified dimensions
    
    entity.getViewComponent().clearChildren();
    entity.getViewComponent().addChild(newTexture);
    
    if (isMirrored) {
      newTexture.setScaleX(-1);
    }
    
    notifyMaterialUpdateListeners();
    
    // Update the toolbar to reflect the new current item
    showToolbar();
  }
  
  private void selectThis() {
    selectedEntity = entity;
    applySelectionEffect(entity);
    showToolbar();
    notifySelectionListeners();
    notifyGlobalSelectionListeners();
  }
  
  private void deselectCurrent() {
    if (selectedEntity != null) {
      clearSelectionEffect(selectedEntity);
      hideToolbar();
      selectedEntity = null;
      notifyGlobalSelectionListeners();
    }
  }
  
  public static void deselectAll() {
    if (selectedEntity != null) {
      clearSelectionEffect(selectedEntity);
      hideToolbar();
      selectedEntity = null;
      notifyGlobalSelectionListeners();
    }
  }
  
  private void updateToolbarPosition() {
    if (toolbar != null) {
      toolbar.setTranslateX(entity.getX() - toolbar.getWidth() / 2 + entity.getWidth() / 2);
      toolbar.setTranslateY(entity.getY() - toolbar.getHeight() - 10);
    }
  }
  
  private static void hideToolbar() {
    if (toolbar != null) {
      FXGL.removeUINode(toolbar);
      toolbar = null;
    }
  }
  
  public void mirror() {

    isMirrored = !isMirrored;
    // change entity's "itemWidth" and "itemLength" properties
    int temp = entity.getInt("itemWidth");
    entity.setProperty("itemWidth", entity.getInt("itemLength"));
    entity.setProperty("itemLength", temp);
    Texture texture = (Texture) entity.getViewComponent().getChildren().get(0);
    texture.setScaleX(isMirrored ? -1 : 1);

  }
  
  public boolean isMirrored() {
    return isMirrored;
  }
  
  private void applySelectionEffect(Entity entity) {
    Texture texture = (Texture) entity.getViewComponent().getChildren().get(0);
    texture.setEffect(selectionEffect);
  }
  
  private static void clearSelectionEffect(Entity entity) {
    if (entity != null && entity.isActive()) {
      entity.getViewComponent().getChildren().stream()
              .filter(node -> node instanceof Texture)
              .findFirst()
              .ifPresent(texture -> ((Texture) texture).setEffect(null));
    }
  }
  
  public static Entity getSelectedEntity() {
    return selectedEntity;
  }
  
  private void notifySelectionListeners() {
    selectionListeners.forEach(Runnable::run);
  }
  
  public static void addMaterialUpdateListener(Runnable listener) {
    materialUpdateListeners.add(listener);
  }
  
  private static void notifyMaterialUpdateListeners() {
    materialUpdateListeners.forEach(Runnable::run);
  }
  
  public static void addGlobalSelectionListener(Runnable listener) {
    globalSelectionListeners.add(listener);
  }
  
  private static void notifyGlobalSelectionListeners() {
    globalSelectionListeners.forEach(Runnable::run);
  }
}