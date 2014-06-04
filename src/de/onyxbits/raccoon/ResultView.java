package de.onyxbits.raccoon;

import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import com.akdeniz.googleplaycrawler.GooglePlay.DocV2;

/**
 * Displays and handles a single search result.
 * 
 * @author patrick
 * 
 */
public class ResultView extends JPanel implements ActionListener {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private DocV2 doc;

	private JButton download;

	private JButton details;

	private JButton permissions;

	private SearchView searchView;

	/**
	 * Construct a new app listing
	 * 
	 * @param searchView
	 *          the searchview that will handle button presses.
	 * 
	 * @param doc
	 *          the source from which to draw app info
	 */
	private ResultView(SearchView searchView, DocV2 doc) {
		this.doc = doc;
		this.searchView = searchView;
		setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
		String title = doc.getTitle();
		String pack = doc.getBackendDocid();
		String author = doc.getCreator();
		String price = doc.getOffer(0).getFormattedAmount();
		String date = doc.getDetails().getAppDetails().getUploadDate();
		String size = humanReadableByteCount(doc.getDetails().getAppDetails().getInstallationSize(),
				true);
		String boiler = "<html><h2>" + title + "</h2><code>" + pack + "</code><br>" + author
				+ "<br> <br>" + size + " &#8213; " + date + " &#8213; " + price + "</html>";

		JPanel buttons = new JPanel();
		buttons.setLayout(new GridLayout(3, 1, 0, 4));
		buttons.setOpaque(false);
		download = new JButton("Download");
		details = new JButton("Google Play");
		permissions = new JButton("Permissions");
		buttons.add(download);
		buttons.add(details);
		buttons.add(permissions);
		add(new JLabel(boiler));
		JPanel container = new JPanel();
		container.setOpaque(false);
		container.add(buttons);
		add(container);
	}

	public static ResultView create(SearchView searchView, DocV2 doc) {
		ResultView ret = new ResultView(searchView, doc);
		ret.setLayout(new BoxLayout(ret, BoxLayout.X_AXIS));
		ret.download.addActionListener(ret);
		ret.details.addActionListener(ret);
		ret.permissions.addActionListener(ret);
		return ret;
	}

	public void actionPerformed(ActionEvent event) {
		Object src = event.getSource();
		if (src == download) {
			DownloadView d = DownloadView.create(searchView.getArchive(), doc);
			searchView.doDownload(d);
		}
		if (src == details) {
			BrowseUtil.openUrl(doc.getShareUrl());
		}
		if (src == permissions) {
			doShowPermissions();
		}
	}

	private void doShowPermissions() {
		List<String> perms = doc.getDetails().getAppDetails().getPermissionList();
		StringBuilder sb = new StringBuilder();
		for (String perm : perms) {
			sb.append(perm);
			sb.append("\n");
		}
		if (sb.length() == 0) {
			sb.append("This app requires no permissions");
		}
		JOptionPane.showMessageDialog(getRootPane(), sb.toString(), doc.getTitle(),
				JOptionPane.PLAIN_MESSAGE);
	}

	public static String humanReadableByteCount(long bytes, boolean si) {
		int unit = si ? 1000 : 1024;
		if (bytes < unit)
			return bytes + " B";
		int exp = (int) (Math.log(bytes) / Math.log(unit));
		String pre = (si ? "kMGTPE" : "KMGTPE").charAt(exp - 1) + (si ? "" : "i");
		return String.format("%.1f %sB", bytes / Math.pow(unit, exp), pre);
	}

}
