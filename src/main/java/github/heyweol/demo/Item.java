package github.heyweol.demo;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonIgnore;
import github.heyweol.demo.utils.ResourceManager;
import javafx.scene.image.Image;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class Item {
  private String filename;
  private String name;
  private String imageName;
  private List<Integer> size;
  private boolean canBePlacedOutside;
  private Map<String, Integer> materialList;
  private String unicode;
  private int baseOffsetX;
  private int baseOffsetY;
  
  
  @JsonIgnore
  private Image image;
  
  @JsonCreator
  public Item(@JsonProperty("filename") String filename,
              @JsonProperty("name") String name,
              @JsonProperty("size") String sizeString,
              @JsonProperty("outside") String outside,
              @JsonProperty("material_list") List<String> materialListStrings) {
    this.filename = filename;
    this.name = name;
    this.imageName = constructImagePath(filename);
    this.size = parseSize(sizeString);
    this.canBePlacedOutside = "Y".equals(outside);
    this.materialList = parseMaterialList(materialListStrings);
    this.unicode = determineUnicode(filename);
    this.baseOffsetX = 0;
    this.baseOffsetY = 0;
  }
  
  private String constructImagePath(String filename) {
    String[] parts = filename.split("_");
    if (parts.length < 4) {
      throw new IllegalArgumentException("Invalid filename format: " + filename);
    }
    String characterName = parts[1];
    String itemType = parts[2];
    return "s2/" + characterName + "/" + itemType + "/" + filename;
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
  public boolean canBePlacedOutside() { return canBePlacedOutside; }
  public Map<String, Integer> getMaterialList() { return materialList; }
  public String getUnicode() { return unicode; }
  
  public int getWidth() { return size.get(0); }
  public int getLength() { return size.get(1); }
  
  @JsonIgnore
  public Image getImage() {
    if (image == null) {
      image = ResourceManager.getImage(imageName);
    }
    return image;
  }
  
  public String getMaterialsAsString() {
    return materialList.entrySet().stream()
            .map(entry -> entry.getKey() + ": " + entry.getValue())
            .collect(Collectors.joining(", "));
  }
  
  public int getBaseOffsetX() {
    return baseOffsetX;
  }
  
  public void setBaseOffsetX(int baseOffsetX) {
    this.baseOffsetX = baseOffsetX;
  }
  
  public int getBaseOffsetY() {
    return baseOffsetY;
  }
  
  public void setBaseOffsetY(int baseOffsetY) {
    this.baseOffsetY = baseOffsetY;
  }
  
}