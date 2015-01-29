package org.nikkii.mapdownloader.download;

import org.nikkii.alertify4j.Alertify;
import org.nikkii.alertify4j.AlertifyBuilder;
import org.nikkii.alertify4j.AlertifyType;
import org.nikkii.mapdownloader.maps.Map;
import org.nikkii.mapdownloader.util.FormatUtil;

import javax.swing.JLabel;
import javax.swing.JProgressBar;

/**
 * A listener to update the GUI for the file download.
 *
 * @author Nikki
 */
public class DownloadProgressListener implements ProgressListener {
	/**
	 * The download text format.
	 */
	private static final String FORMAT = "Downloading %s @ %s/s (%s/%s).";

	/**
	 * The download file size.
	 */
	private long fileSize;

	/**
	 * The download start time (this is set in MapDownloader, however we don't have access from here...)
	 */
	private long startedAt = 0;

	/**
	 * The label to update.
	 */
	private final JLabel label;

	/**
	 * The ProgressBar to update.
	 */
	private final JProgressBar progressBar;

	/**
	 * The map to download.
	 */
	private final Map map;

	/**
	 * Construct a new progress listener.
	 *
	 * @param label The label to update.
	 * @param progressBar The progress bar to update.
	 * @param map The map to download.
	 */
	public DownloadProgressListener(JLabel label, JProgressBar progressBar, Map map) {
		this.label = label;
		this.progressBar = progressBar;
		this.map = map;
	}

	@Override
	public void progressStarted(long fileSize) {
		this.fileSize = fileSize;
		this.startedAt = System.currentTimeMillis();

		label.setText(String.format(FORMAT, map.getName(), FormatUtil.humanReadableByteCount(0, false), FormatUtil.humanReadableByteCount(0, false), FormatUtil.humanReadableByteCount(fileSize, false)));
	}

	@Override
	public void progressUpdated(int percent, long bytes) {
		long elapsed = System.currentTimeMillis() - startedAt;
		long bytesPerSecond = bytes > 0 && elapsed > 0 ? (long) (bytes / Math.ceil((double) elapsed / 1000)) : 0;
		progressBar.setValue(percent);
		label.setText(String.format(FORMAT, map.getName(), FormatUtil.humanReadableByteCount(bytesPerSecond, false), FormatUtil.humanReadableByteCount(bytes, false), FormatUtil.humanReadableByteCount(fileSize, false)));
	}

	@Override
	public void progressFinished() {
		label.setText("Status: Idle.");

		Alertify.show(new AlertifyBuilder().autoClose(5000).type(AlertifyType.INFO).text("Finished downloading " + map.getName()).build());
	}
}
