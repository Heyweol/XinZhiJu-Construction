package github.heyweol.demo.ui;

import com.almasb.fxgl.dsl.FXGL;
import com.almasb.fxgl.entity.Entity;
import github.heyweol.demo.components.GridVisualizerComponent;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.stage.Modality;

public class ItemAdjustmentDialog extends Dialog<ButtonType> {
  private Slider xSlider;
  private Slider ySlider;
  private Slider scaleSlider;
  private Entity item;
  private double originalX;
  private double originalY;
  private double originalScale;
  private GridVisualizerComponent gridVisualizer;
  
  public ItemAdjustmentDialog(Entity item, GridVisualizerComponent gridVisualizer) {
    this.item = item;
    this.gridVisualizer = gridVisualizer;
    this.originalX = item.getX();
    this.originalY = item.getY();
    this.originalScale = item.getScaleX();
    
    setTitle("Adjust Item");
    setHeaderText("Fine-tune item position and size");
    
    initModality(Modality.APPLICATION_MODAL);
    initOwner(FXGL.getGameScene().getRoot().getScene().getWindow());
    
    xSlider = createSlider("X Position", -10, 10, 0);
    ySlider = createSlider("Y Position", -10, 10, 0);
    scaleSlider = createSlider("Scale", 0.5, 1.5, 1);
    
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
    
    setOnShowing(event -> gridVisualizer.show());
    setOnHiding(event -> gridVisualizer.hide());
  }
  
  private Slider createSlider(String name, double min, double max, double initial) {
    Slider slider = new Slider(min, max, initial);
    slider.setShowTickLabels(true);
    slider.setShowTickMarks(true);
    return slider;
  }
  
  private void updateItemPosition() {
    item.setX(originalX + xSlider.getValue());
    item.setY(originalY + ySlider.getValue());
  }
  
  private void updateItemScale() {
    item.setScaleX(scaleSlider.getValue());
    item.setScaleY(scaleSlider.getValue());
  }
  
  private void resetItemPosition() {
    item.setX(originalX);
    item.setY(originalY);
    item.setScaleX(originalScale);
    item.setScaleY(originalScale);
  }
}