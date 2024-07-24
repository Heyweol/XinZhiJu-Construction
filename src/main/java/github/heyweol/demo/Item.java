package github.heyweol.demo;

import com.almasb.fxgl.dsl.FXGL;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;

public class Item {
  private String name;
  private String imageName;
  private int cost;
  private Image image;
  private String imagePath;
  private int width;
  private int length;
  
  
  public Item(String name, String imageName, int cost,int width, int length) {
    this.name = name;
    this.imageName = imageName;
    this.cost = cost;
    this.width = width;
    this.length = length;
    this.imagePath = "/assets/textures/" + imageName;
    loadImage();
  }
  
  private void loadImage() {
    try {
      this.image = FXGL.getAssetLoader().loadImage(imageName);
    } catch (Exception e) {
      System.err.println("Failed to load image: " + imageName);
      System.err.println("Full path attempted: " + FXGL.getAssetLoader().getClass().getResource("/" + imageName));
      e.printStackTrace();
      // Load a placeholder image or set to null
      this.image = null;
    }
  }
  
  public String getName() {
    return name;
  }
  
  public String getImageName() {
    return imageName;
  }
  
  public Image getImage() {
    return image;
  }
  
  public int getCost() {
    return cost;
  }
  
  public String getImagePath() {
    return imagePath;
  }
  
  public int getWidth() {
    return width;
  }
  
  public int getLength() {
    return length;
  }
  
}