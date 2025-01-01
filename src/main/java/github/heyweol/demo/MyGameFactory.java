package github.heyweol.demo;

import com.almasb.fxgl.dsl.FXGL;
import static com.almasb.fxgl.dsl.FXGLForKtKt.entityBuilder;
import com.almasb.fxgl.entity.Entity;
import com.almasb.fxgl.entity.EntityFactory;
import com.almasb.fxgl.entity.SpawnData;
import com.almasb.fxgl.entity.Spawns;
import com.almasb.fxgl.texture.Texture;

import github.heyweol.demo.components.GridVisualizerComponent;
import github.heyweol.demo.components.InteractiveItemComponent;
import github.heyweol.demo.components.ZIndexComponent;

public class MyGameFactory implements EntityFactory {
  
  private IsometricGrid isometricGrid;
  private WallGrid leftWallGrid;
  private WallGrid rightWallGrid;
  private GridVisualizerComponent gridVisualizerComponent;
  
  public MyGameFactory(IsometricGrid isometricGrid, WallGrid leftWallGrid, WallGrid rightWallGrid, GridVisualizerComponent gridVisualizerComponent) {
    this.isometricGrid = isometricGrid;
    this.leftWallGrid = leftWallGrid;
    this.rightWallGrid = rightWallGrid;
    this.gridVisualizerComponent = gridVisualizerComponent;
  }
  
  @Spawns("wallItem")
  public Entity newHangingItem(SpawnData data) {
    System.out.println("Spawning wall item");
    System.out.println("data x,y: " + data.getX() + ", " + data.getY());
    Item item = data.get("item");
    Texture texture = FXGL.texture(item.getImageName());
    
    //get aspect ratio
    double originalWidth = texture.getImage().getWidth();
    double originalHeight = texture.getImage().getHeight();
    double aspectRatio = originalHeight / originalWidth;
    
    // Calculate the width based on the item's dimensions and the wall grid's tile width
    double tileWidth = leftWallGrid.getTileWidth();
    double tileHeight = leftWallGrid.getTileHeight();
    double maxItemWidth = tileWidth * item.getNumTileWidth();
    double maxItemHeight = tileHeight * item.getNumTileHeight() + item.getNumTileWidth() * (tileHeight / 2);
    
    double scaledWidth = maxItemWidth;
    double scaledHeight = maxItemHeight;
    if (scaledWidth * aspectRatio > maxItemHeight) {
      scaledWidth = maxItemHeight / aspectRatio;
    } else {
      scaledHeight = scaledWidth * aspectRatio;
    }
    
    texture.setFitWidth(scaledWidth);
    texture.setFitHeight(scaledHeight);
    texture.setPreserveRatio(true);
    
    texture.setScaleX(item.getScale());
    texture.setScaleY(item.getScale());
//    // Calculate base offset
//    double baseOffsetX = (item.getNumTileWidth() - 1) * tileWidth / 2;
//    double baseOffsetY = (item.getNumTileHeight() - 1) * leftWallGrid.getTileHeight() / 2;
    
    return entityBuilder(data)
            .type(EntityType.WALL_ITEM)
            .viewWithBBox(texture)
            .with("itemWidth", item.getNumTileWidth())
            .with("itemLength", item.getNumTileHeight())
            .with("xOffset", item.getXOffset())
            .with("yOffset", item.getYOffset())
            .with("xOffsetMirror", item.getXOffsetMirror())
            .with("yOffsetMirror", item.getYOffsetMirror())
            .with("scale", item.getScale())
            .with("textureFitWidth", texture.getFitWidth())
            .with("textureFitHeight", texture.getFitHeight())
            .with("wallGrid", data.get("wallGrid"))
            .with("isLeftWall", data.get("isLeftWall"))
            .with(new InteractiveItemComponent(isometricGrid, leftWallGrid, rightWallGrid, gridVisualizerComponent))
//            .with(new ZIndexComponent())
//            .zIndex()
            .build();
  }
  
  @Spawns("floorItem")
  public Entity newPlacedItem(SpawnData data) {
    Item item = data.get("item");
    Texture texture = FXGL.texture(item.getImageName());
    
    // Get the original image dimensions
    double originalWidth = texture.getImage().getWidth();
    double originalHeight = texture.getImage().getHeight();
    double aspectRatio = originalHeight / originalWidth;
    
    // Calculate the width based on the item's dimensions and the grid's tile width
    double tileWidth = isometricGrid.getTileWidth();
    double itemWidth = tileWidth + (item.getNumTileWidth() - 1 + item.getNumTileHeight() - 1) * (tileWidth / 2);
    
    // Apply scaling
    double scaledWidth = itemWidth;
    double scaledHeight = scaledWidth * aspectRatio;
    
    texture.setFitWidth(scaledWidth);
    texture.setFitHeight(scaledHeight);
    texture.setPreserveRatio(true);
    System.out.println("fit height: " + texture.getFitHeight());
    
    texture.setScaleX(item.getScale());
    texture.setScaleY(item.getScale());
    
    return entityBuilder(data)
            .type(EntityType.FLOOR_ITEM)
            .viewWithBBox(texture)
            .with("itemWidth", item.getNumTileWidth())
            .with("itemLength", item.getNumTileHeight())
            .with("xOffset", item.getXOffset())
            .with("yOffset", item.getYOffset())
            .with("xOffsetMirror", item.getXOffsetMirror())
            .with("yOffsetMirror", item.getYOffsetMirror())
            .with("scale", item.getScale())
            .with("textureFitWidth", texture.getFitWidth())
            .with("textureFitHeight", texture.getFitHeight())
            .with("grid", isometricGrid)
            .with(new InteractiveItemComponent(isometricGrid, leftWallGrid, rightWallGrid, gridVisualizerComponent))
            .with(new ZIndexComponent())
            .build();
  }
  
}
