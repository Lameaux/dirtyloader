package com.euromoby.dirty;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.support.ClassPathXmlApplicationContext;

public class DirtyLoader {

	private static final Logger log = LoggerFactory.getLogger(DirtyLoader.class);

	public static final void main(String args[]) {
		ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext("classpath:spring-context.xml");
		DirtyProcessor dirtyProcessor = context.getBean(DirtyProcessor.class);
		try {
			log.info("Starting processing");
			dirtyProcessor.startProcessing();
		} catch (IOException e) {
			log.error("Processing failed", e);
		} finally {
			context.close();
			log.info("Done");
		}
	}

}
