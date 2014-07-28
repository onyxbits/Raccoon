package de.onyxbits.raccoon.gui;

import java.awt.Cursor;
import java.util.List;
import java.util.Locale;

import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;

import com.akdeniz.googleplaycrawler.GooglePlay.DocV2;
import com.akdeniz.googleplaycrawler.GooglePlayAPI;

import de.onyxbits.raccoon.App;
import de.onyxbits.raccoon.io.Archive;

/**
 * Perform a details query for a {@link ResultView}
 * 
 * @author patrick
 * 
 */
class DetailsWorker extends SwingWorker<DocV2, Object> {

	private Archive archive;
	private ResultView callback;
	private String appId;

	public DetailsWorker(Archive archive, ResultView callback, String appId) {
		this.archive = archive;
		this.callback = callback;
		this.appId = appId;
	}

	@Override
	public void process(List<Object> obj) {
		SwingUtilities.windowForComponent(callback).setCursor(
				Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
	}

	@Override
	protected DocV2 doInBackground() throws Exception {
		publish("");
		GooglePlayAPI service = App.createConnection(archive);
		service.setLocalization(Locale.getDefault().getCountry());
		return service.details(appId).getDocV2();
	}

	@Override
	protected void done() {
		try {
			callback.updateEntry(get());
			SwingUtilities.windowForComponent(callback).setCursor(Cursor.getDefaultCursor());
		}
		catch (Exception e) {
			// There is no reason why we should end here
			e.printStackTrace();
			return;
		}
	}

}
