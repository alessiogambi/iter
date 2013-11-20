package at.ac.tuwien.iter;

import java.lang.Thread.UncaughtExceptionHandler;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BasicRunnerTest {

	private Runnable throwTheException = new Runnable() {

		public void run() {
			System.out
					.println("BasicRunnerTest.throwTheException.new Runnable() {...}.run()");

			try {
				Thread.sleep(1000);
			} catch (Exception e) {
				new RuntimeException(" Thread "
						+ Thread.currentThread().getName(), e);
			}
			throw new RuntimeException("Thread "
					+ Thread.currentThread().getName());
		}
	};

	@Test
	public void checkExceptions() throws InterruptedException {

		final Logger logger = LoggerFactory.getLogger(BasicRunnerTest.class);
		final UncaughtExceptionHandler uncaughtExceptionHandler = new UncaughtExceptionHandler() {

			public void uncaughtException(Thread arg0, Throwable arg1) {
				logger.error(" \n\n\n\n uncaughtException " + arg1.getMessage()
						+ " from Thread " + arg0);

			}
		};

		final ThreadFactory factory = new ThreadFactory() {

			public Thread newThread(Runnable runnable) {
				final Thread thread = new Thread(runnable);
				// Force our generated Handler here
				thread.setUncaughtExceptionHandler(uncaughtExceptionHandler);
				return thread;
			}
		};
		int nParallelTests = 2;
		ExecutorService executor = Executors.newFixedThreadPool(nParallelTests,
				factory);

		executor.execute(throwTheException);
		executor.execute(throwTheException);
		executor.execute(throwTheException);

		executor.shutdown();
		executor.awaitTermination(30, TimeUnit.SECONDS);

	}
}
