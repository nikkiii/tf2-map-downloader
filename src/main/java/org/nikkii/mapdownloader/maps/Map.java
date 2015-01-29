package org.nikkii.mapdownloader.maps;

/**
 * A TF2 Map.
 *
 * @author Nikki
 */
public class Map {

	/**
	 * The map source.
	 */
	private final MapSource source;

	/**
	 * The map file (full name, with .bsp and optionally .bz2)
	 */
	private final String file;

	/**
	 * The map name (without bsp and bz2)
	 */
	private final String name;

	/**
	 * Whether the map is compressed
	 */
	private final boolean compressed;

	/**
	 * Construct a new map.
	 *
	 * @param source The map source.
	 * @param file The map file.
	 * @param name The map name.
	 * @param compressed Whether the map is compressed.
	 */
	public Map(MapSource source, String file, String name, boolean compressed) {
		this.source = source;
		this.file = file;
		this.name = name;
		this.compressed = compressed;
	}

	/**
	 * Get the map source.
	 *
	 * @return The map source.
	 */
	public MapSource getSource() {
		return source;
	}

	/**
	 * Get the map name.
	 *
	 * @return The map name.
	 */
	public String getName() {
		return name;
	}

	/**
	 * Get whether the map is compressed.
	 *
	 * @return Whether the map is compressed.
	 */
	public boolean isCompressed() {
		return compressed;
	}

	/**
	 * Get the map url (based on the source)
	 *
	 * @return The map url.
	 */
	public String getUrl() {
		return source.getUrl() + file;
	}

	@Override
	public String toString() {
		return name;
	}

	@Override
	public boolean equals(Object o) {
		if (o.getClass() != getClass()) {
			return false;
		}

		if (o == this) {
			return true;
		}

		Map other = (Map) o;

		return other.name.equals(name) && other.source.equals(source) && other.file.equals(file) && other.compressed == compressed;
	}
}
