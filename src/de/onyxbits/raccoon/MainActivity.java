package de.onyxbits.raccoon;

import java.awt.Event;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
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

public class MainActivity extends JFrame implements ActionListener, WindowListener {

	private static final long serialVersionUID = 1L;

	/**
	 * Preferences key for storing the last opened archive directory.
	 */
	public static final String LASTARCHIVE = "lastarchive";


	private JMenuItem quit;
	private JMenuItem open;

	private JTabbedPane views;
	private ListView downloadList;
	private JScrollPane downloadListScroll;

	public MainActivity() {
		views = new JTabbedPane();
		downloadList = new ListView();

		JMenuBar bar = new JMenuBar();
		JMenu file = new JMenu("File");
		file.setMnemonic('f');
		quit = new JMenuItem("Exit");
		quit.setMnemonic('x');
		quit.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Q, Event.CTRL_MASK));
		open = new JMenuItem("Switch archive");
		file.add(open);
		file.add(quit);
		bar.add(file);
		setJMenuBar(bar);
		setSize(800, 600);
		setContentPane(views);
	}
	
	public static MainActivity create() {
		MainActivity ret = new MainActivity();
		ret.open.addActionListener(ret);
		ret.quit.addActionListener(ret);
		ret.addWindowListener(ret);
		ret.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		return ret;
	}

	/**
	 * Connect actionlisteners etc. must be called before making the frame visible
	 * for the first time.
	 */
	public void resolve() {
		open.addActionListener(this);
		quit.addActionListener(this);
		addWindowListener(this);
		setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
	}

	public void actionPerformed(ActionEvent event) {
		Object src = event.getSource();
		if (src == quit) {
			doQuit();
		}
		if (src == open) {
			doOpen();
		}
	}

	/**
	 * Must be called to connect to an archive.
	 * 
	 * @param archive
	 *          the archive to mount.
	 */
	public void doMount(Archive archive) {
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
			downloadListScroll = new JScrollPane();
			downloadListScroll.setViewportView(downloadList);
			views.addTab("Downloads", downloadListScroll);
			Preferences prefs = Preferences.userNodeForPackage(getClass());
			prefs.put(LASTARCHIVE, archive.getRoot().getAbsolutePath());
			SwingUtilities.invokeLater(sv);
		}
	}

	private void doOpen() {
		if (isDownloading()) {
			int result = JOptionPane.showConfirmDialog(getRootPane(), "This will cancel your current downloads. Really proceed?",
					"Still Downloading", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
			if (result==JOptionPane.YES_OPTION) {
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
			if (result==JOptionPane.YES_OPTION) {
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
	
	public void windowActivated(WindowEvent arg0) {}
	public void windowClosed(WindowEvent arg0) {}
	public void windowDeactivated(WindowEvent arg0) {}
	public void windowDeiconified(WindowEvent arg0) {}
	public void windowIconified(WindowEvent arg0) {}
	public void windowOpened(WindowEvent arg0) {}

}
