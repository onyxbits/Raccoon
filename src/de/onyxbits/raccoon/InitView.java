package de.onyxbits.raccoon;

import java.awt.Component;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

public class InitView extends JPanel implements ActionListener {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private JTextField password;
	private JTextField userId;
	private JTextField androidId;
	private JButton create;
	private JButton help;

	private Archive archive;

	private MainActivity mainActivity;

	private InitView(MainActivity mainActivity, Archive archive) {
		this.mainActivity = mainActivity;
		this.archive = archive;
		password = new JTextField();
		userId = new JTextField();
		androidId = new JTextField();
		create = new JButton("Create");
		help = new JButton("Help");
		JLabel instr = new JLabel(
				"<html>Creating a new archive. A Google account is requried, the Android ID is optional. Press 'Help' for details.</html>");
		instr.setAlignmentX(Component.CENTER_ALIGNMENT);
		add(instr);
		add(Box.createVerticalGlue());
		JPanel container = new JPanel();
		container.setLayout(new GridLayout(3, 2, 10, 5));
		container.add(new JLabel("Username:"));
		container.add(userId);
		container.add(new JLabel("Password:"));
		container.add(password);
		container.add(new JLabel("Android ID:"));
		container.add(androidId);
		container.setBorder(BorderFactory.createTitledBorder("Credentials"));
		JPanel outer = new JPanel();

		outer.add(container);
		add(outer);
		container = new JPanel();
		container.add(create);
		container.add(help);
		add(container);
		setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
	}

	public static InitView create(MainActivity mainActivity, Archive archive) {
		InitView ret = new InitView(mainActivity, archive);
		ret.setLayout(new BoxLayout(ret, BoxLayout.Y_AXIS));
		ret.create.addActionListener(ret);
		ret.help.addActionListener(ret);
		return ret;
	}

	public void actionPerformed(ActionEvent event) {
		Object src = event.getSource();
		if (src == create) {
			archive.setPassword(password.getText());
			archive.setUserId(userId.getText());
			archive.setAndroidId(androidId.getText());
			create.setEnabled(false);
			new InitWorker(archive, this).execute();
		}
		if (src == help) {
			BrowseUtil.openUrl("http://www.onyxbits.de/faq/raccoon");
		}
	}

	protected void doRemount() {
		mainActivity.doMount(archive);
	}

	protected void doErrorMessage() {
		JOptionPane.showMessageDialog(getRootPane(), "Could not login", "Bad credentials",
				JOptionPane.ERROR_MESSAGE);
		create.setEnabled(true);
	}

}
