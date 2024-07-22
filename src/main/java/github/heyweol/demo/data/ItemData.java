package github.heyweol.demo.data;

public record ItemData(
  String itemName,
  String description,
  String imagePath,
  CostData cost
) {
}
