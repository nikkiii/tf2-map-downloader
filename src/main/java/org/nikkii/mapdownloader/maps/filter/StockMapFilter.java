package org.nikkii.mapdownloader.maps.filter;

import org.nikkii.mapdownloader.maps.Map;
import org.nikkii.mapdownloader.util.ui.ListFilter;

import java.util.LinkedList;
import java.util.List;

/**
 * @author Nikki
 */
public class StockMapFilter implements ListFilter<Map> {
	private final List<String> stockMaps = new LinkedList<>();

	@Override
	public void reset() {

	}

	@Override
	public boolean accept(Map element) {
		return !stockMaps.contains(element.getName());
	}

	public void add(String map) {
		stockMaps.add(map);
	}
}
