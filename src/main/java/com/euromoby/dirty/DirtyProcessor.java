package com.euromoby.dirty;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.euromoby.dirty.ffmpeg.Ffmpeg;
import com.euromoby.dirty.http.HttpClientProvider;
import com.euromoby.dirty.model.Video;
import com.euromoby.dirty.utils.StringUtils;

@Component
public class DirtyProcessor {
	private static final Logger log = LoggerFactory.getLogger(DirtyProcessor.class);

	private Config config;
	private DirtyManager dirtyManager;
	private HttpClientProvider httpClientProvider;
	private Ffmpeg ffmpeg;

	private LinkedBlockingQueue<Runnable> queue;
	private ThreadPoolExecutor pool;

	@Autowired
	public DirtyProcessor(Config config, DirtyManager dirtyManager, HttpClientProvider httpClientProvider, Ffmpeg ffmpeg) {
		this.config = config;
		this.dirtyManager = dirtyManager;
		this.httpClientProvider = httpClientProvider;
		this.ffmpeg = ffmpeg;

		// TASK
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

		List<Integer> idListToProcess = new ArrayList<Integer>();
		
		if (!StringUtils.nullOrEmpty(config.getListId())) {
			String[] idStrings = config.getListId().split(",");
			for (String idString : idStrings) {
				idListToProcess.add(Integer.valueOf(idString));
			}
		}
		
		if (idListToProcess.isEmpty()) {
			int startId = config.getStartId();
			int endId = config.getEndId();
			for (int id = startId; id <= endId; id++) {
				idListToProcess.add(id);
			}
		}
		
		for (Integer id : idListToProcess) {
			Video video = dirtyManager.findVideoById(id);
			if (video == null) {
				continue;
			}
			pool.submit(new DirtyWorker(dirtyManager, video.getSourceUrl(), video.getId(), config, httpClientProvider, ffmpeg));
		}
		pool.shutdown();
		try {
			pool.awaitTermination(Integer.MAX_VALUE, TimeUnit.SECONDS);
		} catch (InterruptedException ie) {
			Thread.currentThread().interrupt();
		}

	}

}
