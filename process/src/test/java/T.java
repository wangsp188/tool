import java.util.ArrayList;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;
import java.util.function.*;

import org.junit.Test;

import wang.process.core.CombineProcess;
import wang.process.core.MarkChain;
import wang.process.core.RollbackTaskTemplate;
import wang.process.core.SimpleProcess;

/**
 * @Description
 * @Author wangshaopeng
 * @Date 2020-07-21
 */
public class T {

	public static void main(String[] args) throws ExecutionException, InterruptedException {
		m();
//        t5();
//        t6();
//		t2();
//        t7();
//        t8();
//          t9();
//        t10();

		t11();

	}

	private static void t11() {
		ArrayList<Object> list = new ArrayList<>();
		for (int i = 0; i < 100; i++) {
			list.add(i);
		}
		CombineProcess process = CombineProcess.cycleSharingRollback(list, new RollbackTaskTemplate() {
			@Override
			public void doTask0(SimpleProcess process, MarkChain chain) throws Throwable {
				Integer startParam = process.getParam();
				Thread.sleep(10 * startParam);
//				if(startParam>30){
//					throw new RuntimeException();
//				}
				System.out.println(startParam + "");
			}

			@Override
			public void doRollback0(SimpleProcess process) {
				System.out.println("回滚了:" + process.getParam());
			}
		});
		process.execute();
		System.out.println("结果");
//		process.getFuture().thenRunAsync(() -> System.out.println(process));

	}

	private static void t10() throws InterruptedException, ExecutionException {
		CompletableFuture<Object> future = CompletableFuture.supplyAsync(new Supplier<Object>() {
			@Override
			public Object get() {

				try {
					Thread.sleep(1000000);
				} catch (Exception e) {
					e.printStackTrace();
				}
				return "12";
			}
		});

		new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					Thread.sleep(1000);
					future.completeExceptionally(new TimeoutException());

				} catch (Exception e) {

				}
			}
		}).start();
		System.out.println("我开始等待");

//        System.out.println(future.get());

	}

	private static void t9() {
		CompletableFuture<String> future = CompletableFuture.supplyAsync(new Supplier<String>() {
			@Override
			public String get() {
				try {
					Thread.sleep(1000);
				} catch (Exception e) {
					e.printStackTrace();
				}
				return "值1";
			}
		});

//        future.thenApply(new Function<String, Object>() {
//            @Override
//            public Object apply(String s) {
//                return null;
//            }
//        });

		System.out.println(future.thenCompose(new Function<String, CompletionStage<String>>() {
			@Override
			public CompletionStage<String> apply(String s) {
				System.out.println("nadale" + s);
				CompletableFuture<String> future1 = CompletableFuture.supplyAsync(new Supplier<String>() {
					@Override
					public String get() {
						return s + "123";
					}
				});
				System.out.println(future1);
				return future1;
			}
		}).join());

	}

	private static void t8() throws InterruptedException, ExecutionException {
		CompletableFuture<Object> future = CompletableFuture.supplyAsync(new Supplier<Object>() {
			@Override
			public Object get() {

				try {
					for (int i = 0; i < 10; i++) {
						Thread.sleep(1000);
					}

				} catch (Exception e) {
					e.printStackTrace();
				}
				return 1;
			}
		});
		System.out.println(33);

		Thread.sleep(1000);
		future.cancel(true);
		System.out.println(future.join());

	}

	private static void m() {
		Thread thread = new Thread(new Runnable() {
			@Override
			public void run() {
				while (true) {
					try {
						Thread.sleep(1000);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			}
		});

		thread.setDaemon(false);
		thread.start();
	}

	@Test
	public static void t2() throws ExecutionException, InterruptedException {

		long millis = System.currentTimeMillis();
		CompletableFuture<String> future = CompletableFuture.supplyAsync(() -> {
			System.out.println("开始");

			try {
				Thread.sleep(1000);
			} catch (Exception e) {
				e.printStackTrace();
			}
			return "42";
		});

		System.out.println("最开始的异步");
		CompletableFuture<Object> future1 = future.thenApply(new Function<String, Object>() {
			@Override
			public Object apply(String s) {
				System.out.println("1:" + s + "开始睡了");
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				return "1";
			}
		});

		System.out.println(222);
		CompletableFuture<Object> future2 = future1.thenApply(new Function<Object, Object>() {
			@Override
			public Object apply(Object o) {
				System.out.println("2:" + o);

				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}

				try {
					return "在哪里";
				} finally {
//                    throw new IllegalStateException("不知道的状态");

				}
//                return "2";
			}
		});

		CompletableFuture<Void> future3 = future2.thenAccept(new Consumer<Object>() {
			@Override
			public void accept(Object o) {
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				System.out.println("3:" + o);
				System.out.println(System.currentTimeMillis() - millis);
			}
		});

		CompletableFuture<Void> future4 = future3.thenRun(new Runnable() {
			@Override
			public void run() {
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				System.out.println("4:");

			}
		});

		future2.exceptionally(new Function<Throwable, Object>() {
			@Override
			public Object apply(Throwable throwable) {
				System.out.println("exceptionlly:" + throwable);
				return "出错的";
			}
		}).thenAccept(a -> System.out.println(a));
//        CompletableFuture<Object> future5 = future2.whenCompleteAsync(new BiConsumer<Object, Throwable>() {
//            @Override
//            public void accept(Object aVoid, Throwable throwable) {
//                try {
//                    Thread.sleep(1000);
//                } catch (InterruptedException e) {
//                    e.printStackTrace();
//                }
//                System.out.println("5:"+aVoid+throwable);
//                try {
//                    Thread.sleep(5000);
//                } catch (InterruptedException e) {
//                    e.printStackTrace();
//                }
////                throw new RuntimeException("测试成都市");
//            }
//        });

		System.out.println(111);
		System.out.println("最终:" + future2.get());

	}

	private static void t3() {

		CompletableFuture.supplyAsync(new Supplier<Object>() {
			@Override
			public Object get() {
				System.out.println("进来1");
				try {
					Thread.sleep(5000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				return "hello";
			}
		}).thenAcceptBoth(CompletableFuture.supplyAsync(new Supplier<Object>() {
			@Override
			public Object get() {
				System.out.println("进来2");
				try {
					Thread.sleep(5000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				return "world";
			}
		}), new BiConsumer<Object, Object>() {
			@Override
			public void accept(Object o, Object o2) {
				System.out.println(o + "---" + o2);
			}
		}).join();
		// 1.get()和join()函数差不多,有一点,join函数执行时如果出现异常,他会直接抛出,而get函数则不是这样的,他会将异常包装成ExecutionException再抛出
		// 2.completableFuture他是链式调用的,但是不是想象中的链式调用,他每次调用返回的future不是前一个调的时候那个(除了whenComplete函数,因为他是作为最终结果的逻辑设计)
		// 3.handel函数千万不要理解为,他可以修改执行结果,他的逻辑是这样的,你调用他之前的future,结果就那样了,你可以在他结果的后面,添加逻辑修改自修改返回值,但是这个返回值是在哪呢?他新给你的future,所以,如果上一个future报错了,那么你使用上个future.get()还是会报错
		// 4.注意该框架不保证回调的执行顺序,应该是先注册先执行,并不管你是Async还是不带Async
		// 5.使用不带async的函数时,需要注意一点,执行注册的回调函数时,他是使用主线程,什么意思呢,就是说,其实注册函数后啊,主函数就开始继续干别的活了,但是如果这时候,方法执行完了,这时候就会拿主线程去执行回调函数,如果主线程正在干别的活,就是导致,主线程的阻塞等待,所以建议使用Async的函数,这是永远异步,不会导致主线程阻塞
		// 6.completableFuture的回调较多
		// apply函数可以理解为,我拿到你的结果后我要用并且我还要出结果,而这个结果类型可以和上面的结果类型不易昂,所以可以用作结果转换
		// thenAccept/thenRun 我拿到结果就执行得了,干完活并没啥结果,也不会有返回值
		// thenCombine 可以合并俩future,并将他俩future的结果给一个函数去执行
		// thenCompose 可以看做是一个活干好后,交给另一个,再统一返回结果
		// cancel 其实就是标识状态为取消,仅是在get函数调用的时候回抛出异常而已,虽然后面有参数是否中断,但是没发现什么作用,也不会中断线程
		// thenapply和thencompose 都可实现两future的依赖执行,但是如果有多层嵌套,最好使用thenCompose()。
	}

	private static void t5() {
		CompletableFuture<Object> future = CompletableFuture.supplyAsync(new Supplier<Object>() {
			@Override
			public Object get() {

				try {
					Thread.sleep(1000);
				} catch (Exception e) {
					e.printStackTrace();
				}
				return 1;
			}
		});
		future.thenAccept(new Consumer<Object>() {
			@Override
			public void accept(Object o) {
				System.out.println(o);
			}
		}).thenAccept(o -> {

			try {
				System.out.println(o + "-----");
				Thread.sleep(100);
			} catch (Exception e) {
				e.printStackTrace();
			}
		});
		System.out.println(111);

	}

	private static void t6() {
		CompletableFuture<Object> future = CompletableFuture.supplyAsync(new Supplier<Object>() {
			@Override
			public Object get() {

				try {
					Thread.sleep(1000);
				} catch (Exception e) {
					e.printStackTrace();
				}
				return 12;
			}
		});

		future.handle(new BiFunction<Object, Throwable, Object>() {
			@Override
			public Object apply(Object o, Throwable throwable) {
				return 23;
			}
		});

		System.out.println(future.join());

	}

	private static void t7() {
		CompletableFuture<String> future = CompletableFuture.supplyAsync(() -> {
			for (int i = 0; i < 400; i++) {
				System.out.println(i);
			}
			return "1";
		});

//
//        future.handle(new BiFunction<String, Throwable, Object>() {
//            @Override
//            public Object apply(String s, Throwable throwable) {
//                return null;
//            }
//        });

//        future.thenAcceptAsync(new Consumer<String>() {
//            @Override
//            public void accept(String s) {
//
//                try {
////                    Thread.sleep(2100);
//                    System.out.println(s);
//                } catch (Exception e) {
//                    e.printStackTrace();
//                }
//            }
//        });
//
//        System.out.println(33);
//        future.thenAccept(new Consumer<String>() {
//            @Override
//            public void accept(String s) {
//
//                try {
////                    Thread.sleep(1100);
//                    System.out.println(s+"---");
//                } catch (Exception e) {
//                    e.printStackTrace();
//                }
//            }
//        });

		future.thenApply(new Function<String, Object>() {
			@Override
			public Object apply(String s) {

				try {
					Thread.sleep(1100);
					System.out.println(s + "---");
					Thread.sleep(11000);

				} catch (Exception e) {
					e.printStackTrace();
				}
				;
				return null;
			}
		});

		System.out.println("33-");

		for (int i = 0; i < 100000000; i++) {
			System.out.println(i);
		}
		future.thenApply(new Function<String, Object>() {
			@Override
			public Object apply(String s) {
				try {
					Thread.sleep(1100);
					System.out.println(s);
				} catch (Exception e) {
					e.printStackTrace();
				}
				;
				return null;
			}
		});

	}

	@Test
	public void t() {
		CompletableFuture<String> supplyAsync = CompletableFuture.supplyAsync(new Supplier<String>() {
			@Override
			public String get() {
				return "111";
			}
		});
		CompletableFuture<String> whenComplete = supplyAsync.whenComplete(new BiConsumer<String, Throwable>() {
			@Override
			public void accept(String aVoid, Throwable throwable) {
				System.out.println("whenComplete" + aVoid + throwable);
				throw new IllegalStateException(throwable);
			}
		});
//        System.out.println((supplyAsync==whenComplete) +"upplyAsync==whenComplete");

		supplyAsync.handle(new BiFunction<String, Throwable, Object>() {
			@Override
			public Object apply(String s, Throwable throwable) {
				System.out.println("handle" + s + "-" + throwable);
				throw new IllegalStateException(throwable);

//                return "23455";
			}
		});

		supplyAsync.thenAccept(new Consumer<String>() {
			@Override
			public void accept(String s) {
				System.out.println("accept" + s);
			}
		});

		supplyAsync.exceptionally(new Function<Throwable, String>() {
			@Override
			public String apply(Throwable throwable) {
				System.out.println("exceptionally" + throwable);
				return null;
			}
		});

	}

}
