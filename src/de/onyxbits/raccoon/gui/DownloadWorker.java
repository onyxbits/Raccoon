package de.onyxbits.raccoon.gui;

import java.io.File;
import java.util.List;
import java.util.Vector;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;

import javax.swing.SwingWorker;

import com.akdeniz.googleplaycrawler.GooglePlay.DocV2;
import de.onyxbits.raccoon.io.Archive;
import de.onyxbits.raccoon.io.FetchListener;
import de.onyxbits.raccoon.io.FetchService;

class DownloadWorker extends SwingWorker<Object, Long> implements FetchListener {

	private Archive archive;
	private DocV2 app;

	protected long totalBytes;
	private long received;
	private Exception failure;
	private SearchWorker next;
	private Vector<FetchListener> listeners;

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
		this.next = next;
		this.listeners = new Vector<FetchListener>();
	}

	public void addFetchListener(FetchListener listener) {
		if (!listeners.contains(listener)) {
			listeners.add(listener);
		}
	}

	public File getTarget() {
		return archive.fileUnder(app.getBackendDocid(), app.getDetails().getAppDetails()
				.getVersionCode());
	}

	@Override
	protected void process(List<Long> chunks) {
		if (!isCancelled()) {
			for (FetchListener fl : listeners) {
				// NOTE: get it straight from the horses mouth instead of figuring out
				// which chunk contains the latest value (which it doesn't anyway).
				// There is not potential for a race condition here.
				fl.onChunk(this, received);
			}
		}
	}

	@Override
	protected Object doInBackground() throws Exception {
		publish(0l); // Just so there is no delay in the UI updating
		String pn = app.getBackendDocid();
		int vc = app.getDetails().getAppDetails().getVersionCode();
		int ot = app.getOffer(0).getOfferType();
		totalBytes = app.getDetails().getAppDetails().getInstallationSize();
		new FetchService(archive, pn, vc, ot, this).run();
		return null;
	}

	@Override
	protected void done() {
		try {
			get();
			if (failure != null) {
				for (FetchListener fl : listeners) {
					fl.onFailure(this, failure);
				}
			}
			else {
				for (FetchListener fl : listeners) {
					fl.onComplete(this);
				}
			}
		}
		catch (CancellationException e) {
			for (FetchListener fl : listeners) {
				fl.onAborted(this);
			}
		}
		catch (ExecutionException e) {
			e.printStackTrace();
		}
		catch (InterruptedException e) {
		}

		if (next != null) {
			next.execute();
		}
	}

	public boolean onChunk(Object src, long numBytes) {
		received = numBytes;
		publish(numBytes);
		return isCancelled();
	}

	public void onComplete(Object src) {

	}

	public void onFailure(Object src, Exception e) {
		failure = e;
	}

	public void onAborted(Object src) {
	}

}
