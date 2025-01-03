package github.heyweol.demo.ui;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import github.heyweol.demo.Item;
import github.heyweol.demo.utils.ResourceManager;
import javafx.geometry.Insets;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.image.ImageView;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.HBox;
import javafx.scene.layout.TilePane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;

public class ItemBar extends VBox {
  private static final Logger LOGGER = Logger.getLogger(ItemBar.class.getName());
  
  private TabPane tabPane;
  private ComboBox<String> seasonSelector;
  private ComboBox<String> characterSelector;
  private Map<String, Map<String, Map<String, List<Item>>>> itemsBySeasonAndCharacter;
  private Map<String, Map<String, List<Item>>> itemsByCharacter;
  private Item selectedItem;
  private VBox selectedCard;
  
  private static final Map<String, String> SEASON_NAMES = Map.of(
          "s2", "儿童劫",
          "s3", "海底捞");
  
  private static final Map<String, String> CHARACTER_NAMES = Map.of(
          "fr", "傅融",
          "lb", "刘辩",
          "sc", "孙策",
          "yj", "袁基",
          "zc", "左慈"
  );
  
  public ItemBar(double width, double height) {
    this.setPrefSize(width, height);
    this.setStyle("-fx-background-color: lightgray;");
    
    seasonSelector = new ComboBox<>();
    seasonSelector.setPromptText("Season");
    seasonSelector.setOnAction(e -> updateCharacterSelector());
    seasonSelector.getStyleClass().add("combo-box");
    
    characterSelector = new ComboBox<>();
    characterSelector.setPromptText("Character");
    characterSelector.setOnAction(e -> updateItemDisplay());
    characterSelector.getStyleClass().add("combo-box");

    // Load the CSS file
    String cssPath = getClass().getResource("/assets/ui/css/drop-down.css").toExternalForm();
    this.getStylesheets().add(cssPath);
    
    // Create an HBox to hold the selectors side by side
    HBox selectorBox = new HBox(10); // 10 is the spacing between elements
    selectorBox.getChildren().addAll(seasonSelector, characterSelector);
    
    tabPane = new TabPane();
    tabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);
    
    itemsBySeasonAndCharacter = new HashMap<>();
    
    this.getChildren().addAll(selectorBox, tabPane);
    
    LOGGER.info("ItemBar initialized");
  }
  
  public void addItemType(String season, String character, String type, List<Item> itemList) {
    itemsBySeasonAndCharacter
            .computeIfAbsent(season, k -> new HashMap<>())
            .computeIfAbsent(character, k -> new HashMap<>())
            .put(type, itemList);
    
    String seasonName = SEASON_NAMES.getOrDefault(season, season);
    if (!seasonSelector.getItems().contains(seasonName)) {
      seasonSelector.getItems().add(seasonName);
    }
    
    // If this is the first season added, select it
    if (seasonSelector.getItems().size() == 1) {
      seasonSelector.getSelectionModel().select(0);
      updateCharacterSelector();
    }
  }
  
  private void updateCharacterSelector() {
    String selectedSeason = seasonSelector.getValue();
    String seasonCode = SEASON_NAMES.entrySet().stream()
            .filter(entry -> entry.getValue().equals(selectedSeason))
            .map(Map.Entry::getKey)
            .findFirst()
            .orElse(selectedSeason);
    
    characterSelector.getItems().clear();
    if (seasonCode != null && itemsBySeasonAndCharacter.containsKey(seasonCode)) {
      itemsBySeasonAndCharacter.get(seasonCode).keySet().stream()
              .map(character -> CHARACTER_NAMES.getOrDefault(character, character))
              .forEach(characterSelector.getItems()::add);
      if (!characterSelector.getItems().isEmpty()) {
        characterSelector.getSelectionModel().select(0);
        updateItemDisplay();
      }
    }
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
    String selectedSeason = seasonSelector.getValue();
    String selectedCharacter = characterSelector.getValue();
    
    String seasonCode = SEASON_NAMES.entrySet().stream()
            .filter(entry -> entry.getValue().equals(selectedSeason))
            .map(Map.Entry::getKey)
            .findFirst()
            .orElse(selectedSeason);
    
    String characterCode = CHARACTER_NAMES.entrySet().stream()
            .filter(entry -> entry.getValue().equals(selectedCharacter))
            .map(Map.Entry::getKey)
            .findFirst()
            .orElse(selectedCharacter);
    
    if (seasonCode != null && characterCode != null &&
            itemsBySeasonAndCharacter.containsKey(seasonCode) &&
            itemsBySeasonAndCharacter.get(seasonCode).containsKey(characterCode)) {
      tabPane.getTabs().clear();
      Map<String, List<Item>> characterItems = itemsBySeasonAndCharacter.get(seasonCode).get(characterCode);
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
    // card.getChildren().addAll(imageView);
    
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