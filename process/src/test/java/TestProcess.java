import java.util.ArrayList;

import org.junit.Before;

import wang.process.core.CombineProcess;
import wang.process.core.MarkChain;
import wang.process.core.RollbackTaskTemplate;
import wang.process.core.SimpleProcess;

/**
 * @Description
 * @Author wangshaopeng
 * @Date 2020-07-08
 */
public class TestProcess {

	@Before
	public static void b() {
		Thread thread = new Thread(() -> {
			while (true) {
				try {
					Thread.sleep(100000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		});
		thread.setDaemon(false);
		thread.start();

	}

	public static void main(String[] args) {
		b();
		t1();
//		t2();
	}

	public static void t1() {
		SimpleProcess context = new SimpleProcess();
		for (int i = 0; i < 10; i++) {
			int y = i;
			context.then(new RollbackTaskTemplate() {
				@Override
				public void doTask0(SimpleProcess process, MarkChain chain) throws Throwable {
					System.out.println(y);
					for (int i1 = 0; i1 < 100; i1++) {
//						Thread.sleep(100);
//						System.out.println(Thread.currentThread().isInterrupted());
					}
					chain.doChain(process);
				}

				@Override
				public void doRollback0(SimpleProcess process) {
					System.out.println("回滚11-" + y);
				}

			}).setTimeout(400);
		}
		context.setNeedAsync(true);
		context.execute();

		System.out.println(context);

	}

	public static void t2() {
		ArrayList<Object> objects = new ArrayList<>();
		for (int i = 0; i < 100; i++) {
			objects.add(i);
		}
		CombineProcess combineProcess = CombineProcess.cycleSharingRollback(objects, new RollbackTaskTemplate() {

			@Override
			public void doTask0(SimpleProcess process, MarkChain chain) throws Throwable {
				Integer startParam = process.getParam();
				System.out.println(startParam + "----");
				if (startParam > 80) {
					throw new IllegalArgumentException(startParam + "");
				}
				chain.doChain(process);
			}

			@Override
			public void doRollback0(SimpleProcess process) {
				System.out.println("回滚--" + process.getParam());
			}

		});
		combineProcess.execute();

		System.out.println(combineProcess);

	}

}
