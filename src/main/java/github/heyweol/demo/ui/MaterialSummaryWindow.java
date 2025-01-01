package github.heyweol.demo.ui;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.effect.DropShadow;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;

public class MaterialSummaryWindow extends VBox {
  private VBox contentBox;
  private double dragDeltaX, dragDeltaY;
  private ScrollPane scrollPane;
  
  public MaterialSummaryWindow() {
    this.setPadding(new Insets(5));
    this.setStyle("-fx-background-color: rgba(255, 255, 255, 0.95); -fx-border-color: #cccccc; -fx-border-width: 1; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.2), 5, 0, 0, 0);");
    
    // Header
    HBox header = createHeader();
    
    // Content
    contentBox = new VBox(3);
    contentBox.setPadding(new Insets(5, 0, 0, 0));
    
    scrollPane = new ScrollPane(contentBox);
    scrollPane.setFitToWidth(true);
    scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
    scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
    scrollPane.setPrefViewportWidth(80);
    scrollPane.setStyle("-fx-background-color: transparent;");
    
    this.getChildren().addAll(header, scrollPane);
    
    // Make window draggable
    header.setOnMousePressed(this::onMousePressed);
    header.setOnMouseDragged(this::onMouseDragged);
  }
  
  private HBox createHeader() {
    HBox header = new HBox();
    header.setAlignment(Pos.CENTER_LEFT);
    header.setPadding(new Insets(0, 0, 3, 0));
    header.setStyle("-fx-border-color: #cccccc; -fx-border-width: 0 0 1 0;");
    
    Label titleLabel = new Label("材料总览");
    titleLabel.setFont(Font.font("SimSun", 12));
    titleLabel.setStyle("-fx-font-weight: bold;");
    
    Region spacer = new Region();
    HBox.setHgrow(spacer, Priority.ALWAYS);
    
    Button closeButton = new Button("×");
    closeButton.setStyle("-fx-background-color: transparent; -fx-text-fill: #666666; -fx-font-size: 12; -fx-padding: 0 0 0 0;");
    closeButton.setOnAction(e -> this.setVisible(false));
    
    header.getChildren().addAll(titleLabel, spacer, closeButton);
    return header;
  }
  
  public void updateMaterials(Map<String, Integer> materials) {
    contentBox.getChildren().clear();
    
    // Sort and group materials
    Map<String, Integer> sortedMaterials = materials.entrySet().stream()
            .sorted((e1, e2) -> {
              boolean isPriority1 = isPriorityMaterial(e1.getKey());
              boolean isPriority2 = isPriorityMaterial(e2.getKey());
              if (isPriority1 && !isPriority2) return -1;
              if (!isPriority1 && isPriority2) return 1;
              return e1.getKey().compareTo(e2.getKey());
            })
            .collect(Collectors.toMap(
                    Map.Entry::getKey,
                    Map.Entry::getValue,
                    (e1, e2) -> e1,
                    LinkedHashMap::new
            ));
    
    sortedMaterials.forEach((material, amount) -> {
      HBox materialEntry = new HBox(5);
      materialEntry.setAlignment(Pos.CENTER_LEFT);
      
      Label materialLabel = new Label(material);
      materialLabel.setFont(Font.font("SimSun", 10));
      Label amountLabel = new Label(amount.toString());
      amountLabel.setFont(Font.font("SimSun", 10));
      amountLabel.setStyle("-fx-font-weight: bold;");
      
      if (isPriorityMaterial(material)) {
        Color goldColor = Color.rgb(218, 165, 32); // Darker gold for better contrast
        materialLabel.setTextFill(goldColor);
        amountLabel.setTextFill(goldColor);
        
        // Add subtle shadow effect to gold text
        DropShadow shadow = new DropShadow();
        shadow.setColor(Color.color(0, 0, 0, 0.3));
        shadow.setRadius(1);
        shadow.setOffsetX(1);
        shadow.setOffsetY(1);
        materialLabel.setEffect(shadow);
        amountLabel.setEffect(shadow);
      } else {
        materialLabel.setTextFill(Color.BLACK);
        amountLabel.setTextFill(Color.BLACK);
      }
      
      materialEntry.getChildren().addAll(materialLabel, amountLabel);
      contentBox.getChildren().add(materialEntry);
    });
    
    // Adjust the window size based on content
    int itemCount = materials.size();
    double rowHeight = 15; // Estimated height of each row
    double headerHeight = 20; // Estimated height of the header
    double contentHeight = itemCount * rowHeight;
    double totalHeight = headerHeight + contentHeight + 10; // +10 for padding
    
    // Set a minimum height and cap the maximum height
    double finalHeight = Math.max(Math.min(totalHeight, 200), 50);
    
    this.setPrefHeight(finalHeight);
    scrollPane.setPrefViewportHeight(finalHeight - headerHeight);
    
    // Make the window visible if it's not empty
    this.setVisible(!materials.isEmpty());
  }
  
  private boolean isPriorityMaterial(String material) {
    return material.endsWith("图纸") || material.equals("荷包");
  }
  
  private void onMousePressed(MouseEvent event) {
    dragDeltaX = this.getLayoutX() - event.getSceneX();
    dragDeltaY = this.getLayoutY() - event.getSceneY();
  }
  
  private void onMouseDragged(MouseEvent event) {
    this.setLayoutX(event.getSceneX() + dragDeltaX);
    this.setLayoutY(event.getSceneY() + dragDeltaY);
  }
}