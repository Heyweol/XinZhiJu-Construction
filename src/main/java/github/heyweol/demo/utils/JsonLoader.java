package github.heyweol.demo.utils;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import github.heyweol.demo.Item;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

public class JsonLoader {
  private static final Logger LOGGER = Logger.getLogger(JsonLoader.class.getName());
  
  public static List<Item> loadItems() throws IOException {
    ObjectMapper mapper = new ObjectMapper();
    String resourcePath = "/assets/data/items.json";
    try (InputStream is = JsonLoader.class.getResourceAsStream(resourcePath)) {
      if (is == null) {
        LOGGER.severe("Cannot find resource: " + resourcePath);
        throw new IOException("Cannot find items.json. Ensure it's in the resources/assets/data/ directory.");
      }
      
      List<Map<String, Object>> itemsData = mapper.readValue(is, new TypeReference<List<Map<String, Object>>>() {});
      List<Item> items = new ArrayList<>();
      
      for (Map<String, Object> data : itemsData) {
        try {
          Item item = convertMapToItem(data);
          items.add(item);
        } catch (Exception e) {
          LOGGER.warning("Failed to parse item: " + data.get("name") + ". Error: " + e.getMessage());
          LOGGER.warning("Item data: " + data);
          e.printStackTrace();
        }
      }
      
      LOGGER.info("Loaded " + items.size() + " items");
      return items;
    }
  }
  
  private static Item convertMapToItem(Map<String, Object> map) {
    String filename = (String) map.get("filename");
    String name = (String) map.get("name");
    String imageName = constructImagePath(filename);
    List<Integer> size = parseSize((String) map.get("size"));
    boolean canBePlacedOutside = "Y".equals(map.get("outside"));
    Map<String, Integer> materialList = parseMaterialList((List<String>) map.get("material_list"));
    String unicode = determineUnicode(filename);
    
    LOGGER.fine("Converted item: " + name + ", Image path: " + imageName);
    
    return new Item(filename, name, imageName, size, canBePlacedOutside, materialList, unicode);
  }
  
  private static Map<String, Integer> parseMaterialList(List<String> materialListStrings) {
    Map<String, Integer> materialList = new HashMap<>();
    for (String materialString : materialListStrings) {
      String[] parts = materialString.replaceAll("\"", "").split(":");
      if (parts.length == 2) {
        String material = parts[0].trim();
        int quantity = Integer.parseInt(parts[1].trim());
        materialList.put(material, quantity);
      }
    }
    return materialList;
  }
  
  private static String constructImagePath(String filename) {
    String[] parts = filename.split("_");
    if (parts.length < 4) {
      LOGGER.warning("Invalid filename format: " + filename);
      return filename;
    }
    String characterName = parts[1];
    String itemType = parts[2];
    return "s2/" + characterName + "/" + itemType + "/" + filename;
  }
  
  private static List<Integer> parseSize(String sizeString) {
    String[] sizes = sizeString.replaceAll("[\\[\\]]", "").split(",");
    return List.of(Integer.parseInt(sizes[0].trim()), Integer.parseInt(sizes[1].trim()));
  }
  
  private static Map<String, Integer> parseMaterialList(String materialListString) {
    Map<String, Integer> materialList = new HashMap<>();
    String[] materials = materialListString.split(",");
    for (String material : materials) {
      String[] parts = material.replaceAll("\"", "").split(":");
      if (parts.length == 2) {
        String materialName = parts[0].trim();
        int quantity = Integer.parseInt(parts[1].trim());
        materialList.put(materialName, quantity);
      }
    }
    return materialList;
  }
  
  private static String determineUnicode(String filename) {
    if (filename.contains("guajian")) {
      return "fas-image"; // FontAwesome Solid image icon
    } else if (filename.contains("qiju")) {
      return "mdi-chair-rolling"; // Material Design chair icon
    } else if (filename.contains("zhiwu")) {
      return "fas-leaf"; // FontAwesome Solid leaf icon
    } else if (filename.contains("zhuangshi")) {
      return "fas-image"; // FontAwesome Solid palette icon
    } else {
      return "fas-question"; // FontAwesome Solid question icon
    }
  }
}