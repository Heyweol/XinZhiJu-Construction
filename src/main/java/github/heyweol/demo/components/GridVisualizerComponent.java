package github.heyweol.demo.components;

import com.almasb.fxgl.entity.component.Component;
import github.heyweol.demo.Item;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import javafx.scene.Group;
import javafx.geometry.Point2D;

import github.heyweol.demo.IsometricGrid;
import github.heyweol.demo.WallGrid;
import javafx.scene.shape.Polygon;
import javafx.scene.shape.Rectangle;

public class GridVisualizerComponent extends Component {
  private IsometricGrid floorGrid;
  private WallGrid leftWallGrid;
  private WallGrid rightWallGrid;
  private Group gridLines;
  private Group itemBaseHighlight;
  private double offsetX;
  private double offsetY;
  
  public GridVisualizerComponent(IsometricGrid floorGrid, WallGrid leftWallGrid, WallGrid rightWallGrid, double offsetX, double offsetY) {
    this.floorGrid = floorGrid;
    this.leftWallGrid = leftWallGrid;
    this.rightWallGrid = rightWallGrid;
    this.gridLines = new Group();
    this.itemBaseHighlight = new Group();
    this.offsetX = offsetX;
    this.offsetY = offsetY;
  }
  
  @Override
  public void onAdded() {
    drawGrids();
    gridLines.setVisible(false);
    itemBaseHighlight.setVisible(false);
    entity.getViewComponent().addChild(gridLines);
    entity.getViewComponent().addChild(itemBaseHighlight);
  }
  
  private void drawGrids() {
    drawFloorGrid();
    drawWallGrid(leftWallGrid, Color.LIGHTBLUE);
    drawWallGrid(rightWallGrid, Color.LIGHTGREEN);
  }
  
  private void drawFloorGrid() {
    int gridWidth = floorGrid.getGridWidth();
    int gridLength = floorGrid.getGridLength();
    
    // Draw horizontal lines
    for (int y = 0; y <= gridLength; y++) {
      Point2D start = floorGrid.getIsometricPosition(0, y);
      Point2D end = floorGrid.getIsometricPosition(gridWidth, y);
      Line line = new Line(start.getX() - offsetX, start.getY() - offsetY,
              end.getX() - offsetX, end.getY() - offsetY);
      line.setStroke(Color.LIGHTGRAY);
      gridLines.getChildren().add(line);
    }
    
    // Draw vertical lines
    for (int x = 0; x <= gridWidth; x++) {
      Point2D start = floorGrid.getIsometricPosition(x, 0);
      Point2D end = floorGrid.getIsometricPosition(x, gridLength);
      Line line = new Line(start.getX() - offsetX, start.getY() - offsetY,
              end.getX() - offsetX, end.getY() - offsetY);
      line.setStroke(Color.LIGHTGRAY);
      gridLines.getChildren().add(line);
    }
  }
  
  private void drawWallGrid(WallGrid wallGrid, Color color) {
    int gridWidth = wallGrid.getGridWidth();
    int gridHeight = wallGrid.getGridHeight();
    
    // Draw horizontal lines
    for (int y = 0; y <= gridHeight; y++) {
      Point2D start = wallGrid.getWallPosition(0, y);
      Point2D end = wallGrid.getWallPosition(gridWidth, y);
      Line line = new Line(start.getX() - offsetX, start.getY() - offsetY,
              end.getX() - offsetX, end.getY() - offsetY);
      line.setStroke(color);
      gridLines.getChildren().add(line);
    }
    
    // Draw vertical lines
    for (int x = 0; x <= gridWidth; x++) {
      Point2D start = wallGrid.getWallPosition(x, 0);
      Point2D end = wallGrid.getWallPosition(x, gridHeight);
      Line line = new Line(start.getX() - offsetX, start.getY() - offsetY,
              end.getX() - offsetX, end.getY() - offsetY);
      line.setStroke(color);
      gridLines.getChildren().add(line);
    }
  }
  
  public void showItemBase(Item item, int gridX, int gridY, boolean isWallItem, boolean isLeftWall) {
    itemBaseHighlight.getChildren().clear();
    
    if (isWallItem) {
      showWallItemBase(item, gridX, gridY, isLeftWall);
    } else {
      showFloorItemBase(item, gridX, gridY);
    }
    
    itemBaseHighlight.setVisible(true);
  }
  
  private void showFloorItemBase(Item item, int gridX, int gridY) {
    double tileWidth = floorGrid.getTileWidth();
    double tileHeight = floorGrid.getTileHeight();
    
    for (int x = 0; x < item.getWidth(); x++) {
      for (int y = 0; y < item.getLength(); y++) {
        Point2D topLeft = floorGrid.getIsometricPosition(gridX + x, gridY + y);
        Point2D topRight = floorGrid.getIsometricPosition(gridX + x + 1, gridY + y);
        Point2D bottomLeft = floorGrid.getIsometricPosition(gridX + x, gridY + y + 1);
        Point2D bottomRight = floorGrid.getIsometricPosition(gridX + x + 1, gridY + y + 1);
        
        Polygon diamond = new Polygon(
                topLeft.getX() - offsetX, topLeft.getY() - offsetY,
                topRight.getX() - offsetX, topRight.getY() - offsetY,
                bottomRight.getX() - offsetX, bottomRight.getY() - offsetY,
                bottomLeft.getX() - offsetX, bottomLeft.getY() - offsetY
        );
        
        diamond.setFill(Color.LIGHTGREEN.deriveColor(0, 1, 1, 0.5));
        diamond.setStroke(Color.GREEN);
        itemBaseHighlight.getChildren().add(diamond);
      }
    }
  }
  
  private void showWallItemBase(Item item, int gridX, int gridY, boolean isLeftWall) {
    WallGrid wallGrid = isLeftWall ? leftWallGrid : rightWallGrid;
    double tileWidth = wallGrid.getTileWidth();
    double tileHeight = wallGrid.getTileHeight();
    
    for (int x = 0; x < item.getWidth(); x++) {
      for (int y = 0; y < item.getLength(); y++) {
        Point2D pos = wallGrid.getWallPosition(gridX + x, gridY + y);
        Polygon diamond = new Polygon(
                pos.getX() - offsetX, pos.getY() - offsetY,
                pos.getX() + tileWidth / 2 - offsetX, pos.getY() + tileHeight / 2 - offsetY,
                pos.getX() - offsetX, pos.getY() + tileHeight - offsetY,
                pos.getX() - tileWidth / 2 - offsetX, pos.getY() + tileHeight / 2 - offsetY
        );
        diamond.setFill(Color.LIGHTBLUE.deriveColor(0, 1, 1, 0.5));
        diamond.setStroke(Color.BLUE);
        itemBaseHighlight.getChildren().add(diamond);
      }
    }
  }
  
  public void hideItemBase() {
    itemBaseHighlight.setVisible(false);
  }
  
  public void show() {
    gridLines.setVisible(true);
  }
  
  public void hide() {
    gridLines.setVisible(false);
  }
}