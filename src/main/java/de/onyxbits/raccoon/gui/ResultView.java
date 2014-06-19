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
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JEditorPane;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.SwingUtilities;

import com.akdeniz.googleplaycrawler.GooglePlay.DocV2;

import de.onyxbits.raccoon.BrowseUtil;
import de.onyxbits.raccoon.io.Archive;

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

	private static Icon iconNetwork;
	private static Icon iconIap;
	private static Icon iconLocation;
	private static Icon iconMicrophone;
	private static Icon iconPersonal;
	private static Icon iconPhone;
	private static Icon iconCamera;
	private static Icon iconSystem;
	private static Icon iconStorage;

	static {
		Class<?> clazz = new Object().getClass();
		iconNetwork = new ImageIcon(clazz.getResource("/badges/wi-fi-outline.png"));
		iconIap = new ImageIcon(clazz.getResource("/badges/shopping-cart.png"));
		iconLocation = new ImageIcon(clazz.getResource("/badges/location-outline.png"));
		iconMicrophone = new ImageIcon(clazz.getResource("/badges/microphone-outline.png"));
		iconPersonal = new ImageIcon(clazz.getResource("/badges/contacts.png"));
		iconPhone = new ImageIcon(clazz.getResource("/badges/phone-outline.png"));
		iconCamera = new ImageIcon(clazz.getResource("/badges/camera-outline.png"));
		iconSystem = new ImageIcon(clazz.getResource("/badges/spanner-outline.png"));
		iconStorage = new ImageIcon(clazz.getResource("/badges/folder.png"));
	}

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
		String date = doc.getDetails().getAppDetails().getUploadDate();
		String size = Archive.humanReadableByteCount(doc.getDetails().getAppDetails()
				.getInstallationSize(), true);
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
		details = new JButton("Google Play");
		permissions = new JButton("Permissions");
		buttons.add(download);
		buttons.add(details);
		buttons.add(permissions);
		entry = new JEditorPane("text/html", appOverview);
		entry.setEditable(false);
		entry.setOpaque(false);
		entry.setMargin(new Insets(10, 10, 10, 10));
		add(entry);
		JPanel outer = new JPanel(); // Needed to simplify the layout code.
		outer.setOpaque(false);
		JPanel container = new JPanel();
		container.setLayout(new BoxLayout(container, BoxLayout.Y_AXIS));
		container.setOpaque(false);
		container.add(buttons);
		container.add(Box.createVerticalStrut(10));
		container.add(createBadges(perms));
		container.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
		outer.add(container);
		JSeparator sep = new JSeparator(JSeparator.VERTICAL);
		sep.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
		add(sep);
		add(outer);
	}

	private JPanel createBadges(List<String> perms) {
		JPanel ret = new JPanel();
		ret.setLayout(new GridLayout(0, 3));
		ret.setOpaque(false);
		String[][] groups = {
				{
						"android.permission.INTERNET",
						"android.permission.ACCESS_NETWORK_STATE",
						"android.permission.CHANGE_NETWORK_STATE",
						"android.permission.CHANGE_WIFI_MULTICAST_STATE",
						"android.permission.CHANGE_WIFI_STATE",
						"android.permission.ACCESS_WIFI_STATE",
						"android.permission.BIND_VPN_SERVICE" },
				{ "com.android.vending.BILLING" },
				{ "android.permission.ACCESS_COARSE_LOCATION", "android.permission.ACCESS_FINE_LOCATION" },
				{ "android.permission.RECORD_AUDIO" },
				{ "android.permission.CAMERA" },
				{
						"android.permission.CALL_PHONE",
						"android.permission.PROCESS_OUTGOING_CALLS",
						"android.permission.READ_CALL_LOG",
						"android.permission.READ_PHONE_STATE",
						"android.permission.READ_SMS",
						"android.permission.RECEIVE_SMS",
						"android.permission.SEND_SMS",
						"android.permission.USE_SIP",
						"android.permission.WRITE_CALL_LOG",
						"android.permission.WRITE_SMS" },
				{
						"android.permission.BIND_DEVICE_ADMIN",
						"android.permission.CHANGE_CONFIGURATION",
						"android.permission.DISABLE_KEYGUARD",
						"android.permission.EXPAND_STATUS_BAR",
						"android.permission.GET_TASKS",
						"android.permission.KILL_BACKGROUND_PROCESSES",
						"android.permission.MODIFY_AUDIO_SETTINGS",
						"android.permission.RECEIVE_BOOT_COMPLETED",
						"android.permission.REORDER_TASKS",
						"android.permission.SYSTEM_ALERT_WINDOW",
						"android.permission.SET_WALLPAPER",
						"com.android.launcher.permission.UNINSTALL_SHORTCUT" },
				{
						"android.permission.GET_ACCOUNTS",
						"android.permission.READ_PHONE_STATE",
						"android.permission.GLOBAL_SEARCH",
						"android.permission.MANAGE_DOCUMENTS",
						"android.permission.READ_CALENDAR",
						"android.permission.READ_CALL_LOG",
						"android.permission.READ_CONTACTS",
						"com.android.browser.permission.READ_HISTORY_BOOKMARKS",
						"android.permission.READ_LOGS",
						"android.permission.READ_USER_DICTIONARY",
						"android.permission.USE_CREDENTIALS",
						"android.permission.WRITE_CALENDAR",
						"android.permission.WRITE_CALL_LOG",
						"android.permission.WRITE_CONTACTS",
						"com.android.browser.permission.WRITE_HISTORY_BOOKMARKS",
						"android.permission.WRITE_SOCIAL_STREAM",
						"android.permission.READ_PROFILE",
						"android.permission.USE_CREDENTIALS",
						"android.permission.WRITE_USER_DICTIONARY" },
				{ "android.permission.READ_EXTERNAL_STORAGE", "android.permission.WRITE_EXTERNAL_STORAGE" } };
		for (int x = 0; x < groups.length; x++) {
			for (int y = 0; y < groups[x].length; y++) {
				if (x == 0 && perms.contains(groups[x][y])) {
					JLabel lbl = new JLabel(iconNetwork);
					lbl.setToolTipText("Internet access");
					ret.add(lbl);
					break;
				}
				if (x == 1 && perms.contains(groups[x][y])) {
					JLabel lbl = new JLabel(iconIap);
					lbl.setToolTipText("In App Purchases");
					ret.add(lbl);
					break;
				}
				if (x == 2 && perms.contains(groups[x][y])) {
					JLabel lbl = new JLabel(iconLocation);
					lbl.setToolTipText("Location access");
					ret.add(lbl);
					break;
				}
				if (x == 3 && perms.contains(groups[x][y])) {
					JLabel lbl = new JLabel(iconMicrophone);
					lbl.setToolTipText("Microphone access");
					ret.add(lbl);
					break;
				}
				if (x == 4 && perms.contains(groups[x][y])) {
					JLabel lbl = new JLabel(iconCamera);
					lbl.setToolTipText("Camera access");
					ret.add(lbl);
					break;
				}
				if (x == 5 && perms.contains(groups[x][y])) {
					JLabel lbl = new JLabel(iconPhone);
					lbl.setToolTipText("Telephone access");
					ret.add(lbl);
					break;
				}
				if (x == 6 && perms.contains(groups[x][y])) {
					JLabel lbl = new JLabel(iconSystem);
					lbl.setToolTipText("System access");
					ret.add(lbl);
					break;
				}
				if (x == 7 && perms.contains(groups[x][y])) {
					JLabel lbl = new JLabel(iconPersonal);
					lbl.setToolTipText("Personal data access");
					ret.add(lbl);
					break;
				}
				if (x == 8 && perms.contains(groups[x][y])) {
					JLabel lbl = new JLabel(iconStorage);
					lbl.setToolTipText("Storage access");
					ret.add(lbl);
					break;
				}
			}
		}
		return ret;
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

	private void doShowPermissions() {
		if (showingPermissions) {
			entry.setText(appOverview);
		}
		else {
			entry.setText(appPermissions);
		}
		showingPermissions = !showingPermissions;
		SwingUtilities.invokeLater(searchView);
	}
}
