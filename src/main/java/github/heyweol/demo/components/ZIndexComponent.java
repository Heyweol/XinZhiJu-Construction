package github.heyweol.demo.components;

import com.almasb.fxgl.entity.component.Component;
import com.almasb.fxgl.entity.components.ViewComponent;

import github.heyweol.demo.Item;

public class ZIndexComponent extends Component {
  
  private static final int Y_WEIGHT = 1000; // Give Y position highest priority
  private static final int X_WEIGHT = 10;   // X position secondary priority
  private static final int CARPET_OFFSET = -100000; // Ensure carpets stay below
  
  @Override
  public void onUpdate(double tpf) {
    ViewComponent view = entity.getViewComponent();
    Item item = entity.getObject("item");
    
    // Get base position (top-left corner of the item)
    double baseY = entity.getY();
    double baseX = entity.getX();
    
    // Calculate z-index based on position
    int zIndex = (int)(baseY * Y_WEIGHT + baseX * X_WEIGHT);
    
    // If this is a carpet, ensure it stays below other items
    if (item.isCarpet()) {
        zIndex += CARPET_OFFSET;
    }
    
    view.setZIndex(zIndex);
  }
}