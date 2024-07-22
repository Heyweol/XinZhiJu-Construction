package github.heyweol.demo.components;

import com.almasb.fxgl.dsl.FXGL;
import com.almasb.fxgl.entity.component.Component;
import github.heyweol.demo.data.CostData;
import github.heyweol.demo.data.ItemData;
import javafx.geometry.Point2D;
import javafx.scene.image.Image;
import javafx.scene.input.MouseEvent;

public class ItemComponent extends Component {
  private ItemData itemData;
  private String name;
  private String description;
  private CostData cost;
  private String imagePath;
  private Point2D itemPosition;
  
  
  @Override
  public void onAdded() {
//    itemData = entity.getObject("itemData");
//    name = itemData.itemName();
//    description = itemData.description();
//    cost = itemData.cost();
//    imagePath = itemData.imagePath();
//
//    itemPosition = entity.getPosition();
//    entity.getViewComponent().addChild(FXGL.texture(imagePath));
    
    entity.getViewComponent().addEventHandler(MouseEvent.MOUSE_PRESSED, this::onMousePressed);
    
    
    
  }
  
  private void onMousePressed(MouseEvent e) {
    System.out.println("Pressed at: " + itemPosition);
    e.consume();
  }
  
  
}
