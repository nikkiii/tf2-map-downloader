package org.nikkii.mapdownloader.maps;

/**
 * Represents a fastdownload http server.
 *
 * @author Nikki
 */
public class MapSource {

	/**
	 * The source name.
	 */
	private final String name;

	/**
	 * The source base url.
	 */
	private final String url;

	/**
	 * The source priority. Lower is better.
	 */
	private final int priority;

	/**
	 * Construct a new map source.
	 *
	 * @param name The source name.
	 * @param url The source url.
	 * @param priority The source priority.
	 */
	public MapSource(String name, String url, int priority) {
		this.name = name;
		this.url = url;
		this.priority = priority;
	}

	/**
	 * Get the source name.
	 *
	 * @return The source name.
	 */
	public String getName() {
		return name;
	}

	/**
	 * Get the source base url.
	 *
	 * @return The source url.
	 */
	public String getUrl() {
		return url;
	}

	/**
	 * Get the source priority.
	 *
	 * @return The source priority.
	 */
	public int getPriority() {
		return priority;
	}

	@Override
	public String toString() {
		return name;
	}
}
