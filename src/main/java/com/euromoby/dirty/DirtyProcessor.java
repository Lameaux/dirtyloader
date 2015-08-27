package com.euromoby.dirty;

import java.io.IOException;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.euromoby.dirty.http.HttpClientProvider;

@Component
public class DirtyProcessor {
	private static final Logger log = LoggerFactory.getLogger(DirtyProcessor.class);

	private Config config;
	private DirtyManager dirtyManager;
	private HttpClientProvider httpClientProvider;
	
	private LinkedBlockingQueue<Runnable> queue;
	private ThreadPoolExecutor pool;

	@Autowired
	public DirtyProcessor(Config config, DirtyManager dirtyManager, HttpClientProvider httpClientProvider) {
		this.config = config;
		this.dirtyManager = dirtyManager;
		this.httpClientProvider = httpClientProvider;

		queue = new LinkedBlockingQueue<Runnable>(config.getTaskQueueSize());
		pool = new ThreadPoolExecutor(config.getTaskPoolSize(), config.getTaskPoolSize(), 0L, TimeUnit.MILLISECONDS, queue);
		final int taskRetry = config.getTaskRetry();

		pool.setRejectedExecutionHandler(new RejectedExecutionHandler() {
			@Override
			public void rejectedExecution(Runnable r, ThreadPoolExecutor executor) {
				try {
					Thread.sleep(taskRetry);
				} catch (InterruptedException e) {
					Thread.currentThread().interrupt();
					return;
				}
				executor.execute(r);
			}
		});

	}

	public void startProcessing() throws IOException {
		pool.prestartAllCoreThreads();

		String url = "";
		Integer id = 1;

		pool.submit(new DirtyWorker(dirtyManager, url, id, config, httpClientProvider));

		pool.shutdown();
		try {
			pool.awaitTermination(Integer.MAX_VALUE, TimeUnit.SECONDS);
		} catch (InterruptedException ie) {
			Thread.currentThread().interrupt();
		}

	}

}
