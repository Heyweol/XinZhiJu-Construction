module github.heyweol.demo {
  requires javafx.controls;
  requires javafx.fxml;
  
  requires org.controlsfx.controls;
  requires org.kordamp.ikonli.javafx;
  requires com.almasb.fxgl.all;
  
  opens github.heyweol.demo to javafx.fxml;
  exports github.heyweol.demo;
  
  opens assets.textures to com.almasb.fxgl.all;
}