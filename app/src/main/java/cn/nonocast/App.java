package cn.nonocast;

public class App {
  public static void main(String[] args) {
    System.out.println(calc(1, 2));
  }

  public static native int calc(int a, int b);

  static {
    System.loadLibrary("calc");
  }
}