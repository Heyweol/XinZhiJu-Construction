package github.heyweol.demo.ui;

import com.almasb.fxgl.dsl.FXGL;
import com.almasb.fxgl.entity.Entity;
import com.almasb.fxgl.input.UserAction;
import javafx.geometry.Insets;
import javafx.geometry.Point2D;
import javafx.geometry.Pos;
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

import static com.almasb.fxgl.dsl.FXGL.*;

public class ItemBar extends VBox {
  private TabPane tabPane;
  private Map<String, List<Item>> items;
  private Entity draggedEntity;
  private Item selectedItem;
  private ImageView selectedImageView;
  private boolean isDragging = false;
  
  public ItemBar(double width, double height) {
    this.setPrefSize(width, height);
    this.setStyle("-fx-background-color: lightgray;");
    
    tabPane = new TabPane();
    tabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);
    items = new HashMap<>();
    
    this.getChildren().add(tabPane);
  }
  
  public Item getSelectedItem() {
    return selectedItem;
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