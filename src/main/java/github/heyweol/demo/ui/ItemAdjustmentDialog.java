package github.heyweol.demo.ui;

import com.almasb.fxgl.dsl.FXGL;
import com.almasb.fxgl.entity.Entity;

import github.heyweol.demo.EntityType;
import github.heyweol.demo.IsometricGrid;
import github.heyweol.demo.Item;
import github.heyweol.demo.components.GridVisualizerComponent;
import javafx.geometry.Insets;
import javafx.geometry.Point2D;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.stage.Modality;
import javafx.stage.StageStyle;

import java.util.Optional;

public class ItemAdjustmentDialog extends Dialog<ButtonType> {
  private Slider xOffsetSlider;
  private Slider yOffsetSlider;
  private Slider scaleSlider;
  private Entity entity;
  private Item item;
  private double originalX;
  private double originalY;
  private double originalScale;
  private double originalXOffset;
  private double originalYOffset;
  private GridVisualizerComponent gridVisualizerComponent;
  private IsometricGrid isometricGrid;
  private Label xOffsetLabel;
  private Label yOffsetLabel;
  private Label scaleLabel;
  private Point2D textureOffset;
  private Point2D lastGridPos;
  
  public ItemAdjustmentDialog(Entity entity, GridVisualizerComponent gridVisualizer, IsometricGrid isometricGrid,Point2D lastGridPos) {
    this.entity = entity;
    this.item = entity.getObject("item");
    this.originalX = entity.getX();
    this.originalY = entity.getY();
    this.originalScale = item.getScale();
    this.originalXOffset = item.getXOffset();
    this.originalYOffset = item.getYOffset();
    this.gridVisualizerComponent = gridVisualizer;
    this.isometricGrid = isometricGrid;
    this.textureOffset = new Point2D(-entity.getDouble("textureFitWidth")/2,-entity.getDouble("textureFitHeight"));
    this.lastGridPos = lastGridPos;
    
//    Point2D gridPos = isometricGrid.getGridPosition(entity.getX(), entity.getY());
//    Point2D isoPos = isometricGrid.getIsometricPosition((int)gridPos.getX(), (int)gridPos.getY());
//    isoPos = isoPos.add(item.getXOffset(), item.getYOffset());
//    entity.setPosition(isoPos);

    setTitle("Adjust Item");
    setHeaderText("Fine-tune item position and size");
    
    initModality(Modality.APPLICATION_MODAL);
    initStyle(StageStyle.UTILITY);
    initOwner(FXGL.getGameScene().getRoot().getScene().getWindow());
    
    xOffsetSlider = createSlider("X Offset", -80, 80, 0);
    yOffsetSlider = createSlider("Y Offset", -80, 80, 0);
    scaleSlider = createSlider("Scale", 0.5, 1.5, 1);
    
    xOffsetLabel = new Label(String.format("%.2f", xOffsetSlider.getValue()));
    yOffsetLabel = new Label(String.format("%.2f", yOffsetSlider.getValue()));
    scaleLabel = new Label(String.format("%.2f", scaleSlider.getValue()));
    
    GridPane grid = new GridPane();
    grid.setHgap(10);
    grid.setVgap(10);
    grid.setPadding(new Insets(20, 20, 10, 10));
    
    grid.add(new Label("X Offset:"), 0, 0);
    grid.add(xOffsetSlider, 1, 0);
    grid.add(xOffsetLabel, 2, 0);
    grid.add(new Label("Y Offset:"), 0, 1);
    grid.add(yOffsetSlider, 1, 1);
    grid.add(yOffsetLabel, 2, 1);
    grid.add(new Label("Scale:"), 0, 2);
    grid.add(scaleSlider, 1, 2);
    grid.add(scaleLabel, 2, 2);
    
    getDialogPane().setContent(grid);
    getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
    
    setResultConverter(buttonType -> {
      if (buttonType == ButtonType.OK) {
        double newXOffset = xOffsetSlider.getValue() + item.getXOffset();
        double newYOffset = yOffsetSlider.getValue() + item.getYOffset();
        double newScale = scaleSlider.getValue();
        
        item.setXOffset(newXOffset);
        item.setYOffset(newYOffset);
        item.setScale(newScale * originalScale);
        
        
        showApplyToAllVariantsDialog(newXOffset, newYOffset, newScale*originalScale);
        
        return ButtonType.OK;
      }
      resetItemPosition();
      return ButtonType.CANCEL;
    });
    
    xOffsetSlider.valueProperty().addListener((obs, oldVal, newVal) -> {
      xOffsetLabel.setText(String.format("%.1f", newVal.doubleValue()));
      updateItemPosition();
    });
    yOffsetSlider.valueProperty().addListener((obs, oldVal, newVal) -> {
      yOffsetLabel.setText(String.format("%.1f", newVal.doubleValue()));
      updateItemPosition();
    });
    scaleSlider.valueProperty().addListener((obs, oldVal, newVal) -> {
      scaleLabel.setText(String.format("%.1f", newVal.doubleValue()));
      updateItemScale();
    });
    
    setOnShowing(event -> {
      gridVisualizer.show();
      gridVisualizer.showItemBase(item, (int) entity.getX(), (int) entity.getY(), entity.getType() == EntityType.WALL_ITEM, false);
      positionDialogLeftOfGameScene();
    });
    setOnHiding(event -> {
      gridVisualizer.hide();
      gridVisualizer.hideItemBase();
    });
    
  }
  
  private void showApplyToAllVariantsDialog(double newXOffset, double newYOffset, double newScale) {
    Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
    alert.setTitle("Apply to All Variants");
    alert.setHeaderText("Apply changes to all variants?");
    alert.setContentText("Do you want to apply these adjustments to all variants of this item?");
    
    Optional<ButtonType> result = alert.showAndWait();
    if (result.isPresent() && result.get() == ButtonType.OK) {
      String baseName = item.getName().split("·")[0];
      applyChangesToAllVariants(baseName, newXOffset, newYOffset, newScale);
    }
  }
  
  private void applyChangesToAllVariants(String baseName, double newXOffset, double newYOffset, double newScale) {
    FXGL.getGameWorld().getEntitiesByType(EntityType.FLOOR_ITEM, EntityType.WALL_ITEM)
            .stream()
            .filter(e -> {
              Item item = e.getObject("item");
              return item.getName().split("·")[0].equals(baseName);
            })
            .forEach(e -> {
              Item item = e.getObject("item");
              item.setXOffset(newXOffset);
              item.setYOffset(newYOffset);
              item.setScale(newScale);
            });
  }
  
  private Slider createSlider(String name, double min, double max, double initial) {
    Slider slider = new Slider(min, max, initial);
    slider.setShowTickLabels(true);
    slider.setShowTickMarks(true);
    return slider;
  }
  
  private void updateItemPosition() {
    double xOffset = xOffsetSlider.getValue();
    double yOffset = yOffsetSlider.getValue();
    // item.setXOffset(xOffset);
    // item.setYOffset(yOffset);

    Point2D gridPos = isometricGrid.getGridPosition(originalX, originalY);
    gridPos = lastGridPos;
    
    Point2D isoPos = isometricGrid.getIsometricPosition((int)gridPos.getX(), (int)gridPos.getY());
    isoPos = isoPos.add(xOffset, yOffset);
    isoPos = isoPos.add(item.getXOffset(), item.getYOffset());
    isoPos = isoPos.add(textureOffset);
    entity.setPosition(isoPos);
    
    gridVisualizerComponent.showItemBase(item, (int) entity.getX(), (int) entity.getY(), entity.getType() == EntityType.WALL_ITEM, false);
  }
  
  private void updateItemScale() {
    double scale = scaleSlider.getValue();
    entity.setScaleX(scale);
    entity.setScaleY(scale);
    Item item = entity.getObject("item");
    item.setScale(scale);
    gridVisualizerComponent.showItemBase(item, (int) entity.getX(), (int) entity.getY(), entity.getType() == EntityType.WALL_ITEM, false);
  }
  
  private void resetItemPosition() {
    entity.setX(originalX);
    entity.setY(originalY);
    item.setScale(originalScale);
    item.setXOffset(originalXOffset);
    item.setYOffset(originalYOffset);
  }
  
  public double getXOffset() {
    return originalXOffset + xOffsetSlider.getValue();
  }
  
  public double getYOffset() {
    return originalYOffset + yOffsetSlider.getValue();
  }
  
  private void positionDialogLeftOfGameScene() {
    setX(FXGL.getAppWidth() * 0.05);
    setY(FXGL.getAppHeight() * 0.5 - getHeight() * 0.5);
  }
}