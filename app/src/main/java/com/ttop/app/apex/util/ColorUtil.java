package com.ttop.app.apex.util;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Paint;

import androidx.annotation.ColorInt;
import androidx.annotation.Nullable;
import androidx.palette.graphics.Palette;

import java.util.Collections;
import java.util.Comparator;
import java.util.Timer;

public class ColorUtil {

  @Nullable
  public static Palette generatePalette(Bitmap bitmap) {
    if (bitmap == null) return null;
    return Palette.from(bitmap).generate();
  }

  @ColorInt
  public static int getColor(@Nullable Palette palette, int fallback) {
    if (palette != null) {
      if (palette.getVibrantSwatch() != null) {
        return palette.getVibrantSwatch().getRgb();
      } else if (palette.getMutedSwatch() != null) {
        return palette.getMutedSwatch().getRgb();
      } else if (palette.getDarkVibrantSwatch() != null) {
        return palette.getDarkVibrantSwatch().getRgb();
      } else if (palette.getDarkMutedSwatch() != null) {
        return palette.getDarkMutedSwatch().getRgb();
      } else if (palette.getLightVibrantSwatch() != null) {
        return palette.getLightVibrantSwatch().getRgb();
      } else if (palette.getLightMutedSwatch() != null) {
        return palette.getLightMutedSwatch().getRgb();
      } else if (!palette.getSwatches().isEmpty()) {
        return Collections.max(palette.getSwatches(), SwatchComparator.getInstance()).getRgb();
      }
    }
    return fallback;
  }

  public static int getComplimentColor(Paint paint) {
    return getComplimentColor(paint.getColor());
  }

  /**
   * Returns the complimentary (opposite) color.
   * @param color int RGB color to return the compliment of
   * @return int RGB of compliment color
   */
  public static int getComplimentColor(int color) {
    // get existing colors
    int alpha = Color.alpha(color);
    int red = Color.red(color);
    int blue = Color.blue(color);
    int green = Color.green(color);

    // find compliments
    red = (~red) & 0xff;
    blue = (~blue) & 0xff;
    green = (~green) & 0xff;

    return Color.argb(alpha, red, green, blue);
  }

  /**
   * Converts an int RGB color representation into a hexadecimal {@link String}.
   * @param argbColor int RGB color
   * @return {@link String} hexadecimal color representation
   */
  public static String getHexStringForARGB(int argbColor) {
    String hexString = "#";
    hexString += ARGBToHex(Color.alpha(argbColor));
    hexString += ARGBToHex(Color.red(argbColor));
    hexString += ARGBToHex(Color.green(argbColor));
    hexString += ARGBToHex(Color.blue(argbColor));

    return hexString;
  }

  /**
   * Converts an int R, G, or B value into a hexadecimal {@link String}.
   * @param rgbVal int R, G, or B value
   * @return {@link String} hexadecimal value
   */
  private static String ARGBToHex(int rgbVal) {
    String hexReference = "0123456789ABCDEF";

    rgbVal = Math.max(0,rgbVal);
    rgbVal = Math.min(rgbVal,255);
    rgbVal = Math.round(rgbVal);

    return String.valueOf( hexReference.charAt((rgbVal-rgbVal%16)/16) + "" + hexReference.charAt(rgbVal%16) );
  }

  private static class SwatchComparator implements Comparator<Palette.Swatch> {
    private static SwatchComparator sInstance;

    static SwatchComparator getInstance() {
      if (sInstance == null) {
        sInstance = new SwatchComparator();
      }
      return sInstance;
    }

    @Override
    public int compare(Palette.Swatch lhs, Palette.Swatch rhs) {
      return lhs.getPopulation() - rhs.getPopulation();
    }
  }
}
