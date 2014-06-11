package de.onyxbits.raccoon.gui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JEditorPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;

import com.akdeniz.googleplaycrawler.GooglePlay.DocV2;

import de.onyxbits.raccoon.io.Archive;
import de.onyxbits.raccoon.io.FetchListener;

/**
 * Display download progress and give the user a chance to cancel.
 * 
 * @author patrick
 * 
 */
public class DownloadView extends JPanel implements ActionListener, FetchListener {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private DownloadWorker worker;
	private JProgressBar progress;
	private JButton cancel;
	private DocV2 doc;
	private Archive archive;

	private DownloadView(Archive archive, DocV2 doc) {
		this.doc = doc;
		this.archive = archive;
		this.cancel = new JButton("Cancel");
		this.progress = new JProgressBar(0, 100);
		this.progress.setString("Waiting");
		this.progress.setStringPainted(true);
		String pn = doc.getBackendDocid();
		int vc = doc.getDetails().getAppDetails().getVersionCode();
		String title = doc.getTitle();
		File dest = archive.fileUnder(pn, vc);
		worker = new DownloadWorker(doc, archive, null);
		String boiler = "<html><h2>" + title + "</h2><code>" + dest.getAbsolutePath()
				+ "</code></html>";
		JPanel container = new JPanel();
		container.setOpaque(false);
		container.add(progress);
		container.add(cancel);
		JEditorPane info = new JEditorPane("text/html", boiler);
		info.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
		info.setEditable(false);
		info.setOpaque(false);
		add(info);
		add(container);
	}

	public static DownloadView create(Archive archive, DocV2 doc) {
		DownloadView ret = new DownloadView(archive, doc);
		ret.cancel.addActionListener(ret);
		ret.setLayout(new BoxLayout(ret, BoxLayout.Y_AXIS));
		ret.worker.addFetchListener(ret);
		return ret;
	}

	public boolean isDownloaded() {
		String pn = doc.getBackendDocid();
		int vc = doc.getDetails().getAppDetails().getVersionCode();
		return archive.fileUnder(pn, vc).exists();
	}

	public void addFetchListener(FetchListener listener) {
		worker.addFetchListener(listener);
	}

	public void startWorker() {
		worker.execute();
	}

	public void stopWorker() {
		worker.cancel(true);
	}

	public boolean isDownloading() {
		return !worker.isDone();
	}

	public void actionPerformed(ActionEvent event) {
		if (event.getSource() == cancel) {
			worker.cancel(true);
		}
	}

	public boolean onChunk(Object src, long numBytes) {
		float percent = (float) numBytes / (float) worker.totalBytes;
		int tmp = (int) (100f * percent);
		progress.setValue(tmp);
		progress.setString(tmp + "%");
		return false;
	}

	public void onComplete(Object src) {
		progress.setString("Complete");
		cancel.setEnabled(false);
	}

	public void onFailure(Object src, Exception e) {
		if (e instanceof IndexOutOfBoundsException) {
			progress.setString("Not paid for");
		}
		else {
			progress.setString("Error!");
		}
		cancel.setEnabled(false);
	}

	public void onAborted(Object src) {
		progress.setString("Cancelled");
		cancel.setEnabled(false);
	}
}
