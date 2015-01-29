package org.nikkii.mapdownloader.util;

public class FormatUtil {

	/**
	 * Convert a byte count to a human readable count
	 * 
	 * @param bytes
	 *            The input bytes
	 * @param si
	 *            false = binary, true = si
	 * @return The result of the format
	 */
	public static String humanReadableByteCount(long bytes, boolean si) {
		int unit = si ? 1000 : 1024;
		if (bytes < unit)
			return bytes + " B";
		int exp = (int) (Math.log(bytes) / Math.log(unit));
		String pre = (si ? "kMGTPE" : "KMGTPE").charAt(exp - 1) + (si ? "" : "i");
		return String.format("%.1f %sB", bytes / Math.pow(unit, exp), pre);
	}

	/**
	 * Format time elapsed
	 * 
	 * @param time
	 *            The time elapsed (In milliseconds)
	 * @param pad
	 *            Whether to pad with zeros
	 * @return The formatted string
	 */
	public static String timeElapsed(long time, boolean pad) {
		time = time / 1000;
		int hours = (int) (time / 3600), minutes = (int) ((time % 3600) / 60), seconds = (int) (time % 60);
		StringBuilder bldr = new StringBuilder();
		if (hours > 0) {
			bldr.append(pad ? padZeros(hours) : hours).append(" hour" + (hours == 1 ? "" : "s"));
		}
		if (bldr.length() > 0) {
			bldr.append(", ");
		}
		if (minutes > 0) {
			bldr.append(pad ? padZeros(minutes) : minutes).append(" minute" + (minutes == 1 ? "" : "s"));
		}
		if (bldr.length() > 0) {
			bldr.append(" and ");
		}
		bldr.append(pad ? padZeros(seconds) : seconds).append(" second" + (seconds == 1 ? "" : "s"));

		return bldr.toString();
	}

	/**
	 * Pad a number less than 10 with a zero
	 * 
	 * @param num
	 *            The number to pad
	 * @return The padded number
	 */
	public static String padZeros(int num) {
		if (num >= 0 && num <= 9) {
			return "0" + num;
		}
		return Integer.toString(num);
	}
}