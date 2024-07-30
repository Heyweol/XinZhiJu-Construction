package github.heyweol.demo;

import java.io.Serializable;

public class SceneState implements Serializable {
  private String itemName;
  private double x;
  private double y;
  private boolean isMirrored;
  private String variant;
  
  public SceneState(String itemName, double x, double y, boolean isMirrored, String variant) {
    this.itemName = itemName;
    this.x = x;
    this.y = y;
    this.isMirrored = isMirrored;
    this.variant = variant;
  }
  
  // Getters and setters
  public String getItemName() { return itemName; }
  public void setItemName(String itemName) { this.itemName = itemName; }
  public double getX() { return x; }
  public void setX(double x) { this.x = x; }
  public double getY() { return y; }
  public void setY(double y) { this.y = y; }
  public boolean isMirrored() { return isMirrored; }
  public void setMirrored(boolean mirrored) { isMirrored = mirrored; }
  public String getVariant() { return variant; }
  public void setVariant(String variant) { this.variant = variant; }
}