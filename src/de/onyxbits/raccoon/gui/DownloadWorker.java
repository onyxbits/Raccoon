package de.onyxbits.raccoon.gui;

import java.util.List;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;

import javax.swing.JButton;
import javax.swing.JProgressBar;
import javax.swing.SwingWorker;

import com.akdeniz.googleplaycrawler.GooglePlay.DocV2;
import de.onyxbits.raccoon.io.Archive;
import de.onyxbits.raccoon.io.FetchListener;
import de.onyxbits.raccoon.io.FetchService;

class DownloadWorker extends SwingWorker<Exception, Integer> implements FetchListener {

	private Archive archive;
	private DocV2 app;
	protected JProgressBar progress;
	protected JButton cancel;

	private long totalBytes;
	private Exception failure;
	private SearchWorker next;

	/**
	 * Create an new worker
	 * 
	 * @param app
	 *          app description
	 * @param archive
	 *          download target
	 * @param next
	 *          a worker to start when done. May be null
	 */
	public DownloadWorker(DocV2 app, Archive archive, SearchWorker next) {
		this.app = app;
		this.archive = archive;
		this.next=next;
		this.progress = new JProgressBar(0, 100);
		this.cancel = new JButton("Cancel");
		this.progress.setStringPainted(true);
		this.progress.setString("Waiting");
	}

	@Override
	protected void process(List<Integer> chunks) {
		if (!isCancelled()) {
			int val = chunks.get(chunks.size() - 1);
			progress.setValue(chunks.get(chunks.size() - 1));
			progress.setString(val + "%");
		}
	}

	@Override
	protected Exception doInBackground() throws Exception {
		publish(0); // Just so there is no delay in the UI updating
		String pn = app.getBackendDocid();
		int vc = app.getDetails().getAppDetails().getVersionCode();
		int ot = app.getOffer(0).getOfferType();
		totalBytes = app.getDetails().getAppDetails().getInstallationSize();
		new FetchService(archive, pn, vc, ot, this).run();
		return null;
	}

	@Override
	protected void done() {
		progress.setString("Complete");
		try {
			get();
			if (failure != null) {
				progress.setString("Failure!");
			}
		}
		catch (CancellationException e) {
			progress.setString("Cancelled");
		}
		catch (ExecutionException e) {
			progress.setString("Error!");
			e.printStackTrace();
		}
		catch (InterruptedException e) {
			progress.setString("Aborted");
		}
		cancel.setEnabled(false);
		
		if (next!=null) {
			next.execute();
		}
	}

	public boolean onChunk(Object src, long numBytes) {
		float percent = (float) numBytes / (float) totalBytes;
		publish((int) (100f * percent));
		return isCancelled();
	}

	public void onComplete(Object src) {

	}

	public void onFailure(Object src, Exception e) {
		failure = e;
	}

}
