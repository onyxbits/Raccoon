package de.onyxbits.raccoon.gui;

import java.util.List;
import java.util.Locale;
import java.util.Vector;
import java.util.concurrent.ExecutionException;

import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;

import com.akdeniz.googleplaycrawler.GooglePlay.BulkDetailsEntry;
import com.akdeniz.googleplaycrawler.GooglePlay.BulkDetailsResponse;
import com.akdeniz.googleplaycrawler.GooglePlay.DocV2;
import com.akdeniz.googleplaycrawler.GooglePlay.SearchResponse;
import com.akdeniz.googleplaycrawler.GooglePlayAPI;
import com.akdeniz.googleplaycrawler.GooglePlayException;

import de.onyxbits.raccoon.App;
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
			}
		}
		BulkDetailsResponse bdr = service.bulkDetails(apps);
		Vector<BulkDetailsEntry> ret = new Vector<BulkDetailsEntry>();
		for (int i = 0; i < bdr.getEntryCount(); i++) {
			ret.add(bdr.getEntry(i));
		}
		return ret;
	}

	@Override
	protected void done() {
		try {
			Vector<BulkDetailsEntry> response = get();
			if (response.size() > 0) {
				ListView listing = new ListView();
				for (BulkDetailsEntry bulkDetailsEntry : response) {
					DocV2 doc = bulkDetailsEntry.getDoc();
					listing.add(ResultView.create(searchView, doc));
				}
				searchView.doResultList(listing);
			}
			else {
				if (search == null) {
					searchView.doMessage("No updates");
				}
				else {
					searchView.doMessage("No results");
				}
			}
		}
		catch (InterruptedException e) {
			searchView.doMessage("Search aborted");
		}
		catch (ExecutionException e) {
			// Stuff that happened on the backgroundthread.
			Throwable wrapped = e.getCause();
			if (wrapped instanceof GooglePlayException) {
				searchView.doMessage("Authentication error");
				e.printStackTrace();
			}
			else {
				searchView.doMessage(e.getMessage());
			}
		}
		SwingUtilities.invokeLater(searchView);
	}
}
