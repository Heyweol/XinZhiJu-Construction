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
  
  public static Map<String, List<Item>> scanAndOrganizeItems() {
    Map<String, List<Item>> organizedItems = new HashMap<>();
    File s2Dir = new File(ASSETS_PATH);
    
    System.out.println("Scanning directory: " + s2Dir.getAbsolutePath());
    
    if (s2Dir.exists() && s2Dir.isDirectory()) {
      System.out.println("S2 directory exists and is a directory");
      File[] nameDirs = s2Dir.listFiles(File::isDirectory);
      if (nameDirs != null) {
        System.out.println("Found " + nameDirs.length + " name directories");
        for (File nameDir : nameDirs) {
          String name = nameDir.getName();
          System.out.println("Processing name directory: " + name);
          List<Item> itemsForName = new ArrayList<>();
          
          File[] typeDirs = nameDir.listFiles(File::isDirectory);
          if (typeDirs != null) {
            for (File typeDir : typeDirs) {
              String type = typeDir.getName();
              System.out.println("Processing type directory: " + type);
              
              File[] files = typeDir.listFiles((dir, fileName) -> fileName.toLowerCase().endsWith(".png"));
              if (files != null) {
                System.out.println("Found " + files.length + " PNG files in " + type);
                for (File file : files) {
                  Matcher matcher = FILE_PATTERN.matcher(file.getName());
                  if (matcher.matches()) {
                    String number = matcher.group(3);
                    String relativePath = "s2/" + name + "/" + type + "/" + file.getName();
                    Item item = new Item(name + "-" + number, relativePath, 50); // Default cost 50
                    itemsForName.add(item);
                    System.out.println("Added item: " + item.getName() + " with path: " + item.getImagePath());
                  } else {
                    System.out.println("File doesn't match pattern: " + file.getName());
                  }
                }
              } else {
                System.out.println("No PNG files found in " + type);
              }
            }
          } else {
            System.out.println("No type directories found in " + name);
          }
          
          if (!itemsForName.isEmpty()) {
            organizedItems.put(name, itemsForName);
            System.out.println("Added " + itemsForName.size() + " items for " + name);
          } else {
            System.out.println("No items found for " + name);
          }
        }
      } else {
        System.out.println("No name directories found");
      }
    } else {
      System.out.println("S2 directory does not exist or is not a directory");
    }
    
    System.out.println("Total categories found: " + organizedItems.size());
    return organizedItems;
  }
}