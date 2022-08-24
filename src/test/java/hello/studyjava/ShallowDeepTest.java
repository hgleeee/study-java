package hello.studyjava;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class ShallowDeepTest {

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    static class Human implements Cloneable {
        private String name;
        private int age;

        // 복사 생성자
        public Human(Human human) {
            this.name = human.name;
            this.age = human.age;
        }

        // 복사 팩터리
        public static Human newInstance(Human human) {
            Human instance = new Human();
            instance.setName(human.getName());
            instance.setAge(human.getAge());
            return instance;
        }

        @Override
        protected Human clone() throws CloneNotSupportedException {
            return (Human) super.clone();
        }
    }

    @Test
    @DisplayName("얕은 복사")
    void shallowCopy() {
        Human h1 = new Human("lee", 20);
        Human h2 = h1;
        Assertions.assertEquals(h1.hashCode(), h2.hashCode());

        h1.setAge(21);
        Assertions.assertEquals(21, h2.getAge());
    }

    @Test
    @DisplayName("깊은 복사 1")
    void deepCopy1() {
        Human h1 = new Human("lee", 20);
        Human h2 = new Human(h1);
        Human h3 = new Human(h1);

        Assertions.assertNotEquals(h1.hashCode(), h2.hashCode());
        Assertions.assertNotEquals(h1.hashCode(), h3.hashCode());

        h1.setAge(21);
        Assertions.assertNotEquals(21, h2.getAge());
        Assertions.assertNotEquals(21, h3.getAge());
    }

    @Test
    @DisplayName("깊은 복사2")
    void deepCopy2() {
        Human h1 = new Human("lee", 20);
        Human h2 = new Human();
        h2.setAge(h1.getAge());
        h2.setName(h1.getName());

        Assertions.assertNotEquals(h1.hashCode(), h2.hashCode());

        h1.setAge(21);
        Assertions.assertNotEquals(21, h2.getAge());
    }

    @Test
    @DisplayName("깊은 복사3")
    void deepCopy3() throws CloneNotSupportedException {
        Human h1 = new Human("lee", 20);
        Human h2 = h1.clone();

        Assertions.assertNotEquals(h1.hashCode(), h2.hashCode());

        h1.setAge(21);
        Assertions.assertNotEquals(21, h2.getAge());
    }
}
