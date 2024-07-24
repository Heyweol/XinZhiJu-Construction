package github.heyweol.demo;

import com.almasb.fxgl.dsl.FXGL;
import com.almasb.fxgl.entity.Entity;
import com.almasb.fxgl.entity.EntityFactory;
import com.almasb.fxgl.entity.SpawnData;
import com.almasb.fxgl.entity.Spawns;

import com.almasb.fxgl.texture.Texture;
import github.heyweol.demo.components.GridVisualizerComponent;
import github.heyweol.demo.components.InteractiveItemComponent;

import static com.almasb.fxgl.dsl.FXGLForKtKt.*;

public class MyGameFactory implements EntityFactory {
  
  private IsometricGrid isometricGrid;
  private GridVisualizerComponent gridVisualizerComponent;
  
  public MyGameFactory(IsometricGrid isometricGrid, GridVisualizerComponent gridVisualizerComponent) {
    this.isometricGrid = isometricGrid;
    this.gridVisualizerComponent = gridVisualizerComponent;
  }
  
  
  @Spawns("furniture")
  public Entity spawnFurniture(SpawnData data) {
    return entityBuilder(data)
            .view(data.get("itemName") + ".png")
            .build();
  }
  
  @Spawns("decor")
  public Entity spawnDecor(SpawnData data) {
    return entityBuilder(data)
            .view(data.get("itemName") + ".png")
            .build();
  }
  
  @Spawns("placedItem")
  public Entity newPlacedItem(SpawnData data) {
    Item item = data.get("item");
    Texture texture = FXGL.texture(item.getImageName());
    texture.setFitWidth(60);
    texture.setPreserveRatio(true);
    return entityBuilder(data)
            .type(EntityType.PLACED_ITEM)
            .viewWithBBox(texture)
//            .with(new SelectableComponent())
//            .with(new DraggableComponent())
            .with(new InteractiveItemComponent(isometricGrid,gridVisualizerComponent))
            .with("itemWidth", item.getWidth())
            .with("itemLength", item.getLength())
            .build();
  }
  
}
