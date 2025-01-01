package github.heyweol.demo.utils;

import github.heyweol.demo.Item;
import javafx.application.Application;
import javafx.geometry.Point2D;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Polygon;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ItemConfigTool extends Application {
  
  private ImageView imageView;
  private Pane gridPane;
  private StackPane centerPane;
  private List<List<Polygon>> gridCells;
  private List<List<Boolean>> gridData;
  private ComboBox<String> itemSelector;
  private Item currentItem;
  private List<Item> allItems;
  private Slider scaleSlider;
  private double gridScale = 1.0;
  private double gridOffsetX = 0;
  private double gridOffsetY = 0;
  private Point2D dragStart;
  private static final double CELL_WIDTH = 50;
  private static final double CELL_HEIGHT = 25;
  private double originalImageWidth;
  private double originalImageHeight;
  private double scaleFactor;
  private Label debugLabel;
  private Rectangle imageBBox;
  private Label gridCenterLabel;
  private Circle gridCenterMarker;
  private double imageOffsetX;
  private double imageOffsetY;
  
  
  @Override
  public void start(Stage primaryStage) {
    BorderPane root = new BorderPane();
    
    // Top controls
    VBox controls = new VBox(10);
    HBox topControls = new HBox(10);
    itemSelector = new ComboBox<>();
    itemSelector.setOnAction(e -> loadSelectedItem());
    scaleSlider = new Slider(0.5, 5.0, 1.0);
    scaleSlider.setShowTickLabels(true);
    scaleSlider.setShowTickMarks(true);
    scaleSlider.valueProperty().addListener((obs, oldVal, newVal) -> {
      gridScale = newVal.doubleValue();
      updateGrid();
    });
    Button saveButton = new Button("Save Configuration");
    saveButton.setOnAction(e -> saveConfiguration());
    topControls.getChildren().addAll(new Label("Select Item:"), itemSelector,
            new Label("Grid Scale:"), scaleSlider, saveButton);
    
    debugLabel = new Label();
    gridCenterLabel = new Label();
    controls.getChildren().addAll(topControls, debugLabel, gridCenterLabel);
    
    // Center content
    centerPane = new StackPane();
    imageView = new ImageView();
    imageView.setPreserveRatio(true);
    gridPane = new Pane();
    imageBBox = new Rectangle();
    imageBBox.setFill(Color.TRANSPARENT);
    imageBBox.setStroke(Color.RED);
    imageBBox.setStrokeWidth(2);
    gridCenterMarker = new Circle(3, Color.BLUE);
    
    Group imageGroup = new Group(imageView, gridPane);
    centerPane.getChildren().addAll(imageGroup, imageBBox, gridCenterMarker);
    
    // Grid pane event handlers for dragging
    gridPane.setOnMousePressed(this::startDrag);
    gridPane.setOnMouseDragged(this::drag);
    gridPane.setOnMouseMoved(this::updateDebugInfo);
    
    root.setTop(controls);
    root.setCenter(centerPane);
    
    Scene scene = new Scene(root, 800, 600);
    primaryStage.setTitle("Item Configuration Tool");
    primaryStage.setScene(scene);
    primaryStage.show();
    
    loadItems();
  }
  
  private void loadItems() {
    allItems = JsonLoader.loadItems();
    itemSelector.getItems().addAll(allItems.stream().map(Item::getName).collect(Collectors.toList()));
  }
  
  private void loadSelectedItem() {
    String selectedItemName = itemSelector.getValue();
    currentItem = allItems.stream()
            .filter(item -> item.getName().equals(selectedItemName))
            .findFirst()
            .orElse(null);
    
    if (currentItem != null) {
      Image image = ResourceManager.getImage(currentItem.getImageName());
      imageView.setImage(image);
      
      originalImageWidth = image.getWidth();
      originalImageHeight = image.getHeight();
      
      // Scale image to fit in the view
      scaleFactor = Math.min(400 / image.getWidth(), 400 / image.getHeight());
      imageView.setFitWidth(image.getWidth() * scaleFactor);
      imageView.setFitHeight(image.getHeight() * scaleFactor);
      
      imageBBox.setWidth(imageView.getFitWidth());
      imageBBox.setHeight(imageView.getFitHeight());
      
      // Center the image in the view
      imageOffsetX = (centerPane.getWidth() - imageView.getFitWidth()) / 2;
      imageOffsetY = (centerPane.getHeight() - imageView.getFitHeight()) / 2;
      imageView.setLayoutX(imageOffsetX);
      imageView.setLayoutY(imageOffsetY);
      imageBBox.setX(imageOffsetX);
      imageBBox.setY(imageOffsetY);
      
      // Position the grid over the image
      gridOffsetX = imageOffsetX;
      gridOffsetY = imageOffsetY;
      gridScale = Math.min(imageView.getFitWidth() / (currentItem.getNumTileWidth() * CELL_WIDTH),
              imageView.getFitHeight() / (currentItem.getNumTileHeight() * CELL_HEIGHT));
      scaleSlider.setValue(gridScale);
      
      updateGrid();
      updateDebugInfo(null);
    }
  }
  
  private void updateGrid() {
    gridPane.getChildren().clear();
    gridCells = new ArrayList<>();
    gridData = new ArrayList<>();
    
    int gridWidth = currentItem.getNumTileWidth();
    int gridLength = currentItem.getNumTileHeight();
    
    for (int y = 0; y < gridLength; y++) {
      List<Polygon> row = new ArrayList<>();
      List<Boolean> dataRow = new ArrayList<>();
      for (int x = 0; x < gridWidth; x++) {
        Polygon cell = createIsometricCell(x, y);
        gridPane.getChildren().add(cell);
        row.add(cell);
        dataRow.add(false);
      }
      gridCells.add(row);
      gridData.add(dataRow);
    }
    
    updateGridCenterDisplay();
  }
  private Point2D getGridCenter() {
    int gridWidth = currentItem.getNumTileWidth();
    int gridLength = currentItem.getNumTileHeight();
    
    return gridToImageCoordinates((int) (gridWidth / 2.0), (int) (gridLength / 2.0));
  }
  
  private void updateGridCenterDisplay() {
    Point2D centerPoint = getGridCenter();
    gridCenterMarker.setCenterX(centerPoint.getX());
    gridCenterMarker.setCenterY(centerPoint.getY());
    gridCenterLabel.setText(String.format("Grid Center: (%.2f, %.2f)",
            centerPoint.getX() - imageOffsetX,
            centerPoint.getY() - imageOffsetY));
  }
  
  private Point2D gridToImageCoordinates(int x, int y) {
    double cellWidth = CELL_WIDTH * gridScale;
    double cellHeight = CELL_HEIGHT * gridScale;
    
    double isoX = gridOffsetX + (x - y) * cellWidth / 2;
    double isoY = gridOffsetY + (x + y) * cellHeight / 2;
    
    return new Point2D(isoX, isoY);
  }
  private Polygon createIsometricCell(int x, int y) {
    double cellWidth = CELL_WIDTH * gridScale;
    double cellHeight = CELL_HEIGHT * gridScale;
    Point2D cellPos = gridToImageCoordinates(x, y);
    
    Polygon diamond = new Polygon(
            cellPos.getX(), cellPos.getY(),
            cellPos.getX() + cellWidth / 2, cellPos.getY() + cellHeight / 2,
            cellPos.getX(), cellPos.getY() + cellHeight,
            cellPos.getX() - cellWidth / 2, cellPos.getY() + cellHeight / 2
    );
    
    diamond.setFill(Color.TRANSPARENT);
    diamond.setStroke(Color.BLACK);
    diamond.setStrokeWidth(0.5);
    
    diamond.setOnMouseClicked(e -> toggleCell(diamond, x, y));
    diamond.setOnMouseEntered(e -> {
      if (e.isShiftDown()) {
        toggleCell(diamond, x, y);
      }
    });
    
    return diamond;
  }
  
  private void toggleCell(Polygon cell, int x, int y) {
    boolean isSelected = !gridData.get(y).get(x);
    gridData.get(y).set(x, isSelected);
    cell.setFill(isSelected ? Color.RED.deriveColor(1, 1, 1, 0.5) : Color.TRANSPARENT);
  }
  
  private void startDrag(MouseEvent event) {
    dragStart = new Point2D(event.getX(), event.getY());
  }
  
  private void drag(MouseEvent event) {
    if (dragStart != null) {
      double deltaX = event.getX() - dragStart.getX();
      double deltaY = event.getY() - dragStart.getY();
      gridOffsetX += deltaX;
      gridOffsetY += deltaY;
      updateGrid();
      dragStart = new Point2D(event.getX(), event.getY());
    }
    updateDebugInfo(event);
  }
  
  private void updateDebugInfo(MouseEvent event) {
    String mouseInfo = "Mouse: N/A";
    if (event != null) {
      double mouseX = event.getX() - imageOffsetX;
      double mouseY = event.getY() - imageOffsetY;
      double relativeX = mouseX / imageView.getFitWidth();
      double relativeY = mouseY / imageView.getFitHeight();
      mouseInfo = String.format("Mouse: (%.2f, %.2f) Relative: (%.4f, %.4f)", mouseX, mouseY, relativeX, relativeY);
    }
    debugLabel.setText(String.format("Original Size: %.0fx%.0f, Displayed Size: %.0fx%.0f, Scale Factor: %.4f, Grid Scale: %.4f, %s",
            originalImageWidth, originalImageHeight, imageView.getFitWidth(), imageView.getFitHeight(), scaleFactor, gridScale, mouseInfo));
  }
  
  private void saveConfiguration() {
    if (currentItem == null) {
      showAlert("No item selected", "Please select an item before saving the configuration.");
      return;
    }
    
    ObjectMapper mapper = new ObjectMapper();
    ObjectNode itemNode = mapper.createObjectNode();
    
    itemNode.put("filename", currentItem.getFilename());
    itemNode.put("name", currentItem.getName());
    itemNode.put("size", "[" + currentItem.getNumTileWidth() + "," + currentItem.getNumTileHeight() + "]");
    itemNode.put("outside", currentItem.canBePlacedOutside() ? "Y" : "N");
    
    ArrayNode materialList = mapper.createArrayNode();
    for (Map.Entry<String, Integer> entry : currentItem.getMaterialList().entrySet()) {
      materialList.add("\"" + entry.getKey() + "\":\"" + entry.getValue() + "\"");
    }
    itemNode.set("material_list", materialList);
    
    ArrayNode baseShape = mapper.createArrayNode();
    Point2D gridCenter = getGridCenter();
//    for (int y = 0; y < gridData.size(); y++) {
//      for (int x = 0; x < gridData.get(y).size(); x++) {
//        if (gridData.get(y).get(x)) {
//          double cellWidth = CELL_WIDTH * gridScale;
//          double cellHeight = CELL_HEIGHT * gridScale;
//          double centerX = gridOffsetX + (x - y) * cellWidth / 2 + cellWidth / 4;
//          double centerY = gridOffsetY + (x + y) * cellHeight / 2 + cellHeight / 4;
//
//          // Convert to relative coordinates
//          double relativeX = centerX / imageView.getFitWidth();
//          double relativeY = centerY / imageView.getFitHeight();
//
//          baseShape.add(String.format("%.4f,%.4f", relativeX, relativeY));
//        }
//      }
//    }
    for (int y = 0; y < gridData.size(); y++) {
      for (int x = 0; x < gridData.get(y).size(); x++) {
        if (gridData.get(y).get(x)) {
          Point2D cellCenter = gridToImageCoordinates(x, y);
          
          // Calculate relative position from image top-left corner
          double relativeX = (cellCenter.getX() - imageOffsetX) / imageView.getFitWidth();
          double relativeY = (cellCenter.getY() - imageOffsetY) / imageView.getFitHeight();
          
          baseShape.add(String.format("%.4f,%.4f", relativeX, relativeY));
        }
      }
    }
    itemNode.set("base_shape", baseShape);
    
    try {
      File outputFile = new File("updated_items.json");
      List<ObjectNode> allItemNodes = new ArrayList<>();
      
      if (outputFile.exists()) {
        allItemNodes = mapper.readValue(outputFile, mapper.getTypeFactory().constructCollectionType(List.class, ObjectNode.class));
      }
      
      // Update or add the current item
      boolean updated = false;
      for (int i = 0; i < allItemNodes.size(); i++) {
        if (allItemNodes.get(i).get("filename").asText().equals(currentItem.getFilename())) {
          allItemNodes.set(i, itemNode);
          updated = true;
          break;
        }
      }
      if (!updated) {
        allItemNodes.add(itemNode);
      }
      
      mapper.writerWithDefaultPrettyPrinter().writeValue(outputFile, allItemNodes);
      showAlert("Configuration Saved", "Configuration saved to " + outputFile.getAbsolutePath());
    } catch (IOException e) {
      e.printStackTrace();
      showAlert("Error", "Failed to save configuration: " + e.getMessage());
    }
  }
  
  private void showAlert(String title, String content) {
    Alert alert = new Alert(Alert.AlertType.INFORMATION);
    alert.setTitle(title);
    alert.setHeaderText(null);
    alert.setContentText(content);
    alert.showAndWait();
  }
  
  public static void main(String[] args) {
    launch(args);
  }
}