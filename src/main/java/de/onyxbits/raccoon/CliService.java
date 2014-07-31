package de.onyxbits.raccoon;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import org.apache.commons.cli.BasicParser;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

import com.akdeniz.googleplaycrawler.GooglePlay.BulkDetailsEntry;
import com.akdeniz.googleplaycrawler.GooglePlay.BulkDetailsResponse;
import com.akdeniz.googleplaycrawler.GooglePlay.DocV2;
import com.akdeniz.googleplaycrawler.GooglePlayAPI;

import de.onyxbits.raccoon.io.Archive;
import de.onyxbits.raccoon.io.DownloadLogger;
import de.onyxbits.raccoon.io.FetchListener;
import de.onyxbits.raccoon.io.FetchService;

/**
 * Runs the app in command line interface mode.
 * 
 * @author patrick
 * 
 */
public class CliService implements FetchListener, Runnable {

	private String[] cmdLine;
	private DownloadLogger logger;
	private Archive destination;

	public CliService(String[] cmdLine) {
		this.cmdLine = cmdLine;
	}

	public boolean onChunk(FetchService src, long numBytes) {
		System.out.print('.');
		return false;
	}

	public void onFailure(FetchService src, Exception e) {
		System.err.println("Failure: " + e.getMessage());
		// e.printStackTrace();
	}

	public void onAborted(FetchService src) {
	}

	public void run() {
		Option help = new Option("h", false, "Show commandline usage");
		Option version = new Option("v", false, "Show version and exit");
		Option update = new Option("u", false, "Update archive (requires -a).");
		Option archive = new Option("a", true, "Archive to work on");
		Option importer = new Option(
				"i",
				true,
				"Import apps. A text file, containing one market url per line must be given (requires -a and -u to actually download).");
		Option paid = new Option("p", true,
				"Use with -f to download paid apps (the app must already have been bought).");
		archive.setArgName("directory");

		Option fetch = new Option("f", true, "Fetch an app (requires -a).");
		fetch.setArgName("packId,versionCode,offerType");
		Options opts = new Options();
		opts.addOption(version);
		opts.addOption(archive);
		opts.addOption(help);
		opts.addOption(update);
		opts.addOption(importer);
		opts.addOption(fetch);
		opts.addOption(paid);
		CommandLine cmd = null;
		try {
			cmd = new BasicParser().parse(opts, cmdLine);
		}
		catch (ParseException e1) {
			System.err.println(e1.getMessage());
			System.exit(1);
		}

		if (cmd.hasOption('h')) {
			new HelpFormatter().printHelp("Raccoon", opts);
			System.exit(0);
		}

		if (cmd.hasOption('v')) {
			System.out.println(App.VERSIONSTRING);
			System.exit(0);
		}

		if (cmd.hasOption('a')) {
			destination = new Archive(new File(cmd.getOptionValue('a')));
			logger = new DownloadLogger(destination);
			logger.clear();
		}

		if (cmd.hasOption("i")) {
			requireArchive();
			try {
				doImport(new File(cmd.getOptionValue('i')));
			}
			catch (IOException e) {
				System.err.println(e.getMessage());
			}
		}

		if (cmd.hasOption("u")) {
			requireArchive();
			doUpdate();
		}

		if (cmd.hasOption("f")) {
			requireArchive();

			String[] tmp = cmd.getOptionValues('f')[0].split(",");
			try {
				String appId = tmp[0];
				int vc = Integer.parseInt(tmp[1]);
				int ot = Integer.parseInt(tmp[2]);
				new FetchService(destination, appId, vc, ot, cmd.hasOption('p'), this).run();
			}
			catch (Exception e) {
				System.err.println("Format: packagename,versioncode,offertype");
				System.exit(1);
			}
		}
	}

	private void doUpdate() {
		BulkDetailsResponse response = null;
		GooglePlayAPI service = null;
		try {
			service = App.createConnection(destination);
			response = service.bulkDetails(destination.list());
		}
		catch (Exception e) {
			e.printStackTrace();
			return;
		}

		for (BulkDetailsEntry bulkDetailsEntry : response.getEntryList()) {
			DocV2 doc = bulkDetailsEntry.getDoc();
			String pn = doc.getBackendDocid();
			int vc = -1;
			int ot = -1;
			boolean paid = false;
			try {
				vc = doc.getDetails().getAppDetails().getVersionCode();
				ot = doc.getOffer(0).getOfferType();
				paid = doc.getOffer(0).getCheckoutFlowRequired();
			}
			catch (Exception e) {
				// Something in the apk storage did not resolve. This could be an app
				// that was pulled from Google Play or a directory s/he created. Design
				// decision: ignore silently. In the first case the user doesn't want
				// to bother in the second, s/he does not need to.
				continue;
			}
			File target = destination.fileUnder(pn, vc);
			if (!target.exists()) {
				FetchService fs = new FetchService(destination, pn, vc, ot, paid, this);
				fs.run();
			}
		}
	}

	private void doImport(File file) throws IOException {
		BufferedReader br = new BufferedReader(new FileReader(file));
		String line;
		String prefix = "market://details?id=";
		while ((line = br.readLine()) != null) {
			if (line.startsWith(prefix) && line.length() > prefix.length()) {
				// Let's keep it simple.
				String id = line.substring(prefix.length(), line.length());
				destination.fileUnder(id, 0).getParentFile().mkdirs();
				System.out.println("Adding: " + id);
			}
		}
		br.close();
	}

	/**
	 * Check if an archive is specified, bail out if not.
	 */
	private void requireArchive() {
		if (destination == null) {
			System.err.println("No archive specified!");
			System.exit(1);
		}
	}

	@Override
	public void onBeginFile(FetchService src, File file) {
		System.err.println("Starting: " + file.getName());
	}

	@Override
	public void onFinishFile(FetchService src, File file) {
		try {
			// NOTE: Not correct to do that here, but since the user can't cancel,
			// we can get away with it.
			logger.addEntry(file);
			System.out.println("\nSuccess");
		}
		catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void onComplete(FetchService src) {

	}
}