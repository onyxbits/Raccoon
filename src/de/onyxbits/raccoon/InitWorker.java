package de.onyxbits.raccoon;

import java.util.Locale;
import java.util.concurrent.ExecutionException;

import javax.swing.SwingWorker;

import com.akdeniz.googleplaycrawler.GooglePlayAPI;
import com.akdeniz.googleplaycrawler.GooglePlayException;

/**
 * A worker for initializing a new archive. This class will log into the
 * provided account, get a new android id and save the whole thing to the
 * credentials file.
 * 
 * @author patrick
 * 
 */
public class InitWorker extends SwingWorker<String, Object> {

	private Archive archive;
	private InitView initView;

	/**
	 * 
	 * @param a
	 *          archive. It must have a valid username/password in it, but does
	 *          not need to have anything on disk, yet. It will get an aid filled
	 *          in and the credentials will also be written to file.
	 */
	public InitWorker(Archive a, InitView callback) {
		this.archive = a;
		this.initView = callback;
	}

	@Override
	protected String doInBackground() throws Exception {
		String uid = archive.getUserId();
		String pwd = archive.getPassword();
		String aid = archive.getAndroidId();
		String loc = Locale.getDefault().getCountry();
		// Register the account with GPlay.
		GooglePlayAPI service = new GooglePlayAPI(uid, pwd, aid);
		service.setLocalization(loc);
		if (aid.length() == 0) {
			service.checkin();
			service.login();
			service.uploadDeviceConfig();
		}
		else {
			service.login();
		}
		// Persist credentials through a separate object...
		Archive a = new Archive(archive.getRoot());
		a.setUserId(uid);
		a.setPassword(pwd);
		a.setAndroidId(service.getAndroidID());
		a.saveCredentials();
		// ... and transport back the new aid, so we only modify the submitted
		// archive on the UI thread.
		return service.getAndroidID();
	}

	@Override
	protected void done() {
		try {
			archive.setAndroidId(get());
			initView.doRemount();
		}
		catch (InterruptedException e) {
			e.printStackTrace();
		}
		catch (ExecutionException e) {
			if (e.getCause() instanceof GooglePlayException) {
				initView.doErrorMessage();
			}
			e.printStackTrace();
		}
	}

}
