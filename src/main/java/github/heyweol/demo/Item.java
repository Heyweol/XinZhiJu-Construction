package github.heyweol.demo;

import com.almasb.fxgl.dsl.FXGL;
import javafx.scene.image.Image;

import java.util.List;
import java.util.Map;

public class Item {
  private String filename;
  private String name;
  private String imageName;
  private List<Integer> size;
  private boolean canBePlacedOutside;
  private Map<String, Integer> materialList;
  private String unicode;
  private Image image;
  
  public Item(String filename, String name, String imageName, List<Integer> size, boolean canBePlacedOutside, Map<String, Integer> materialList, String unicode) {
    this.filename = filename;
    this.name = name;
    this.imageName = imageName;
    this.size = size;
    this.canBePlacedOutside = canBePlacedOutside;
    this.materialList = materialList;
    this.unicode = unicode;
    loadImage();
  }
  
  private void loadImage() {
    try {
      this.image = FXGL.image(imageName);
    } catch (Exception e) {
      System.err.println("Failed to load image: " + imageName);
      e.printStackTrace();
      this.image = null;
    }
  }
  
  // Getters
  public String getFilename() { return filename; }
  public String getName() { return name; }
  public String getImageName() { return imageName; }
  public List<Integer> getSize() { return size; }
  public boolean canBePlacedOutside() { return canBePlacedOutside; }
  public Map<String, Integer> getMaterialList() { return materialList; }
  public String getUnicode() { return unicode; }
  public Image getImage() { return image; }
  
  public int getWidth() { return size.get(0); }
  public int getLength() { return size.get(1); }
  
  // Method to calculate total cost based on material prices
  public int calculateCost(Map<String, Integer> materialPrices) {
    return materialList.entrySet().stream()
            .mapToInt(entry -> entry.getValue() * materialPrices.getOrDefault(entry.getKey(), 0))
            .sum();
  }
}