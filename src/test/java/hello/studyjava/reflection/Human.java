package hello.studyjava.reflection;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Human {

    private String name;
    protected int age;
    public String hobby;

    public Human() {
    }

    public Human(String name, int age, String hobby) {
        this.name = name;
        this.age = age;
        this.hobby = hobby;
    }

    public void speak(String message) {
        System.out.println(message);
    }

    private void secret() {
        System.out.println("비밀번호는 0000");
    }

    @Override
    public String toString() {
        return "Member{" +
                "name='" + name + "\'" +
                ", age='" + age + "\'" +
                ", hobby='" + hobby + "\'" +
                "}";
    }
}
