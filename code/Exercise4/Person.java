public class Person {
    private int age;
    private String name;

    public String getName() { return name; }

    // Not a valid getter
    public int getA() { return age; }

    // Not a valid getter
    public String getAge() { return name; }



    public boolean isAdult() {
        return age > 17;
    }
}