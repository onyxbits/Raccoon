package de.onyxbits.raccoon.gui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JEditorPane;
import javax.swing.JPanel;

import com.akdeniz.googleplaycrawler.GooglePlay.DocV2;

import de.onyxbits.raccoon.io.Archive;

/**
 * Display download progress and give the user a chance to cancel.
 * 
 * @author patrick
 * 
 */
public class DownloadView extends JPanel implements ActionListener {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private DownloadWorker worker;
	private DocV2 doc;
	private Archive archive;
	
	private DownloadView(Archive archive, DocV2 doc) {
		this.doc=doc;
		this.archive=archive;
		String pn = doc.getBackendDocid();
		int vc = doc.getDetails().getAppDetails().getVersionCode();
		String title = doc.getTitle();
		File dest = archive.fileUnder(pn,vc);
		worker = new DownloadWorker(doc,archive);
		String boiler = "<html><h2>"+title+"</h2><code>"+dest.getAbsolutePath()+"</code></html>";
		JPanel container = new JPanel();
		container.setOpaque(false);
		container.add(worker.progress);
		container.add(worker.cancel);
		JEditorPane info = new JEditorPane("text/html",boiler);
		info.setBorder(BorderFactory.createEmptyBorder(10,10,10,10));
		info.setEditable(false);
		info.setOpaque(false);
		add(info);
		add(container);
	}
	
	public static DownloadView create(Archive archive, DocV2 doc) {
		DownloadView ret = new DownloadView(archive,doc);
		ret.worker.cancel.addActionListener(ret);
		ret.setLayout(new BoxLayout(ret,BoxLayout.Y_AXIS));
		return ret;
	}
	
	public boolean isDownloaded() {
		String pn = doc.getBackendDocid();
		int vc = doc.getDetails().getAppDetails().getVersionCode();
		return archive.fileUnder(pn,vc).exists();
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
		if (event.getSource()==worker.cancel) {
			worker.cancel(true);
		}
	}

}
