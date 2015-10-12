package org.nikkii.mapdownloader.util.ui;

import javax.swing.AbstractListModel;
import javax.swing.ListModel;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * A ListModel which can filter items based on multiple filters.
 *
 * Based off {@link http://stackoverflow.com/a/14759042}
 *
 * @param <T> The model item type.
 */
public class FilteredListModel<T> extends AbstractListModel implements ListDataListener {

	/**
	 * The model source.
	 */
	private final ListModel source;

	/**
	 * The model filters.
	 */
	private final List<ListFilter<T>> filters = new LinkedList<>();

	/**
	 * The sorted model indices.
	 */
	private final List<Integer> indices = new ArrayList<>();

	/**
	 * Flag to enable/disable filtering temporarily.
	 */
	private boolean filterEnabled = false;

	/**
	 * Construct a new list model with the specified source.
	 *
	 * @param source The original model source.
	 */
	public FilteredListModel(ListModel source) {
		if (source == null)
			throw new IllegalArgumentException("Source is null");

		this.source = source;
		this.source.addListDataListener(this);
	}

	/**
	 * Add a new filter to the list.
	 *
	 * @param f The filter to add.
	 */
	public void addFilter(ListFilter<T> f) {
		filters.add(f);
		doFilter();
	}

	/**
	 * Filter this model's items.
	 */
	public void doFilter() {
		if (!filterEnabled) {
			return;
		}

		synchronized(source) {
			indices.clear();

			filters.forEach((filter) -> filter.reset());

			int count = source.getSize();

			for (int i = 0; i < count; i++) {
				T element = (T) source.getElementAt(i);
				boolean rejected = false;
				for (ListFilter<T> filter : filters) {
					if (!filter.accept(element)) {
						rejected = true;
						break;
					}
				}
				if (!rejected) {
					indices.add(i);
				}
			}

			fireContentsChanged(this, 0, getSize() - 1);
		}
	}

	@Override
	public int getSize() {
		synchronized(source) {
			return !filters.isEmpty() ? indices.size() : source.getSize();
		}
	}

	@Override
	public Object getElementAt(int index) {
		synchronized(source) {
			return !filters.isEmpty() ? source.getElementAt(indices.get(index)) : source.getElementAt(index);
		}
	}

	@Override
	public void intervalAdded(ListDataEvent e) {
		doFilter();
	}

	@Override
	public void intervalRemoved(ListDataEvent e) {
		doFilter();
	}

	@Override
	public void contentsChanged(ListDataEvent e) {
		doFilter();
	}

	/**
	 * Set the filter enabled flag.
	 * @param filterEnabled
	 */
	public void setFilterEnabled(boolean filterEnabled) {
		this.filterEnabled = filterEnabled;
	}
}