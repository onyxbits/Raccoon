package de.onyxbits.raccoon;

import java.io.File;
import java.util.prefs.Preferences;

import javax.swing.SwingUtilities;

import org.apache.commons.cli.BasicParser;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

import com.akdeniz.googleplaycrawler.GooglePlayAPI;

/**
 * Just the application launcher.
 * 
 * @author patrick
 * 
 */
public class App implements Runnable {

	/**
	 * Application Entry
	 * 
	 * @param args
	 * @throws ParseException
	 */
	public static void main(String[] args) throws ParseException {
		Option help = new Option("h", false, "Show commandline usage");
		Option update = new Option("u", false, "Update archive (requires -a).");
		Option archive = new Option("a", true, "Archive to work on");
		archive.setArgName("directory");

		Option fetch = new Option("f", true, "Fetch an app (requires -a).");
		fetch.setArgName("packId,versionCode,offerType");
		Options opts = new Options();
		opts.addOption(archive);
		opts.addOption(help);
		opts.addOption(update);
		opts.addOption(fetch);
		CommandLine cmd = new BasicParser().parse(opts, args);
		Archive dest = null;

		if (cmd.hasOption('h')) {
			new HelpFormatter().printHelp("Raccoon", opts);
			System.exit(0);
		}

		if (cmd.hasOption('a')) {
			dest = new Archive(new File(cmd.getOptionValue('a')));
		}

		if (cmd.hasOption("u")) {
			if (dest == null) {
				System.err.println("No archive specified!");
				System.exit(1);
			}
			new UpdateService(dest).run();
			System.exit(0);
		}

		if (cmd.hasOption("f")) {
			if (dest == null) {
				System.err.println("No archive specified!");
				System.exit(1);
			}
			String[] tmp = cmd.getOptionValues('f')[0].split(",");
			try {
				String appId = tmp[0];
				int vc = Integer.parseInt(tmp[1]);
				int ot = Integer.parseInt(tmp[2]);
				new FetchService(dest, appId, vc, ot).run();
			}
			catch (Exception e) {
				System.err.println("Format: packagename,versioncode,offertype");
				System.exit(1);
			}
			System.exit(0);
		}

		SwingUtilities.invokeLater(new App());
	}

	public void run() {
		Preferences prefs = Preferences.userNodeForPackage(MainActivity.class);

		Archive a = new Archive(new File(prefs.get(MainActivity.LASTARCHIVE, "Raccoon")));
		a.getDownloadLogger().clear();
		MainActivity ma = MainActivity.create();
		ma.doMount(a);
		ma.setVisible(true);
	}

	/**
	 * Utility method for hooking up with Google Play.
	 * 
	 * @param archive
	 *          The archive from which to take configuration data.
	 * @return a ready to use connection
	 * @throws Exception
	 *           if something goes seriously wrong.
	 */
	public static GooglePlayAPI createConnection(Archive archive) throws Exception {
		String pwd = archive.getPassword();
		String uid = archive.getUserId();
		String aid = archive.getAndroidId();
		GooglePlayAPI ret = new GooglePlayAPI(uid, pwd, aid);

		if (archive.getProxyClient() != null) {
			ret.setClient(archive.getProxyClient());
		}
		ret.setToken(archive.getAuthToken());
		if (ret.getToken() == null) {
			ret.login();
			archive.setAuthToken(ret.getToken());
		}
		return ret;
	}

}
