package de.onyxbits.raccoon.gui;

import java.awt.Event;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
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
import javax.swing.JToggleButton;
import javax.swing.SwingUtilities;
import javax.swing.text.DefaultCaret;

import com.akdeniz.googleplaycrawler.GooglePlay.DocV2;

import de.onyxbits.raccoon.BrowseUtil;
import de.onyxbits.raccoon.Messages;
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

	private JButton gplay;

	private JToggleButton details;

	private JToggleButton permissions;

	private SearchView searchView;

	private JEditorPane entry;

	private HashMap<String, Object> model;

	private static Icon iconNetwork;
	private static Icon iconIap;
	private static Icon iconLocation;
	private static Icon iconMicrophone;
	private static Icon iconPersonal;
	private static Icon iconPhone;
	private static Icon iconCamera;
	private static Icon iconSystem;
	private static Icon iconStorage;
	private static Icon iconLicenseCheck;
	private static Icon iconDownload;

	static {
		Class<?> clazz = new Object().getClass();
		iconNetwork = new ImageIcon(clazz.getResource("/rsrc/badges/wi-fi-outline.png")); //$NON-NLS-1$
		iconIap = new ImageIcon(clazz.getResource("/rsrc/badges/shopping-cart.png")); //$NON-NLS-1$
		iconLocation = new ImageIcon(clazz.getResource("/rsrc/badges/location-outline.png")); //$NON-NLS-1$
		iconMicrophone = new ImageIcon(clazz.getResource("/rsrc/badges/microphone-outline.png")); //$NON-NLS-1$
		iconPersonal = new ImageIcon(clazz.getResource("/rsrc/badges/contacts.png")); //$NON-NLS-1$
		iconPhone = new ImageIcon(clazz.getResource("/rsrc/badges/phone-outline.png")); //$NON-NLS-1$
		iconCamera = new ImageIcon(clazz.getResource("/rsrc/badges/camera-outline.png")); //$NON-NLS-1$
		iconSystem = new ImageIcon(clazz.getResource("/rsrc/badges/spanner-outline.png")); //$NON-NLS-1$
		iconStorage = new ImageIcon(clazz.getResource("/rsrc/badges/folder.png")); //$NON-NLS-1$
		iconLicenseCheck = new ImageIcon(clazz.getResource("/rsrc/badges/key-outline.png")); //$NON-NLS-1$
		iconDownload = new ImageIcon(clazz.getResource("/rsrc/icons/download.png")); //$NON-NLS-1$
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

		model = new HashMap<String, Object>();
		model.put("i18n_installs", Messages.getString("ResultView.1")); //$NON-NLS-1$ //$NON-NLS-2$
		model.put("i18n_rating", Messages.getString("ResultView.3")); //$NON-NLS-1$ //$NON-NLS-2$
		model.put("i18n_price", Messages.getString("ResultView.5")); //$NON-NLS-1$ //$NON-NLS-2$
		model.put("i18n_date", Messages.getString("ResultView.7")); //$NON-NLS-1$ //$NON-NLS-2$
		model.put("i18n_version", Messages.getString("ResultView.2")); //$NON-NLS-1$ //$NON-NLS-2$
		model.put("i18n_size", Messages.getString("ResultView.9")); //$NON-NLS-1$ //$NON-NLS-2$
		model.put("i18n_permissions", Messages.getString("ResultView.27")); //$NON-NLS-1$ //$NON-NLS-2$
		model.put("i18n_changelog", Messages.getString("ResultView.4")); //$NON-NLS-1$ //$NON-NLS-2$
		model.put("title", doc.getTitle()); //$NON-NLS-1$
		model.put("installs", doc.getDetails().getAppDetails().getNumDownloads()); //$NON-NLS-1$
		model.put("rating", String.format("%.2f", doc.getAggregateRating().getStarRating())); //$NON-NLS-1$ //$NON-NLS-2$
		model.put("package", doc.getBackendDocid()); //$NON-NLS-1$
		model.put("author", doc.getCreator()); //$NON-NLS-1$
		model.put("price", doc.getOffer(0).getFormattedAmount()); //$NON-NLS-1$
		model.put("date", doc.getDetails().getAppDetails().getUploadDate()); //$NON-NLS-1$
		model.put("size", Archive.humanReadableByteCount(doc.getDetails().getAppDetails() //$NON-NLS-1$
				.getInstallationSize(), true));
		File icon = SearchWorker.getImageCacheFile(doc.getBackendDocid(), 4);
		if (icon.exists()) {
			model.put("icon", icon.toURI()); //$NON-NLS-1$
		}

		JPanel buttons = new JPanel();
		buttons.setLayout(new GridLayout(0, 1, 0, 4));
		buttons.setOpaque(false);
		download = new JButton(Messages.getString("ResultView.25"), iconDownload); //$NON-NLS-1$
		gplay = new JButton(Messages.getString("ResultView.26")); //$NON-NLS-1$
		details = new JToggleButton(Messages.getString("ResultView.6")); //$NON-NLS-1$
		permissions = new JToggleButton(Messages.getString("ResultView.27")); //$NON-NLS-1$
		buttons.add(download);
		buttons.add(gplay);
		buttons.add(details);
		buttons.add(permissions);
		entry = new JEditorPane("text/html", TmplTool.transform("app.html", model)); //$NON-NLS-1$ //$NON-NLS-2$
		entry.setEditable(false);
		entry.setOpaque(false);
		entry.setMargin(new Insets(10, 10, 10, 10));
		entry.addHyperlinkListener(new BrowseUtil());
		// Keep enclosing scrollpanes steady
		DefaultCaret caret = (DefaultCaret) entry.getCaret();
		caret.setUpdatePolicy(DefaultCaret.NEVER_UPDATE);
		JPanel outer = new JPanel(); // Needed to simplify the layout code.
		outer.setOpaque(false);
		JPanel container = new JPanel();
		container.setLayout(new BoxLayout(container, BoxLayout.Y_AXIS));
		container.setOpaque(false);
		container.add(buttons);
		container.add(Box.createVerticalStrut(10));
		container.add(createBadges(doc.getDetails().getAppDetails().getPermissionList()));
		container.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
		outer.add(container);
		JSeparator sep = new JSeparator(JSeparator.VERTICAL);
		sep.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
		add(outer);
		add(sep);
		add(entry);
	}

	private JPanel createBadges(List<String> perms) {
		JPanel ret = new JPanel();
		ret.setLayout(new GridLayout(0, 3));
		ret.setOpaque(false);
		String[][] groups = { { "android.permission.INTERNET", //$NON-NLS-1$
				"android.permission.ACCESS_NETWORK_STATE", //$NON-NLS-1$
				"android.permission.CHANGE_NETWORK_STATE", //$NON-NLS-1$
				"android.permission.CHANGE_WIFI_MULTICAST_STATE", //$NON-NLS-1$
				"android.permission.CHANGE_WIFI_STATE", //$NON-NLS-1$
				"android.permission.ACCESS_WIFI_STATE", //$NON-NLS-1$
				"android.permission.BIND_VPN_SERVICE" }, //$NON-NLS-1$
				{ "com.android.vending.BILLING" }, //$NON-NLS-1$
				{ "com.android.vending.CHECK_LICENSE" }, //$NON-NLS-1$
				{ "android.permission.ACCESS_COARSE_LOCATION", "android.permission.ACCESS_FINE_LOCATION" }, //$NON-NLS-1$ //$NON-NLS-2$
				{ "android.permission.RECORD_AUDIO" }, //$NON-NLS-1$
				{ "android.permission.CAMERA" }, //$NON-NLS-1$
				{ "android.permission.CALL_PHONE", //$NON-NLS-1$
						"android.permission.PROCESS_OUTGOING_CALLS", //$NON-NLS-1$
						"android.permission.READ_CALL_LOG", //$NON-NLS-1$
						"android.permission.READ_PHONE_STATE", //$NON-NLS-1$
						"android.permission.READ_SMS", //$NON-NLS-1$
						"android.permission.RECEIVE_SMS", //$NON-NLS-1$
						"android.permission.SEND_SMS", //$NON-NLS-1$
						"android.permission.USE_SIP", //$NON-NLS-1$
						"android.permission.WRITE_CALL_LOG", //$NON-NLS-1$
						"android.permission.WRITE_SMS" }, //$NON-NLS-1$
				{ "android.permission.BIND_DEVICE_ADMIN", //$NON-NLS-1$
						"android.permission.CHANGE_CONFIGURATION", //$NON-NLS-1$
						"android.permission.DISABLE_KEYGUARD", //$NON-NLS-1$
						"android.permission.EXPAND_STATUS_BAR", //$NON-NLS-1$
						"android.permission.GET_TASKS", //$NON-NLS-1$
						"android.permission.KILL_BACKGROUND_PROCESSES", //$NON-NLS-1$
						"android.permission.MODIFY_AUDIO_SETTINGS", //$NON-NLS-1$
						"android.permission.RECEIVE_BOOT_COMPLETED", //$NON-NLS-1$
						"android.permission.REORDER_TASKS", //$NON-NLS-1$
						"android.permission.SYSTEM_ALERT_WINDOW", //$NON-NLS-1$
						"android.permission.SET_WALLPAPER", //$NON-NLS-1$
						"com.android.launcher.permission.UNINSTALL_SHORTCUT" }, //$NON-NLS-1$
				{ "android.permission.GET_ACCOUNTS", //$NON-NLS-1$
						"android.permission.READ_PHONE_STATE", //$NON-NLS-1$
						"android.permission.GLOBAL_SEARCH", //$NON-NLS-1$
						"android.permission.MANAGE_DOCUMENTS", //$NON-NLS-1$
						"android.permission.READ_CALENDAR", //$NON-NLS-1$
						"android.permission.READ_CALL_LOG", //$NON-NLS-1$
						"android.permission.READ_CONTACTS", //$NON-NLS-1$
						"com.android.browser.permission.READ_HISTORY_BOOKMARKS", //$NON-NLS-1$
						"android.permission.READ_LOGS", //$NON-NLS-1$
						"android.permission.READ_USER_DICTIONARY", //$NON-NLS-1$
						"android.permission.USE_CREDENTIALS", //$NON-NLS-1$
						"android.permission.WRITE_CALENDAR", //$NON-NLS-1$
						"android.permission.WRITE_CALL_LOG", //$NON-NLS-1$
						"android.permission.WRITE_CONTACTS", //$NON-NLS-1$
						"com.android.browser.permission.WRITE_HISTORY_BOOKMARKS", //$NON-NLS-1$
						"android.permission.WRITE_SOCIAL_STREAM", //$NON-NLS-1$
						"android.permission.READ_PROFILE", //$NON-NLS-1$
						"android.permission.USE_CREDENTIALS", //$NON-NLS-1$
						"android.permission.WRITE_USER_DICTIONARY" }, //$NON-NLS-1$
				{ "android.permission.READ_EXTERNAL_STORAGE", "android.permission.WRITE_EXTERNAL_STORAGE" } }; //$NON-NLS-1$ //$NON-NLS-2$
		for (int x = 0; x < groups.length; x++) {
			for (int y = 0; y < groups[x].length; y++) {
				if (x == 0 && perms.contains(groups[x][y])) {
					JLabel lbl = new JLabel(iconNetwork);
					lbl.setToolTipText(Messages.getString("ResultView.84")); //$NON-NLS-1$
					ret.add(lbl);
					break;
				}
				if (x == 1 && perms.contains(groups[x][y])) {
					JLabel lbl = new JLabel(iconIap);
					lbl.setToolTipText(Messages.getString("ResultView.85")); //$NON-NLS-1$
					ret.add(lbl);
					break;
				}
				if (x == 2 && perms.contains(groups[x][y])) {
					JLabel lbl = new JLabel(iconLicenseCheck);
					lbl.setToolTipText(Messages.getString("ResultView.0")); //$NON-NLS-1$
					ret.add(lbl);
					break;
				}
				if (x == 3 && perms.contains(groups[x][y])) {
					JLabel lbl = new JLabel(iconLocation);
					lbl.setToolTipText(Messages.getString("ResultView.86")); //$NON-NLS-1$
					ret.add(lbl);
					break;
				}
				if (x == 4 && perms.contains(groups[x][y])) {
					JLabel lbl = new JLabel(iconMicrophone);
					lbl.setToolTipText(Messages.getString("ResultView.87")); //$NON-NLS-1$
					ret.add(lbl);
					break;
				}
				if (x == 5 && perms.contains(groups[x][y])) {
					JLabel lbl = new JLabel(iconCamera);
					lbl.setToolTipText(Messages.getString("ResultView.88")); //$NON-NLS-1$
					ret.add(lbl);
					break;
				}
				if (x == 6 && perms.contains(groups[x][y])) {
					JLabel lbl = new JLabel(iconPhone);
					lbl.setToolTipText(Messages.getString("ResultView.89")); //$NON-NLS-1$
					ret.add(lbl);
					break;
				}
				if (x == 7 && perms.contains(groups[x][y])) {
					JLabel lbl = new JLabel(iconSystem);
					lbl.setToolTipText(Messages.getString("ResultView.90")); //$NON-NLS-1$
					ret.add(lbl);
					break;
				}
				if (x == 8 && perms.contains(groups[x][y])) {
					JLabel lbl = new JLabel(iconPersonal);
					lbl.setToolTipText(Messages.getString("ResultView.91")); //$NON-NLS-1$
					ret.add(lbl);
					break;
				}
				if (x == 9 && perms.contains(groups[x][y])) {
					JLabel lbl = new JLabel(iconStorage);
					lbl.setToolTipText(Messages.getString("ResultView.92")); //$NON-NLS-1$
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
		ret.gplay.addActionListener(ret);
		ret.permissions.addActionListener(ret);
		ret.details.addActionListener(ret);
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
				entry.setContentType("text/plain"); //$NON-NLS-1$
				entry.setText(doc.toString());
			}
			else {
				doToggleDetails();
			}
		}
		if (src == gplay) {
			BrowseUtil.openUrl(doc.getShareUrl());
			SwingUtilities.invokeLater(searchView); // Re - focus
		}
		if (src == permissions) {
			doTogglePermissions();
		}
	}

	private void doTogglePermissions() {
		if (model.containsKey("permissions_list")) { //$NON-NLS-1$
			model.remove("permissions_list"); //$NON-NLS-1$
			model.remove("permissions_none"); //$NON-NLS-1$
		}
		else {
			List<String> perms = doc.getDetails().getAppDetails().getPermissionList();
			if (perms.size() > 0) {
				ArrayList<String> sortMe = new ArrayList<String>(perms);
				Collections.sort(sortMe);
				model.put("permissions_list", sortMe); //$NON-NLS-1$
			}
			else {
				ArrayList<String> none = new ArrayList<String>();
				none.add(Messages.getString("ResultView.22")); //$NON-NLS-1$
				model.put("permissions_list", none); //$NON-NLS-1$ //$NON-NLS-2$
			}
		}
		entry.setText(TmplTool.transform("app.html", model)); //$NON-NLS-1$
		SwingUtilities.invokeLater(searchView);
	}

	private void doToggleDetails() {
		if (model.containsKey("description")) { //$NON-NLS-1$
			model.remove("version"); //$NON-NLS-1$
			model.remove("vcode"); //$NON-NLS-1$
			model.remove("email"); //$NON-NLS-1$
			model.remove("website"); //$NON-NLS-1$
			model.remove("description"); //$NON-NLS-1$
			model.remove("changelog"); //$NON-NLS-1$
			entry.setText(TmplTool.transform("app.html", model)); //$NON-NLS-1$
			SwingUtilities.invokeLater(searchView);
		}
		else {
			details.setEnabled(false);
			new DetailsWorker(searchView.getArchive(), this, doc.getBackendDocid()).execute();
		}
	}

	protected void updateEntry(DocV2 docV2) {
		details.setEnabled(true);
		doc = docV2;
		model.put("version", doc.getDetails().getAppDetails().getVersionString()); //$NON-NLS-1$
		model.put("vcode", doc.getDetails().getAppDetails().getVersionCode()); //$NON-NLS-1$
		model.put("website", doc.getDetails().getAppDetails().getDeveloperWebsite()); //$NON-NLS-1$
		model.put("email", doc.getDetails().getAppDetails().getDeveloperEmail()); //$NON-NLS-1$
		model.put("description", doc.getDescriptionHtml()); //$NON-NLS-1$
		model.put("changelog", doc.getDetails().getAppDetails().getRecentChangesHtml()); //$NON-NLS-1$
		entry.setText(TmplTool.transform("app.html", model)); //$NON-NLS-1$
		SwingUtilities.invokeLater(searchView);
	}
}
