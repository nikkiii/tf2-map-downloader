package org.nikkii.mapdownloader;

import org.nikkii.mapdownloader.download.DecompressProgressListener;
import org.nikkii.mapdownloader.download.DownloadProgressListener;
import org.nikkii.mapdownloader.download.MapDownloader;
import org.nikkii.mapdownloader.maps.Map;
import org.nikkii.mapdownloader.maps.MapSource;
import org.nikkii.mapdownloader.maps.filter.DuplicateMapFilter;
import org.nikkii.mapdownloader.maps.filter.MapNameFilter;
import org.nikkii.mapdownloader.maps.filter.MapSourceFilter;
import org.nikkii.mapdownloader.util.ui.FilteredListModel;
import org.nikkii.mapdownloader.util.ui.PlaceholderTextField;

import javax.swing.DefaultListCellRenderer;
import javax.swing.DefaultListModel;
import javax.swing.JFileChooser;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JProgressBar;
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

		mapList.setModel(filteredModel);
		jScrollPane1.setViewportView(mapList);

		mapList.setCellRenderer(new DefaultListCellRenderer() {
			@Override
			public Component getListCellRendererComponent(JList list, Object value, int index,  boolean isSelected, boolean cellHasFocus) {
				Component c = super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
				if (mapFolder != null && mapFolder.exists() && value instanceof Map) {
					File mapFile = new File(mapFolder, ((Map) value).getName() + ".bsp");
					if (mapFile.exists()) {
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

		mapList.addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent e) {
				Point point = e.getPoint();

				int index = mapList.locationToIndex(point);

				mapList.setSelectedIndex(index);

				if (index == -1) {
					return;
				}

				final Map map = (Map) filteredModel.getElementAt(index);

				if (mapFolder == null || !mapFolder.exists()) {
					return;
				}

				File mapFile = new File(mapFolder, map.getName() + ".bsp");

				if (e.getClickCount() == 2) {
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

		changeButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
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
			}
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

	/**
	 * Download a specific map.
	 *
	 * @param map The map to download.
	 */
	private void downloadMap(Map map) {
		System.out.println("Download " + map + " from " + map.getSource() + " to " + mapFolder);

		if (!installPath.exists() || downloader != null) {
			return;
		}

		if (!mapFolder.exists()) {
			mapFolder.mkdirs();
		}

		downloader = new MapDownloader(map, new File(mapFolder, map.getName() + ".bsp"));

		downloader.addListener(new DownloadProgressListener(currentFileLabel, progressBar, map));

		downloader.addListener(new ProgressAdaptor() {
			@Override
			public void progressFinished() {
				downloader = null;
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
	 * Add the specified maps to the model. THIS IS NOT THREAD SAFE!
	 *
	 * @param maps The maps to add.
	 */
	public void addMaps(Map... maps) {
		synchronized(mapListModel) {
			for (Map map : maps) {
				mapListModel.addElement(map);
			}
		}
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
}