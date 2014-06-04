package de.onyxbits.raccoon;

import java.awt.BorderLayout;
import java.awt.CardLayout;
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


/**
 * A container for putting search results in.
 * 
 * @author patrick
 * 
 */
public class SearchView extends JPanel implements ActionListener, ChangeListener {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	protected Archive archive;

	private static final String CARDRESULTS = "results";
	private static final String CARDPROGRESS = "progress";
	private static final String CARDMESSAGE = "message";

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
		query.setToolTipText("Keywords or packagename");
		page = new JSpinner(new SpinnerNumberModel(1, 1, 10000, 1));
		page.setToolTipText("Result page");
		results = new JScrollPane();
		cardLayout = new CardLayout();

		message = new JLabel();
		progress = new JProgressBar();
		progress.setIndeterminate(true);
		progress.setString("Searching");
		progress.setStringPainted(true);

		JPanel container = new JPanel();
		main = new JPanel();
		main.setLayout(cardLayout);
		container.add(message);
		main.add(container, CARDMESSAGE);
		main.add(results, CARDRESULTS);
		container = new JPanel();
		container.add(progress);
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
		SearchView ret = new SearchView(mainActivity,archive);
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
	}

	protected void doSearch() {
		query.setEnabled(false);
		page.setEnabled(false);
		cardLayout.show(main, CARDPROGRESS);
		int offset = (Integer) page.getValue();
		offset = (offset - 1) * 10;
		new SearchWorker(archive, query.getText(), this).withOffset(offset)
				.withLimit(offset + 10).execute();
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

	protected void doDownload(DownloadView d) {
		if (d.isDownloaded()) {
			int result = JOptionPane.showConfirmDialog(getRootPane(), "Overwrite file?", "File exists",
					JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
			if (result==JOptionPane.YES_OPTION) {
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
