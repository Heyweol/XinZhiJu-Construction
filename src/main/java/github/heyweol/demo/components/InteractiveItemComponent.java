package github.heyweol.demo.components;

import com.almasb.fxgl.entity.component.Component;
import com.almasb.fxgl.entity.Entity;
import com.almasb.fxgl.texture.Texture;
import github.heyweol.demo.IsometricGrid;
import javafx.animation.PauseTransition;
import javafx.geometry.Point2D;
import javafx.scene.effect.DropShadow;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.util.Duration;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;


public class InteractiveItemComponent extends Component {
  private static Entity selectedEntity = null;
  private static DropShadow selectionEffect;
  private PauseTransition longPressTransition;
  private boolean isLongPress = false;
  private Point2D dragOffset;
  private boolean isDragging = false;
  
  private static List<Runnable> globalSelectionListeners = new ArrayList<>();
  private List<Runnable> selectionListeners = new ArrayList<>();
  private List<Consumer<Boolean>> longPressListeners = new ArrayList<>();
  
  private IsometricGrid isometricGrid;
  private GridVisualizerComponent gridVisualizerComponent;
  
  static {
    selectionEffect = new DropShadow();
    selectionEffect.setColor(Color.BLUE);
    selectionEffect.setRadius(10);
  }
  
  
  public InteractiveItemComponent(IsometricGrid grid, GridVisualizerComponent gridVisualizerComponent) {
    this.isometricGrid = grid;
    this.gridVisualizerComponent = gridVisualizerComponent;
  }
  
  @Override
  public void onAdded() {
    longPressTransition = new PauseTransition(Duration.seconds(0.5));
    longPressTransition.setOnFinished(event -> {
      isLongPress = true;
      isDragging = true;
      notifyLongPressListeners(true);
    });
    
    entity.getViewComponent().addEventHandler(MouseEvent.MOUSE_PRESSED, this::onMousePressed);
    entity.getViewComponent().addEventHandler(MouseEvent.MOUSE_DRAGGED, this::onMouseDragged);
    entity.getViewComponent().addEventHandler(MouseEvent.MOUSE_RELEASED, this::onMouseReleased);
  }
  
  private void onMousePressed(MouseEvent e) {
    longPressTransition.playFromStart();
    dragOffset = new Point2D(e.getSceneX() - entity.getX(), e.getSceneY() - entity.getY());
    gridVisualizerComponent.show();
    e.consume();
  }
  
  private void onMouseDragged(MouseEvent e) {
    if (isDragging) {
      double newX = e.getSceneX();
      double newY = e.getSceneY();
      Point2D gridPos = isometricGrid.getGridPosition(newX, newY);
      if (isometricGrid.canPlaceItem((int) gridPos.getX(), (int) gridPos.getY(),
              entity.getInt("itemWidth"), entity.getInt("itemLength"))) {
        Point2D isoPos = isometricGrid.getIsometricPosition((int) gridPos.getX(), (int) gridPos.getY());
        entity.setPosition(isoPos);
        
        // Trigger z-index update
        entity.getComponent(ZIndexComponent.class).onUpdate(0);
      }
    }
    e.consume();
  }
  
  private void onMouseReleased(MouseEvent e) {
    longPressTransition.stop();
    gridVisualizerComponent.hide();
    
    if (isLongPress) {
      notifyLongPressListeners(false);
      // Update the item's position in the grid
      Point2D gridPos = isometricGrid.getGridPosition(entity.getX(), entity.getY());
      isometricGrid.removeItem(entity);
      isometricGrid.placeItem(entity, (int) gridPos.getX(), (int) gridPos.getY(),
              entity.getInt("itemWidth"), entity.getInt("itemLength"));
    } else {
      // Handle click (selection)
      if (selectedEntity != entity) {
        deselectCurrent();
        selectThis();
      } else {
        deselectCurrent();
      }
    }
    
    isLongPress = false;
    isDragging = false;
    e.consume();
  }
  
  private void selectThis() {
    selectedEntity = entity;
    applySelectionEffect(entity);
    notifySelectionListeners();
    notifyGlobalSelectionListeners();
  }
  
  private void deselectCurrent() {
    if (selectedEntity != null) {
      clearSelectionEffect(selectedEntity);
      selectedEntity = null;
      notifyGlobalSelectionListeners();
    }
  }
  
  public static void deselectAll() {
    if (selectedEntity != null) {
      clearSelectionEffect(selectedEntity);
      selectedEntity = null;
      notifyGlobalSelectionListeners();
    }
  }
  
  private void applySelectionEffect(Entity entity) {
    Texture texture = (Texture) entity.getViewComponent().getChildren().get(0);
    texture.setEffect(selectionEffect);
  }
  
  private static void clearSelectionEffect(Entity entity) {
    Texture texture = (Texture) entity.getViewComponent().getChildren().get(0);
    texture.setEffect(null);
  }
  
  public static Entity getSelectedEntity() {
    return selectedEntity;
  }
  
  public static void clearSelection() {
    if (selectedEntity != null) {
      clearSelectionEffect(selectedEntity);
      selectedEntity = null;
    }
  }
  
  public void addSelectionListener(Runnable listener) {
    selectionListeners.add(listener);
  }
  
  private void notifySelectionListeners() {
    selectionListeners.forEach(Runnable::run);
  }
  
  public void addLongPressListener(Consumer<Boolean> listener) {
    longPressListeners.add(listener);
  }
  
  private void notifyLongPressListeners(boolean isLongPressActive) {
    longPressListeners.forEach(listener -> listener.accept(isLongPressActive));
  }
  
  
  
  public static void addGlobalSelectionListener(Runnable listener) {
    globalSelectionListeners.add(listener);
  }
  
  private static void notifyGlobalSelectionListeners() {
    globalSelectionListeners.forEach(Runnable::run);
  }
}