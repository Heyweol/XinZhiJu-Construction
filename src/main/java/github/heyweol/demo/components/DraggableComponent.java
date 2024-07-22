package github.heyweol.demo.components;

import com.almasb.fxgl.entity.component.Component;
import com.almasb.fxgl.entity.Entity;
import com.almasb.fxgl.entity.components.BoundingBoxComponent;
import com.almasb.fxgl.physics.BoundingShape;
import com.almasb.fxgl.physics.HitBox;
import github.heyweol.demo.EntityType;
import github.heyweol.demo.utils.ShapeUtils;
import javafx.geometry.BoundingBox;
import javafx.geometry.Point2D;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.scene.shape.Polygon;

import static com.almasb.fxgl.dsl.FXGL.*;

public class DraggableComponent extends Component {
  private Point2D dragOffset;

  
  @Override
  public void onAdded() {
    
    
    entity.getViewComponent().addEventHandler(MouseEvent.MOUSE_PRESSED, this::onMousePressed);
    entity.getViewComponent().addEventHandler(MouseEvent.MOUSE_DRAGGED, this::onMouseDragged);
    entity.getViewComponent().addEventHandler(MouseEvent.MOUSE_RELEASED, this::onMouseReleased);
    
    
  }

  
  private void onMousePressed(MouseEvent e) {
    dragOffset = new Point2D(e.getSceneX() - entity.getX(), e.getSceneY() - entity.getY());
    System.out.println("Pressed at: " + dragOffset);
    e.consume();
  }
  
  private void onMouseDragged(MouseEvent e) {
    double newX = e.getSceneX() - dragOffset.getX();
    double newY = e.getSceneY() - dragOffset.getY();
    
    entity.setPosition(newX, newY);

    e.consume();
  }
  
  private void onMouseReleased(MouseEvent e) {
    // You can add logic here if needed when the drag is finished
    System.out.println("Released at: " + entity.getPosition());
    e.consume();
  }

  
}