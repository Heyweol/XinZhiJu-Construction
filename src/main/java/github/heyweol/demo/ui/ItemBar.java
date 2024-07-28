package github.heyweol.demo.ui;

import com.almasb.fxgl.dsl.FXGL;
import github.heyweol.demo.Item;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.ImageView;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.TilePane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

public class ItemBar extends VBox {
  private static final Logger LOGGER = Logger.getLogger(ItemBar.class.getName());
  
  private TabPane tabPane;
  private ComboBox<String> characterSelector;
  private Map<String, Map<String, List<Item>>> itemsByCharacter;
  private Item selectedItem;
  private ImageView selectedImageView;
  
  public ItemBar(double width, double height) {
    this.setPrefSize(width, height);
    this.setStyle("-fx-background-color: lightgray;");
    
    characterSelector = new ComboBox<>();
    characterSelector.setPromptText("Select Character");
    characterSelector.setOnAction(e -> updateItemDisplay());
    
    tabPane = new TabPane();
    tabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);
    itemsByCharacter = new HashMap<>();
    
    this.getChildren().addAll(characterSelector, tabPane);
    
    LOGGER.info("ItemBar initialized");
  }
  
  public void addItemType(String character, String type, List<Item> itemList) {
    LOGGER.info("Adding " + itemList.size() + " items for character " + character + " and type " + type);
    itemsByCharacter.computeIfAbsent(character, k -> new HashMap<>()).put(type, itemList);
    if (!characterSelector.getItems().contains(character)) {
      characterSelector.getItems().add(character);
    }
  }
  
  private void updateItemDisplay() {
    String selectedCharacter = characterSelector.getValue();
    LOGGER.info("Updating display for character: " + selectedCharacter);
    if (selectedCharacter != null && itemsByCharacter.containsKey(selectedCharacter)) {
      tabPane.getTabs().clear();
      Map<String, List<Item>> characterItems = itemsByCharacter.get(selectedCharacter);
      for (Map.Entry<String, List<Item>> entry : characterItems.entrySet()) {
        createTab(entry.getKey(), entry.getValue());
      }
    }
  }
  
  private void createTab(String type, List<Item> itemList) {
    LOGGER.info("Creating tab for type " + type + " with " + itemList.size() + " items");
    Tab tab = new Tab(type);
    ScrollPane scrollPane = new ScrollPane();
    TilePane tilePane = new TilePane();
    tilePane.setPrefColumns(3);
    tilePane.setHgap(5);
    tilePane.setVgap(5);
    tilePane.setPadding(new Insets(5));
    
    for (Item item : itemList) {
      ImageView imageView = createImageViewForItem(item);
      if (imageView != null) {
        tilePane.getChildren().add(imageView);
      }
    }
    
    scrollPane.setContent(tilePane);
    scrollPane.setFitToWidth(true);
    tab.setContent(scrollPane);
    tabPane.getTabs().add(tab);
  }
  
  private ImageView createImageViewForItem(Item item) {
    if (item.getImage() == null) {
      LOGGER.warning("No image available for item: " + item.getName());
      return null;
    }
    
    ImageView imageView = new ImageView(item.getImage());
    imageView.setFitWidth(50);
    imageView.setFitHeight(50);
    imageView.setPreserveRatio(true);
    
    // Set up drag-and-drop
    imageView.setOnDragDetected(event -> {
      Dragboard db = imageView.startDragAndDrop(TransferMode.ANY);
      ClipboardContent content = new ClipboardContent();
      content.putString(item.getName());
      db.setContent(content);
      event.consume();
    });
    
    // Set up selection
    imageView.setOnMouseClicked(event -> {
      selectItem(imageView, item);
      event.consume();
    });
    
    return imageView;
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
    LOGGER.info("Selected item: " + item.getName());
  }
  
  public void deselectItem() {
    if (selectedImageView != null) {
      selectedImageView.setEffect(null);
    }
    selectedImageView = null;
    selectedItem = null;
    LOGGER.info("Deselected item");
  }
  
  public Item getSelectedItem() {
    return selectedItem;
  }
}