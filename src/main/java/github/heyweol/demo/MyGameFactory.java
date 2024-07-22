package github.heyweol.demo;

import com.almasb.fxgl.dsl.FXGL;
import com.almasb.fxgl.entity.Entity;
import com.almasb.fxgl.entity.EntityFactory;
import com.almasb.fxgl.entity.SpawnData;
import com.almasb.fxgl.entity.Spawns;

import com.almasb.fxgl.inventory.ItemStack;
import com.almasb.fxgl.texture.Texture;
import github.heyweol.demo.components.DraggableComponent;

import static com.almasb.fxgl.dsl.FXGLForKtKt.*;

public class MyGameFactory implements EntityFactory {
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
            .with(new DraggableComponent())
            .build();
  }
  
}
