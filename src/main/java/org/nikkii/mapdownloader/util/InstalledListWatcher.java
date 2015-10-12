package org.nikkii.mapdownloader.util;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.util.List;
import java.util.function.Consumer;

import static java.nio.file.StandardWatchEventKinds.ENTRY_CREATE;
import static java.nio.file.StandardWatchEventKinds.ENTRY_DELETE;
import static java.nio.file.StandardWatchEventKinds.OVERFLOW;

/**
 * @author Nikki
 */
public class InstalledListWatcher implements Runnable {
	private final Path dir;

	private final WatchService watcher;

	private WatcherCallback callback;

	public InstalledListWatcher(Path dir, WatcherCallback callback) throws IOException {
		this.dir = dir;
		this.watcher = dir.getFileSystem().newWatchService();
		this.callback = callback;
	}

	public void run() {
		try {
			dir.register(watcher, ENTRY_CREATE, ENTRY_DELETE);
		} catch (IOException e) {
			return;
		}
		for (;;) {
			WatchKey key;
			try {
				key = watcher.take();
			} catch (InterruptedException x) {
				return;
			}

			for (WatchEvent<?> event: key.pollEvents()) {
				WatchEvent.Kind<?> kind = event.kind();

				if (kind == OVERFLOW) {
					continue;
				}

				WatchEvent<Path> ev = (WatchEvent<Path>)event;
				Path filename = ev.context();

				// Update our list

				String name = filename.toString();

				if (!name.endsWith("bsp")) {
					continue;
				}

				name = name.substring(0, name.indexOf('.'));


				if (kind == ENTRY_CREATE) {
					System.out.println("Map " + name + " was added.");
					callback.mapAdded(name);
				} else if (kind == ENTRY_DELETE) {
					System.out.println("Map " + name + " was removed.");
					callback.mapRemoved(name);
				}
			}

			boolean valid = key.reset();
			if (!valid) {
				break;
			}
		}
	}
}
