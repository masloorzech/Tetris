package src;

import java.awt.*;

public enum Tetromino {
  I_SHAPE(new int[][]{{1, 1, 1, 1}}, Color.CYAN),
  J_SHAPE(new int[][]{{1, 0, 0}, {1, 1, 1}}, Color.BLUE),
  L_SHAPE(new int[][]{{0, 0, 1}, {1, 1, 1}}, Color.ORANGE),
  O_SHAPE(new int[][]{{1, 1}, {1, 1}}, Color.YELLOW),
  S_SHAPE(new int[][]{{0, 1, 1}, {1, 1, 0}}, Color.GREEN),
  T_SHAPE(new int[][]{{0, 1, 0}, {1, 1, 1}}, Color.MAGENTA),
  Z_SHAPE(new int[][]{{1, 1, 0}, {0, 1, 1}}, Color.RED);

  private final int[][] shape;
  private final Color color;

  Tetromino(int[][] shape, Color color) {
    this.shape = shape;
    this.color = color;
  }

  public int[][] getShape() {
    return shape;
  }

  public Color getColor() {
    return color;
  }
}
