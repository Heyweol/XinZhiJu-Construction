package github.heyweol.demo;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class SavedItemState {
  private String itemName;
  private EntityType type;
  private String gridType; // "floor", "leftWall", or "rightWall"
  private int gridX;
  private int gridY;
  private boolean isMirrored;
  
  @JsonCreator
  public SavedItemState(
          @JsonProperty("itemName") String itemName,
          @JsonProperty("type") EntityType type,
          @JsonProperty("gridType") String gridType,
          @JsonProperty("gridX") int gridX,
          @JsonProperty("gridY") int gridY,
          @JsonProperty("isMirrored") boolean isMirrored) {
    this.itemName = itemName;
    this.type = type;
    this.gridType = gridType;
    this.gridX = gridX;
    this.gridY = gridY;
    this.isMirrored = isMirrored;
  }
  
  // Default constructor for Jackson
  public SavedItemState() {}
  
  // Getters and setters
  public String getItemName() {
    return itemName;
  }
  
  public void setItemName(String itemName) {
    this.itemName = itemName;
  }
  
  public EntityType getType() {
    return type;
  }
  
  public void setType(EntityType type) {
    this.type = type;
  }
  
  public String getGridType() {
    return gridType;
  }
  
  public void setGridType(String gridType) {
    this.gridType = gridType;
  }
  
  public int getGridX() {
    return gridX;
  }
  
  public void setGridX(int gridX) {
    this.gridX = gridX;
  }
  
  public int getGridY() {
    return gridY;
  }
  
  public void setGridY(int gridY) {
    this.gridY = gridY;
  }
  
  public boolean isMirrored() {
    return isMirrored;
  }
  
  public void setMirrored(boolean mirrored) {
    isMirrored = mirrored;
  }
}