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

class DownloadWorker extends SwingWorker<Object, File> implements FetchListener {

	private Archive archive;
	private DocV2 app;

	protected long totalBytes;
	private long received;
	private Exception failure;
	private SearchWorker next;
	private FetchService service;
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
	protected void process(List<File> lst) {
		if (!isCancelled()) {
			for (FetchListener fl : listeners) {
				fl.onChunk(service, received);
				for (File f : lst) {
					fl.onBeginFile(service, f);
				}
			}
		}
	}

	@Override
	protected Object doInBackground() throws Exception {
		publish(new File[0]); // Just so there is no delay in the UI updating
		String pn = app.getBackendDocid();
		int vc = app.getDetails().getAppDetails().getVersionCode();
		int ot = app.getOffer(0).getOfferType();
		totalBytes = app.getDetails().getAppDetails().getInstallationSize();
		boolean paid = app.getOffer(0).getCheckoutFlowRequired();
		service= new FetchService(archive, pn, vc, ot, paid, this);
		service.run();
		return null;
	}

	@Override
	protected void done() {
		try {
			get();
			if (failure != null) {
				for (FetchListener fl : listeners) {
					fl.onFailure(service, failure);
				}
			}
			else {
				for (FetchListener fl : listeners) {
					fl.onComplete(service);
				}
			}
		}
		catch (CancellationException e) {
			for (FetchListener fl : listeners) {
				fl.onAborted(service);
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

	public boolean onChunk(FetchService src, long numBytes) {
		received = numBytes;
		publish(new File[0]);
		return isCancelled();
	}

	public void onComplete(FetchService src) {

	}

	public void onFailure(FetchService src, Exception e) {
		failure = e;
	}

	public void onAborted(FetchService src) {
	}

	@Override
	public void onBeginFile(FetchService src, File file) {
		publish(file);
	}

	@Override
	public void onFinishFile(FetchService src, File file) {

	}

}
