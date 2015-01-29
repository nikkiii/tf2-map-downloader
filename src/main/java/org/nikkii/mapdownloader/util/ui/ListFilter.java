package org.nikkii.mapdownloader.util.ui;

/**
 * A filter which filters out JList elements.
 *
 * @param <T> The element type.
 */
public interface ListFilter<T> {

	/**
	 * Reset any counters/etc before the filter is used.
	 */
	public void reset();

	/**
	 * Check if we should accept the specified element.
	 *
	 * @param element The element to check.
	 * @return Whether we should accept it.
	 */
	public boolean accept(T element);
}