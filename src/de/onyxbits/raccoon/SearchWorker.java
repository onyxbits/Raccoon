package de.onyxbits.raccoon;

import java.util.List;
import java.util.Locale;
import java.util.Vector;
import java.util.concurrent.ExecutionException;

import javax.swing.SwingWorker;

import com.akdeniz.googleplaycrawler.GooglePlay.BulkDetailsEntry;
import com.akdeniz.googleplaycrawler.GooglePlay.BulkDetailsResponse;
import com.akdeniz.googleplaycrawler.GooglePlay.DocV2;
import com.akdeniz.googleplaycrawler.GooglePlay.SearchResponse;
import com.akdeniz.googleplaycrawler.GooglePlayAPI;
import com.akdeniz.googleplaycrawler.GooglePlayException;

/**
 * Perform a search query.
 * 
 * @author patrick
 * 
 */
class SearchWorker extends SwingWorker<BulkDetailsResponse, String> {

	private String search;
	private SearchView searchView;
	private int offset;
	private int limit;
	private String localization;
	private Archive archive;

	/**
	 * 
	 * @param androidId
	 *          device id
	 * @param userId
	 *          username
	 * @param password
	 *          password
	 * @param search
	 *          what to submit to google (ideally a packagename, but the search
	 *          engine also accpets anything else)
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

		if (search == null || search.length() == 0) {
			throw new IllegalArgumentException("Bad search query");
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
	protected BulkDetailsResponse doInBackground() throws Exception {
		String pwd = archive.getPassword();
		String uid = archive.getUserId();
		String aid = archive.getAndroidId();
		GooglePlayAPI service = new GooglePlayAPI(uid, pwd, aid);
		service.setLocalization(localization);
		service.setToken(archive.getAuthToken());
		if (service.getToken()==null) {
			service.login();
			archive.setAuthToken(service.getToken());
		}
		SearchResponse response = service.search(search, offset, limit);

		List<String> apps = new Vector<String>();
		if (response.getDocCount() > 0) {
			DocV2 all = response.getDoc(0);
			for (int i = 0; i < all.getChildCount(); i++) {
				apps.add(all.getChild(i).getBackendDocid());
			}
		}
		return service.bulkDetails(apps);
	}

	@Override
	protected void done() {
		try {
			BulkDetailsResponse response = get();
			if (response.getEntryCount() > 0) {
				ListView listing = new ListView();
				for (BulkDetailsEntry bulkDetailsEntry : response.getEntryList()) {
					DocV2 doc = bulkDetailsEntry.getDoc();
					listing.add(ResultView.create(searchView, doc));
				}
				searchView.doResultList(listing);
			}
			else {
				searchView.doMessage("No results");
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
			}
			else {
				searchView.doMessage(e.getMessage());
			}
		}
	}
}
