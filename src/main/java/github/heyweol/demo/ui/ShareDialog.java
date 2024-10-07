// New file: ShareDialog.java
package github.heyweol.demo.ui;

import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.geometry.Insets;

public class ShareDialog extends Dialog<ShareDialog.ShareData> {
  private TextField nicknameField;
  private TextArea descriptionArea;
  
  public ShareDialog() {
    setTitle("Share Your Creation");
    setHeaderText("Enter your details to share your screenshot");
    
    ButtonType shareButtonType = new ButtonType("Share", ButtonBar.ButtonData.OK_DONE);
    getDialogPane().getButtonTypes().addAll(shareButtonType, ButtonType.CANCEL);
    
    GridPane grid = new GridPane();
    grid.setHgap(10);
    grid.setVgap(10);
    grid.setPadding(new Insets(20, 150, 10, 10));
    
    nicknameField = new TextField();
    nicknameField.setPromptText("Your nickname");
    descriptionArea = new TextArea();
    descriptionArea.setPromptText("Describe your creation");
    
    grid.add(new Label("Nickname:"), 0, 0);
    grid.add(nicknameField, 1, 0);
    grid.add(new Label("Description:"), 0, 1);
    grid.add(descriptionArea, 1, 1);
    
    getDialogPane().setContent(grid);
    
    setResultConverter(dialogButton -> {
      if (dialogButton == shareButtonType) {
        return new ShareData(nicknameField.getText(), descriptionArea.getText());
      }
      return null;
    });
  }
  
  public static class ShareData {
    public final String nickname;
    public final String description;
    
    public ShareData(String nickname, String description) {
      this.nickname = nickname;
      this.description = description;
    }
  }
}