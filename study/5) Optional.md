# Optional
## 1. Optional 이란?
### 등장배경
- java 8 이전에 null check를 했던 방식을 코드를 통해 살펴보자.

```java
public class Progress {
    private Duration studyDuration;

    private boolean finished;

    public Duration getStudyDuration() {
        return studyDuration;
    }

    public void setStudyDuration(Duration studyDuration) {
        this.studyDuration = studyDuration;
    }
}

public class OnlineClass {
    private Integer id;
    private String title;
    private boolean closed;
    private Progress progress;

    public OnlineClass(Integer id, String title, boolean closed) {
        this.id = id;
        this.title = title;
        this.closed = closed;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public boolean isClosed() {
        return closed;
    }

    public void setClosed(boolean closed) {
        this.closed = closed;
    }

    public Progress getProgress() {
        return progress;
    }

    public void setProgress(Progress progress) {
        this.progress = progress;
    }

    @Override
    public String toString() {
        return "OnlineClass{" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", closed=" + closed +
                '}';
    }
}
```
- 위와 같이 수업의 진행상황 정보 객체(Progress)와 수업 객체(OnlineClass)가 있다고 할 때

```java
public static void main(String[] args) {
    OnlineClass spring_boot = new OnlineClass(1, "spring boot", true);
    Duration studyDuration = spring_boot.getProgress().getStudyDuration();
    System.out.println("studyDuration = " + studyDuration);
}
```

- 이렇게 특정 수업의 진행상황 정보를 조회하면 제대로 조회가 될까?
  - 당연히, Progress에는 값이 설정되지 않았기 때문에 NullPointException이 발생한다. 
  - 그래서 java 8 이전에는 이런 Exception을 막기 위해 아래와 같이 로직을 구현했다.

```java
public static void main(String[] args) {
    OnlineClass spring_boot = new OnlineClass(1, "spring boot", true);
    Progress progress = spring_boot.getProgress();
    if (progress != null) {
        System.out.println(progress.getStudyDuration());    
    }
}
```

- 이런 식의 코드는 문제가 발생할 여지가 많다.
  - 프로그래머는 기계가 아닌 인간이기에 실수를 하거나 코드를 누락할 수 있고 null-check와 같은 작은 유효성 검사는 놓칠 수 있다.
  - 그렇기에 NullPointException의 발생이 종종 일어났다.
- 아니면, 반환하는 측에서 해당 값이 null인 경우 의도적으로 Exception을 발생하는 방법도 있지만, 이 역시 추천하기는 힘든 방법인게 에러 발생시 stack trace를 찍게 되어있고 이는 성능 하락의 원인이 된다. 
```java
public Progress getProgress() {
    if (this.progress == null) {
        throw new IllegalStateException();
    }
    return progress;
}
```

### Java 8 이후 Optional의 등장
- java 8 이후 이런 null 처리를 위해 Optional이 등장했다.
- 이는 간혹 비어있는 객체를 반환할 수 있는 상황에서 Optional 객체로 반환값을 래핑해준다면 반환되는 값이 null일지라도 Optional 객체로 래핑되어 반환되기 때문에 null로부터 안전해진다. 

```java
public Optional<Progress> getProgress() {
    return Optional.ofNullable(progress);
}
```
 
#### 주의 사항
- 문법적으로 Optional은 어디에 들어가도 에러가 발생하거나 문제가 되진 않는다. 
- 하지만 API 공식 문서에서는 Optional을 리턴값으로만 쓰기를 권장한다.
  - 이 말은 Optional 타입을 메소드 매개변수 타입, 맵의 키 타입, 인스턴스 필드 타입으로 쓰지 말라는 의미가 되는데, 그 이유를 하나씩 알아보자면, 

##### 1. 메소드 매개변수 타입
```java
public void setProgress(Optional<Progress> progress) {
    if (progress != null) {
        progress.ifPresent(p-> this.progress = p);
    }
}
```
- Optional이 null을 고려한 타입이지만 매개변수가 null로 들어오는 경우를 고려해야하기 때문에 결국 progress가 null인지 검사해야 한다.
- 그리고 그게 끝이 아니라 그 다음 progress라는 Optional 타입 안에 값이 제대로 들어있는지 확인하는 ifPresent 메소드도 호출한 다음에서야 값을 넣을 수 있다.
  - 이는 Optional이 없을 때보다 번거로워진다. 

##### 2. 맵의 키 타입
- 맵의 가장 큰 특징 중 하나는 맵의 key는 not null이라는 점인데 key가 Optional 타입이라는 것은 맵의 근본을 해하는 것이다. 

##### 3. 인스턴스 필드 타입
- 예를 들어 필드가 progress라고 하면 이 해당 필드가 있을 수도 있고 없을 수도 있다는 의미인데, 
```java
public class OnlineClass {
    private Integer id;
    private String title;
    private boolean closed;
    private Optional<Progress> progress;
		...
}
```

- 이는 도메인 클래스 설계의 문제인데 권장되지 않고 분리하는 것이 좋다. 
---

- 프리미티브 타입용 Optional은 따로 있다.
```java
Optional.of(10);
```
- int, long, boolean 등의 프리미티브 타입을 Optional에 넣게 되면 박싱/언박싱이 이뤄지게 되는데 이렇게 불필요한 박싱 언박싱이 반복될수록 성능이 떨어진다.
  - 그렇기 때문에 OptionalInt, OptionalLong등 primitive type용 Optional이 있으니 해당 Optional을 쓰는것이 좋다. 
- 리턴 타입이 Optional인 메소드에서 null을 리턴하지 말자. 
  - Optional 반환 타입인 메소드를 호출할 때는 반환 값이 null이 아니고 null일지라도 Optional 객체로 래핑되어 올 것을 고려하기 때문에,
  - 반환값이 null이라면 메소드 호출하는 위치에서 에러가 발생할 수 있기에 정 반환값이 없을 경우 Optional.empty()를 반환하도록 하자.
- Collection, Map, Stream Array, Optional은 Optional로 감싸지 말 것
  - 해당 컨테이너들은 모두 그 자체로 빈 값에 대한 처리가 가능한 객체들인데 Optional로 감싸게되면 이중 랩핑이 되기 때문에 번거롭고 효율도 떨어진다. 

## 2. Optional API
### 1. Optional 만들기
- Optional.of()
- Optional.ofNullable()
- Optional.empty()
```java
public static void main(String[] args) {
    Optional<Integer> integer = Optional.of(10);
    Optional<Integer> integerOptional = 
						Optional.ofNullable((Math.random() * 10) > 5 ? null : 1);
    Optional<Object> empty = Optional.empty();
}
```

#### 1. Optional.of(10)
- 값이 10인 Optional 객체를 만든다.
- 안의 객체로 들어갈 값은 null을 고려하지 않는다.
- 만일, null 값이 들어간다면 NullPointException이 발생한다.

#### 2. Optional.ofNullable((Math.random() * 10) > 5 ? null : 1);
- 0~9까지의 실수가 5보다 클 경우 null을 아닐 경우 1을 인자값으로 Optional을 만든다.
- 이처럼 값이 null일 수도 있을 경우 of는 에러를 발생하기 때문에 ofNullable()을 사용한다.

#### 3. Optional.empty()
- 내부 값이 비어있는 Optional을 반환한다. 주로 반환값이 null일 때 사용한다.

### 2. Optional에 값이 있는지 없는지 확인하기
- isPresent()
- isEmpty() (Java 11부터 제공)

```java
public static void main(String[] args) {
    Optional<Integer> integer = Optional.of(10);
    Optional<Object> empty = Optional.empty();

    System.out.println(integer.isPresent());
    System.out.println(empty.isPresent());
    
    System.out.println(integer.isEmpty());
    System.out.println(empty.isEmpty());
}
```

#### 1. integer.isPresent()
- Optional 내부에 값이 있는지 여부를 논리값으로 반환하는 메소드이다.
- integer는 내부에 10이라는 값을 가지고 있기 때문에 true를 반환한다. 

#### 2. empty.isPresent()
- empty의 값은 비어있기 때문에 false를 반환한다.

#### 3. integer.isEmpty()
- Optional 객체 integer의 값이 비어있는지(null) 확인한다. 비어있을 경우 true를 반환한다.

### 3. Optional에 있는 값 가져오기
```java
public static void main(String[] args) {
    Optional<Integer> integer = Optional.ofNullable(10);
    Optional<Object> empty = Optional.empty();

    System.out.println(integer.get());
    integer.ifPresent(System.out::println);
    integer.orElse(testInteger());
    integer.orElseGet(App::testInteger);
    integer.orElseThrow(NoSuchElementException::new);

}

public static Integer testInteger(){
    System.out.println("create integer test");
    return (int)(Math.random() * 10);
}
```
- get()
- 내부 값이 null인걸 고려하는 경우
  - ifPresent(Consumer)
    - Optional에 값이 있는 경우 그 값을 Consumer Functional Interface에 전달 후 로직을 수행.
  - orElse(T)
    - 값이 있는 경우 가져오고 없는 경우 인자값으로 선언한 내용을 반환한다.
  - orElseGet(Supplier)
    - 값이 있으면 가져오고 없는 경우에 Supplier Functional Interface로직을 수행한다.
  - orElseThrow()
    - 값이 있으면 가져오고 없으면 에러를 던진다.
- 내부 값을 걸러내서 가져오기
  - Optional filter(Predicate)
    - Predicate Functional Interface를 수행 하여 조건에 부합하는 값을 가져온다.
- 내부 값 변환하기
  - Optional map(Function)
    - Function Functional Interface를 수행하여 내부 값을 순회하며 변경한 후 반환한다.
  - Optional flatMap(Function)
    - Optional 안에 들어있는 인스턴스가 Optional인 경우 내부 원소값을 꺼낼 때 사용한다.
