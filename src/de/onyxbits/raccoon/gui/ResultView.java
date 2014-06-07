package de.onyxbits.raccoon.gui;

import java.awt.Event;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JEditorPane;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.SwingUtilities;

import com.akdeniz.googleplaycrawler.GooglePlay.DocV2;

import de.onyxbits.raccoon.BrowseUtil;

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

	private JEditorPane entry;

	private String appOverview;

	private String appPermissions;
	
	private boolean showingPermissions;

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

		// FIXME: The api returns wrong values for the commentcount, number of
		// ratings, version and summary.

		String title = doc.getTitle();
		String installs = doc.getDetails().getAppDetails().getNumDownloads();
		String rating = String.format("%.2f", doc.getAggregateRating().getStarRating());
		String pack = doc.getBackendDocid();
		String author = doc.getCreator();
		String price = doc.getOffer(0).getFormattedAmount();
		if (hasInAppPurchase(doc)) {
			price += " (+IAP)";
		}
		String date = doc.getDetails().getAppDetails().getUploadDate();
		String size = humanReadableByteCount(doc.getDetails().getAppDetails().getInstallationSize(),
				true);
		appOverview = "<html><style>table {border: 1px solid #9E9E9E;}  th {text-align: left;	background-color: #9E9E9E;color: black;} td	{padding-right: 15px;} </style><p><big><u>"
				+ title
				+ "</u></big></p><p><strong>"
				+ author
				+ "</strong> <strong>&#124;</strong> <code>"
				+ pack
				+ "</code></p><p><table><tr><th>Size</th><th>Published</th><th>Price</th><th>Installs</th><th>Rating</th></tr><tr><td>"
				+ size
				+ "</td><td>"
				+ date
				+ "</td><td>"
				+ price
				+ "</td><td>"
				+ installs
				+ "</td><td>"
				+ rating + "</td></tr></table>";

		List<String> perms = doc.getDetails().getAppDetails().getPermissionList();
		ArrayList<String> sortMe = new ArrayList<String>(perms);
		Collections.sort(sortMe);
		StringBuilder sb = new StringBuilder();
		for (String perm : sortMe) {
			sb.append("<li>");
			sb.append(perm);
			sb.append("\n");
		}
		if (sb.length() == 0) {
			sb.append("This app requires no permissions");
		}
		sb.insert(0, "<html><p><big><u>" + title + "</u></big></p><ul>");
		sb.append("</ul></html>");
		appPermissions = sb.toString();

		JPanel buttons = new JPanel();
		buttons.setLayout(new GridLayout(3, 1, 0, 4));
		buttons.setOpaque(false);
		download = new JButton("Download");
		if (doc.getOffer(0).getCheckoutFlowRequired()) {
			// Paid apps - no idea how to do a checkout.
			download.setEnabled(false);
		}
		details = new JButton("Google Play");
		permissions = new JButton("Permissions");
		buttons.add(download);
		buttons.add(details);
		buttons.add(permissions);
		entry = new JEditorPane("text/html", appOverview);
		entry.setEditable(false);
		entry.setOpaque(false);
		entry.setMargin(new Insets(10,10,10,10));
		add(entry);
		JPanel container = new JPanel();
		container.setOpaque(false);
		container.add(buttons);
		JSeparator sep = new JSeparator(JSeparator.VERTICAL);
		sep.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
		add(sep);
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
			if ((event.getModifiers() & Event.SHIFT_MASK) == Event.SHIFT_MASK) {
				// This is indented for debugging!
				entry.setContentType("text/plain");
				entry.setText(doc.toString());
			}
			else {
				BrowseUtil.openUrl(doc.getShareUrl());
				SwingUtilities.invokeLater(searchView);
			}
		}
		if (src == permissions) {
			doShowPermissions();
		}
	}

	private static boolean hasInAppPurchase(DocV2 doc) {
		List<String> perms = doc.getDetails().getAppDetails().getPermissionList();
		for (String perm : perms) {
			if (perm.equals("com.android.vending.BILLING")) {
				return true;
			}
		}
		return false;
	}

	private void doShowPermissions() {
		if (showingPermissions) {
			entry.setText(appOverview);
		}
		else {
			entry.setText(appPermissions);
		}
		showingPermissions=!showingPermissions;
		SwingUtilities.invokeLater(searchView);
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
