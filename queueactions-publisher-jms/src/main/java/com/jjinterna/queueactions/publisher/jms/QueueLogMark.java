package com.jjinterna.queueactions.publisher.jms;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class QueueLogMark {

	private Path path;
	
	public QueueLogMark(String uri) {
		this.path = Paths.get(uri);
	}
	
	public int getMark() {
		try {
			return Integer.valueOf(new String(Files.readAllBytes(path)));
		}
		catch (Exception e) {
			return 0;
		}
	}

	public void setMark(String s) throws IOException {
		Files.write(path, s.getBytes());
	}
}
