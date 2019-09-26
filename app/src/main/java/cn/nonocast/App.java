package cn.nonocast;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class App {
  private static final Logger logger = LoggerFactory.getLogger(App.class);

  public static void main(String[] args) {
    // Logger logger = LoggerFactory.getLogger(App.class);
    logger.info("[1 + 2 = {}]", calc(1, 2));
  }

  public static native int calc(int a, int b);

  static {
    System.loadLibrary("calc");
  }
}