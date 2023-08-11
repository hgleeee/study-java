# ThreadLocal
## 개요
- ThreadLocal은 각각의 쓰레드 별로 별도의 저장공간을 제공하는 컨테이너이다. 
- 멀티쓰레드 환경에서 각각의 쓰레드에게 별도의 자원을 제공함으로써 공유되는 서비스에서 별도의 자원에 접근하게끔 하여 스레드가 각각의 상태를 가질 수 있도록 도와준다. 
- ThreadLocal이 활용되는 환경은 해당 컨테이너를 가지고 있는 서비스가 싱글톤 객체로 공유되는 객체임을 가정한다.
	- 그렇기에 각각의 스레드는 동일한 서비스객체에 접근을 하게 되고, 동일한 ThreadLocal이라는 자원에 접근하는 것이다. 

## ThreadLocal을 사용하지 않는 경우
- ThreadLocal을 사용하지 않고, 서비스를 구현해보고 이를 사용해보자. 
```java
public class ServiceA {
	// 사용자 인증 정보
	private Authentication authentication;
	private UserRepository userRepository;
	private final ServiceA instance = new ServiceA();

	public static ServiceA getInstance(){
		return instance;
	}

	public boolean login(LoginForm form) {
		User user = userRepository.findById(form.getId()).orElseThrow(NoSuchException::new);
	
		if(PasswordEncoder.matches(user.getPassword(), form.getPassword())){
			authentication = Authentication.of(form.getId(), form.getPassword, ...);
		}
	}

	public boolean hasPrincipal(){
		return !authentication == null;
	}
}
```

- 위 코드는 로그인 정보를 토대로 로그인하여 인증 정보를 저장하고, 저장한 인증 정보를 활용하는 싱글톤 객체이다.
- 그럼 이 서비스를 2개의 사용자가 사용한다고 하면 어떤 일이 일어날까? 

1.
Thread A에서 로그인을 요청하며 로그인 정보를 전달한다.
2.
서비스에서는 로그인 정보를 비교 후 인증정보를 저장한다. 
3.
Thread B에서 자원 접근을 요청한다.
4.
서비스에서는 hasPrincipal() 메서드를 통해 인증 정보가 존재하는지 확인한다. 
5.
Thread A를 통해 저장된 인증정보가 있기에 Thread B의 요청은 접근이 허가된다.
인증은 Thread A가 있는데 Thread B도 접근이 허가된다! 
ServiceA라는 객체가 여러 스레드에게 공유되는 싱글톤 객체이기 때문에 내부 자원들도 모두 공유되기 때문에, 다른 쓰레드의 자원을 모두가 공유할 수 있게 되는 문제가 생기는 것이다. 
이뿐만이 아니다. 자원이 공유되는 것 뿐아니라 Thread B에서 인증 정보의 암호를 변경이라도 하면 더 큰 문제가 된다.  
이러한 이유들로 각각의 쓰레드가 공통된 객체 내부에서도 다른 상태를 가지길 바라면서 나온 개념이 ThreadLocal이다. 
ThreadLocal의 내부 구조
그럼 ThreadLocal은 어떻게 각각의 쓰레드별로 변수를 할당해서 사용할 수 있게 해주는 걸까? 
그걸 알기 위해서는 Thread와 ThreadLocal 클래스양 쪽을 살펴 볼 필요가 있다. 
public class Thread implements Runnable {
	//...logics
	ThreadLocal.ThreadLocalMap threadLocals = null;
}
Java
Thread 클래스의 threadLocals 인스턴스 변수
public class ThreadLocal<T> {
		ThreadLocalMap getMap(Thread t) {
        return t.threadLocals;
    }

    void createMap(Thread t, T firstValue) { 
        t.threadLocals = new ThreadLocalMap(this, firstValue);
    }


    public void set(T value) {
        Thread t = Thread.currentThread();
        ThreadLocalMap map = getMap(t); 
        if (map != null)                                   
             map.set(this, value);
        else
            createMap(t, value);                      
    }

    public T get() {
        Thread t = Thread.currentThread();
        ThreadLocalMap map = getMap(t);
        if (map != null) {
            ThreadLocalMap.Entry e = map.getEntry(this);
            if (e != null) {
                @SuppressWarnings("unchecked")
                T result = (T)e.value;
                return result;
            }
        }
        return setInitialValue();
    }


    public void remove() {
        ThreadLocalMap m = getMap(Thread.currentThread());
        if (m != null)
            m.remove(this);
   }

	static class ThreadLocalMap {
		static class Entry extends WeakReference<ThreadLocal<?>> {
            /** The value associated with this ThreadLocal. */
            Object value;

            Entry(ThreadLocal<?> k, Object v) {
                super(k);
                value = v;
            }
        }
	}
}
Java
ThreadLocal
두 클래스를 살펴보면 Thread는 객체는 threadLocals라는 인스턴스변수를 상태로 가지고 있는데 ThreadLocal클래스를 이용해 ThreadLocal 내부의 ThreadLocalMap이라는 클래스를 이용해 key/value로 데이터를 보관하는 것이였다. 
그리고 ThreadLocal의 get, set등의 메서드들의 원리도 Thread에서 현재 수행중인 thread를 currentThread() 메서드를 통해 꺼낸 뒤 이 Thread에서 ThreadLocalMap을 찾아 활용하는 것이였다. 

LocalThread의 대표적인 사용처
1. Spring Security
: 스프링 시큐리티(Spring Security)에서는 SecurityContextHolder에 SecurityContext → Authentication순으로 인증 정보를 보관한다. 여기서 SecurityContextHolder는 SecurityContext를 저장하는 방식을 전략패턴으로 유연하게 대응하는데, 이 중 기본 전략이 MODE_THREADLOCAL로 ThreadLocal을 사용하여 SecurityContext를 보관하는 방식이다. 
public class SecurityContextHolder {
	//...
	private static void initialize() {
		if (!StringUtils.hasText(strategyName)) {
			// Set default
			strategyName = MODE_THREADLOCAL; //기본 전략이 ThreadLocal을 사용한다.
		}

		if (strategyName.equals(MODE_THREADLOCAL)) {
			strategy = new ThreadLocalSecurityContextHolderStrategy();
		}
		else if (strategyName.equals(MODE_INHERITABLETHREADLOCAL)) {
			strategy = new InheritableThreadLocalSecurityContextHolderStrategy();
		}
		else if (strategyName.equals(MODE_GLOBAL)) {
			strategy = new GlobalSecurityContextHolderStrategy();
		}
		else {
			// Try to load a custom strategy
			try {
				Class<?> clazz = Class.forName(strategyName);
				Constructor<?> customStrategy = clazz.getConstructor();
				strategy = (SecurityContextHolderStrategy) customStrategy.newInstance();
			}
			catch (Exception ex) {
				ReflectionUtils.handleReflectionException(ex);
			}
		}

		initializeCount++;
	}
}
Java
SecurityContextHolder.java
final class ThreadLocalSecurityContextHolderStrategy implements SecurityContextHolderStrategy {
	//...
	private static final ThreadLocal<SecurityContext> contextHolder = new ThreadLocal<>();

	public void clearContext() {
		contextHolder.remove();
	}
	//...
}
Java
ThreadLocal을 사용하는 전략객체 ThreadLocalSecurityContextHolderStrategy.java
2. RequestContextHolder
HttpServletRequest를 조회할 수 있는 RequestContextHolder에서도 요청 정보를 ThreadLocal을 이용해 관리한다.
public abstract class RequestContextHolder {
  ...
  private static final ThreadLocal<RequestAttributes> requestAttributesHolder = new NamedThreadLocal("Request attributes");
  private static final ThreadLocal<RequestAttributes> inheritableRequestAttributesHolder = new NamedInheritableThreadLocal("Request context");
  ...
	public static void resetRequestAttributes() {
		requestAttributesHolder.remove();
		inheritableRequestAttributesHolder.remove();
	}
}
Java
RequestContextHolder.java
주의점
우리가 기억을 떠올려야 하는 사실이 하나 있다. 
WAS(Web Application Service)에서는 API요청하고 반환하며 Thread를 사용한 뒤에도 Thread를 종료시키지 않는다. 애플리케이션이 구동될 때 전략에 따라 일정 수(Ex: Spring은 200개)의 Thread를 생성해 스레드풀로 관리한다. 그리고 클라이언트의 요청마다 이 스레드 풀에서 여유 Thread를 발급하여 사용하고 반환받아서 다시 관리하는 식으로 사용을 한다. 
그렇기에, 이전 사용자가 Thread를 사용하며 ThreadLocal에 데이터를 저장해놓았을 때, 다른 사용자가 이 Thread를 받게되면 안의 있는 내용물을 위/변조 할 수 있게 된다는 점이다. 
이 정보들이 민감한 정보일수록 문제는 커진다. 
그렇기에, Thread 의 사용이 끝나는 시점에 Thread Pool에 반환을 하기 직전 반드시 ThreadLocal을 초기화시켜주는 작업을 해줘야 한다. 
스프링 시큐리티에서는 clearContext()라는 메서드를 통해 정보를 초기화해주고 RequestContextHolder에서는 resetRequestAttributes()라는 메서드로 초기화해준다. 


## [QUESTION 1] ThreadLocal이란?
- 일종의 쓰레드 지역변수이다. 오직 하나의 쓰레드에 의해 읽고 쓸 수 있는 변수로써, 다른 각각의 쓰레드가 하나의 ThreadLocal을 호출해도 서로 다른 값을 바라본다.
- Thread의 정보를 Key로 하는 Map 형식으로 데이터를 저장해두고 사용하는 자료구조이다.
- ThreadPool을 사용하여 Thread 재활용 시 이전에 저장된 ThreadLocal을 호출하게 되므로 모든 ThreadLocal은 사용 후 remove 과정이 필수이다.

## [QUESTION 2] ThreadLocal은 보통 언제 사용하는지?
- 사용자 인증정보 : Spring Security에서 사용자마다 다른 사용자 인증 정보 세션을 사용할 때.
- 트랜잭션 컨텍스트 : 트랜잭션 매니저가 트랜잭션 컨텍스트를 전파할 때.

## [QUESTION 3] ThreadLocal 사용법
[ANSWER] 1) ThreadLocal 객체 생성
[?] ThreadLocal Generic Type 사용 가능할까?
-> ThreadLocal Generic Type 사용 가능(<>로 사용방법 동일)

ThreadLocal<String> threadLocalGeneric = new ThreadLocal<>();

[?] ThreadLocal Class의 Default 초기값 설정 방법?
-> ThreadLocal에서 initialValue method를 Override 하면 이 ThreadLocal 변수를 사용하는 모든 쓰레드의 default값이 존재한다.
-> 즉, 별도의 set 함수로 값 설정하기 전에도 get으로 동일한 default값을 꺼내 사용할 수 있다.

ThreadLocal<String> subThreadLocal = new ThreadLocal<String>() {
			@Override
			protected String initialValue() {
				return "Init Value";
			}
		};
 


[ANSWER] 2) 현재 ThreadLocal에서 값 저장(.set())

ThreadLocal<String> threadLocalGeneric = new ThreadLocal<>();
threadLocalGeneric.set("TEMP");
 


[ANSWER] 3) 현재 ThreadLocal에서 값 불러오기(.get())

ThreadLocal<String> threadLocalGeneric = new ThreadLocal<>();
String result = threadLocalGeneric.get();
 


[ANSWER] 4) 사용 완료 후 ThreadLocal 값 삭제(.remove())

  -> 삭제하지 않으면 메모리 누수(Memory Leak) 발생할 수 있다.

  -> 추가로, 재사용되는 쓰레드(ThreadPool의 Thread)가 올바르지 않는 데이터를 참조할 수 있다.

ThreadLocal<String> threadLocalGeneric = new ThreadLocal<>();
threadLocalGeneric.set("TEMP");
System.out.println(threadLocalGeneric.get());
// OUTPUT: TEMP

threadLocalGeneric.remove();
System.out.println(threadLocalGeneric.get());
// OUTPUT: null
 
 


[QUESTION 4] ThreadLocal 사용 예시
[ANSWER] 1) Runnable 인터페이스 구현(MyRunnable) -> threadLocal에 랜덤 값 저장

public static class MyRunnable implements Runnable {

	        private ThreadLocal<Integer> threadLocal = new ThreadLocal<Integer>();

	        @Override
	        public void run() {
	            threadLocal.set( (int) (Math.random() * 100D) );
	    
	            try {
	                Thread.sleep(50);
	            } catch (InterruptedException e) {
	            }
	    
	            System.out.println(threadLocal.get());
	        }
	    }
 


[ANSWER] 2) 하나의 Runnable 객체를 2개의 쓰레드에 담기

MyRunnable runnable = new MyRunnable();
Thread thread1 = new Thread(runnable);
Thread thread2 = new Thread(runnable);
 


[ANSWER] 3) 각 쓰레드 run 시 두 쓰레드에 다른 값이 담기는지 확인(쓰레드 별 ThreadLocal에 랜덤 값이 담기기 때문에 다르다.)

System.out.print("thread1 get 결과 :: ");
thread1.start();
// OUTPUT: thread1 get 결과 :: 19

Thread.sleep(200);
System.out.print("thread2 get 결과 :: ");
thread2.start();
// OUTPUT: thread2 get 결과 :: 65

Thread.sleep(200);
 


[QUESTION 5] InheritableThreadLocal이란?
[ANSWER] 부모 쓰레드에서 생성된 ThreadLocal을 자식 쓰레드에서도 동일한 값으로 사용할 수 있는 ThreadLocal Class.
-> Servlet에서는 사용하면 안되는데,
-> 1) Request가 ThreadPool에서 처리된다는 점
-> 2) 메모리 누수(Memory Leak) 발생 => 자식 쓰레드도 사용하기 때문에 매 순간의 remove가 발생하지 않기 때문에
[ANSWER] Thread 생성자 또는 Runnable 구현 시 함수 내부에서 inheritableThreadLocal.set()을 통해 값을 저장하면 해당 쓰레드의 자식 쓰레드도 사용 가능

 

ThreadLocal<String> threadLocal = new ThreadLocal<>();
InheritableThreadLocal<String> inheritableThreadLocal = new InheritableThreadLocal<>();

Thread thread1 = new Thread(() -> {
System.out.println("========== [" + Thread.currentThread().getName() + "] START ==========");
threadLocal.set("[" + Thread.currentThread().getName() + "] ThreadLocal Value");
inheritableThreadLocal.set("[" + Thread.currentThread().getName() + "] InheritableThreadLocal Value");

System.out.println("threadLocal get :: " + threadLocal.get());
System.out.println("inheritableThreadLocal get :: " + inheritableThreadLocal.get());

Thread childThread = new Thread( () -> {
System.out.println("========== [" + Thread.currentThread().getName() + " - ChildThread] START ==========");
System.out.println("threadLocal get :: " + threadLocal.get());
System.out.println("inheritableThreadLocal get :: " + inheritableThreadLocal.get());
});
childThread.start();
});

thread1.start();

Thread thread2 = new Thread(() -> {
try {
Thread.sleep(1500);
} catch (InterruptedException e) {
e.printStackTrace();
}

System.out.println("========== [" + Thread.currentThread().getName() + "] START ==========");
System.out.println("threadLocal get :: " + threadLocal.get());
System.out.println("inheritableThreadLocal get :: " + inheritableThreadLocal.get());
System.out.println("------------------------------------------------------------------------------");
});
thread2.start();

/* OUTPUT
 *
========== [Thread-2] START ==========
threadLocal get :: [Thread-2] ThreadLocal Value
inheritableThreadLocal get :: [Thread-2] InheritableThreadLocal Value
========== [Thread-4 - ChildThread] START ==========
threadLocal get :: null
inheritableThreadLocal get :: [Thread-2] InheritableThreadLocal Value
========== [Thread-3] START ==========
threadLocal get :: null
inheritableThreadLocal get :: null
*/
