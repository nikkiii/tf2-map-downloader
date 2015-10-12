package org.nikkii.mapdownloader.util;

/**
 * @author Nikki
 */
public interface WatcherCallback {
	public void mapAdded(String name);

	public void mapRemoved(String name);
}
