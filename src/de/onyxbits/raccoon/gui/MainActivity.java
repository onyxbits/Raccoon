package de.onyxbits.raccoon.gui;

import java.awt.Event;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.File;
import java.util.prefs.Preferences;

import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;

import de.onyxbits.raccoon.BrowseUtil;
import de.onyxbits.raccoon.io.Archive;

/**
 * The main UI. This class must be started by creating an object and passing it
 * to SwingUtils.invokeLater()
 * 
 * @author patrick
 * 
 */
public class MainActivity extends JFrame implements ActionListener, WindowListener, Runnable {

	private static final long serialVersionUID = 1L;

	/**
	 * Preferences key for storing the last opened archive directory.
	 */
	public static final String LASTARCHIVE = "lastarchive";

	private JMenuItem quit;
	private JMenuItem open;
	private JMenuItem search;
	private JMenuItem downloads;
	private JMenuItem contents;

	private JTabbedPane views;
	private ListView downloadList;
	private JScrollPane downloadListScroll;

	private Archive archive;

	/**
	 * New GUI
	 * 
	 * @param archive
	 *          the archive to display. This may be null.
	 */
	public MainActivity(Archive archive) {
		this.archive = archive;
		views = new JTabbedPane();
		downloadList = new ListView();

		JMenuBar bar = new JMenuBar();
		JMenu file = new JMenu("File");
		file.setMnemonic('f');
		quit = new JMenuItem("Exit", 'x');
		quit.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Q, Event.CTRL_MASK));
		open = new JMenuItem("Switch archive");
		file.add(open);
		file.add(quit);
		bar.add(file);

		JMenu view = new JMenu("View");
		view.setMnemonic('v');
		search = new JMenuItem("Search", 's');
		search.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F, Event.CTRL_MASK));
		downloads = new JMenuItem("Downloads", 'd');
		downloads.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_L, Event.CTRL_MASK));
		view.add(search);
		view.add(downloads);
		search.setEnabled(false);
		downloads.setEnabled(false);
		bar.add(view);

		JMenu help = new JMenu("Help");
		help.setMnemonic('h');
		contents = new JMenuItem("Contents", 'c');
		contents.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F1, 0));
		help.add(contents);
		bar.add(help);

		setJMenuBar(bar);
		setContentPane(views);
	}

	public void run() {
		if (archive == null) {
			Preferences prefs = Preferences.userNodeForPackage(getClass());
			archive = new Archive(new File(prefs.get(MainActivity.LASTARCHIVE, "Raccoon")));
		}
		archive.getDownloadLogger().clear();
		open.addActionListener(this);
		quit.addActionListener(this);
		search.addActionListener(this);
		contents.addActionListener(this);
		downloads.addActionListener(this);
		addWindowListener(this);
		setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		doMount(archive);
		pack();
		setSize(800, 600);
		setVisible(true);
	}

	public void actionPerformed(ActionEvent event) {
		Object src = event.getSource();
		if (src == quit) {
			doQuit();
		}
		if (src == open) {
			doOpen();
		}
		if (src == downloads) {
			views.setSelectedIndex(1);
		}
		if (src == search) {
			views.setSelectedIndex(0);
		}
		if (src == contents) {
			BrowseUtil.openUrl("http://www.onyxbits.de/raccoon/handbook");
		}
	}

	/**
	 * Must be called to connect to an archive.
	 * 
	 * @param archive
	 *          the archive to mount.
	 */
	protected void doMount(Archive archive) {
		this.archive=archive;
		archive.getRoot().mkdirs();
		archive.getDownloadLogger().clear();
		setTitle("Raccoon - " + archive.getRoot().getAbsolutePath());
		views.removeAll();
		if (archive.getAndroidId().length() == 0) {
			views.addTab("Init", InitView.create(this, archive));
		}
		else {
			SearchView sv = SearchView.create(this, archive);
			views.addTab("Search", sv);
			views.addChangeListener(sv);
			search.setEnabled(true);
			downloads.setEnabled(true);
			downloadListScroll = new JScrollPane();
			downloadListScroll.setViewportView(downloadList);
			downloadListScroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
			views.addTab("Downloads", downloadListScroll);
			Preferences prefs = Preferences.userNodeForPackage(getClass());
			prefs.put(LASTARCHIVE, archive.getRoot().getAbsolutePath());
			SwingUtilities.invokeLater(sv);
		}
	}

	private void doOpen() {
		if (isDownloading()) {
			int result = JOptionPane.showConfirmDialog(getRootPane(),
					"This will cancel your current downloads. Really proceed?", "Still Downloading",
					JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
			if (result == JOptionPane.YES_OPTION) {
				for (int i = 0; i < downloadList.getComponentCount(); i++) {
					DownloadView dv = (DownloadView) downloadList.getComponent(i);
					dv.stopWorker();
				}
			}
		}
		JFileChooser chooser = new JFileChooser();
		chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		int ret = chooser.showOpenDialog(this);
		if (ret == JFileChooser.APPROVE_OPTION) {
			doMount(new Archive(chooser.getSelectedFile()));
		}
	}

	/**
	 * Trigger a download
	 * 
	 * @param d
	 *          packagename.
	 */
	public void doDownload(DownloadView d) {
		downloadList.add(d);
		d.startWorker();
		views.setSelectedComponent(downloadListScroll);
	}

	private boolean isDownloading() {
		for (int i = 0; i < downloadList.getComponentCount(); i++) {
			DownloadView dv = (DownloadView) downloadList.getComponent(i);
			if (dv.isDownloading()) {
				return true;
			}
		}
		return false;
	}

	private void doQuit() {
		if (isDownloading()) {
			int result = JOptionPane.showConfirmDialog(getRootPane(), "Really quit?",
					"Still Downloading", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
			if (result == JOptionPane.YES_OPTION) {
				for (int i = 0; i < downloadList.getComponentCount(); i++) {
					DownloadView dv = (DownloadView) downloadList.getComponent(i);
					dv.stopWorker();
				}
				System.exit(0);
			}
			else {
				return;
			}
		}
		System.exit(0);
	}

	public void windowClosing(WindowEvent arg0) {
		doQuit();
	}

	public void windowActivated(WindowEvent arg0) {
	}

	public void windowClosed(WindowEvent arg0) {
	}

	public void windowDeactivated(WindowEvent arg0) {
	}

	public void windowDeiconified(WindowEvent arg0) {
	}

	public void windowIconified(WindowEvent arg0) {
	}

	public void windowOpened(WindowEvent arg0) {
	}

}
