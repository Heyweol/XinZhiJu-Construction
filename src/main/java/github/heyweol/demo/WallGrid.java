package github.heyweol.demo;

import com.almasb.fxgl.entity.Entity;
import javafx.geometry.Point2D;

public class WallGrid {
  private int gridWidth;
  private final int gridHeight;
  private final double tileWidth;
  private final double tileHeight;
  private final double originX;
  private final double originY;
  private final boolean isLeftWall;
  private Entity[][] grid;
  
  
  public WallGrid(int width, int height, double tileWidth, double tileHeight,
                  double originX, double originY, boolean isLeftWall) {
    this.gridWidth = width;
    this.gridHeight = height;
    this.tileWidth = tileWidth;
    this.tileHeight = tileHeight;
    this.originX = originX;
    this.originY = originY;
    this.isLeftWall = isLeftWall;
    this.grid = new Entity[width][height];
  }
  
  public Point2D getWallPosition(int gridX, int gridY) {
    double x, y;
    if (isLeftWall) {
      // Left wall extends to the left
      x = originX - gridX * tileWidth ;
    } else {
      // Right wall extends to the right
      x = originX + gridX * tileWidth ;
    }
    y = originY + gridY * tileHeight + gridX * (tileHeight / 2);
    return new Point2D(x, y);
  }
  
  /**
   * center screen position of a tile in the wall grid
   * @param gridX
   * @param gridY
   * @return
   */
  public Point2D getWallGrindCenter(int gridX, int gridY) {
    Point2D centerOffset;
    if (isLeftWall) {
      centerOffset = new Point2D(-tileWidth / 2, tileHeight / 2);
    } else {
      centerOffset = new Point2D(tileWidth / 2, tileHeight / 2);
    }
    return getWallPosition(gridX, gridY).add(centerOffset);
  }
  
  public Point2D getGridPosition(double screenX, double screenY) {
    double relativeX = screenX - originX;
    double relativeY = screenY - originY;
    
    int gridX, gridY;
    
    if (isLeftWall) {
      gridX = (int) Math.floor(-relativeX / tileWidth);
      gridY = (int) Math.floor((relativeY - gridX * (tileHeight / 2)) / tileHeight);
    } else {
      gridX = (int) Math.floor(relativeX / tileWidth);
      gridY = (int) Math.floor((relativeY - gridX * (tileHeight / 2)) / tileHeight);
    }
    
    return new Point2D(gridX, gridY);
  }
  
  public boolean canPlaceItem(int gridX, int gridY, int itemWidth, int itemHeight) {
    if(!( gridX >= 0 && gridY >= 0 && gridX + itemWidth <= gridWidth && gridY + itemHeight <= gridHeight)) return false;
    for (int x = gridX; x < gridX + itemWidth; x++) {
      for (int y = gridY; y < gridY + itemHeight; y++) {
        if (grid[x][y] != null) return false;
      }
    }
    return true;
  }
  
  public boolean canPlaceEntity(Entity entity, int gridX, int gridY) {
    int itemWidth = entity.getInt("itemWidth");
    int itemHeight = entity.getInt("itemLength");
    
    return canPlaceItem(gridX, gridY, itemWidth, itemHeight);
  }
  
  public boolean canPlaceEntity(Entity entity, double screenX, double screenY) {
    return canPlaceEntity(entity, (int) getGridPosition(screenX, screenY).getX(), (int) getGridPosition(screenX, screenY).getY());
  }
  
  public boolean placeEntity(Entity entity, int gridX, int gridY) {
    int itemWidth = entity.getInt("itemWidth");
    int itemHeight = entity.getInt("itemLength");
    
    if (canPlaceItem(gridX, gridY, itemWidth, itemHeight)) {
      for (int x = gridX; x < gridX + itemWidth; x++) {
        for (int y = gridY; y < gridY + itemHeight; y++) {
          grid[x][y] = entity;
        }
      }
      entity.setProperty("isLeftWall", isLeftWall);
      return true;
    }
    return false;
  }
  
  public void removeEntity(Entity entity) {
    for (int x = 0; x < gridWidth; x++) {
      for (int y = 0; y < gridHeight; y++) {
        if (grid[x][y] == entity) {
          grid[x][y] = null;
        }
      }
    }
  }
  
  public void expand(int steps){
    gridWidth += steps;
    Entity[][] newGrid = new Entity[gridWidth][gridHeight];
    for (int x = 0; x < gridWidth; x++) {
      for (int y = 0; y < gridHeight; y++) {
        if (x < gridWidth - steps) {
          newGrid[x][y] = grid[x][y];
        }
      }
    }
    grid = newGrid;
  }
  
  // Getters
  public int getGridWidth() { return gridWidth; }
  public int getGridHeight() { return gridHeight; }
  public double getTileWidth() { return tileWidth; }
  public double getTileHeight() { return tileHeight; }
  public double getOriginX() { return originX; }
  public boolean isLeftWall() { return isLeftWall; }
  public boolean isOccupied(int gridX, int gridY) { return grid[gridX][gridY] != null; }
}