package de.onyxbits.raccoon.gui;

import java.awt.Event;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.File;
import java.net.URL;
import java.util.List;
import java.util.StringTokenizer;
import java.util.Vector;
import java.util.prefs.Preferences;

import javax.swing.ImageIcon;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JList;
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
	public static final String LASTARCHIVE = "lastarchive"; //$NON-NLS-1$

	/**
	 * Preferences key for whether or not to fetch icons.
	 */
	public static final String FETCHICONS = "fetchicons"; //$NON-NLS-1$

	private JMenuItem quit;
	private JMenuItem open;
	private JMenuItem search;
	private JMenuItem close;
	private JMenuItem update;
	private JMenuItem exportArchive;
	private JMenuItem importArchive;
	private JMenuItem downloads;
	private JMenuItem contents;
	private JMenuItem newArchive;
	private JRadioButtonMenuItem fetchIcons;

	private JTabbedPane views;
	private ListView downloadList;
	private JScrollPane downloadListScroll;

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
		update = new JMenuItem(
				Messages.getString("MainActivity.5"), KeyStroke.getKeyStroke(Messages.getString("MainActivity.25")).getKeyCode()); //$NON-NLS-1$ //$NON-NLS-2$
		update.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_U, Event.CTRL_MASK));
		update.setEnabled(false);
		newArchive = new JMenuItem(
				Messages.getString("MainActivity.29"), KeyStroke.getKeyStroke(Messages.getString("MainActivity.30")) //$NON-NLS-1$ //$NON-NLS-2$
						.getKeyCode());
		newArchive.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_N, Event.CTRL_MASK));
		exportArchive = new JMenuItem(
				Messages.getString("MainActivity.33"), KeyStroke.getKeyStroke(Messages.getString("MainActivity.35")).getKeyCode()); //$NON-NLS-1$ //$NON-NLS-2$
		importArchive = new JMenuItem(
				Messages.getString("MainActivity.36"), KeyStroke.getKeyStroke(Messages.getString("MainActivity.37")).getKeyCode()); //$NON-NLS-1$ //$NON-NLS-2$

		Preferences prefs = Preferences.userNodeForPackage(getClass());
		fetchIcons = new JRadioButtonMenuItem(
				Messages.getString("MainActivity.34"), prefs.getBoolean(FETCHICONS, true)); //$NON-NLS-1$
		file.add(newArchive);
		file.add(open);
		file.add(new JSeparator());
		file.add(importArchive);
		file.add(exportArchive);
		file.add(update);
		file.add(new JSeparator());
		file.add(close);
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
		importArchive.setEnabled(false);
		exportArchive.setEnabled(false);
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
		update.addActionListener(this);
		fetchIcons.addActionListener(this);
		exportArchive.addActionListener(this);
		importArchive.addActionListener(this);
		addWindowListener(this);
		setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		URL img = getClass().getResource("/rsrc/icons/appicon.png"); //$NON-NLS-1$
		setIconImage(new ImageIcon(img, "").getImage()); //$NON-NLS-1$
		doMount(archive);
		pack();
		setSize(1024, 768);
		setVisible(true);
		all.add(this);
	}

	public void actionPerformed(ActionEvent event) {
		Object src = event.getSource();
		if (src == downloads) {
			views.setSelectedIndex(1);
		}
		if (src == search) {
			views.setSelectedIndex(0);
		}
		if (src == newArchive) {
			if (searchView != null) {
				searchView.doMessage(null);
			}
			doNewArchive();
		}
		if (src == quit) {
			if (searchView != null) {
				searchView.doMessage(null);
			}
			doQuit();
		}
		if (src == close) {
			if (searchView != null) {
				searchView.doMessage(null);
			}
			doClose();
		}
		if (src == open) {
			if (searchView != null) {
				searchView.doMessage(null);
			}
			doOpen();
		}
		if (src == contents) {
			if (searchView != null) {
				searchView.doMessage(null);
			}
			BrowseUtil.openUrl(Messages.getString("MainActivity.12")); //$NON-NLS-1$
		}
		if (src == update) {
			views.setSelectedIndex(0);
			searchView.doUpdateSearch();
		}
		if (src == fetchIcons) {
			Preferences prefs = Preferences.userNodeForPackage(getClass());
			prefs.putBoolean(FETCHICONS, fetchIcons.isSelected());
		}
		if (src == exportArchive) {
			searchView.doMessage(null);
			doExport();
		}
		if (src == importArchive) {
			searchView.doMessage(null);
			doImport();
		}
	}

	private void doImport() {
		try {
			String data = (String) Toolkit.getDefaultToolkit().getSystemClipboard()
					.getData(DataFlavor.stringFlavor);
			String prefix = "market://details?id="; //$NON-NLS-1$
			StringTokenizer st = new StringTokenizer(data);
			Vector<String> lst = new Vector<String>();
			int count = 0;
			while (st.hasMoreElements()) {
				// Lets first check the entire clipboard if its content is well formed.
				String url = st.nextToken();
				if (url.startsWith(prefix) && url.length() > prefix.length()) {
					// Let's keep it simple.
					String id = url.substring(prefix.length(), url.length());
					if (!lst.contains(id) && !archive.fileUnder(id, 0).getParentFile().exists()) {
						lst.add(id);
						count++;
					}
				}
			}

			// We got at least one new app id. Ask the user to confirm the list, then
			// create files.
			if (count > 0) {
				JList<String> all = new JList<String>(lst);
				all.addSelectionInterval(0, lst.size() - 1);
				if (JOptionPane
						.showConfirmDialog(
								this,
								new JScrollPane(all),
								Messages.getString("MainActivity.42"), JOptionPane.OK_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE, null) == JOptionPane.OK_OPTION) {//$NON-NLS-1$
					int[] sel = all.getSelectedIndices();
					for (int idx : sel) {
						archive.fileUnder(all.getModel().getElementAt(idx), 0).getParentFile().mkdirs();
					}
					searchView.doMessage(Messages.getString("MainActivity.39")); //$NON-NLS-1$
				}
			}
			else {
				// Tell the user that no new items were found.
				searchView.doMessage(Messages.getString("MainActivity.43")); //$NON-NLS-1$
			}
		}
		catch (Exception e) {
			searchView.doMessage(Messages.getString("MainActivity.40")); //$NON-NLS-1$
			// e.printStackTrace();
		}
	}

	private void doExport() {
		List<String> lst = archive.list();
		StringBuilder sb = new StringBuilder();
		for (String s : lst) {
			sb.append("market://details?id="); //$NON-NLS-1$
			sb.append(s);
			sb.append("\n"); //$NON-NLS-1$
		}
		StringSelection stringSelection = new StringSelection(sb.toString());
		Clipboard clpbrd = Toolkit.getDefaultToolkit().getSystemClipboard();
		clpbrd.setContents(stringSelection, null);
		searchView.doMessage(Messages.getString("MainActivity.38")); //$NON-NLS-1$
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
		new DownloadLogger(archive).clear();
		setTitle("Raccoon - " + archive.getRoot().getAbsolutePath()); //$NON-NLS-1$
		views.removeAll();
		if (archive.getAndroidId().length() == 0) {
			views.addTab(Messages.getString("MainActivity.16"), InitView.create(this, archive)); //$NON-NLS-1$
		}
		else {
			searchView = SearchView.create(this, archive);
			update.setEnabled(true);
			views.addTab(Messages.getString("MainActivity.17"), searchView); //$NON-NLS-1$
			views.addChangeListener(searchView);
			search.setEnabled(true);
			downloads.setEnabled(true);
			importArchive.setEnabled(true);
			exportArchive.setEnabled(true);
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

}
