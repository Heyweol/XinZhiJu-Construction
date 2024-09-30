package github.heyweol.demo;

import com.almasb.fxgl.dsl.FXGL;
import static com.almasb.fxgl.dsl.FXGLForKtKt.entityBuilder;
import com.almasb.fxgl.entity.Entity;
import com.almasb.fxgl.entity.EntityFactory;
import com.almasb.fxgl.entity.SpawnData;
import com.almasb.fxgl.entity.Spawns;
import com.almasb.fxgl.physics.BoundingShape;
import com.almasb.fxgl.physics.HitBox;
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
    Item item = data.get("item");
    Texture texture = FXGL.texture(item.getImageName());
    
    // Calculate the width based on the item's dimensions and the wall grid's tile width
    double tileWidth = leftWallGrid.getTileWidth();
    double itemWidth = tileWidth * item.getNumTileWidth();
    
    texture.setFitWidth(itemWidth);
    texture.setPreserveRatio(true);
    texture.setScaleX(item.getScale());
    texture.setScaleY(item.getScale());
//    // Calculate base offset
//    double baseOffsetX = (item.getNumTileWidth() - 1) * tileWidth / 2;
//    double baseOffsetY = (item.getNumTileHeight() - 1) * leftWallGrid.getTileHeight() / 2;
    
    double bboxWidth = tileWidth * item.getNumTileWidth();
    double bboxHeight = leftWallGrid.getTileHeight() * item.getNumTileHeight();

    return entityBuilder(data)
            .type(EntityType.WALL_ITEM)
            .view(texture)
            .bbox(new HitBox(BoundingShape.box(bboxWidth, bboxHeight)))
            .with(new InteractiveItemComponent(isometricGrid, leftWallGrid, rightWallGrid, gridVisualizerComponent))
            .with(new ZIndexComponent())
            .with("itemWidth", item.getNumTileWidth())
            .with("itemLength", item.getNumTileHeight())
            .with("xOffset", item.getXOffset())
            .with("yOffset", item.getYOffset())
            .with("xOffsetMirror", item.getXOffsetMirror())
            .with("yOffsetMirror", item.getYOffsetMirror())
            .build();
  }
  
  @Spawns("floorItem")
  public Entity newPlacedItem(SpawnData data) {
    Item item = data.get("item");
    Texture texture = FXGL.texture(item.getImageName());
    
    
    // Calculate the width based on the item's dimensions and the grid's tile width
    double tileWidth = isometricGrid.getTileWidth();
    double itemWidth = tileWidth + (item.getWidth() - 1 + item.getLength() - 1) * (tileWidth / 2);
    
    texture.setFitWidth(itemWidth);
    texture.setPreserveRatio(true);
    
    texture.setScaleX(item.getScale());
    texture.setScaleY(item.getScale());
    
    return entityBuilder(data)
            .type(EntityType.FLOOR_ITEM)
            .viewWithBBox(texture)
            .with(new InteractiveItemComponent(isometricGrid, leftWallGrid, rightWallGrid, gridVisualizerComponent))
            .with(new ZIndexComponent())
            .with("itemWidth", item.getNumTileWidth())
            .with("itemLength", item.getNumTileHeight())
            .with("xOffset", item.getXOffset())
            .with("yOffset", item.getYOffset())
            .with("xOffsetMirror", item.getXOffsetMirror())
            .with("yOffsetMirror", item.getYOffsetMirror())
            .with("scale", item.getScale())
            .build();
  }
  
}
