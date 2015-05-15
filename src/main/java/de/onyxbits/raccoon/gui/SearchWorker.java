package de.onyxbits.raccoon.gui;

import java.io.File;
import java.net.URL;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Vector;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.prefs.Preferences;

import javax.swing.JLabel;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;

import org.apache.commons.io.FileUtils;

import com.akdeniz.googleplaycrawler.GooglePlay.BulkDetailsEntry;
import com.akdeniz.googleplaycrawler.GooglePlay.BulkDetailsResponse;
import com.akdeniz.googleplaycrawler.GooglePlay.DocV2;
import com.akdeniz.googleplaycrawler.GooglePlay.Image;
import com.akdeniz.googleplaycrawler.GooglePlay.SearchResponse;
import com.akdeniz.googleplaycrawler.GooglePlayAPI;
import com.akdeniz.googleplaycrawler.GooglePlayException;

import de.onyxbits.raccoon.App;
import de.onyxbits.raccoon.BrowseUtil;
import de.onyxbits.raccoon.Messages;
import de.onyxbits.raccoon.io.Archive;

/**
 * A background task for performing searches on Google Play.
 * 
 * @author patrick
 * 
 */
class SearchWorker extends SwingWorker<Vector<BulkDetailsEntry>, String> {

	private String search;
	private SearchView searchView;
	private int offset;
	private int limit;
	private String localization;
	private Archive archive;
	private boolean fetchIcons;

	/**
	 * 
	 * @param archive
	 *          storage area.
	 * @param search
	 *          what to submit to google (ideally a packagename, but the search
	 *          engine also accepts anything else). May be null to search for
	 *          updates.
	 * @param callback
	 *          where to report back when the results are in.
	 */
	public SearchWorker(Archive archive, String search, SearchView callback) {
		this.archive = archive;
		this.search = search;
		this.searchView = callback;
		this.limit = 10;
		this.localization = Locale.getDefault().getCountry();
		if (callback == null) {
			throw new NullPointerException();
		}
		Preferences prefs = Preferences.userNodeForPackage(MainActivity.class);
		fetchIcons = prefs.getBoolean(MainActivity.FETCHICONS, true);
	}

	/**
	 * Optional starting page (default: 0)
	 * 
	 * @param o
	 *          index
	 * @return a this reference for chaining.
	 */
	public SearchWorker withOffset(int o) {
		this.offset = o;
		return this;
	}

	/**
	 * Max number of results to retrieve (default: 10).
	 * 
	 * @param l
	 *          amount of result entries
	 * @return a this reference for chaining.
	 */
	public SearchWorker withLimit(int l) {
		this.limit = l;
		return this;
	}

	/**
	 * What language to return results in (default "en");
	 * 
	 * @param l
	 *          locale id
	 * @return this reference for chaining.
	 */
	public SearchWorker withLocalization(String l) {
		this.localization = l;
		return this;
	}

	@Override
	protected Vector<BulkDetailsEntry> doInBackground() throws Exception {
		if (search != null) {
			return doQuerySearch();
		}
		else {
			return doUpdateSearch();
		}
	}

	private Vector<BulkDetailsEntry> doUpdateSearch() throws Exception {
		GooglePlayAPI service = App.createConnection(archive);
		BulkDetailsResponse response = service.bulkDetails(archive.list());
		Vector<BulkDetailsEntry> ret = new Vector<BulkDetailsEntry>();
		for (BulkDetailsEntry bulkDetailsEntry : response.getEntryList()) {
			DocV2 doc = bulkDetailsEntry.getDoc();
			String pn = doc.getBackendDocid();
			int vc = doc.getDetails().getAppDetails().getVersionCode();
			if (!archive.fileUnder(pn, vc).exists()) {
				ret.add(bulkDetailsEntry);
			}
			if (fetchIcons) {
				fetchIcon(doc);
			}
		}
		return ret;
	}

	private Vector<BulkDetailsEntry> doQuerySearch() throws Exception {
		GooglePlayAPI service = App.createConnection(archive);
		service.setLocalization(localization);
		SearchResponse response = service.search(search, offset, limit);

		List<String> apps = new Vector<String>();
		if (response.getDocCount() > 0) {
			DocV2 all = response.getDoc(0);
			for (int i = 0; i < all.getChildCount(); i++) {
				apps.add(all.getChild(i).getBackendDocid());
				if (fetchIcons) {
					fetchIcon(all.getChild(i));
				}
			}
		}
		BulkDetailsResponse bdr = service.bulkDetails(apps);
		Vector<BulkDetailsEntry> ret = new Vector<BulkDetailsEntry>();
		for (int i = 0; i < bdr.getEntryCount(); i++) {
			ret.add(bdr.getEntry(i));
		}
		return ret;
	}

	private void fetchIcon(DocV2 doc) {
		if (isCancelled()) {
			return;
		}
		List<Image> lst = doc.getImageList();
		Iterator<Image> it = lst.iterator();
		while (it.hasNext()) {
			Image img = it.next();
			if (img.getImageType() == 4) {
				try {
					File f = getImageCacheFile(doc.getBackendDocid(), 4);
					if (!f.exists()) {
						URL u = new URL(img.getImageUrl());
						FileUtils.copyURLToFile(u, f);
					}
				}
				catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
	}

	/**
	 * Figure out where to images
	 * 
	 * @param appId
	 *          package id
	 * @param type
	 *          the numerical type of the image
	 * @return where to cache the image.
	 */
	public static File getImageCacheFile(String appId, int type) {
		return new File(App.getDir(App.CACHEDIR), appId + "-img-" + type); //$NON-NLS-1$
	}

	@Override
	protected void done() {
		Vector<BulkDetailsEntry> response = new Vector<BulkDetailsEntry>();
		try {
			response = get();
		}
		catch (CancellationException e) {
			searchView.doMessage(Messages.getString("SearchWorker.4")); //$NON-NLS-1$
			SwingUtilities.invokeLater(searchView);
			return;
		}
		catch (InterruptedException e) {
			searchView.doMessage(Messages.getString("SearchWorker.5")); //$NON-NLS-1$
			SwingUtilities.invokeLater(searchView);
			return;
		}
		catch (ExecutionException e) {
			// Stuff that happened on the backgroundthread.
			Throwable wrapped = e.getCause();
			if (wrapped instanceof GooglePlayException) {
				searchView.doMessage(Messages.getString("SearchWorker.1")); //$NON-NLS-1$
				e.printStackTrace();
			}
			else {
				searchView.doMessage(e.getMessage());
			}
			SwingUtilities.invokeLater(searchView);
			return;
		}

		ListView listing = new ListView();
		for (BulkDetailsEntry bulkDetailsEntry : response) {
			DocV2 doc = bulkDetailsEntry.getDoc();
			try {
				listing.add(ResultView.create(searchView, doc));
			}
			catch (Exception e) {
				// We likely get here when trying to update an archive and the user
				// either had the brilliant idea of creating his/her own directories
				// in the APK storage or if a stored app is no longer listed on
				// Google. Maybe, the user even thought it clever to dump externally
				// downloaded apps into the storage. Design decision: silently
				// ignore, don't bother alerting the user and most certainly don't
				// try to automatically fix anything.
			}
		}

		if (listing.getComponentCount() > 0) {
			HypertextPane hp = new HypertextPane(Messages.getString("SearchWorker.6"));
			hp.addHyperlinkListener(new BrowseUtil());
			listing.add(hp);
			hp.setBorder(null);
			searchView.doResultList(listing);
		}
		else {
			if (search == null) {
				searchView.doMessage(Messages.getString("SearchWorker.2")); //$NON-NLS-1$
			}
			else {
				searchView.doMessage(Messages.getString("SearchWorker.3")); //$NON-NLS-1$
			}
		}
		SwingUtilities.invokeLater(searchView);
	}
}
