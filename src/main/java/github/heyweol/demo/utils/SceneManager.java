package github.heyweol.demo.utils;

import com.almasb.fxgl.dsl.FXGL;
import com.almasb.fxgl.entity.Entity;
import com.almasb.fxgl.entity.SpawnData;
import github.heyweol.demo.EntityType;
import github.heyweol.demo.Item;
import github.heyweol.demo.SceneState;
import github.heyweol.demo.components.InteractiveItemComponent;

import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class SceneManager {
  private static final String SAVE_DIRECTORY = "saves";
  private static final String SAVE_EXTENSION = ".ser";
  private static List<Runnable> sceneLoadListeners = new ArrayList<>();
  
  static {
    try {
      Files.createDirectories(Paths.get(SAVE_DIRECTORY));
    } catch (IOException e) {
      e.printStackTrace();
    }
  }
  
  public static void saveScene(String saveName) {
    List<SceneState> sceneStates = new ArrayList<>();
    
    for (Entity entity : FXGL.getGameWorld().getEntitiesByType(EntityType.PLACED_ITEM, EntityType.HANGING)) {
      Item item = entity.getObject("item");
      InteractiveItemComponent component = entity.getComponent(InteractiveItemComponent.class);
      
      SceneState state = new SceneState(
              item.getName(),
              entity.getX(),
              entity.getY(),
              component.isMirrored(),
              item.getFilename()
      );
      sceneStates.add(state);
    }
    
    String fileName = SAVE_DIRECTORY + File.separator + saveName + SAVE_EXTENSION;
    try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(fileName))) {
      oos.writeObject(sceneStates);
    } catch (IOException e) {
      e.printStackTrace();
    }
  }
  
  public static void loadScene(String saveName) {
    String fileName = SAVE_DIRECTORY + File.separator + saveName + SAVE_EXTENSION;
    try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(fileName))) {
      List<SceneState> sceneStates = (List<SceneState>) ois.readObject();
      
      // Clear existing items
      FXGL.getGameWorld().getEntitiesByType(EntityType.PLACED_ITEM, EntityType.HANGING)
              .forEach(Entity::removeFromWorld);
      
      for (SceneState state : sceneStates) {
        Item item = ResourceManager.getItemByName(state.getItemName());
        if (item != null) {
          EntityType type = item.getFilename().contains("guajian") ? EntityType.HANGING : EntityType.PLACED_ITEM;
          Entity entity = FXGL.spawn(type == EntityType.HANGING ? "hangingItem" : "placedItem",
                  new SpawnData(state.getX(), state.getY()).put("item", item));
          
          InteractiveItemComponent component = entity.getComponent(InteractiveItemComponent.class);
          if (state.isMirrored()) {
            component.mirror();
          }
          
          if (!state.getVariant().equals(item.getFilename())) {
            Item variantItem = ResourceManager.getItemByFilename(state.getVariant());
            if (variantItem != null) {
              component.changeVariant(variantItem);
            }
          }
        }
      }
      
      // Notify listeners that the scene has been loaded
      notifySceneLoadListeners();
      
    } catch (IOException | ClassNotFoundException e) {
      e.printStackTrace();
    }
  }
  
  public static void addSceneLoadListener(Runnable listener) {
    sceneLoadListeners.add(listener);
  }
  
  private static void notifySceneLoadListeners() {
    for (Runnable listener : sceneLoadListeners) {
      listener.run();
    }
  }
  
  public static List<String> getSaveFiles() {
    try (Stream<Path> paths = Files.walk(Paths.get(SAVE_DIRECTORY))) {
      return paths
              .filter(Files::isRegularFile)
              .map(Path::getFileName)
              .map(Path::toString)
              .filter(name -> name.endsWith(SAVE_EXTENSION))
              .map(name -> name.substring(0, name.length() - SAVE_EXTENSION.length()))
              .collect(Collectors.toList());
    } catch (IOException e) {
      e.printStackTrace();
      return new ArrayList<>();
    }
  }
  
  public static void deleteSave(String saveName) {
    String fileName = SAVE_DIRECTORY + File.separator + saveName + SAVE_EXTENSION;
    try {
      Files.deleteIfExists(Paths.get(fileName));
    } catch (IOException e) {
      e.printStackTrace();
    }
  }
}