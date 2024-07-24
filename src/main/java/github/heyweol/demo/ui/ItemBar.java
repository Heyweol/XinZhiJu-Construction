package github.heyweol.demo.ui;

import com.almasb.fxgl.dsl.FXGL;
import com.almasb.fxgl.entity.Entity;
import com.almasb.fxgl.input.UserAction;
import javafx.geometry.Insets;
import javafx.geometry.Point2D;
import javafx.geometry.Pos;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.TilePane;
import javafx.scene.layout.VBox;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import github.heyweol.demo.Item;
import javafx.scene.paint.Color;
import javafx.util.StringConverter;

import static com.almasb.fxgl.dsl.FXGL.*;

public class ItemBar extends VBox {
  private TabPane tabPane;
  private Map<String, List<Item>> items;
  private Entity draggedEntity;
  private Item selectedItem;
  private ImageView selectedImageView;
  private boolean isDragging = false;
  private ComboBox<String> characterSelector;
  private Map<String, Map<String, List<Item>>> itemsByCharacter;
  
  private static final Map<String, String> CHARACTER_NAMES = new HashMap<>();
  static {
    CHARACTER_NAMES.put("fr", "FR");
    CHARACTER_NAMES.put("lb", "LB");
    CHARACTER_NAMES.put("sc", "SC");
    CHARACTER_NAMES.put("yj", "YJ");
    CHARACTER_NAMES.put("zc", "ZC");
  }
  
  public ItemBar(double width, double height) {
    this.setPrefSize(width, height);
    this.setStyle("-fx-background-color: lightgray;");
    
    characterSelector = new ComboBox<>();
    characterSelector.getItems().addAll(CHARACTER_NAMES.keySet());
    characterSelector.setPromptText("Select Character");
    characterSelector.setConverter(new StringConverter<String>() {
      @Override
      public String toString(String initial) {
        return CHARACTER_NAMES.getOrDefault(initial, initial);
      }
      
      @Override
      public String fromString(String fullName) {
        for (Map.Entry<String, String> entry : CHARACTER_NAMES.entrySet()) {
          if (entry.getValue().equals(fullName)) {
            return entry.getKey();
          }
        }
        return fullName;
      }
    });
    characterSelector.setOnAction(e -> updateItemDisplay());
    
    tabPane = new TabPane();
    tabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);
    itemsByCharacter = new HashMap<>();
    
    this.getChildren().addAll(characterSelector, tabPane);
  }
  
  public Item getSelectedItem() {
    return selectedItem;
  }
  
  public void addItemType(String character, String type, List<Item> itemList) {
    itemsByCharacter.computeIfAbsent(character, k -> new HashMap<>()).put(type, itemList);
  }
  
  private void updateItemDisplay() {
    String selectedCharacter = characterSelector.getValue();
    if (selectedCharacter != null && itemsByCharacter.containsKey(selectedCharacter)) {
      tabPane.getTabs().clear();
      Map<String, List<Item>> characterItems = itemsByCharacter.get(selectedCharacter);
      for (Map.Entry<String, List<Item>> entry : characterItems.entrySet()) {
        createTab(entry.getKey(), entry.getValue());
      }
    }
  }
  
  public void addItemType(String type, List<Item> itemList) {
    items.put(type, itemList);
    Tab tab = new Tab(type);
    ScrollPane scrollPane = new ScrollPane();
    TilePane tilePane = new TilePane();
    tilePane.setPrefColumns(3);
    tilePane.setHgap(2);
    tilePane.setVgap(2);
    tilePane.setPadding(new Insets(2));
    tilePane.setAlignment(Pos.TOP_CENTER);
    
    for (Item item : itemList) {
      ImageView imageView = new ImageView(FXGL.image(item.getImageName()));
      imageView.setFitWidth(45);
      imageView.setPreserveRatio(true);
      imageView.setId(item.getName());
      imageView.setOnMouseClicked(event -> {
        if (event.getButton() == MouseButton.PRIMARY) {
          selectItem(imageView, item);
          event.consume();
        }
      });
      
      tilePane.getChildren().add(imageView);
    }
    
    scrollPane.setContent(tilePane);
    scrollPane.setFitToWidth(true);
    tab.setContent(scrollPane);
    tabPane.getTabs().add(tab);
  }
  
  private void createTab(String type, List<Item> itemList) {
    Tab tab = new Tab(type);
    ScrollPane scrollPane = new ScrollPane();
    TilePane tilePane = new TilePane();
    tilePane.setPrefColumns(3);
    tilePane.setHgap(2);
    tilePane.setVgap(2);
    tilePane.setPadding(new Insets(2));
    tilePane.setAlignment(Pos.TOP_CENTER);
    
    for (Item item : itemList) {
      ImageView imageView = new ImageView(FXGL.image(item.getImageName()));
      imageView.setFitWidth(45);
      imageView.setPreserveRatio(true);
      imageView.setId(item.getName());
      imageView.setOnMouseClicked(event -> {
        if (event.getButton() == MouseButton.PRIMARY) {
          selectItem(imageView, item);
          event.consume();
        }
      });
      
      tilePane.getChildren().add(imageView);
    }
    
    scrollPane.setContent(tilePane);
    scrollPane.setFitToWidth(true);
    tab.setContent(scrollPane);
    tabPane.getTabs().add(tab);
  }
  
  private void selectItem(ImageView imageView, Item item) {
    if (selectedImageView != null) {
      selectedImageView.setEffect(null);
    }
    selectedImageView = imageView;
    selectedItem = item;
    DropShadow dropShadow = new DropShadow();
    dropShadow.setColor(Color.BLUE);
    dropShadow.setRadius(10);
    selectedImageView.setEffect(dropShadow);
    System.out.println("Selected item: " + item.getName());
  }
  
  public void deselectItem() {
    if (selectedImageView != null) {
      selectedImageView.setEffect(null);
    }
    selectedImageView = null;
    selectedItem = null;
    System.out.println("Deselected item");
  }
  
  
}