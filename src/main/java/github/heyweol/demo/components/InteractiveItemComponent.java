package github.heyweol.demo.components;

import com.almasb.fxgl.dsl.FXGL;
import com.almasb.fxgl.entity.component.Component;
import com.almasb.fxgl.entity.Entity;
import com.almasb.fxgl.texture.Texture;
import github.heyweol.demo.EntityType;
import github.heyweol.demo.IsometricGrid;
import github.heyweol.demo.Item;
import github.heyweol.demo.WallGrid;
import github.heyweol.demo.utils.ResourceManager;
import javafx.geometry.Point2D;
import javafx.scene.control.Button;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import org.kordamp.ikonli.fontawesome5.FontAwesomeSolid;
import org.kordamp.ikonli.javafx.FontIcon;

import java.util.ArrayList;
import java.util.List;

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
    dragOffset = new Point2D(e.getSceneX() - entity.getX(), e.getSceneY() - entity.getY());
    e.consume();
  }
  
  private void onMouseDragged(MouseEvent e) {
    double dragDistance = new Point2D(e.getSceneX() - (entity.getX() + dragOffset.getX()),
            e.getSceneY() - (entity.getY() + dragOffset.getY())).magnitude();
    if (dragDistance > 5 || isDragging) {
      if (!isDragging) {
        // Remove the item from the appropriate grid when dragging starts
        EntityType type = (EntityType) entity.getType();
        if (type == EntityType.HANGING) {
          // For wall items, we don't remove them from a grid
          // Instead, we might want to mark them as being dragged
          entity.setProperty("isDragging", true);
        } else {
          isometricGrid.removeItem(entity);
        }
      }
      isDragging = true;
      hideToolbar();
      gridVisualizerComponent.show();
    }
    
    if (isDragging) {
      double newX = e.getSceneX() - dragOffset.getX();
      double newY = e.getSceneY() - dragOffset.getY();
      
      EntityType type = (EntityType) entity.getType();
      if (type == EntityType.HANGING) {
        // Handle hanging items (wall placement)
        Point2D leftGridPos = leftWallGrid.getGridPosition(newX, newY);
        Point2D rightGridPos = rightWallGrid.getGridPosition(newX, newY);
        
        if (leftWallGrid.canPlaceItem((int) leftGridPos.getX(), (int) leftGridPos.getY(),
                entity.getInt("itemWidth"), entity.getInt("itemLength"))) {
          Point2D wallPos = leftWallGrid.getWallPosition((int) leftGridPos.getX(), (int) leftGridPos.getY());
          entity.setPosition(wallPos);
        } else if (rightWallGrid.canPlaceItem((int) rightGridPos.getX(), (int) rightGridPos.getY(),
                entity.getInt("itemWidth"), entity.getInt("itemLength"))) {
          Point2D wallPos = rightWallGrid.getWallPosition((int) rightGridPos.getX(), (int) rightGridPos.getY());
          entity.setPosition(wallPos);
        }
      } else {
        // Handle floor items (existing code)
        Point2D gridPos = isometricGrid.getGridPosition(newX, newY);
        if (isometricGrid.canPlaceItem((int) gridPos.getX(), (int) gridPos.getY(),
                entity.getInt("itemWidth"), entity.getInt("itemLength"))) {
          Point2D isoPos = isometricGrid.getIsometricPosition((int) gridPos.getX(), (int) gridPos.getY());
          entity.setPosition(isoPos);
        }
      }
      
      entity.getComponent(ZIndexComponent.class).onUpdate(0);
    }
    e.consume();
  }
  
  private void onMouseReleased(MouseEvent e) {
    gridVisualizerComponent.hide();
    
    if (isDragging) {
      EntityType type = (EntityType) entity.getType();
      if (type == EntityType.HANGING) {
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
        isometricGrid.placeItem(entity, (int) gridPos.getX(), (int) gridPos.getY(),
                entity.getInt("itemWidth"), entity.getInt("itemLength"));
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
    
    removeBtn.setOnAction(e -> {
      entity.removeFromWorld();
      isometricGrid.removeItem(entity);
      hideToolbar();
      notifyMaterialUpdateListeners();
    });
    
    mirrorBtn.setOnAction(e -> {
      mirror();
    });
    
    toolbar.getChildren().addAll(mirrorBtn, removeBtn);
    
    updateToolbarPosition();
    FXGL.addUINode(toolbar);
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
  
  public static void clearSelection() {
    if (selectedEntity != null) {
      clearSelectionEffect(selectedEntity);
      hideToolbar();
      selectedEntity = null;
      notifyGlobalSelectionListeners();
      notifyMaterialUpdateListeners();
    }
  }
  
  public void addSelectionListener(Runnable listener) {
    selectionListeners.add(listener);
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