package github.heyweol.demo;

import javafx.geometry.Point2D;

public class WallGrid {
  private final int gridWidth;
  private final int gridHeight;
  private final double tileWidth;
  private final double tileHeight;
  private final double originX;
  private final double originY;
  private final boolean isLeftWall;
  
  public WallGrid(int width, int height, double tileWidth, double tileHeight,
                  double originX, double originY, boolean isLeftWall) {
    this.gridWidth = width;
    this.gridHeight = height;
    this.tileWidth = tileWidth;
    this.tileHeight = tileHeight;
    this.originX = originX;
    this.originY = originY;
    this.isLeftWall = isLeftWall;
  }
  
  public Point2D getWallPosition(int gridX, int gridY) {
    double x, y;
    if (isLeftWall) {
      // Left wall extends to the left
      x = originX - gridX * tileWidth;
      y = originY + gridY * tileHeight + gridX * (tileHeight / 2);
    } else {
      // Right wall extends to the right
      x = originX + gridX * tileWidth;
      y = originY + gridY * tileHeight + gridX * (tileHeight / 2);
    }
    return new Point2D(x, y);
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
    return gridX >= 0 && gridY >= 0 && gridX + itemWidth <= gridWidth && gridY + itemHeight <= gridHeight;
  }
  
  // Getters
  public int getGridWidth() { return gridWidth; }
  public int getGridHeight() { return gridHeight; }
  public double getTileWidth() { return tileWidth; }
  public double getTileHeight() { return tileHeight; }
  public boolean isLeftWall() { return isLeftWall; }
}