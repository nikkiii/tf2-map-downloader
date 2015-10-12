package org.nikkii.mapdownloader;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.nikkii.mapdownloader.maps.Map;
import org.nikkii.mapdownloader.maps.MapSource;
import org.nikkii.mapdownloader.util.WinRegistry;

import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.UIManager;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * @author Nikki
 */
public class MapDownloader {

	/**
	 * Registry keys for windows systems. These SHOULD be reliable on most windows systems.
	 */
	public static final String[] PATHS = new String[] {
		"SOFTWARE\\Microsoft\\Windows\\CurrentVersion\\Uninstall\\Steam App 440",
		"SOFTWARE\\Wow6432Node\\Microsoft\\Windows\\CurrentVersion\\Uninstall\\Steam App 440"
	};

	/**
	 * The download sources.
	 */
	private static final MapSource[] SOURCES = new MapSource[] {
		new MapSource("ProbablyAServer", "http://cdn.probablyaserver.com/tf/maps/", 0),
		new MapSource("FakkelBrigade", "http://fakkelbrigade.eu/maps/", 1)
	};

	public static void main(String[] args) throws Exception {
		UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());

		new MapDownloader();
	}

	/**
	 * The GUI instance.
	 */
	private final MapDownloaderPanel mapPanel;

	/**
	 * Construct a new downloader.
	 *
	 * @throws Exception
	 */
	public MapDownloader() throws Exception {
		mapPanel = new MapDownloaderPanel();

		String installPathString = null;

		try {
			for (String s : PATHS) {
				installPathString = WinRegistry.readString(WinRegistry.HKEY_LOCAL_MACHINE, s, "InstallLocation");

				if (installPathString != null) {
					break;
				}
			}
		} catch (Exception e) {
			System.out.println("Unable to find install location.");
		}

		File installPath = new File(installPathString);

		if (installPathString != null && installPath.exists()) {
			System.out.println("Found install path " + installPathString);
			mapPanel.setInstallPath(installPath);
		}

		initFrame();

		loadMaps();
	}

	/**
	 * Initialize the JMenu.
	 */
	private void initFrame() {
		JMenuBar menu = new JMenuBar();

		final JMenu sources = new JMenu("Sources");

		for (final MapSource source : SOURCES) {
			final JCheckBoxMenuItem sourceItem = new JCheckBoxMenuItem(source.getName(), true);
			sourceItem.addChangeListener(e -> mapPanel.setSourceEnabled(source, sourceItem.isSelected()));
			sources.add(sourceItem);
		}

		menu.add(sources);

		menu.add(Box.createHorizontalGlue());

		JButton downloadSelected = new JButton("Download");

		mapPanel.setDownloadButton(downloadSelected);

		downloadSelected.addActionListener(e -> mapPanel.downloadSelectedMaps());

		menu.add(downloadSelected);

		JFrame frame = new JFrame("Map Downloader");
		frame.add(mapPanel);
		frame.setJMenuBar(menu);
		frame.setResizable(false);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.pack();
		frame.setVisible(true);
	}

	/**
	 * Load the maps from the directory listings.
	 */
	public void loadMaps() {
		List<Map> maps = new ArrayList<>();

		mapPanel.setStatusLabelText("Downloading map lists...");

		mapPanel.getProgressBar().setIndeterminate(true);

		for (MapSource source : SOURCES) {
			try {
				mapPanel.setStatusLabelText("Downloading map list for " + source.getName() + "...");

				Document document = Jsoup.connect(source.getUrl()).get();

				Elements links = document.select("a");

				int count = 0;

				for (Element link : links) {
					String href = link.attr("href");

					if (href.endsWith(".bsp") || href.endsWith(".bsp.bz2")) {
						String mapName = href.substring(0, href.indexOf('.'));

						maps.add(new Map(source, href, mapName, href.indexOf(".bz2") != -1));
						count++;
					}
				}

				mapPanel.setSourceEnabled(source, true);

				System.out.println("Loaded " + count + " maps from " + source.getName());
			} catch (IOException e) {
				// Skip
			}
		}

		mapPanel.getProgressBar().setIndeterminate(false);

		mapPanel.setStatusLabelText("Status: Idle.");

		Collections.sort(maps, new Comparator<Map>() {
			@Override
			public int compare(Map map1, Map map2) {
				int comp = map1.getName().compareToIgnoreCase(map2.getName());

				if (comp == 0) {
					if (map1.getSource().getPriority() < map2.getSource().getPriority()) {
						return -1;
					} else if(map1.getSource().getPriority() > map2.getSource().getPriority()) {
						return 1;
					}
				}

				return comp;
			}
		});

		mapPanel.addMaps(maps.toArray(new Map[maps.size()]));
	}
}
