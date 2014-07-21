package de.onyxbits.raccoon.gui;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.HashMap;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JEditorPane;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;

import de.onyxbits.raccoon.App;
import de.onyxbits.raccoon.BrowseUtil;
import de.onyxbits.raccoon.Messages;
import de.onyxbits.raccoon.io.Archive;
import de.onyxbits.raccoon.io.DownloadLogger;
import de.onyxbits.raccoon.io.FileNode;
import de.onyxbits.raccoon.rss.Loader;
import de.onyxbits.raccoon.rss.Parser;

/**
 * A container for putting search results in.
 * 
 * @author patrick
 * 
 */
public class SearchView extends JPanel implements ActionListener, ChangeListener, Runnable,
		HyperlinkListener {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	protected Archive archive;

	private static final String CARDRESULTS = "results"; //$NON-NLS-1$
	private static final String CARDPROGRESS = "progress"; //$NON-NLS-1$
	private static final String CARDMESSAGE = "message"; //$NON-NLS-1$

	private JTextField query;
	private JSpinner page;
	private JScrollPane results;
	private JProgressBar progress;
	private JEditorPane message;
	private JPanel main;
	private CardLayout cardLayout;
	private MainActivity mainActivity;

	private SearchView(MainActivity mainActivity, Archive archive) {
		this.archive = archive;
		this.mainActivity = mainActivity;
		setLayout(new BorderLayout());
		query = new JTextField();
		query.setToolTipText(Messages.getString("SearchView.3")); //$NON-NLS-1$
		page = new JSpinner(new SpinnerNumberModel(1, 1, 10000, 1));
		page.setToolTipText(Messages.getString("SearchView.4")); //$NON-NLS-1$
		results = new JScrollPane();
		cardLayout = new CardLayout();

		HashMap<String, Object> model = new HashMap<String, Object>();
		model.put("app_version", App.VERSIONSTRING); //$NON-NLS-1$
		model.put("i18n_latestnews", Messages.getString("SearchView.2")); //$NON-NLS-1$ //$NON-NLS-2$
		model.put("i18n_lastsession", Messages.getString("SearchView.10")); //$NON-NLS-1$ //$NON-NLS-2$
		model.put("i18n_archive_appcount", Messages.getString("SearchView.1")); //$NON-NLS-1$ //$NON-NLS-2$
		model.put("i18n_archive_folder", Messages.getString("SearchView.11")); //$NON-NLS-1$ //$NON-NLS-2$
		model.put("i18n_none", Messages.getString("SearchView.12")); //$NON-NLS-1$ //$NON-NLS-2$
		model.put("archive_count", archive.countApps()); //$NON-NLS-1$
		model.put("archive_folder", new FileNode(archive.getRoot())); //$NON-NLS-1$
		try {
			Parser parser = new Parser(Loader.getFeedCache().toURI().toString());
			model.put("newsfeed", parser.readFeed().getMessages()); //$NON-NLS-1$
		}
		catch (Exception e) {
			// We can do without the newsfeed
			e.printStackTrace();
		}
		try {
			DownloadLogger dl = new DownloadLogger(archive);
			model.put("lastsession", dl.getLastSessionDownloads()); //$NON-NLS-1$
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		message = new JEditorPane("text/html", TmplTool.transform("splash.html", model)); //$NON-NLS-1$ //$NON-NLS-2$
		message.setEditable(false);
		message.setOpaque(false);
		message.setMargin(new Insets(10, 10, 10, 10));
		;
		progress = new JProgressBar();
		progress.setIndeterminate(true);
		progress.setString(Messages.getString("SearchView.5")); //$NON-NLS-1$
		progress.setStringPainted(true);

		GridBagConstraints center = new GridBagConstraints();
		center.anchor = GridBagConstraints.CENTER;
		center.fill = GridBagConstraints.NONE;

		main = new JPanel();
		main.setLayout(cardLayout);
		main.add(new JScrollPane(message), CARDMESSAGE);
		main.add(results, CARDRESULTS);
		JPanel container = new JPanel();
		container.setOpaque(false);
		container.setLayout(new GridBagLayout());
		container.add(progress, center);
		main.add(new JScrollPane(container), CARDPROGRESS);

		container = new JPanel();
		container.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
		container.setLayout(new BoxLayout(container, BoxLayout.X_AXIS));
		container.add(query);
		container.add(page);
		add(container, BorderLayout.NORTH);
		add(main, BorderLayout.CENTER);
	}

	public static SearchView create(MainActivity mainActivity, Archive archive) {
		SearchView ret = new SearchView(mainActivity, archive);
		ret.query.addActionListener(ret);
		ret.page.addChangeListener(ret);
		ret.message.addHyperlinkListener(ret);
		return ret;
	}

	public void actionPerformed(ActionEvent event) {
		if (event.getSource() == query) {
			page.setValue(1);
			doSearch();
		}
	}

	public void stateChanged(ChangeEvent event) {
		if (event.getSource() == page) {
			doSearch();
		}
		// Slightly ugly: a searchview is meant to sit in a JTabbedPane and
		// registered as it's ChangeListener, so the query can get focus whenever
		// the user switches to this view. In fact, we consider the query field to
		// be so important that we always focus it, no matter what.
		query.requestFocusInWindow();
	}

	/**
	 * After adding this view to a JTabbedPane, post it to the EDT via
	 * SwingUtils.invokeLater() to ensure the query gets the inputfocus.
	 */
	public void run() {
		query.requestFocusInWindow();
	}

	protected void doSearch() {
		if (query.getText().length() == 0) {
			doMessage(Messages.getString("SearchView.6")); //$NON-NLS-1$
		}
		else {
			query.setEnabled(false);
			page.setEnabled(false);
			cardLayout.show(main, CARDPROGRESS);
			int offset = (Integer) page.getValue();
			offset = (offset - 1) * 10;
			new SearchWorker(archive, query.getText(), this).withOffset(offset).withLimit(10).execute();
		}
	}

	protected void doMessage(String status) {
		query.setEnabled(true);
		page.setEnabled(true);
		cardLayout.show(main, CARDMESSAGE);
		HashMap<String, Object> model = new HashMap<String, Object>();
		model.put("message", status); //$NON-NLS-1$
		message.setText(TmplTool.transform("message.html", model)); //$NON-NLS-1$
	}

	protected void doResultList(JPanel listing) {
		query.setEnabled(true);
		page.setEnabled(true);
		cardLayout.show(main, CARDRESULTS);
		results.setViewportView(listing);
	}

	public void doUpdateSearch() {
		query.setEnabled(false);
		page.setEnabled(false);
		cardLayout.show(main, CARDPROGRESS);
		new SearchWorker(archive, null, this).execute();
	}

	protected void doDownload(DownloadView d) {
		if (d.isDownloaded()) {
			int result = JOptionPane.showConfirmDialog(getRootPane(),
					Messages.getString("SearchView.7"), Messages.getString("SearchView.8"), //$NON-NLS-1$ //$NON-NLS-2$
					JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
			if (result == JOptionPane.YES_OPTION) {
				mainActivity.doDownload(d);
			}
		}
		else {
			mainActivity.doDownload(d);
		}
	}

	public Archive getArchive() {
		return archive;
	}

	@Override
	public void hyperlinkUpdate(HyperlinkEvent e) {
		if (e.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
			if ("file".equals(e.getURL().getProtocol())) {
				try {
					BrowseUtil.openFile(new File(e.getURL().toURI()));
				}
				catch (Exception exp) {
					exp.printStackTrace();
				}
			}
			if ("http".equals(e.getURL().getProtocol())) {
				BrowseUtil.openUrl(e.getURL().toString());
			}
		}
	}
}
