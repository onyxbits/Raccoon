package de.onyxbits.raccoon.gui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.HashMap;
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JEditorPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;

import com.akdeniz.googleplaycrawler.GooglePlay.DocV2;

import de.onyxbits.raccoon.BrowseUtil;
import de.onyxbits.raccoon.Messages;
import de.onyxbits.raccoon.io.Archive;
import de.onyxbits.raccoon.io.DownloadLogger;
import de.onyxbits.raccoon.io.FetchListener;
import de.onyxbits.raccoon.io.FetchService;
import de.onyxbits.raccoon.io.FileNode;

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
	private JButton open;
	private DocV2 doc;
	private Archive archive;
	private HashMap<String, Object> model;
	private Vector<FileNode> files;
	private JEditorPane info;

	private DownloadView(Archive archive, DocV2 doc) {
		this.doc = doc;
		this.archive = archive;
		this.cancel = new JButton(Messages.getString("DownloadView.0")); //$NON-NLS-1$
		this.open = new JButton(Messages.getString("DownloadView.2")); //$NON-NLS-1$
		this.open.setEnabled(false);
		this.progress = new JProgressBar(0, 100);
		this.progress.setString(Messages.getString("DownloadView.1")); //$NON-NLS-1$
		this.progress.setStringPainted(true);

		String pn = doc.getBackendDocid();
		int vc = doc.getDetails().getAppDetails().getVersionCode();
		File dest = archive.fileUnder(pn, vc);
		model = new HashMap<String, Object>();
		model.put("title", doc.getTitle());
		model.put("path", dest.getParent());
		files = new Vector<FileNode>();

		worker = new DownloadWorker(doc, archive, null);
		JPanel container = new JPanel();
		container.setOpaque(false);
		container.add(progress);
		container.add(cancel);
		container.add(open);
		info = new HypertextPane(TmplTool.transform("download.html", model)); //$NON-NLS-1$
		info.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
		info.setEditable(false);
		info.setOpaque(false);
		add(info);
		add(container);
	}

	public static DownloadView create(Archive archive, DocV2 doc) {
		DownloadView ret = new DownloadView(archive, doc);
		ret.cancel.addActionListener(ret);
		ret.open.addActionListener(ret);
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
		if (event.getSource() == open) {
			String pn = doc.getBackendDocid();
			int vc = doc.getDetails().getAppDetails().getVersionCode();
			BrowseUtil.openFile(archive.fileUnder(pn, vc).getParentFile());
		}
	}

	public boolean onChunk(FetchService src, long numBytes) {
		float percent = (float) numBytes / (float) worker.totalBytes;
		int tmp = (int) (100f * percent);
		progress.setValue(tmp);
		progress.setString(tmp + "%"); //$NON-NLS-1$
		return false;
	}

	public void onComplete(FetchService src) {
		progress.setString(Messages.getString("DownloadView.7")); //$NON-NLS-1$
		progress.setValue(100);
		cancel.setEnabled(false);
		open.setEnabled(true);
		try {
			DownloadLogger dl = new DownloadLogger(archive);
			for (FileNode fn : files) {
				dl.addEntry(fn.file);
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void onFailure(FetchService src, Exception e) {
		if (e instanceof IndexOutOfBoundsException) {
			progress.setString(Messages.getString("DownloadView.8")); //$NON-NLS-1$
		}
		else {
			progress.setString(Messages.getString("DownloadView.9")); //$NON-NLS-1$
		}
		cancel.setEnabled(false);
	}

	public void onAborted(FetchService src) {
		model.remove("files");
		model.put("deleted_files", files);
		info.setText(TmplTool.transform("download.html", model)); //$NON-NLS-1$
		progress.setString(Messages.getString("DownloadView.10")); //$NON-NLS-1$
		cancel.setEnabled(false);
	}

	@Override
	public void onBeginFile(FetchService src, File file) {
		files.add(new FileNode(file));
		model.put("files", files);
		info.setText(TmplTool.transform("download.html", model)); //$NON-NLS-1$
	}

	@Override
	public void onFinishFile(FetchService src, File file) {

	}
}
