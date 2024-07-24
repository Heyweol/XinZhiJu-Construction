package github.heyweol.demo.components;

import com.almasb.fxgl.entity.component.Component;
import com.almasb.fxgl.dsl.FXGL;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import javafx.scene.Group;

import github.heyweol.demo.IsometricGrid;

public class GridVisualizerComponent extends Component {
  private IsometricGrid grid;
  private Group gridLines;
  
  public GridVisualizerComponent(IsometricGrid grid) {
    this.grid = grid;
    this.gridLines = new Group();
  }
  
  @Override
  public void onAdded() {
    drawGrid();
    gridLines.setVisible(false);
    entity.getViewComponent().addChild(gridLines);
  }
  
  private void drawGrid() {
    double tileWidth = grid.getTileWidth();
    double tileHeight = grid.getTileHeight();
    int gridWidth = grid.getGridWidth();
    int gridLength = grid.getGridLength();
    
    // Draw horizontal lines
    for (int y = 0; y <= gridLength; y++) {
      Line line = new Line();
      line.setStartX((0 - y) * tileWidth / 2);
      line.setStartY(y * tileHeight / 2);
      line.setEndX((gridWidth - y) * tileWidth / 2);
      line.setEndY((y + gridWidth) * tileHeight / 2);
      line.setStroke(Color.LIGHTGRAY);
      gridLines.getChildren().add(line);
    }
    
    // Draw vertical lines
    for (int x = 0; x <= gridWidth; x++) {
      Line line = new Line();
      line.setStartX(x * tileWidth / 2);
      line.setStartY(0);
      line.setEndX((x - gridLength) * tileWidth / 2);
      line.setEndY(gridLength * tileHeight / 2);
      line.setStroke(Color.LIGHTGRAY);
      gridLines.getChildren().add(line);
    }
  }
  
  public void show() {
    gridLines.setVisible(true);
  }
  
  public void hide() {
    gridLines.setVisible(false);
  }
}