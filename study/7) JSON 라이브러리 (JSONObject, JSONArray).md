# JSON 라이브러리 (JSONObject, JSONArray)
- Java에서 org.json 라이브러리를 이용하여 JSON 데이터를 다룰 수 있다.
- 해당 라이브러리에서 제공하는 JSONObject, JSONArray 클래스는 JSON 데이터를 갖고 있고, JSON 형식의 문자열로 출력할 수 있다.
  - 또한 JSON 문자열을 파일로 저장할 수도 있다.

```
JSONObject 객체 생성
HashMap으로 JSONObject 생성
JSON 문자열로 JSONObject 객체 생성
POJO로 JSONObject 객체 생성
JSONArray 객체 생성
List로 JSONArray 객체 생성
Java에서 JSON을 파일로 저장
```

## 1. JSON 이란?
- JSON은 JavaScript Object Notation의 약자로, Javascript에서 데이터를 전달하기 위해 만들어졌다.
- JSON 파일은 다음과 같이 key-value 형태로 데이터를 갖고 있다.

```json
{
   "pageInfo": {
         "pageName": "abc",
         "pagePic": "http://example.com/content.jpg"
    },
    "posts": [
         {
              "post_id": "123456789012_123456789012",
              "message": "Sounds cool. Can't wait to see it!",
              "likesCount": "2",
         }
    ]
}
```

## 2. JSON 라이브러리 의존성
- org.json은 Java의 JSON 라이브러리이다.
- gradle 프로젝트에서 다음과 같이 build.gradle에 의존성을 추가할 수 있다.

```php
dependencies {
    ...
    implementation group: 'org.json', name: 'json', version: '20090211'
}
```

## 3. JSONObject
- JSONObject는 JSON에서 key-value 쌍으로 데이터를 표현하는 객체이다.
- JSONObject에 데이터를 입력할 때는 put(key, value)으로 입력한다.
- toString()은 JSONObject가 갖고 있는 데이터를 JSON 형식으로 출력한다.

```java
import org.json.JSONException;
import org.json.JSONObject;

public class JsonExample {

    public static void main(String[] args) throws JSONException {

        JSONObject jo = new JSONObject();
        jo.put("name", "Jone");
        jo.put("city", "Seoul");

        System.out.println(jo.toString());
    }
}
```
```bash
{"city":"Seoul","name":"Jone"}
```

## 4. HashMap으로 JSONObject 생성
- HashMap에 저장된 데이터를 JSON으로 변환할 수 있다.
- 다음과 같이 HashMap 객체를 JSONObject 생성자의 인자로 전달하면 된다.

```java
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class JsonExample2 {

    public static void main(String[] args) throws JSONException {

        Map<String, String> map = new HashMap<>();
        map.put("name", "Jone");
        map.put("city", "Seoul");

        JSONObject jo = new JSONObject(map);

        System.out.println(jo.toString());
    }
}
```

```bash
{"city":"Seoul","name":"Jone"}
```

## 5. JSON 문자열로 JSONObject 객체 생성
- JSON 문자열로 JSONObject 객체를 생성할 수 있다.
- 이렇게 생성된 JSONObject에 데이터를 추가할 수도 있다.

```java
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Iterator;

public class JsonExample3 {

    public static void main(String[] args) throws JSONException {

        JSONObject jo = new JSONObject("{\"city\":\"Seoul\",\"name\":\"Jone\"}");

        Iterator it = jo.keys();
        while (it.hasNext()) {
            String key = (String) it.next();
            System.out.println("key: " + key + ", value: " + jo.getString(key));
        }
        System.out.println(jo.toString());

        jo.put("age", "30");
        System.out.println(jo.toString());
    }
}
```
```bash
{"city":"Seoul","name":"Jone"}
{"city":"Seoul","name":"Jone","age":"30"}
```

## 6. POJO로 JSONObject 객체 생성
- POJO(Plain Old Java Object)는 아래 예제의 Customer 클래스와 같이, 단순히 get, set 메소드들만 있는 자바 클래스를 의미한다.
- JSONObject는 POJO 객체를 인자로 받으며, 여기서 key와 value를 추출하여 JSON 데이터로 추가한다.

```java
import org.json.JSONException;
import org.json.JSONObject;

public class JsonExample4 {

    public static class Customer {
        private String name;
        private String city;

        Customer(String name, String city) {
            this.name = name;
            this.city = city;
        }
        public void setName(String name) {
            this.name = name;
        }
        public void setCity(String city) {
            this.city = city;
        }
        public String getCity() {
            return city;
        }
        public String getName() {
            return name;
        }
    }


    public static void main(String[] args) throws JSONException {

        Customer customer = new Customer("Jone", "Seoul");

        JSONObject jo = new JSONObject(customer);

        System.out.println(jo.toString());
    }
}
```

```bash
{"city":"Seoul","name":"Jone"}
```

## 7. JSONArray
- JSON은 key-value 형태로 데이터를 갖고 있는데, 여기서 value는 아래와 같이 Array 타입이 될 수 있다.

```json
"friends":["Harry","Sam"]
```

- 다음 예제와 같이 JSONArray에 put(value)로 0개 이상의 데이터를 배열에 추가할 수 있다.
- 그리고 JSONArray는 JSONObject의 value가 되도록 다시 추가할 수 있다.

```java
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class JsonExample5 {

    public static void main(String[] args) throws JSONException {

        JSONArray ja = new JSONArray();
        ja.put("Harry");
        ja.put("Sam");

        JSONObject jo = new JSONObject();
        jo.put("name", "Jone");
        jo.put("city", "Seoul");
        jo.put("friends", ja);

        System.out.println(jo.toString());
    }
}
```

```bash
{"city":"Seoul","name":"Jone","friends":["Harry","Sam"]}
```

## 8. List로 JSONArray 객체 생성
- JSONArray 생성자는 인자로 List를 받고, List의 모든 데이터를 JSONArray에 추가한다.

```java
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class JsonExample6 {

    public static void main(String[] args) throws JSONException {

        List<String> list = new ArrayList<>();
        list.add("Harry");
        list.add("Sam");

        JSONArray ja = new JSONArray(list);

        JSONObject jo = new JSONObject();
        jo.put("name", "Jone");
        jo.put("city", "Seoul");
        jo.put("friends", ja);

        System.out.println(jo.toString());
    }
}
```
```bash
{"city":"Seoul","name":"Jone","friends":["Harry","Sam"]}
```

## 9. JSON을 파일로 저장
- Java의 JSON 객체들이 갖고 있는 데이터를 JSON 형식의 문자열로 변환하고 파일로 저장하는 예제이다.

```java
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class JsonExample7 {

    public static void main(String[] args) throws JSONException, IOException {

        JSONObject jo = new JSONObject();
        jo.put("name", "Jone");
        jo.put("city", "Seoul");

        String jsonStr = jo.toString();
        File jsonFile = new File("/var/tmp/example.json");

        writeStringToFile(jsonStr, jsonFile);
    }

    public static void writeStringToFile(String str, File file) throws IOException {
        BufferedWriter writer = new BufferedWriter(new FileWriter(file));
        writer.write(str);
        writer.close();
    }

}
```

```bash
/var/tmp$ cat example.json
{"city":"Seoul","name":"Jone"}
```
