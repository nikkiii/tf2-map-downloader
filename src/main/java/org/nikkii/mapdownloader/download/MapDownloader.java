package org.nikkii.mapdownloader.download;

import org.nikkii.mapdownloader.maps.Map;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.LinkedList;
import java.util.List;

/**
 * A simple wrapper to provide percentage updates and other asynchronous functions for downloading files from URLs.
 *
 * Originally written by me, however modified to only download maps, and decompress them.
 * 
 * @author Nikki
 *
 */
public class MapDownloader implements Runnable {

	/**
	 * The map to download.
	 */
	private final Map map;

	/**
	 * The output file.
	 */
	private final File outputFile;

	/**
	 * The list of download listeners to inform for status updates
	 */
	private List<ProgressListener> listeners = new LinkedList<ProgressListener>();
	/**
	 * The list of download listeners to inform for status updates
	 */
	private List<ProgressListener> decompressorListeners = new LinkedList<ProgressListener>();
	
	/**
	 * File size
	 */
	private long length = 0;
	
	/**
	 * Count of amount downloaded so far
	 */
	private long downloaded = 0;
	
	/**
	 * Current percentage
	 */
	private int percent = 0;
	
	/**
	 * Download start time
	 */
	private long startTime = 0;
	
	/**
	 * Construct a new Downloader
	 * @param map
	 * 			The Download
	 */
	public MapDownloader(Map map, File outputFile) {
		this.map = map;
		this.outputFile = outputFile;
	}
	
	/**
	 * Download the file.
	 */
	@Override
	public void run() {
		try {
			File tempOutput = File.createTempFile(map.getName(), ".bsp.tmp");

			try (OutputStream output = new FileOutputStream(tempOutput)) {
				URLConnection connection = new URL(map.getUrl()).openConnection();

				connection.setRequestProperty("User-Agent", "Java Downloader");

				length = connection.getContentLength();

				InputStream input = connection.getInputStream();

				byte[] buffer = new byte[10240];

				int lastPercent = 0;

				progressStarted(length);

				startTime = System.currentTimeMillis();

				try {
					while (true) {
						int read = input.read(buffer, 0, buffer.length);
						if (read < 0) {
							break;
						}
						downloaded += read;

						percent = (int) (((double) downloaded / (double) length) * 100);
						if (percent > lastPercent) {
							progressEvent(percent, downloaded);
							lastPercent = percent;
						}
						output.write(buffer, 0, read);
					}
				} finally {
					input.close();
					output.close();
				}
			}

			if (map.isCompressed()) { // If compressed, decompress the map.
				Decompressor decompressor = new Decompressor(tempOutput, outputFile);

				for (ProgressListener listener : decompressorListeners) {
					decompressor.addListener(listener);
				}

				decompressor.run();
			} else { // Otherwise, move the file.
				tempOutput.renameTo(outputFile);
			}

			// If the output file exists still, delete it.
			if (tempOutput.exists()) {
				if (!tempOutput.delete()) tempOutput.deleteOnExit();
			}
			
			progressFinished();
		} catch (FileNotFoundException e) {
		} catch (IOException e) {
		}
	}
	
	/**
	 * Add a listener to the list
	 * @param listener
	 * 			The listener to add
	 */
	public void addListener(ProgressListener listener) {
		listeners.add(listener);
	}
	
	/**
	 * Start a new thread to download
	 */
	public void start() {
		new Thread(this).start();
	}
	
	/**
	 * Get the download's full size
	 * @return
	 * 		The size of the download as reported by the content length header
	 */
	public long getFileSize() {
		return length;
	}
	
	/**
	 * Get the amount of bytes downloaded so far
	 * @return
	 * 		The byte count downloaded
	 */
	public long getDownloaded() {
		return downloaded;
	}
	
	/**
	 * Get the current download percentage
	 * @return
	 * 		The percentage
	 */
	public int getPercentage() {
		return percent;
	}
	
	/**
	 * Get the download start time
	 * @return
	 * 		The download's start time
	 */
	public long getStartTime() {
		return startTime;
	}
	
	/**
	 * Notify the listeners for the download starting
	 * @param fileSize
	 * 			The file size parsed off the header
	 */
	private void progressStarted(long fileSize) {
		for(ProgressListener listener : listeners) {
			listener.progressStarted(fileSize);
		}
	}
	
	/**
	 * Called whenever percent > lastPercent (Like 80 -> 81)
	 * @param percent
	 * 			The current percentage
	 * @param bytes
	 * 			The amount of bytes downloaded since start
	 */
	private void progressEvent(int percent, long bytes) {
		for(ProgressListener listener : listeners) {
			listener.progressUpdated(percent, bytes);
		}
	}
	
	/**
	 * Called when the download finishes.
	 */
	private void progressFinished() {
		for(ProgressListener listener : listeners) {
			listener.progressFinished();
		}
	}

	/**
	 * Add a listener to be added to our decompressor after downloading.
	 *
	 * @param listener The listener to add.
	 */
	public void addDecompressorListener(ProgressListener listener) {
		decompressorListeners.add(listener);
	}
}
