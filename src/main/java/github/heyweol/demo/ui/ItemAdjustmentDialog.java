package github.heyweol.demo.ui;

import com.almasb.fxgl.dsl.FXGL;
import com.almasb.fxgl.entity.Entity;

import github.heyweol.demo.EntityType;
import github.heyweol.demo.IsometricGrid;
import github.heyweol.demo.Item;
import github.heyweol.demo.components.GridVisualizerComponent;
import javafx.geometry.Insets;
import javafx.geometry.Point2D;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.layout.GridPane;
import javafx.stage.Modality;
import javafx.stage.StageStyle;

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
  
  public ItemAdjustmentDialog(Entity entity, GridVisualizerComponent gridVisualizer, IsometricGrid isometricGrid) {
    this.entity = entity;
    this.item = entity.getObject("item");
    this.originalX = entity.getX();
    this.originalY = entity.getY();
    this.originalScale = entity.getScaleX();
    this.originalXOffset = item.getXOffset();
    this.originalYOffset = item.getYOffset();
    this.gridVisualizerComponent = gridVisualizer;
    this.isometricGrid = isometricGrid;
    
    setTitle("Adjust Item");
    setHeaderText("Fine-tune item position and size");
    
    initModality(Modality.APPLICATION_MODAL);
    initStyle(StageStyle.UTILITY);
    initOwner(FXGL.getGameScene().getRoot().getScene().getWindow());
    
    xOffsetSlider = createSlider("X Offset", -50, 50, 0);
    yOffsetSlider = createSlider("Y Offset", -50, 50, 0);
    scaleSlider = createSlider("Scale", 0.5, 1.5, 1);
    
    GridPane grid = new GridPane();
    grid.setHgap(10);
    grid.setVgap(10);
    grid.setPadding(new Insets(20, 20, 10, 10));
    
    grid.add(new Label("X Offset:"), 0, 0);
    grid.add(xOffsetSlider, 1, 0);
    grid.add(new Label("Y Offset:"), 0, 1);
    grid.add(yOffsetSlider, 1, 1);
    grid.add(new Label("Scale:"), 0, 2);
    grid.add(scaleSlider, 1, 2);
    
    getDialogPane().setContent(grid);
    getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
    
    setResultConverter(buttonType -> {
      if (buttonType == ButtonType.OK) {
        item.setXOffset(getXOffset());
        item.setYOffset(getYOffset());
        item.setScale(scaleSlider.getValue());
        return ButtonType.OK;
      }
      resetItemPosition();
      return ButtonType.CANCEL;
    });
    
    xOffsetSlider.valueProperty().addListener((obs, oldVal, newVal) -> updateItemPosition());
    yOffsetSlider.valueProperty().addListener((obs, oldVal, newVal) -> updateItemPosition());
    scaleSlider.valueProperty().addListener((obs, oldVal, newVal) -> updateItemScale());
    
    setOnShowing(event -> {
      gridVisualizer.show();
      gridVisualizer.showItemBase(item, (int) entity.getX(), (int) entity.getY(), entity.getType() == EntityType.HANGING, false);
      positionDialogLeftOfGameScene();
    });
    setOnHiding(event -> {
      gridVisualizer.hide();
      gridVisualizer.hideItemBase();
    });
  }
  
  private Slider createSlider(String name, double min, double max, double initial) {
    Slider slider = new Slider(min, max, initial);
    slider.setShowTickLabels(true);
    slider.setShowTickMarks(true);
    return slider;
  }
  
  private void updateItemPosition() {
    double xOffset = originalXOffset + xOffsetSlider.getValue();
    double yOffset = originalYOffset + yOffsetSlider.getValue();
    item.setXOffset(xOffset);
    item.setYOffset(yOffset);
    
    Point2D gridPos = isometricGrid.getGridPosition(originalX, originalY);
    Point2D isoPos = isometricGrid.getIsometricPosition((int)gridPos.getX(), (int)gridPos.getY());
    isoPos = isoPos.add(xOffset, yOffset);
    entity.setPosition(isoPos);
    
    gridVisualizerComponent.showItemBase(item, (int) entity.getX(), (int) entity.getY(), entity.getType() == EntityType.HANGING, false);
  }
  
  private void updateItemScale() {
    double scale = scaleSlider.getValue();
    entity.setScaleX(scale);
    entity.setScaleY(scale);
    gridVisualizerComponent.showItemBase(item, (int) entity.getX(), (int) entity.getY(), entity.getType() == EntityType.HANGING, false);
  }
  
  private void resetItemPosition() {
    entity.setX(originalX);
    entity.setY(originalY);
    entity.setScaleX(originalScale);
    entity.setScaleY(originalScale);
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