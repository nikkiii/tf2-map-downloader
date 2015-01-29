package org.nikkii.mapdownloader.download;

import org.apache.commons.compress.compressors.bzip2.BZip2CompressorInputStream;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.LinkedList;
import java.util.List;

/**
 * A simple wrapper to provide percentage updates and other asynchronous functions for downloading files from URLs
 * 
 * @author Nikki
 *
 */
public class Decompressor implements Runnable {

	private final File inputFile;

	private final File outputFile;
	/**
	 * The list of download listeners to inform for status updates
	 */
	private List<ProgressListener> listeners = new LinkedList<ProgressListener>();

	/**
	 * File size
	 */
	private long length = 0;

	/**
	 * Count of amount decompressed so far
	 */
	private long decompressed = 0;

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
	 * @param inputFile
	 * 			The Download
	 */
	public Decompressor(File inputFile, File outputFile) {
		this.inputFile = inputFile;
		this.outputFile = outputFile;
	}
	
	/**
	 * Download the file.
	 */
	@Override
	public void run() {
		try {
			try (InputStream input = new BZip2CompressorInputStream(new FileInputStream(inputFile)); OutputStream output = new FileOutputStream(outputFile)) {
				length = inputFile.length();

				byte[] buffer = new byte[10240];

				int lastPercent = 0;

				decompressionStarted(length);

				startTime = System.currentTimeMillis();

				try {
					while (true) {
						int read = input.read(buffer, 0, buffer.length);
						if (read < 0) {
							break;
						}
						decompressed += read;

						percent = (int) (((double) decompressed / (double) length) * 100);
						if (percent > lastPercent) {
							progressEvent(percent, decompressed);
							lastPercent = percent;
						}
						output.write(buffer, 0, read);
					}
				} finally {
					input.close();
					output.close();
				}
			}
			
			decompressionFinished();
		} catch (FileNotFoundException e) {
		} catch (IOException e) {
		}
	}
	
	/**
	 * Add a listener to the list
	 *
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
	public long getCompressedLength() {
		return length;
	}
	
	/**
	 * Get the amount of bytes decompressed so far
	 * @return
	 * 		The byte count decompressed
	 */
	public long getDecompressed() {
		return decompressed;
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
	 *
	 * @param fileSize
	 * 			The file size parsed off the header
	 */
	private void decompressionStarted(long fileSize) {
		for(ProgressListener listener : listeners) {
			listener.progressStarted(fileSize);
		}
	}
	
	/**
	 * Called whenever percent > lastPercent (Like 80 -> 81)
	 *
	 * @param percent
	 * 			The current percentage
	 * @param bytes
	 * 			The amount of bytes decompressed since start
	 */
	private void progressEvent(int percent, long bytes) {
		for(ProgressListener listener : listeners) {
			listener.progressUpdated(percent, bytes);
		}
	}
	
	/**
	 * Called when the download finishes.
	 */
	private void decompressionFinished() {
		for(ProgressListener listener : listeners) {
			listener.progressFinished();
		}
	}
}
