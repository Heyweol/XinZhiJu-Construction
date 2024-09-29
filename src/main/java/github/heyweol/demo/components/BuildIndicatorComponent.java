package github.heyweol.demo.components;

import com.almasb.fxgl.entity.component.Component;

import github.heyweol.demo.IsometricGrid;
import github.heyweol.demo.Item;
import javafx.geometry.Point2D;
import javafx.scene.paint.Color;
import javafx.scene.shape.Polygon;

public class BuildIndicatorComponent extends Component {
  private IsometricGrid grid;
  private Polygon indicator;
  private int width;
  private int height;
  
  public BuildIndicatorComponent(IsometricGrid grid) {
    this.grid = grid;
    this.indicator = new Polygon();
    this.indicator.setFill(Color.rgb(0, 255, 0, 0.3)); // Semi-transparent green
    this.indicator.setStroke(Color.GREEN);
    this.indicator.setStrokeWidth(2);
  }
  
  @Override
  public void onAdded() {
    entity.getViewComponent().addChild(indicator);
  }
  
  public void updateSize(Item item) {
    this.width = item.getNumTileWidth();
    this.height = item.getNumTileHeight();
    updateShape();
  }
  
  public void updatePosition(double x, double y) {
    Point2D gridPos = grid.getGridPosition(x, y);
    Point2D isoPos = grid.getIsometricPosition((int)gridPos.getX(), (int)gridPos.getY());
    entity.setPosition(isoPos);
    updateShape();
  }
  
  private void updateShape() {
    double tileWidth = grid.getTileWidth();
    double tileHeight = grid.getTileHeight();
    
    double w = width * tileWidth;
    double h = height * tileHeight;
    
    indicator.getPoints().clear();
    indicator.getPoints().addAll(
            0.0, h / 2,
            w / 2, 0.0,
            w, h / 2,
            w / 2, h
    );
  }
  
  public boolean isValidPlacement() {
    Point2D gridPos = grid.getGridPosition(entity.getX(), entity.getY());
    return grid.canPlaceItem((int)gridPos.getX(), (int)gridPos.getY(), width, height);
  }
  
  public void setValid(boolean valid) {
    if (valid) {
      indicator.setFill(Color.rgb(0, 255, 0, 0.3)); // Semi-transparent green
      indicator.setStroke(Color.GREEN);
    } else {
      indicator.setFill(Color.rgb(255, 0, 0, 0.3)); // Semi-transparent red
      indicator.setStroke(Color.RED);
    }
  }
}