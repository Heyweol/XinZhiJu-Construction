package github.heyweol.demo.components;

import com.almasb.fxgl.entity.component.Component;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import javafx.scene.Group;
import javafx.geometry.Point2D;

import github.heyweol.demo.IsometricGrid;
import github.heyweol.demo.WallGrid;

public class GridVisualizerComponent extends Component {
  private IsometricGrid floorGrid;
  private WallGrid leftWallGrid;
  private WallGrid rightWallGrid;
  private Group gridLines;
  private double offsetX;
  private double offsetY;
  
  public GridVisualizerComponent(IsometricGrid floorGrid, WallGrid leftWallGrid, WallGrid rightWallGrid, double offsetX, double offsetY) {
    this.floorGrid = floorGrid;
    this.leftWallGrid = leftWallGrid;
    this.rightWallGrid = rightWallGrid;
    this.gridLines = new Group();
    this.offsetX = offsetX;
    this.offsetY = offsetY;
  }
  
  @Override
  public void onAdded() {
    drawGrids();
    gridLines.setVisible(false);
    entity.getViewComponent().addChild(gridLines);
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
  
  public void show() {
    gridLines.setVisible(true);
  }
  
  public void hide() {
    gridLines.setVisible(false);
  }
}