package org.nikkii.mapdownloader.download;

import javax.swing.JLabel;
import javax.swing.JProgressBar;

/**
 * A listener to change the progress bar to the "no value" sliding version until done.
 *
 * @author Nikki
 */
public class DecompressProgressListener implements ProgressListener {

	/**
	 * The JLabel to update.
	 */
	private final JLabel label;

	/**
	 * The ProgressBar to update.
	 */
	private final JProgressBar progressBar;

	/**
	 * The text to set.
	 */
	private final String text;

	/**
	 * Construct a new progress listener for decompression.
	 *
	 * @param label The JLabel.
	 * @param progressBar The ProgressBar.
	 * @param text The text.
	 */
	public DecompressProgressListener(JLabel label, JProgressBar progressBar, String text) {
		this.label = label;
		this.progressBar = progressBar;
		this.text = text;
	}

	@Override
	public void progressStarted(long fileSize) {
		label.setText(text);
		progressBar.setIndeterminate(true);
	}

	@Override
	public void progressUpdated(int percent, long bytes) {

	}

	@Override
	public void progressFinished() {
		progressBar.setIndeterminate(false);
		progressBar.setValue(0);
	}
}
