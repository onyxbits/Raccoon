package de.onyxbits.raccoon.gui;

import java.awt.Event;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.File;
import java.io.IOException;
import java.util.Vector;
import java.util.prefs.Preferences;

import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JTabbedPane;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;

import de.onyxbits.raccoon.App;
import de.onyxbits.raccoon.BrowseUtil;
import de.onyxbits.raccoon.Messages;
import de.onyxbits.raccoon.io.Archive;
import de.onyxbits.raccoon.io.DownloadLogger;
import de.onyxbits.raccoon.io.FetchListener;

/**
 * The main UI. This class must be started by creating an object and passing it
 * to SwingUtils.invokeLater()
 * 
 * @author patrick
 * 
 */
public class MainActivity extends JFrame implements ActionListener, WindowListener, Runnable,
		FetchListener {

	private static final long serialVersionUID = 1L;

	/**
	 * Preferences key for storing the last opened archive directory.
	 */
	public static final String LASTARCHIVE = "lastarchive"; //$NON-NLS-1$

	/**
	 * Preferences key for whether or not to fetch icons.
	 */
	public static final String FETCHICONS = "fetchicons"; //$NON-NLS-1$

	private JMenuItem quit;
	private JMenuItem open;
	private JMenuItem search;
	private JMenuItem close;
	private JMenuItem updates;
	private JMenuItem downloads;
	private JMenuItem contents;
	private JMenuItem newArchive;
	private JRadioButtonMenuItem fetchIcons;

	private JTabbedPane views;
	private ListView downloadList;
	private JScrollPane downloadListScroll;
	private DownloadLogger logger;

	private SearchView searchView;

	private Archive archive;
	private static Vector<MainActivity> all = new Vector<MainActivity>();

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
		JMenu file = new JMenu(Messages.getString("MainActivity.1")); //$NON-NLS-1$
		file.setMnemonic(KeyStroke.getKeyStroke(Messages.getString("MainActivity.0")).getKeyCode()); //$NON-NLS-1$
		quit = new JMenuItem(
				Messages.getString("MainActivity.2"), KeyStroke.getKeyStroke(Messages.getString("MainActivity.22")).getKeyCode()); //$NON-NLS-1$ //$NON-NLS-2$
		quit.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Q, Event.CTRL_MASK));
		open = new JMenuItem(
				Messages.getString("MainActivity.3"), KeyStroke.getKeyStroke(Messages.getString("MainActivity.23")).getKeyCode()); //$NON-NLS-1$ //$NON-NLS-2$
		open.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O, Event.CTRL_MASK));
		close = new JMenuItem(
				Messages.getString("MainActivity.4"), KeyStroke.getKeyStroke(Messages.getString("MainActivity.24")).getKeyCode()); //$NON-NLS-1$ //$NON-NLS-2$
		close.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_W, Event.CTRL_MASK));
		updates = new JMenuItem(
				Messages.getString("MainActivity.5"), KeyStroke.getKeyStroke(Messages.getString("MainActivity.25")).getKeyCode()); //$NON-NLS-1$ //$NON-NLS-2$
		updates.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_U, Event.CTRL_MASK));
		updates.setEnabled(false);
		newArchive = new JMenuItem(
				Messages.getString("MainActivity.29"), KeyStroke.getKeyStroke(Messages.getString("MainActivity.30")) //$NON-NLS-1$ //$NON-NLS-2$
						.getKeyCode());
		newArchive.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_N, Event.CTRL_MASK));

		Preferences prefs = Preferences.userNodeForPackage(getClass());
		fetchIcons = new JRadioButtonMenuItem(Messages.getString("MainActivity.34"), prefs.getBoolean(FETCHICONS, false)); //$NON-NLS-1$
		file.add(newArchive);
		file.add(open);
		file.add(new JSeparator());
		file.add(updates);
		file.add(close);
		file.add(new JSeparator());
		file.add(quit);
		bar.add(file);

		JMenu view = new JMenu(Messages.getString("MainActivity.6")); //$NON-NLS-1$
		view.setMnemonic(KeyStroke.getKeyStroke(Messages.getString("MainActivity.15")).getKeyCode()); //$NON-NLS-1$
		search = new JMenuItem(
				Messages.getString("MainActivity.7"), KeyStroke.getKeyStroke(Messages.getString("MainActivity.26")).getKeyCode()); //$NON-NLS-1$ //$NON-NLS-2$
		search.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F, Event.CTRL_MASK));
		downloads = new JMenuItem(
				Messages.getString("MainActivity.8"), KeyStroke.getKeyStroke(Messages.getString("MainActivity.27")).getKeyCode()); //$NON-NLS-1$ //$NON-NLS-2$
		downloads.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_L, Event.CTRL_MASK));
		view.add(search);
		view.add(downloads);
		view.add(new JSeparator());
		view.add(fetchIcons);
		search.setEnabled(false);
		downloads.setEnabled(false);
		bar.add(view);

		JMenu help = new JMenu(Messages.getString("MainActivity.9")); //$NON-NLS-1$
		help.setMnemonic(KeyStroke.getKeyStroke(Messages.getString("MainActivity.21")).getKeyCode()); //$NON-NLS-1$
		contents = new JMenuItem(
				Messages.getString("MainActivity.10"), KeyStroke.getKeyStroke(Messages.getString("MainActivity.28")).getKeyCode()); //$NON-NLS-1$ //$NON-NLS-2$
		contents.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F1, 0));
		help.add(contents);
		bar.add(help);

		setJMenuBar(bar);
		setContentPane(views);
	}

	public void run() {
		if (archive == null) {
			Preferences prefs = Preferences.userNodeForPackage(getClass());
			String tmp = prefs.get(MainActivity.LASTARCHIVE, null);
			if (tmp != null) {
				archive = new Archive(new File(tmp));
			}
			else {
				archive = new Archive(new File(App.getDir(App.ARCHIVEDIR),
						Messages.getString("MainActivity.11"))); //$NON-NLS-1$
			}
		}
		newArchive.addActionListener(this);
		open.addActionListener(this);
		quit.addActionListener(this);
		close.addActionListener(this);
		search.addActionListener(this);
		contents.addActionListener(this);
		downloads.addActionListener(this);
		updates.addActionListener(this);
		fetchIcons.addActionListener(this);
		addWindowListener(this);
		setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		doMount(archive);
		pack();
		setSize(800, 600);
		setVisible(true);
		all.add(this);
	}

	public void actionPerformed(ActionEvent event) {
		Object src = event.getSource();
		if (src == newArchive) {
			doNewArchive();
		}
		if (src == quit) {
			doQuit();
		}
		if (src == close) {
			doClose();
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
			BrowseUtil.openUrl(Messages.getString("MainActivity.12")); //$NON-NLS-1$
		}
		if (src == updates) {
			views.setSelectedIndex(0);
			searchView.doUpdateSearch();
		}
		if (src == fetchIcons) {
			Preferences prefs = Preferences.userNodeForPackage(getClass());
			prefs.putBoolean(FETCHICONS, fetchIcons.isSelected());
		}
	}

	private void doNewArchive() {
		String res = JOptionPane
				.showInputDialog(
						this,
						Messages.getString("MainActivity.32"), Messages.getString("MainActivity.31"), JOptionPane.QUESTION_MESSAGE); //$NON-NLS-1$ //$NON-NLS-2$
		if (res != null && res.length() > 0) {
			File file = new File(App.getDir(App.ARCHIVEDIR), res);
			MainActivity ma = new MainActivity(new Archive(file));
			SwingUtilities.invokeLater(ma);
		}
	}

	private void doClose() {
		if (isDownloading()) {
			int result = JOptionPane
					.showConfirmDialog(
							getRootPane(),
							Messages.getString("MainActivity.13"), //$NON-NLS-1$
							Messages.getString("MainActivity.14"), JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE); //$NON-NLS-1$
			if (result == JOptionPane.NO_OPTION) {
				return;
			}
			for (int i = 0; i < downloadList.getComponentCount(); i++) {
				DownloadView dv = (DownloadView) downloadList.getComponent(i);
				dv.stopWorker();
			}
		}
		all.remove(this);
		setVisible(false);
		if (all.size() == 0) {
			System.exit(0);
		}
	}

	/**
	 * Must be called to connect to an archive.
	 * 
	 * @param archive
	 *          the archive to mount.
	 */
	protected void doMount(Archive archive) {
		this.archive = archive;
		archive.getRoot().mkdirs();
		logger = new DownloadLogger(archive);
		logger.clear();
		setTitle("Raccoon - " + archive.getRoot().getAbsolutePath()); //$NON-NLS-1$
		views.removeAll();
		if (archive.getAndroidId().length() == 0) {
			views.addTab(Messages.getString("MainActivity.16"), InitView.create(this, archive)); //$NON-NLS-1$
		}
		else {
			searchView = SearchView.create(this, archive);
			updates.setEnabled(true);
			views.addTab(Messages.getString("MainActivity.17"), searchView); //$NON-NLS-1$
			views.addChangeListener(searchView);
			search.setEnabled(true);
			downloads.setEnabled(true);
			downloadListScroll = new JScrollPane();
			downloadListScroll.setViewportView(downloadList);
			downloadListScroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
			views.addTab(Messages.getString("MainActivity.18"), downloadListScroll); //$NON-NLS-1$
			Preferences prefs = Preferences.userNodeForPackage(getClass());
			prefs.put(LASTARCHIVE, archive.getRoot().getAbsolutePath());
			SwingUtilities.invokeLater(searchView);
		}
	}

	private void doOpen() {
		JFileChooser chooser = new JFileChooser(App.getDir(App.ARCHIVEDIR));
		chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		int ret = chooser.showOpenDialog(this);
		if (ret == JFileChooser.APPROVE_OPTION) {
			MainActivity ma = new MainActivity(new Archive(chooser.getSelectedFile()));
			SwingUtilities.invokeLater(ma);
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
		d.addFetchListener(this);
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
		boolean ask = false;
		for (MainActivity ma : all) {
			if (ma.isDownloading()) {
				ask = true;
				break;
			}
		}

		if (ask) {
			int result = JOptionPane
					.showConfirmDialog(
							getRootPane(),
							Messages.getString("MainActivity.19"), //$NON-NLS-1$
							Messages.getString("MainActivity.20"), JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE); //$NON-NLS-1$
			if (result == JOptionPane.NO_OPTION) {
				return;
			}
		}
		for (MainActivity ma : all) {
			for (int i = 0; i < downloadList.getComponentCount(); i++) {
				DownloadView dv = (DownloadView) ma.downloadList.getComponent(i);
				dv.stopWorker();
			}
		}
		System.exit(0);
	}

	public void windowClosing(WindowEvent arg0) {
		doClose();
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

	public boolean onChunk(Object src, long numBytes) {
		return false;
	}

	public void onComplete(Object src) {
		try {
			logger.addEntry(((DownloadWorker) src).getTarget());
		}
		catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void onFailure(Object src, Exception e) {
		e.printStackTrace();
	}

	public void onAborted(Object src) {
	}

}
