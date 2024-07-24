package github.heyweol.demo.components;

import com.almasb.fxgl.entity.component.Component;
import com.almasb.fxgl.entity.components.ViewComponent;

public class ZIndexComponent extends Component {
  
  @Override
  public void onUpdate(double tpf) {
    ViewComponent view = entity.getViewComponent();
    
    // Calculate z-index based on y-position
    // Items with higher y-value (lower on screen) should have higher z-index
    int zIndex = (int) (entity.getY() * 100);
    
    // Set the z-index of the entity's view
    view.setZIndex(zIndex);
  }
}