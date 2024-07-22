package github.heyweol.demo.utils;

import javafx.scene.shape.Polygon;


public class ShapeUtils {
  public static Polygon createDiamondShape(double width, double height) {
    return new Polygon(
            width / 2, 0.0,      // Top
            width, height / 2,   // Right
            width / 2, height,   // Bottom
            0.0, height / 2      // Left
    );
  }
  
  public static double[] toDoubleArray(Polygon polygon) {
    double[] doubleArray = new double[polygon.getPoints().size()];
    for (int i = 0; i < polygon.getPoints().size(); i++) {
      doubleArray[i] = polygon.getPoints().get(i);
    }
    return doubleArray;
  }
}
