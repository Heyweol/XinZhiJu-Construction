package github.heyweol.demo.utils;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.DeserializationFeature;
import github.heyweol.demo.Item;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

public class JsonLoader {
  private static final Logger LOGGER = Logger.getLogger(JsonLoader.class.getName());
  private static final ObjectMapper objectMapper = new ObjectMapper()
          .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
  
  public static List<Item> loadItems() {
    String resourcePath = "/assets/data/items_test.json";
    try (InputStream is = JsonLoader.class.getResourceAsStream(resourcePath)) {
      if (is == null) {
        LOGGER.severe("Cannot find resource: " + resourcePath);
        throw new IOException("Cannot find items_test.json. Ensure it's in the resources/assets/data/ directory.");
      }
      
      List<Item> items = objectMapper.readValue(is, new TypeReference<List<Item>>() {});
      LOGGER.info("Loaded " + items.size() + " items");
      return items;
    } catch (IOException e) {
      LOGGER.severe("Failed to load items: " + e.getMessage());
      e.printStackTrace();
      return new ArrayList<>();
    }
  }
}