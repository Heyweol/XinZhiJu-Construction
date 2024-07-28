package github.heyweol.demo.utils;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.DeserializationFeature;
import javafx.scene.image.Image;
import github.heyweol.demo.Item;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

public class ResourceManager {
  private static final Logger LOGGER = Logger.getLogger(ResourceManager.class.getName());
  private static final Map<String, Image> imageCache = new HashMap<>();
  private static final ObjectMapper objectMapper = new ObjectMapper()
          .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
  
  public static Image getImage(String path) {
    return imageCache.computeIfAbsent(path, k -> {
      try (InputStream is = ResourceManager.class.getResourceAsStream("/assets/textures/" + k)) {
        if (is == null) {
          LOGGER.warning("Image not found: " + k);
          return null;
        }
        return new Image(is);
      } catch (IOException e) {
        LOGGER.severe("Failed to load image: " + k);
        return null;
      }
    });
  }
  
  public static List<Item> loadItems() {
    try (InputStream is = ResourceManager.class.getResourceAsStream("/assets/data/items.json")) {
      if (is == null) {
        LOGGER.severe("Cannot find items.json");
        return List.of();
      }
      List<Item> items = objectMapper.readValue(is, new TypeReference<List<Item>>() {});
      // Preload images for all items
      for (Item item : items) {
        getImage(item.getImageName());
      }
      return items;
    } catch (IOException e) {
      LOGGER.severe("Failed to load items: " + e.getMessage());
      e.printStackTrace(); // Add this line for more detailed error information
      return List.of();
    }
  }
  
  public static void clearCache() {
    imageCache.clear();
  }
}