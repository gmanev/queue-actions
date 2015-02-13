package com.jjinterna.queueactions.publisher.jms;

import static java.nio.file.StandardWatchEventKinds.*;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class QueueLogWatcher implements Runnable {

	Path path;
	private static final Logger log = LoggerFactory.getLogger(QueueLogWatcher.class);
	
	public QueueLogWatcher(String fileName) {
		path = Paths.get(fileName);
	}
	
	public abstract void onEntryCreate() throws Exception;
	public abstract void onEntryDelete() throws Exception;

	@Override
	public void run() {
		Path dir = path.getParent();
		log.info("Start");

		try {
			WatchService watcher = dir.getFileSystem().newWatchService();
			WatchKey key = dir.register(watcher,
					ENTRY_CREATE,
					ENTRY_DELETE);
			for (;;) {
				try {
					key = watcher.take();
				} catch (InterruptedException e) {
					log.error("Interrupted");
					return;
				}
				for (WatchEvent<?> event : key.pollEvents()) {
					WatchEvent.Kind<?> kind = event.kind();
					if (kind == OVERFLOW) {
						continue;
					}
					WatchEvent<Path> ev = (WatchEvent<Path>)event;
					Path filename = ev.context();
					
					Path child = dir.resolve(filename);

					if (child.equals(path)) {
						log.info("Received " + kind.toString());						
						try {
							if (kind == ENTRY_CREATE) {
								onEntryCreate();
							} else if (kind == ENTRY_DELETE) {
								onEntryDelete();
							}
						}
						catch (Exception e) {}
					}
				}
				
				boolean valid = key.reset();
				if (!valid) {
					break;
				}
			}
		} catch (IOException e) {
			log.error(e.getClass().getSimpleName(), e);
		}

		log.info("Stop");
	}
}
