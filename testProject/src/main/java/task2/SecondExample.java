package task2;

public class SecondExample {
  public static void main(String... args) {
    Demo demo = new Demo();
    String s1 = demo.source();
    String s2 = "foo" + s1;
    String s3= "bar" + s2;
    demo.sink(s3);
  }
}
