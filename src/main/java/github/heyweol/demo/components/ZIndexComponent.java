package github.heyweol.demo.components;

import com.almasb.fxgl.entity.component.Component;
import com.almasb.fxgl.entity.components.ViewComponent;
import github.heyweol.demo.Item;

public class ZIndexComponent extends Component {
  
  @Override
  public void onUpdate(double tpf) {
    ViewComponent view = entity.getViewComponent();
    
    // Get base z-index from y-position
    int zIndex = (int) (entity.getY() * 25 + entity.getX());
    
    // If this is a carpet, put it below other items
    Item item = entity.getObject("item");
    if (item.isCarpet()) {
        zIndex -= (int) (entity.getY() + entity.getX()); // This ensures carpets are always below other items
    }
    
    view.setZIndex(zIndex);
  }
}