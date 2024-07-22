package github.heyweol.demo.components;

import com.almasb.fxgl.dsl.FXGL;
import com.almasb.fxgl.entity.component.Component;
import com.almasb.fxgl.texture.Texture;
import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.RadialGradient;
import javafx.scene.paint.Stop;
import javafx.scene.shape.Circle;

public class BuildIndicatorComponent extends Component {
  
  private final RadialGradient okFill;
  private final RadialGradient disabledFill;
  
  private Texture texture;
  private Circle circle;

  public BuildIndicatorComponent() {
    okFill = new RadialGradient(
            0.0, 0.0, 0.5, 0.5, 0.5, true, CycleMethod.NO_CYCLE,
            new Stop(0.9, new Color(1.0, 0.0, 0.0, 0.0)),
            new Stop(1.0, new Color(0.0, 1.0, 0.39, 0.4)));
    disabledFill = new RadialGradient(
            0.0, 0.0, 0.5, 0.5, 0.5, true, CycleMethod.NO_CYCLE,
            new Stop(0.9, new Color(1.0, 0.0, 0.0, 0.0)),
            new Stop(1.0, new Color(1.0, 0.0, 0.0, 0.38)));
  }
  
  @Override
  public void onAdded() {
  
    texture = FXGL.texture("furniture/袁基-书架1.png");
    circle = new Circle(30, disabledFill);
    circle.setTranslateX(texture.getWidth() / 2.0);
    circle.setTranslateY(texture.getHeight() / 2.0);
    entity.getViewComponent().addChild(texture);
    entity.getViewComponent().addChild(circle);
    
  }
  
  public void canBuild(boolean canBuild) {
    circle.setFill(canBuild ? okFill : disabledFill);
  }
  
  
}
