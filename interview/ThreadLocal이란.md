# ThreadLocal
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
