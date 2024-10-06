package github.heyweol.demo.ui;

import com.almasb.fxgl.app.scene.FXGLMenu;
import com.almasb.fxgl.app.scene.MenuType;
import com.almasb.fxgl.dsl.FXGL;
import javafx.scene.control.Button;
import javafx.scene.layout.VBox;

public class MyGameMenu extends FXGLMenu {
  
  public MyGameMenu() {
    super(MenuType.GAME_MENU);
    
    VBox content = new VBox(10);
    content.setTranslateX(50);
    content.setTranslateY(50);
    
    Button resumeButton = new Button("Resume");
    resumeButton.setOnAction(e -> fireResume());
    
    Button settingsButton = new Button("Settings");
    settingsButton.setOnAction(e -> {
      // TODO: Implement settings functionality
    });
    
    Button exitButton = new Button("Exit to Main Menu");
    exitButton.setOnAction(e -> {
      fireExitToMainMenu();
    });
    
    content.getChildren().addAll(resumeButton, settingsButton, exitButton);
    
    getContentRoot().getChildren().add(content);
  }
}