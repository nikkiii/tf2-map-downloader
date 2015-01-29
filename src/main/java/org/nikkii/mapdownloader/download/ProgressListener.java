package org.nikkii.mapdownloader.download;

/**
 * A listener to provide progress updates
 * 
 * @author Nikki
 *
 */
public interface ProgressListener {

	/**
	 * Called after the filesize is found and the progress element is about to start.
	 * @param fileSize
	 * 			The file size
	 */
	public void progressStarted(long fileSize);

	/**
	 * Called whenever the percentage increases (Like 81% -> 82%).
	 * @param percent
	 * 			The current percentage.
	 * @param bytes
	 * 			The current amount of bytes progressed.
	 */
	public void progressUpdated(int percent, long bytes);
	
	/**
	 * Called when a progress action finishes.
	 */
	public void progressFinished();
}
