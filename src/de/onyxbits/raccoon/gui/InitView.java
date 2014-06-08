package de.onyxbits.raccoon.gui;

import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import de.onyxbits.raccoon.BrowseUtil;
import de.onyxbits.raccoon.io.Archive;

public class InitView extends JPanel implements ActionListener {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private JTextField password;
	private JTextField userId;
	private JTextField androidId;
	private JLabel status;
	private JButton create;
	private JButton help;

	private Archive archive;

	private MainActivity mainActivity;

	private InitView(MainActivity mainActivity, Archive archive) {
		this.mainActivity = mainActivity;
		this.archive = archive;
		password = new JTextField();
		userId = new JTextField("", 20);
		androidId = new JTextField();
		status = new JLabel(" ");
		create = new JButton("Create");
		help = new JButton("Help");
		JLabel instr = new JLabel(
				"<html>This archive needs to be linked to a Google account and a device. The Google account is required, the Android ID is optional (a new one will automatically be generated if none is given). Press 'Help' for details.</html>");
		instr.setAlignmentX(Component.CENTER_ALIGNMENT);
		add(instr);
		add(Box.createVerticalGlue());

		JPanel outer = new JPanel();

		outer.add(createCredentials());
		add(outer);
		JPanel container = new JPanel();
		container.add(create);
		container.add(help);
		add(container);
		add(status);
		setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
	}

	private JPanel createCredentials() {
		JPanel ret = new JPanel();
		ret.setLayout(new GridBagLayout());
		ret.setBorder(BorderFactory.createTitledBorder("Credentials"));
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.insets = new Insets(2, 2, 2, 2);
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.gridx = 0;
		gbc.gridy = 0;
		ret.add(new JLabel("Username:"), gbc);
		gbc.gridx = 1;
		gbc.gridy = 0;
		ret.add(userId, gbc);
		gbc.gridx = 0;
		gbc.gridy = 1;
		ret.add(new JLabel("Password:"), gbc);
		gbc.gridx = 1;
		gbc.gridy = 1;
		ret.add(password, gbc);
		gbc.gridx = 0;
		gbc.gridy = 2;
		gbc.insets = new Insets(14, 2, 2, 2);
		ret.add(new JLabel("Android ID:"), gbc);
		gbc.gridx = 1;
		gbc.gridy = 2;
		ret.add(androidId, gbc);
		return ret;
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
		status.setText("Bad credentials");
		create.setEnabled(true);
	}

	protected void doInProgress() {
		status.setText("Login... (Please wait)");
	}

}
