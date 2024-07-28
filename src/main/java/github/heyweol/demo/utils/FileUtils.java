package github.heyweol.demo.utils;

import github.heyweol.demo.Item;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class FileUtils {
  private static final String ASSETS_PATH = "src/main/resources/assets/textures/s2/";
  private static final Pattern FILE_PATTERN = Pattern.compile("s2_(.+)_(.+)_(\\d+)\\.png");
  
  public static Map<String, Map<String, List<Item>>> scanAndOrganizeItems() {
    Map<String, Map<String, List<Item>>> organizedItems = new HashMap<>();
    File s2Dir = new File(ASSETS_PATH);
    
    if (s2Dir.exists() && s2Dir.isDirectory()) {
      File[] characterDirs = s2Dir.listFiles(File::isDirectory);
      if (characterDirs != null) {
        for (File characterDir : characterDirs) {
          String character = characterDir.getName();
          Map<String, List<Item>> itemsByType = new HashMap<>();
          
          File[] typeDirs = characterDir.listFiles(File::isDirectory);
          if (typeDirs != null) {
            for (File typeDir : typeDirs) {
              String type = typeDir.getName();
              List<Item> itemsForType = new ArrayList<>();
              
              File[] files = typeDir.listFiles((dir, fileName) -> fileName.toLowerCase().endsWith(".png"));
              if (files != null) {
                for (File file : files) {
                  Matcher matcher = FILE_PATTERN.matcher(file.getName());
                  if (matcher.matches()) {
                    String number = matcher.group(3);
                    String relativePath = "s2/" + character + "/" + type + "/" + file.getName();
                    String itemType = convertItemType(type);
//                    Item item = new Item(character + "-" + itemType + "-" + number, relativePath, 50, 1, 1);
//                    itemsForType.add(item);
                  }
                }
              }
              
              if (!itemsForType.isEmpty()) {
                itemsByType.put(convertItemType(type), itemsForType);
              }
            }
          }
          
          if (!itemsByType.isEmpty()) {
            organizedItems.put(character, itemsByType);
          }
        }
      }
    }
    
    return organizedItems;
  }
  
  private static String convertItemType(String originalType) {
    switch (originalType) {
      case "guajia":
        return "hanging";
      case "qiju":
        return "furniture";
      case "zhiwu":
        return "plant";
      case "zhuangshi":
        return "decor";
      default:
        return originalType;
    }
  }
  
}