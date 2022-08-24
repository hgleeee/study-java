package hello.studyjava.reflection;

import org.junit.jupiter.api.Test;

import java.lang.reflect.Constructor;
import java.util.Arrays;

public class ReflectionTest {

    @Test
    void classInstance() throws ClassNotFoundException {
        Class<Human> humanClass = Human.class;
        System.out.println(System.identityHashCode(humanClass));

        Human human = new Human("Lee", 20, "개발");
        Class<? extends Human> humanClass2 = human.getClass();
        System.out.println(System.identityHashCode(humanClass2));

        Class<?> humanClass3 = Class.forName("hello.studyjava.reflection.Human");
        System.out.println(System.identityHashCode(humanClass3));
    }

    @Test
    void classInstanceTest() throws Exception {
        // Human의 모든 생성자 출력
        Human human = new Human();
        Class<? extends Human> humanClass = human.getClass();
        Arrays.stream(humanClass.getConstructors()).forEach(System.out::println);

        // Human의 기본 생성자를 통한 인스턴스 생성
        Constructor<? extends Human> constructor = humanClass.getConstructor();
        Human human2 = constructor.newInstance();
        System.out.println("human2 = " + human2);

        // Human의 다른 생성자를 통한 인스턴스 생성
        Constructor<? extends Human> fullConstructor = humanClass.getConstructor(String.class, int.class, String.class);
        Human human3 = fullConstructor.newInstance("Lee", 20, "개발");
        System.out.println("human3 = " + human3);
    }
}
