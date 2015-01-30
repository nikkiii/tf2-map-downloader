package org.nikkii.mapdownloader;

import org.nikkii.mapdownloader.download.DecompressProgressListener;
import org.nikkii.mapdownloader.download.DownloadProgressListener;
import org.nikkii.mapdownloader.download.MapDownloader;
import org.nikkii.mapdownloader.maps.Map;
import org.nikkii.mapdownloader.maps.MapSource;
import org.nikkii.mapdownloader.maps.filter.DuplicateMapFilter;
import org.nikkii.mapdownloader.maps.filter.MapNameFilter;
import org.nikkii.mapdownloader.maps.filter.MapSourceFilter;
import org.nikkii.mapdownloader.maps.filter.StockMapFilter;
import org.nikkii.mapdownloader.util.ui.FilteredListModel;
import org.nikkii.mapdownloader.util.ui.PlaceholderTextField;

import javax.swing.DefaultListCellRenderer;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JProgressBar;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.Color;
import java.awt.Component;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.util.LinkedList;
import java.util.Queue;

/**
 * @author Nikki
 */
public class MapDownloaderPanel extends javax.swing.JPanel {
	/**
	 * The decompression string format.
	 */
	private static final String DECOMPRESS_FORMAT = "Decompressing {map}...";

	/**
	 * The base map model.
	 */
	private final DefaultListModel mapListModel = new DefaultListModel();

	/**
	 * The filtered map model.
	 */
	private final FilteredListModel<Map> filteredModel = new FilteredListModel<Map>(mapListModel);

	/**
	 * The game install path.
	 */
	private File installPath;

	/**
	 * The game map folder.
	 */
	private File mapFolder;

	/**
	 * The source list filter.
	 */
	private final MapSourceFilter sourceFilter = new MapSourceFilter();

	/**
	 * The stock map filter.
	 */
	private final StockMapFilter stockFilter = new StockMapFilter();

	/**
	 * The search query list filter.
	 */
	private final MapNameFilter filter = new MapNameFilter();

	/**
	 * The duplicate map name filter.
	 */
	private final DuplicateMapFilter duplicateFilter = new DuplicateMapFilter();

	/**
	 * The downloader instance. This'll be null unless we already have a download going.
	 */
	private MapDownloader downloader;

	/**
	 * The download queue.
	 */
	private Queue<Map> queue = new LinkedList<>();

	/**
	 * Creates new form MapDownloader
	 */
	public MapDownloaderPanel() {
		initComponents();
	}

	@SuppressWarnings("unchecked")
	private void initComponents() {
		searchField = new PlaceholderTextField();
		jScrollPane1 = new javax.swing.JScrollPane();
		mapList = new javax.swing.JList();
		progressBar = new javax.swing.JProgressBar();
		currentFileLabel = new javax.swing.JLabel();
		jLabel1 = new javax.swing.JLabel();
		changeButton = new javax.swing.JButton();

		setPreferredSize(new java.awt.Dimension(560, 600));

		searchField.setPlaceholder("Search");

		searchField.addKeyListener(new KeyAdapter() {
			@Override
			public void keyReleased(KeyEvent e) {
				filter.setFilter(searchField.getText());
				filteredModel.doFilter();
			}
		});

		filteredModel.addFilter(sourceFilter);
		filteredModel.addFilter(filter);
		filteredModel.addFilter(duplicateFilter);

		mapList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);

		mapList.setModel(filteredModel);
		jScrollPane1.setViewportView(mapList);

		mapList.setCellRenderer(new DefaultListCellRenderer() {
			@Override
			public Component getListCellRendererComponent(JList list, Object value, int index,  boolean isSelected, boolean cellHasFocus) {
				Component c = super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
				if (mapFolder != null && mapFolder.exists() && value instanceof Map) {
					if (mapExists((Map) value)) {
						setBackground(Color.GREEN);
					} else {
						setBackground(getBackground());
					}
					if (isSelected) {
						setBackground(getBackground().darker());
					}
				}
				return c;
			}
		});

		mapList.addListSelectionListener(e -> {
			int[] selected = mapList.getSelectedIndices();

			if (selected.length < 1) {
				return;
			}

			int existingCount = 0;

			for (int index : selected) {
				Map map = (Map) filteredModel.getElementAt(index);

				if (mapExists(map)) {
					existingCount++;
				}
			}

			if (existingCount == selected.length) {
				downloadButton.setText("Delete");
			} else {
				downloadButton.setText("Download");
			}
		});

		mapList.addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent e) {
				if (e.getClickCount() == 2) {
					Point point = e.getPoint();

					int index = mapList.locationToIndex(point);

					mapList.setSelectedIndex(index);

					if (index == -1) {
						return;
					}

					if (downloader != null) {
						return;
					}

					Map map = (Map) filteredModel.getElementAt(index);

					if (mapFolder == null || !mapFolder.exists()) {
						return;
					}

					File mapFile = new File(mapFolder, map.getName() + ".bsp");

					if (mapFile.exists()) {
						int confirm = JOptionPane.showConfirmDialog(MapDownloaderPanel.this, "Do you wish to delete " + map + "?", "Delete map", JOptionPane.YES_NO_OPTION);

						if (confirm == JOptionPane.YES_OPTION) {
							mapFile.delete();
						}
					} else {
						int confirm = JOptionPane.showConfirmDialog(MapDownloaderPanel.this, "Do you wish to download " + map + "?", "Download map", JOptionPane.YES_NO_OPTION);

						if (confirm == JOptionPane.YES_OPTION) {
							downloadMap(map);
						}
					}
				}
			}
		});

		currentFileLabel.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
		currentFileLabel.setText("Status: Idle.");

		jLabel1.setText("Path: Unknown.");

		changeButton.setText("Browse");

		changeButton.addActionListener(e -> {
			JFileChooser chooser = new JFileChooser();

			chooser.setCurrentDirectory(installPath != null ? installPath : new File(System.getProperty("user.home")));

			chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);

			chooser.setVisible(true);

			int option = chooser.showOpenDialog(null);

			if (option != JFileChooser.APPROVE_OPTION) {
				return;
			}

			File newDir = chooser.getSelectedFile(), tfDir = new File(newDir, "tf");

			if (!tfDir.exists()) {
				JOptionPane.showMessageDialog(null, "Unable to find \"tf\" folder in the specified directory. Please try again.");
				return;
			}

			setInstallPath(newDir);
		});

		javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
		this.setLayout(layout);
		layout.setHorizontalGroup(
			layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
				.addComponent(searchField)
				.addComponent(jScrollPane1)
				.addComponent(progressBar, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
				.addComponent(currentFileLabel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
				.addGroup(layout.createSequentialGroup()
					.addComponent(jLabel1, javax.swing.GroupLayout.DEFAULT_SIZE, 483, Short.MAX_VALUE)
					.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
					.addComponent(changeButton))
		);
		layout.setVerticalGroup(
			layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
				.addGroup(layout.createSequentialGroup()
					.addComponent(searchField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
					.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
					.addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 494, javax.swing.GroupLayout.PREFERRED_SIZE)
					.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
					.addComponent(progressBar, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE)
					.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
					.addComponent(currentFileLabel)
					.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
					.addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
						.addComponent(jLabel1)
						.addComponent(changeButton))
					.addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
		);
	}// </editor-fold>

	private boolean mapExists(Map map) {
		return new File(mapFolder, map.getName() + ".bsp").exists();
	}

	/**
	 * Download a specific map.
	 *
	 * @param map The map to download.
	 */
	private void downloadMap(Map map) {
		if (!installPath.exists() || downloader != null) {
			return;
		}

		System.out.println("Download " + map + " from " + map.getSource() + " to " + mapFolder);

		if (!mapFolder.exists()) {
			mapFolder.mkdirs();
		}

		downloader = new MapDownloader(map, new File(mapFolder, map.getName() + ".bsp"));

		downloader.addListener(new DownloadProgressListener(currentFileLabel, progressBar, map));

		downloader.addListener(new ProgressAdaptor() {
			@Override
			public void progressFinished() {
				downloader = null;

				if (!queue.isEmpty()) {
					downloadMap(queue.poll());
				} else {
					downloadButton.setText("Download");
				}
			}
		});

		downloader.addDecompressorListener(new ProgressAdaptor() {
			@Override
			public void progressStarted(long fileSize) {
				downloadButton.setEnabled(false);
			}

			@Override
			public void progressFinished() {
				downloadButton.setEnabled(true);
			}
		});
		downloader.addDecompressorListener(new DecompressProgressListener(currentFileLabel, progressBar, DECOMPRESS_FORMAT.replace("{map}", map.getName())));

		downloader.start();
	}


	// Variables declaration - do not modify
	private javax.swing.JButton changeButton;
	private javax.swing.JLabel currentFileLabel;
	private javax.swing.JLabel jLabel1;
	private javax.swing.JScrollPane jScrollPane1;
	private javax.swing.JList mapList;
	private javax.swing.JProgressBar progressBar;
	private PlaceholderTextField searchField;

	/**
	 * Actually belongs to a JFrame, but we keep a reference.
	 */
	private JButton downloadButton;

	/**
	 * Add the specified maps to the model. THIS IS NOT THREAD SAFE!
	 *
	 * @param maps The maps to add.
	 */
	public void addMaps(Map... maps) {
		synchronized(mapListModel) {
			for (Map map : maps) {
				if (!stockFilter.accept(map)) {
					continue;
				}
				mapListModel.addElement(map);
			}
		}
	}

	public void setDownloadButton(JButton downloadButton) {
		this.downloadButton = downloadButton;
	}

	/**
	 * Set the install path.
	 *
	 * @param installPath The install path.
	 */
	public void setInstallPath(File installPath) {
		this.installPath = installPath;
		this.mapFolder = new File(installPath, "tf/download/maps");

		if (!mapFolder.exists()) {
			mapFolder.mkdirs();
		}

		File stockFolder = new File("tf/maps");

		if (stockFolder.exists()) {
			for (File file : stockFolder.listFiles()) {
				String f = file.getName();

				if (!f.contains("bsp")) continue;

				f = f.substring(0, f.indexOf('.'));

				stockFilter.add(f);
			}
		}

		jLabel1.setText("Path: " + installPath.getAbsolutePath());
	}

	/**
	 * Enable/Disable a source.
	 *
	 * @param source The map source.
	 * @param enabled The enable flag.
	 */
	public void setSourceEnabled(MapSource source, boolean enabled) {
		sourceFilter.setSourceEnabled(source, enabled);
		filteredModel.doFilter();
	}

	/**
	 * Set the status label text.
	 *
	 * @param text The label text.
	 */
	public void setStatusLabelText(String text) {
		currentFileLabel.setText(text);
	}

	/**
	 * Get the progress bar.
	 *
	 * @return The progress bar.
	 */
	public JProgressBar getProgressBar() {
		return progressBar;
	}

	public void downloadSelectedMaps() {
		if (downloadButton.getText().equals("Cancel")) {
			queue.clear();
			// Cancel the current download if possible.
			if (downloader != null) {
				downloader.cancel();
			}
			return;
		}

		int[] selected = mapList.getSelectedIndices();

		if (selected.length < 1) {
			return;
		}

		boolean delete = downloadButton.getText().equals("Delete");

		for (int index : selected) {
			Map map = (Map) filteredModel.getElementAt(index);

			if (delete) {
				if (mapExists(map)) {
					new File(mapFolder, map.getName() + ".bsp").delete();
				}
			} else {
				queue.add(map);
			}
		}

		if (!queue.isEmpty()) {
			if (downloader == null) {
				downloadMap(queue.poll());
			}

			downloadButton.setText("Cancel");
		}
	}
}