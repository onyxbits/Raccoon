package de.onyxbits.raccoon;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.List;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;

import javax.swing.JButton;
import javax.swing.JProgressBar;
import javax.swing.SwingWorker;


import com.akdeniz.googleplaycrawler.GooglePlay.DocV2;
import com.akdeniz.googleplaycrawler.GooglePlayAPI;

class DownloadWorker extends SwingWorker<Exception, Integer> {

	private Archive archive;
	private DocV2 app;
	protected JProgressBar progress;
	protected JButton cancel;

	public DownloadWorker(DocV2 app, Archive archive) {
		this.app = app;
		this.archive = archive;
		this.progress = new JProgressBar(0, 100);
		this.cancel = new JButton("Cancel");
		this.progress.setStringPainted(true);
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

		GooglePlayAPI service = App.createConnection(archive);
		String pn = app.getBackendDocid();
		int vc = app.getDetails().getAppDetails().getVersionCode();
		int ot = app.getOffer(0).getOfferType();
		long size = app.getDetails().getAppDetails().getInstallationSize();
		long complete = 0;
		InputStream in = service.download(pn, vc, ot);

		File dest = archive.fileUnder(pn, vc);
		dest.getParentFile().mkdirs();
		FileOutputStream out = new FileOutputStream(dest);
		byte[] buffer = new byte[1024 * 16];
		int length;
		while ((length = in.read(buffer)) > 0) {
			complete += length;
			out.write(buffer, 0, length);
			float percent = (float) complete / (float) size;
			publish((int) (100f * percent));
			if (isCancelled()) {
				out.close();
				in.close();
				dest.delete();
				return null;
			}
		}
		out.close();
		in.close();
		archive.getDownloadLogger().addEntry(dest);

		return null;
	}

	@Override
	protected void done() {
		progress.setString("Complete");
		try {
			get();
		}
		catch (CancellationException e) {
			progress.setString("Aborted");
		}
		catch (ExecutionException e) {
			progress.setString("Error!");
			e.printStackTrace();
		}
		catch (InterruptedException e) {
			progress.setString("Aborted");
		}
		cancel.setEnabled(false);
	}

}
