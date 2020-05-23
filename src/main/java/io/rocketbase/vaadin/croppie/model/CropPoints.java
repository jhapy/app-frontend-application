package io.rocketbase.vaadin.croppie.model;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class CropPoints {

  private int topLeftX, topLeftY, bottomRightX, bottomRightY;

  public static CropPoints parseArray(String arrayString) {
    if (arrayString != null) {
      Pattern pattern = Pattern.compile(
          ".*\\[[ \"]*([0-9]+)[ \"]*,[ \"]*([0-9]+)[ \"]*,[ \"]*([0-9]+)[ \"]*,[ \"]*([0-9]+)[ \"]*\\].*");
      Matcher matcher = pattern.matcher(arrayString);
      if (matcher.matches()) {
        return new CropPoints(Integer.parseInt(matcher.group(1)),
            Integer.parseInt(matcher.group(2)),
            Integer.parseInt(matcher.group(3)),
            Integer.parseInt(matcher.group(4)));
      }
    }
    return null;
  }

  public String getJsonString() {
    return String.format("[%d, %d, %d, %d]", topLeftX, topLeftY, bottomRightX, bottomRightY);
  }
}