package github.heyweol.demo.ui;

import com.almasb.fxgl.dsl.FXGL;
import github.heyweol.demo.Item;
import github.heyweol.demo.utils.ResourceManager;
import javafx.application.Platform;
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
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import org.kordamp.ikonli.javafx.FontIcon;
import org.kordamp.ikonli.fontawesome5.FontAwesomeSolid;
import org.kordamp.ikonli.materialdesign2.MaterialDesignA;

import java.util.*;
import java.util.logging.Logger;

public class ItemBar extends VBox {
  private static final Logger LOGGER = Logger.getLogger(ItemBar.class.getName());
  
  private TabPane tabPane;
  private ComboBox<String> characterSelector;
  private Map<String, Map<String, List<Item>>> itemsByCharacter;
  private Item selectedItem;
  private VBox selectedCard;
  
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
    itemsByCharacter.computeIfAbsent(character, k -> new HashMap<>()).put(type, itemList);
    if (!characterSelector.getItems().contains(character)) {
      characterSelector.getItems().add(character);
    }
    // If this is the first character added, select it
    if (characterSelector.getItems().size() == 1) {
      characterSelector.getSelectionModel().select(0);
      updateItemDisplay();
    }
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
      VBox itemCard = createItemCard(item);
      if (itemCard != null) {
        tilePane.getChildren().add(itemCard);
      }
    }
    
    scrollPane.setContent(tilePane);
    scrollPane.setFitToWidth(true);
    tab.setContent(scrollPane);
    tabPane.getTabs().add(tab);
  }
  
  private VBox createItemCard(Item item) {
    if (item.getImage() == null) {
      LOGGER.warning("No image available for item: " + item.getName());
      return null;
    }
    
    VBox card = new VBox(5);
    card.setStyle("-fx-background-color: #E0E0E0; -fx-padding: 5; -fx-background-radius: 5;");
    
    ImageView imageView = new ImageView(ResourceManager.getImage(item.getImageName()));
    imageView.setFitWidth(50);
    imageView.setFitHeight(50);
    imageView.setPreserveRatio(true);
    
    Label nameLabel = new Label(item.getName());
    nameLabel.setFont(Font.font("System", FontWeight.BOLD, 10));
    nameLabel.setWrapText(true);
    nameLabel.setMaxWidth(60);
    
    Text materialsText = new Text(item.getMaterialsAsString());
    materialsText.setFont(Font.font("System", 8));
    materialsText.setWrappingWidth(60);
    
    card.getChildren().addAll(imageView, nameLabel, materialsText);
    
    // Set up drag-and-drop
    card.setOnDragDetected(event -> {
      Dragboard db = card.startDragAndDrop(TransferMode.ANY);
      ClipboardContent content = new ClipboardContent();
      content.putString(item.getName());
      db.setContent(content);
      event.consume();
    });
    
    // Set up selection
    card.setOnMouseClicked(event -> {
      selectItem(card, item);
      event.consume();
    });
    
    return card;
  }
  
  private void selectItem(VBox card, Item item) {
    if (selectedCard != null) {
      selectedCard.setStyle("-fx-background-color: #E0E0E0; -fx-padding: 5; -fx-background-radius: 5;");
    }
    selectedCard = card;
    selectedItem = item;
    card.setStyle("-fx-background-color: #E0E0E0; -fx-padding: 5; -fx-background-radius: 5; -fx-effect: dropshadow(three-pass-box, blue, 10, 0, 0, 0);");
    LOGGER.info("Selected item: " + item.getName());
  }
  
  public void deselectItem() {
    if (selectedCard != null) {
      selectedCard.setStyle("-fx-background-color: #E0E0E0; -fx-padding: 5; -fx-background-radius: 5;");
    }
    selectedCard = null;
    selectedItem = null;
    LOGGER.info("Deselected item");
  }
  
  public Item getSelectedItem() {
    return selectedItem;
  }
}