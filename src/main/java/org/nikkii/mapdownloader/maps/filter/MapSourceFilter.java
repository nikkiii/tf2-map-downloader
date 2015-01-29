package org.nikkii.mapdownloader.maps.filter;

import org.nikkii.mapdownloader.maps.Map;
import org.nikkii.mapdownloader.maps.MapSource;
import org.nikkii.mapdownloader.util.ui.ListFilter;

import java.util.LinkedList;
import java.util.List;

/**
 * A filter which filters out maps based on their source.
 *
 * @author Nikki
 */
public class MapSourceFilter implements ListFilter<Map> {

	private List<MapSource> enabledSources = new LinkedList<>();

	@Override
	public void reset() {

	}

	@Override
	public boolean accept(Map element) {
		return enabledSources.contains(element.getSource());
	}

	public void setSourceEnabled(MapSource source, boolean enabled) {
		if (enabled && !enabledSources.contains(source)) {
			enabledSources.add(source);
		} else if (!enabled) {
			enabledSources.remove(source);
		}
	}
}
