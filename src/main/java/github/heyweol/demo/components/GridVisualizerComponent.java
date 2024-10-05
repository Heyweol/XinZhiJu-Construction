package github.heyweol.demo.components;

import com.almasb.fxgl.dsl.FXGL;
import com.almasb.fxgl.entity.component.Component;
import github.heyweol.demo.EntityType;
import github.heyweol.demo.IsometricGrid;
import github.heyweol.demo.Item;
import github.heyweol.demo.WallGrid;
import javafx.geometry.Point2D;
import javafx.scene.Group;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import javafx.scene.shape.Polygon;

public class GridVisualizerComponent extends Component {
  private final IsometricGrid floorGrid;
  private final WallGrid leftWallGrid;
  private final WallGrid rightWallGrid;
  private final Group gridLines;
  private final Group itemBaseHighlight;
  private final double offsetX;
  private final double offsetY;
  private double wallHeight;
  
  public GridVisualizerComponent(IsometricGrid floorGrid, WallGrid leftWallGrid, WallGrid rightWallGrid, double offsetX, double offsetY, double wallHeight) {
    this.floorGrid = floorGrid;
    this.leftWallGrid = leftWallGrid;
    this.rightWallGrid = rightWallGrid;
    this.gridLines = new Group();
    this.itemBaseHighlight = new Group();
    this.offsetX = offsetX;
    this.offsetY = offsetY;
    this.wallHeight = wallHeight;
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
  
  public void showItemBase(Item item, int entityX, int entityY, boolean isWallItem, boolean isLeftWall) {
    
    int gridX = entityX + item.getBaseOffsetX();
    int gridY = entityY + item.getBaseOffsetY();
    
    if (isWallItem) {
      showWallItemBase(item, gridX, gridY, isLeftWall);
    } else {
      showFloorItemBase(item, gridX, gridY);
    }
    
    itemBaseHighlight.setVisible(true);
  }
  
  public void showAllOccupiedGrids() {
    itemBaseHighlight.getChildren().clear();
    
    for (int x = 0; x < floorGrid.getGridWidth(); x++) {
      for (int y = 0; y < floorGrid.getGridLength(); y++) {
        if (floorGrid.isOccupied(x, y)) {
          Point2D pos = floorGrid.getIsometricPosition(x, y);
          Polygon rect = new Polygon(
                  pos.getX() - offsetX, pos.getY() - offsetY,
                  pos.getX() + floorGrid.getTileWidth() / 2 - offsetX, pos.getY() + floorGrid.getTileHeight() / 2 - offsetY,
                  pos.getX() - offsetX, pos.getY() + floorGrid.getTileHeight() - offsetY,
                  pos.getX() - floorGrid.getTileWidth() / 2 - offsetX, pos.getY() + floorGrid.getTileHeight() / 2 - offsetY
          );
          
          rect.setFill(Color.LIGHTGREEN.deriveColor(0, 1, 1, 0.5));
          rect.setStroke(Color.GREEN);
          itemBaseHighlight.getChildren().add(rect);
        }
      }
    }
    
    FXGL.getGameWorld().getEntities().stream()
            .filter(e -> e.getType() == EntityType.WALL_ITEM)
            .forEach(e -> {
              Item item = e.getObject("item");
              Point2D gridPos = ((WallGrid) e.getObject("wallGrid")).getGridPosition(e.getX(), e.getY());
              int gridX = (int) e.getX();
              int gridY = (int) e.getY();
              boolean isLeftWall = e.getBoolean("isLeftWall");
              showItemBase(item, gridX, gridY, true, isLeftWall);
            });
    
    itemBaseHighlight.setVisible(true);
  }
  
  public void HideAllOccupiedGrids() {
    itemBaseHighlight.setVisible(false);
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
    int itemWidth = item.getNumTileWidth();
    int itemLength = item.getNumTileHeight();
    
    Point2D ur = wallGrid.getWallPosition(gridX, gridY);
    Point2D ul = wallGrid.getWallPosition(gridX + itemWidth, gridY);
    Point2D lr = wallGrid.getWallPosition(gridX, gridY + itemLength);
    Point2D ll = wallGrid.getWallPosition(gridX + itemWidth, gridY + itemLength);
    
    Polygon diamond = new Polygon(
            ur.getX() - offsetX, ur.getY() - offsetY,
            ul.getX() - offsetX, ul.getY() - offsetY,
            ll.getX() - offsetX, ll.getY() - offsetY,
            lr.getX() - offsetX, lr.getY() - offsetY
    );
    diamond.setFill(Color.LIGHTBLUE.deriveColor(0, 1, 1, 0.5));
    diamond.setStroke(Color.BLUE);
    itemBaseHighlight.getChildren().add(diamond);
    
    
//    for (int x = 0; x < item.getWidth(); x++) {
//      for (int y = 0; y < item.getLength(); y++) {
//        Point2D pos = wallGrid.getWallPosition(gridX + x, gridY + y);
//        Polygon diamond = new Polygon(
//                pos.getX() - offsetX, pos.getY() - offsetY,
//                pos.getX() + tileWidth / 2 - offsetX, pos.getY() + tileHeight / 2 - offsetY,
//                pos.getX() - offsetX, pos.getY() + tileHeight - offsetY,
//                pos.getX() - tileWidth / 2 - offsetX, pos.getY() + tileHeight / 2 - offsetY
//        );
//        diamond.setFill(Color.LIGHTBLUE.deriveColor(0, 1, 1, 0.5));
//        diamond.setStroke(Color.BLUE);
//        itemBaseHighlight.getChildren().add(diamond);
//      }
//    }
  
  
  
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