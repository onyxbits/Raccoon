package de.onyxbits.raccoon.gui;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import de.onyxbits.raccoon.Messages;
import de.onyxbits.raccoon.io.Archive;

/**
 * A container for putting search results in.
 * 
 * @author patrick
 * 
 */
public class SearchView extends JPanel implements ActionListener, ChangeListener, Runnable {

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
	private JLabel message;
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

		message = new JLabel();
		progress = new JProgressBar();
		progress.setIndeterminate(true);
		progress.setString(Messages.getString("SearchView.5")); //$NON-NLS-1$
		progress.setStringPainted(true);

		GridBagConstraints center = new GridBagConstraints();
		center.anchor = GridBagConstraints.CENTER;
		center.fill = GridBagConstraints.NONE;

		main = new JPanel();
		main.setLayout(cardLayout);
		JPanel container = new JPanel();
		container.setLayout(new GridBagLayout());
		container.add(message, center);
		main.add(container, CARDMESSAGE);
		main.add(results, CARDRESULTS);
		container = new JPanel();
		container.setLayout(new GridBagLayout());
		container.add(progress, center);
		main.add(container, CARDPROGRESS);

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
		message.setText(status);
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
			int result = JOptionPane.showConfirmDialog(getRootPane(), Messages.getString("SearchView.7"), Messages.getString("SearchView.8"), //$NON-NLS-1$ //$NON-NLS-2$
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

}
