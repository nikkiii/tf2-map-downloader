package org.nikkii.mapdownloader.maps.filter;

import org.nikkii.mapdownloader.maps.Map;
import org.nikkii.mapdownloader.util.ui.ListFilter;

import java.util.LinkedList;
import java.util.List;

/**
 * A filter which weeds out duplicate map files.
 *
 * @author Nikki
 */
public class DuplicateMapFilter implements ListFilter<Map> {

	private List<String> existingMaps = new LinkedList<>();

	@Override
	public void reset() {
		existingMaps.clear();
	}

	@Override
	public boolean accept(Map element) {
		if (existingMaps.contains(element.getName())) {
			return false;
		}

		existingMaps.add(element.getName());
		return true;
	}
}
