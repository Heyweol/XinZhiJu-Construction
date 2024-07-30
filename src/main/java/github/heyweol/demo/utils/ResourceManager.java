package github.heyweol.demo.utils;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.DeserializationFeature;
import javafx.scene.image.Image;
import github.heyweol.demo.Item;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class ResourceManager {
  private static final Logger LOGGER = Logger.getLogger(ResourceManager.class.getName());
  private static final Map<String, Image> imageCache = new HashMap<>();
  private static final ObjectMapper objectMapper = new ObjectMapper()
          .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
  
  private static List<Item> loadedItems;
  private static Map<String, List<Item>> itemsByBaseName;
  
  public static void initialize() {
    loadItems();
    preloadImages();
    organizeItemsByBaseName();
  }
  
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
  
  public static void loadItems() {
    try (InputStream is = ResourceManager.class.getResourceAsStream("/assets/data/items.json")) {
      if (is == null) {
        LOGGER.severe("Cannot find items.json");
        loadedItems = new ArrayList<>();
        return;
      }
      loadedItems = objectMapper.readValue(is, new TypeReference<List<Item>>() {});
      LOGGER.info("Loaded " + loadedItems.size() + " items");
      
      // Log the unique characters found
      Set<String> characters = loadedItems.stream()
              .map(item -> item.getFilename().split("_")[1])
              .collect(Collectors.toSet());
      LOGGER.info("Found characters: " + characters);
      
    } catch (IOException e) {
      LOGGER.severe("Failed to load items: " + e.getMessage());
      e.printStackTrace();
      loadedItems = new ArrayList<>();
    }
  }
  
  private static void preloadImages() {
    for (Item item : loadedItems) {
      getImage(item.getImageName());
    }
  }
  
  private static void organizeItemsByBaseName() {
    itemsByBaseName = loadedItems.stream()
            .collect(Collectors.groupingBy(item -> item.getName().split("·")[0]));
  }
  
  public static List<Item> getAllItems() {
    return new ArrayList<>(loadedItems);
  }
  
  public static List<Item> getItemsByBaseName(String baseName) {
    return itemsByBaseName.getOrDefault(baseName, new ArrayList<>());
  }
  
  public static Item getItemByName(String name) {
    return loadedItems.stream()
            .filter(item -> item.getName().equals(name))
            .findFirst()
            .orElse(null);
  }
  
  public static Item getItemByFilename(String filename) {
    return loadedItems.stream()
            .filter(item -> item.getFilename().equals(filename))
            .findFirst()
            .orElse(null);
  }
  
  public static void clearCache() {
    imageCache.clear();
  }
}