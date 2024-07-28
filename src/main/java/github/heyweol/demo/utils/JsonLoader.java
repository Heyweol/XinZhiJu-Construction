package github.heyweol.demo.utils;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import github.heyweol.demo.Item;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.logging.Logger;

public class JsonLoader {
  private static final Logger LOGGER = Logger.getLogger(JsonLoader.class.getName());
  
  public static List<Item> loadItems() throws IOException {
    ObjectMapper mapper = new ObjectMapper();
    InputStream inputStream = JsonLoader.class.getResourceAsStream("/assets/data/items.json");
    if (inputStream == null) {
      LOGGER.severe("Could not find items.json file");
      return List.of();
    }
    List<Map<String, Object>> itemsData = mapper.readValue(inputStream, new TypeReference<List<Map<String, Object>>>(){});
    
    LOGGER.info("Loaded " + itemsData.size() + " items from JSON");
    
    List<Item> items = itemsData.stream().map(JsonLoader::convertMapToItem).collect(Collectors.toList());
    LOGGER.info("Converted " + items.size() + " items");
    return items;
  }
  
  private static Item convertMapToItem(Map<String, Object> map) {
    String filename = (String) map.get("filename");
    String name = (String) map.get("name");
    String imageName = constructImagePath(filename);
    List<Integer> size = parseSize((String) map.get("size"));
    boolean canBePlacedOutside = "Y".equals(map.get("outside"));
    Map<String, Integer> materialList = parseMaterialList((List<String>) map.get("material_list"));
    String unicode = (String) map.get("unicode");
    
    LOGGER.fine("Converted item: " + name + ", Image path: " + imageName);
    
    return new Item(filename, name, imageName, size, canBePlacedOutside, materialList, unicode);
  }
  
  private static String constructImagePath(String filename) {
    // filename format: s2_<charactername>_<itemtype>_<number>.png
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
    return List.of(Integer.parseInt(sizes[0]), Integer.parseInt(sizes[1]));
  }
  
  private static Map<String, Integer> parseMaterialList(List<String> materialListStrings) {
    return materialListStrings.stream()
            .map(s -> s.replaceAll("\"", "").split(":"))
            .collect(Collectors.toMap(
                    arr -> arr[0],
                    arr -> Integer.parseInt(arr[1])
            ));
  }
}