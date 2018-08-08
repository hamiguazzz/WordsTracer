package hamiguazzz.utils;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.function.Consumer;

public class ThreadsPoolUtils {
	private static Logger logger = LogManager.getLogger(ThreadsPoolUtils.class.getName());

	public static <T> List<Thread> poolToThreads(List<Deque<T>> pool, String poolName, long sleepTime, Consumer<T>
			target) {
		List<Thread> threads = new ArrayList<>();
		for (int i = 0; i < pool.size(); i++) {
			int finalI = i;
			threads.add(new Thread(() -> {
				pool.get(finalI).forEach(target);
				try {
					Thread.sleep(sleepTime);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}, poolName + "id = " + i));
		}
		return threads;
	}

	public static <T> List<Deque<T>> balancePool(List<T> objs, int threadsCount) {
		ArrayList<Deque<T>> pool = new ArrayList<>(threadsCount);
		for (int i = 0; i < threadsCount; i++) {
			pool.add(new ArrayDeque<>());
		}
		for (int i = 0; i < objs.size(); i += threadsCount) {
			for (int j = 0; j < threadsCount; j++) {
				if (i + j < objs.size())
					pool.get(j).add(objs.get(i + j));
			}
		}
		return pool;
	}

	public static void joinAll(List<Thread> threads) {
		for (Thread thread : threads) {
			try {
				thread.join();
			} catch (InterruptedException e) {
				logger.error(thread.getName() + " can't join");
				e.printStackTrace();
			}
		}
	}
}
