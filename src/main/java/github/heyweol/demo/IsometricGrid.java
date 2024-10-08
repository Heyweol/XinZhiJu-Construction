package github.heyweol.demo;

import com.almasb.fxgl.entity.Entity;

import javafx.geometry.Point2D;

public class IsometricGrid {
  private int gridWidth;
  private int gridLength;
  private final double tileWidth;
  private final double tileHeight;
  private Entity[][] grid;
  private final double originX;
  private final double originY;
  
  public IsometricGrid(int width, int length, double tileWidth, double tileHeight, double originX, double originY) {
    this.gridWidth = width;
    this.gridLength = length;
    this.tileWidth = tileWidth;
    this.tileHeight = tileHeight;
    this.grid = new Entity[width][length];
    this.originX = originX;
    this.originY = originY;
  }
  
  /**
   * Given a grid position (ie, column as X, row as Y), X points to lower right, Y to lower left
   * @param gridX
   * @param gridY
   * @return Point2D of screen position of center of the tile
   */
  public Point2D getIsometricPosition(int gridX, int gridY) {
    double x = originX + (gridX - gridY) * (tileWidth / 2.0);
    double y = originY + (gridX + gridY) * (tileHeight / 2.0);
    return new Point2D(x, y);
  }
  
  /**
   * Given a screen position, returns the grid position
   * @param screenX
   * @param screenY
   * @return grid position, X as column, Y as row
   */
  public Point2D getGridPosition(double screenX, double screenY) {
    double x = screenX - originX;
    double y = screenY - originY;
    int gridX = (int) Math.floor((x / (tileWidth / 2.0) + y / (tileHeight / 2.0)) / 2.0);
    int gridY = (int) Math.floor((y / (tileHeight / 2.0) - x / (tileWidth / 2.0)) / 2.0);
    return new Point2D(gridX, gridY);
  }
  
  public boolean positionInBounds(int gridX, int gridY) {
    return gridX >= 0 && gridX < gridWidth && gridY >= 0 && gridY < gridLength;
  }
  
  public boolean positionInBounds(double screenX, double screenY) {
    Point2D gridPos = getGridPosition(screenX, screenY);
    return positionInBounds((int)gridPos.getX(), (int)gridPos.getY());
  }

  
  
  public boolean canPlaceItem(int gridX, int gridY, int itemWidth, int itemLength) {
    if (gridX < 0 || gridY < 0 || gridX + itemWidth > gridWidth || gridY + itemLength > gridLength) {
      return false;
    }
    for (int x = gridX; x < gridX + itemWidth; x++) {
      for (int y = gridY; y < gridY + itemLength; y++) {
        if (grid[x][y] != null) {
          return false;
        }
      }
    }
    return true;
  }
  
  public void placeEntity(Entity entity, int gridX, int gridY) {
    placeEntity(entity, gridX, gridY, entity.getInt("itemWidth"), entity.getInt("itemLength"));
  }
  
  public void placeEntity(Entity entity, int gridX, int gridY, int itemWidth, int itemLength) {
    placeEntity(entity, gridX, gridY, itemWidth, itemLength, Point2D.ZERO);
  }

  public void placeEntity(Entity entity, int gridX, int gridY, int itemWidth, int itemLength, Point2D offset) {
    if (!canPlaceItem(gridX, gridY, itemWidth, itemLength)) {
      return;
    }
    for (int x = gridX; x < gridX + itemWidth; x++) {
      for (int y = gridY; y < gridY + itemLength; y++) {
        grid[x][y] = entity;
      }
    }
    
  }
  
  /**
   * Removes the item from the grid by setting the grid cell to null
   * @param item
   */
  public void removeEntity(Entity item) {
    for (int x = 0; x < gridWidth; x++) {
      for (int y = 0; y < gridLength; y++) {
        if (grid[x][y] == item) {
          grid[x][y] = null;
        }
      }
    }
  }

  public void expand(int steps) {
    gridWidth += steps;
    gridLength += steps;
    Entity[][] newGrid = new Entity[gridWidth][gridLength];
    for (int x = 0; x < gridWidth - steps; x++) {
        System.arraycopy(grid[x], 0, newGrid[x], 0, gridLength - steps);
    }
    grid = newGrid;
  }
  
  // Getters
  public double getTileWidth() { return tileWidth; }
  public double getTileHeight() { return tileHeight; }
  public int getGridWidth() { return gridWidth; }
  public int getGridLength() { return gridLength; }
  public double getOriginX() { return originX; }
  public double getOriginY() { return originY; }
  
  public boolean isOccupied(int gridX, int gridY) {
    return grid[gridX][gridY] != null;
  }
}