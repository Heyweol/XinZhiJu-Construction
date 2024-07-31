package github.heyweol.demo.ui;

import com.almasb.fxgl.dsl.FXGL;
import com.almasb.fxgl.entity.Entity;
import github.heyweol.demo.EntityType;
import github.heyweol.demo.Item;
import github.heyweol.demo.components.GridVisualizerComponent;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.stage.Modality;

import java.util.Optional;

public class ItemAdjustmentDialog extends Dialog<ButtonType> {
  private Slider xSlider;
  private Slider ySlider;
  private Slider scaleSlider;
  private Spinner<Integer> baseOffsetXSpinner;
  private Spinner<Integer> baseOffsetYSpinner;
  private Entity entity;
  private Item item;
  private double originalX;
  private double originalY;
  private double originalScale;
  private int originalBaseOffsetX;
  private int originalBaseOffsetY;
  private GridVisualizerComponent gridVisualizerComponent;
  
  public ItemAdjustmentDialog(Entity entity, GridVisualizerComponent gridVisualizer) {
    this.entity = entity;
    this.item = entity.getObject("item");
    this.gridVisualizerComponent = gridVisualizer;
    this.originalX = entity.getX();
    this.originalY = entity.getY();
    this.originalScale = entity.getScaleX();
    this.originalBaseOffsetX = item.getBaseOffsetX();
    this.originalBaseOffsetY = item.getBaseOffsetY();
    
    setTitle("Adjust Item");
    setHeaderText("Fine-tune item position, size, and base");
    
    initModality(Modality.APPLICATION_MODAL);
    initOwner(FXGL.getGameScene().getRoot().getScene().getWindow());
    
    xSlider = createSlider("X Position", -30, 30, 0);
    ySlider = createSlider("Y Position", -30, 30, 0);
    scaleSlider = createSlider("Scale", 0.5, 1.5, 1);
    baseOffsetXSpinner = createSpinner("Base Offset X", -5, 5, item.getBaseOffsetX());
    baseOffsetYSpinner = createSpinner("Base Offset Y", -5, 5, item.getBaseOffsetY());
    
    GridPane grid = new GridPane();
    grid.setHgap(10);
    grid.setVgap(10);
    grid.setPadding(new Insets(20, 150, 10, 10));
    
    grid.add(new Label("X Position:"), 0, 0);
    grid.add(xSlider, 1, 0);
    grid.add(new Label("Y Position:"), 0, 1);
    grid.add(ySlider, 1, 1);
    grid.add(new Label("Scale:"), 0, 2);
    grid.add(scaleSlider, 1, 2);
    grid.add(new Label("Base Offset X:"), 0, 3);
    grid.add(baseOffsetXSpinner, 1, 3);
    grid.add(new Label("Base Offset Y:"), 0, 4);
    grid.add(baseOffsetYSpinner, 1, 4);
    
    getDialogPane().setContent(grid);
    getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
    
    setResultConverter(buttonType -> {
      if (buttonType == ButtonType.OK) {
        return ButtonType.OK;
      }
      resetItemPosition();
      return ButtonType.CANCEL;
    });
    
    xSlider.valueProperty().addListener((obs, oldVal, newVal) -> updateItemPosition());
    ySlider.valueProperty().addListener((obs, oldVal, newVal) -> updateItemPosition());
    scaleSlider.valueProperty().addListener((obs, oldVal, newVal) -> updateItemScale());
    baseOffsetXSpinner.valueProperty().addListener((obs, oldVal, newVal) -> updateItemBase());
    baseOffsetYSpinner.valueProperty().addListener((obs, oldVal, newVal) -> updateItemBase());
    
    setOnShowing(event -> gridVisualizer.show());
    setOnHiding(event -> gridVisualizer.hide());
  }
  
  private Slider createSlider(String name, double min, double max, double initial) {
    Slider slider = new Slider(min, max, initial);
    slider.setShowTickLabels(true);
    slider.setShowTickMarks(true);
    return slider;
  }
  
  private Spinner<Integer> createSpinner(String name, int min, int max, int initial) {
    Spinner<Integer> spinner = new Spinner<>(min, max, initial);
    spinner.setEditable(true);
    return spinner;
  }
  
  private void updateItemPosition() {
    entity.setX(originalX + xSlider.getValue());
    entity.setY(originalY + ySlider.getValue());
    updateItemBase();
  }
  
  private void updateItemScale() {
    entity.setScaleX(scaleSlider.getValue());
    entity.setScaleY(scaleSlider.getValue());
  }
  
//  private void updateItemBase() {
//    int newBaseOffsetX = baseOffsetXSpinner.getValue();
//    int newBaseOffsetY = baseOffsetYSpinner.getValue();
//    item.setBaseOffsetX(newBaseOffsetX);
//    item.setBaseOffsetY(newBaseOffsetY);
//
//    // Update the grid visualizer to show the new base
//    boolean isHanging = entity.getType() == EntityType.HANGING;
//    boolean isLeftWall = isHanging && entity.getBoolean("isLeftWall");
//    gridVisualizer.showItemBase(item,
//            (int)entity.getX() + newBaseOffsetX,
//            (int)entity.getY() + newBaseOffsetY,
//            isHanging,
//            isLeftWall);
//  }
  
  private void updateItemBase() {
    item.setBaseOffsetX(baseOffsetXSpinner.getValue());
    item.setBaseOffsetY(baseOffsetYSpinner.getValue());
    
    boolean isHanging = entity.getType() == EntityType.HANGING;
    boolean isLeftWall = isHanging && entity.getBoolean("isLeftWall");
    
    gridVisualizerComponent.showItemBase(item, (int) entity.getX(), (int) entity.getY(), isHanging, isLeftWall);
  }
  
  private void resetItemPosition() {
    entity.setX(originalX);
    entity.setY(originalY);
    entity.setScaleX(originalScale);
    entity.setScaleY(originalScale);
    item.setBaseOffsetX(originalBaseOffsetX);
    item.setBaseOffsetY(originalBaseOffsetY);
  }
  
  public int getBaseOffsetX() {
    return baseOffsetXSpinner.getValue();
  }
  
  public int getBaseOffsetY() {
    return baseOffsetYSpinner.getValue();
  }
  
  private void showBaseAdjustmentDialog() {
    ItemAdjustmentDialog dialog = new ItemAdjustmentDialog(entity, gridVisualizerComponent);
    Optional<ButtonType> result = dialog.showAndWait();
    if (result.isPresent() && result.get() == ButtonType.OK) {
      Item item = entity.getObject("item");
      item.setBaseOffsetX(dialog.getBaseOffsetX());
      item.setBaseOffsetY(dialog.getBaseOffsetY());
      updateItemBase();
    }
  }
}