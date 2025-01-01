package github.heyweol.demo;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import github.heyweol.demo.utils.ResourceManager;
import javafx.geometry.Point2D;
import javafx.scene.image.Image;

public class Item {
  private String filename;
  private String name;
  private String imageName;
  private List<Integer> size;
  private boolean canBePlacedAbove;
  private Map<String, Integer> materialList;
  private String unicode;
  private int baseOffsetX;
  private int baseOffsetY;
  private double ratio;
  private double scale;
  private double xOffset;
  private double yOffset;
  private double xOffsetMirror;
  private double yOffsetMirror;
  private int numTileWidth;
  private int numTileHeight;
  private String season;
  @JsonIgnore
  private Image image;
  
  @JsonCreator
  public Item(@JsonProperty("filename") String filename,
              @JsonProperty("name") String name,
//              @JsonProperty("size") String sizeString,
              @JsonProperty("size") List<Integer> size,
              @JsonProperty("outside") String outside,
//              @JsonProperty("material_list") List<String> materialListStrings,
//              @JsonProperty("material_list") String materialListStrings,
              @JsonProperty("materialList") Map<String,Integer> materialListStrings,
              @JsonProperty("xOffset") double xOffset,
              @JsonProperty("yOffset") double yOffset,
              @JsonProperty("xOffsetMirror") double xOffsetMirror,
              @JsonProperty("yOffsetMirror") double yOffsetMirror,
              @JsonProperty("scale") Double scale) {
    this.filename = filename;
    this.name = name;
    this.imageName = constructImagePath(filename);
//    this.size = parseSize(sizeString);
    this.size = size;
    this.canBePlacedAbove = "Y".equals(outside);
//    this.materialList = parseMaterialList(materialListStrings);
    this.materialList = materialListStrings;
    this.unicode = determineUnicode(filename);
    this.baseOffsetX = 0;
    this.baseOffsetY = 0;
    this.scale = scale!=null? scale : 1.0;
    this.xOffset = xOffset;
    this.yOffset = yOffset;
    this.xOffsetMirror = xOffsetMirror;
    this.yOffsetMirror = yOffsetMirror;
    this.numTileWidth = size.get(0);
    this.numTileHeight = size.get(1);
    this.season = filename.split("_")[0];
    
    if (this.filename.contains("guajian")) {
      this.numTileHeight = size.get(0);
      this.numTileWidth = size.get(1);
    }
    
  }
  
  private String constructImagePath(String filename) {
    String[] parts = filename.split("_");
    if (parts.length < 4) {
      throw new IllegalArgumentException("Invalid filename format: " + filename);
    }
    String season = parts[0];
    String characterName = parts[1];
    String itemType = parts[2];
    return season + "/" + characterName + "/" + itemType + "/" + filename;
  }
  
  private List<Integer> parseSize(String sizeString) {
    String[] sizes = sizeString.replaceAll("[\\[\\]]", "").split(",");
    return List.of(Integer.parseInt(sizes[0].trim()), Integer.parseInt(sizes[1].trim()));
  }
  
  private Map<String, Integer> parseMaterialList(List<String> materialListStrings) {
    return materialListStrings.stream()
            .map(s -> s.replaceAll("\"", "").split(":"))
            .collect(Collectors.toMap(
                    parts -> parts[0].trim(),
                    parts -> Integer.parseInt(parts[1].trim())
            ));
  }
  
 private Map<String, Integer>parseMaterialList(String materialListString) {
   materialListString = materialListString.replaceAll("[\\[\\]\"]", "");
   return Arrays.stream(materialListString.split(","))
           .map(s -> s.split(":"))
           .filter(parts -> parts.length == 2)
           .collect(Collectors.toMap(
                   parts -> parts[0].trim(),
                   parts -> Integer.parseInt(parts[1].trim())
           ));
  }
  
  private String determineUnicode(String filename) {
    if (filename.contains("guajian")) return "\u1F3A8";
    else if (filename.contains("qiju")) return "\u1F6CB";
    else if (filename.contains("zhiwu")) return "\u1F331";
    else if (filename.contains("zhuangshi")) return "\u1F381";
    else return "\u2753";
  }
  
  // Getters
  public String getFilename() { return filename; }
  public String getName() { return name; }
  public String getImageName() { return imageName; }
  public List<Integer> getSize() { return size; }
  public boolean canBePlacedOutside() { return canBePlacedAbove; }
  public Map<String, Integer> getMaterialList() { return materialList; }
  public String getUnicode() { return unicode; }
  
  /**
   *  @deprecated Use {@link #getNumTileWidth()} instead.
   */
  @Deprecated
  public int getWidth() { return getNumTileWidth(); }
  
  /**
   *  @deprecated Use {@link #getNumTileHeight()} instead.
   */
  @Deprecated
  public int getLength() { return getNumTileHeight(); }
  
  public int getNumTileWidth() { return numTileWidth; }
  public int getNumTileHeight() { return numTileHeight; }
  
  @JsonIgnore
  public Image getImage() {
    if (image == null) {
      image = ResourceManager.getImage(imageName);
    }
    if (image != null) {
      ratio = image.getHeight() / image.getWidth();
    } else {
      ratio = 1.0;
    }
    
    return image;
  }
  
  public String getMaterialsAsString() {
    return materialList.entrySet().stream()
            .map(entry -> entry.getKey() + ": " + entry.getValue())
            .collect(Collectors.joining(", "));
  }
  public Point2D calculateDisplayOffset(double tileWidth) {
    double displayWidth = this.getNumTileWidth() * tileWidth;
    return new Point2D(-displayWidth / 4, -displayWidth * this.getRatio() / 2);
  }
  
  @Deprecated
  public int getBaseOffsetX() { return baseOffsetX; }
  public void setBaseOffsetX(int baseOffsetX) { this.baseOffsetX = baseOffsetX; }
  @Deprecated
  public int getBaseOffsetY() { return baseOffsetY; }
  public void setBaseOffsetY(int baseOffsetY) { this.baseOffsetY = baseOffsetY; }
  public double getRatio() { return ratio; }
  
  public double getXOffset() { return xOffset; }
  public void setXOffset(double xOffset) { this.xOffset = xOffset; }
  public double getYOffset() { return yOffset; }
  public void setYOffset(double yOffset) { this.yOffset = yOffset; }
  public double getXOffsetMirror() { return xOffsetMirror; }
  public void setXOffsetMirror(double xOffsetMirror) { this.xOffsetMirror = xOffsetMirror; }
  public double getYOffsetMirror() { return yOffsetMirror; }
  public void setYOffsetMirror(double yOffsetMirror) { this.yOffsetMirror = yOffsetMirror; }
  public double getScale() { return scale; }
  public void setScale(double scale) { this.scale = scale; }
  
  public void setNumTileWidth(int width) {
    numTileWidth = width;
  }
  
  public void setNumTileHeight(int height) {
    numTileHeight = height;
  }
  
  @Deprecated
  public void setSize(double scale) {
    this.size =  List.of((int) (numTileWidth ), (int) (numTileHeight ));
  }
  
  public boolean isCarpet() {
    return name.contains("地垫") || name.contains("毯");
  }
}