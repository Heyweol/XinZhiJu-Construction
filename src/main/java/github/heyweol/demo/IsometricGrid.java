package github.heyweol.demo;

import com.almasb.fxgl.entity.Entity;
import javafx.geometry.Point2D;

public class IsometricGrid {
  private final int gridWidth;
  private final int gridLength;
  private final double tileWidth;
  private final double tileHeight;
  private final Entity[][] grid;
  private final double offsetX;
  private final double offsetY;
  
  public IsometricGrid(int width, int length, double tileWidth, double tileHeight, double offsetX, double offsetY) {
    this.gridWidth = width;
    this.gridLength = length;
    this.tileWidth = tileWidth;
    this.tileHeight = tileHeight;
    this.grid = new Entity[width][length];
    this.offsetX = offsetX;
    this.offsetY = offsetY;
  }
  
  public Point2D getIsometricPosition(int gridX, int gridY) {
    double x = offsetX + (gridX - gridY) * (tileWidth / 2.0);
    double y = offsetY + (gridX + gridY) * (tileHeight / 2.0);
    return new Point2D(x, y);
  }
  
  public Point2D getGridPosition(double screenX, double screenY) {
    double x = screenX - offsetX;
    double y = screenY - offsetY;
    int gridX = (int) Math.round((x / (tileWidth / 2.0) + y / (tileHeight / 2.0)) / 2.0);
    int gridY = (int) Math.round((y / (tileHeight / 2.0) - x / (tileWidth / 2.0)) / 2.0);
    return new Point2D(gridX, gridY);
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
  
  public void placeItem(Entity item, int gridX, int gridY, int itemWidth, int itemLength) {
    if (!canPlaceItem(gridX, gridY, itemWidth, itemLength)) {
      return; // Don't place the item if it's not allowed
    }
    for (int x = gridX; x < gridX + itemWidth; x++) {
      for (int y = gridY; y < gridY + itemLength; y++) {
        grid[x][y] = item;
      }
    }
    Point2D isoPos = getIsometricPosition(gridX, gridY);
    item.setPosition(isoPos);
  }
  
  public void removeItem(Entity item) {
    for (int x = 0; x < gridWidth; x++) {
      for (int y = 0; y < gridLength; y++) {
        if (grid[x][y] == item) {
          grid[x][y] = null;
        }
      }
    }
  }
  
  // Getters
  public double getTileWidth() { return tileWidth; }
  public double getTileHeight() { return tileHeight; }
  public int getGridWidth() { return gridWidth; }
  public int getGridLength() { return gridLength; }
  public double getOffsetX() { return offsetX; }
  public double getOffsetY() { return offsetY; }
}