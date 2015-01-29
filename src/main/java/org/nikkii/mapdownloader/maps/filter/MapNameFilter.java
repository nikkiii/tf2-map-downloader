package org.nikkii.mapdownloader.maps.filter;

import org.nikkii.mapdownloader.maps.Map;
import org.nikkii.mapdownloader.util.ui.ListFilter;

/**
 * A filter which filters out maps based on a search string.
 *
 * @author Nikki
 */
public class MapNameFilter implements ListFilter<Map> {

	private String filterString = "";

	public MapNameFilter() {

	}

	@Override
	public void reset() {
	}

	@Override
	public boolean accept(Map map) {
		return map.getName().contains(filterString);
	}

	public void setFilter(String filterString) {
		this.filterString = filterString;
	}
}
