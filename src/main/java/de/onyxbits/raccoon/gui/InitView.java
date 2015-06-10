package de.onyxbits.raccoon.gui;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JRadioButton;
import javax.swing.JTextField;

import de.onyxbits.raccoon.BrowseUtil;
import de.onyxbits.raccoon.Messages;
import de.onyxbits.raccoon.io.Archive;

public class InitView extends JPanel implements ActionListener {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private JPasswordField password;
	private JTextField userId;
	private JTextField androidId;
	private JLabel status;
	private JButton login;
	private JButton help;
	private JRadioButton existing;
	private JRadioButton generate;
	private ButtonGroup profile;

	private Archive archive;

	private MainActivity mainActivity;

	private InitView(MainActivity mainActivity, Archive archive) {
		this.mainActivity = mainActivity;
		this.archive = archive;
		password = new JPasswordField("", 20); //$NON-NLS-1$
		userId = new JTextField("", 20); //$NON-NLS-1$
		androidId = new JTextField(Messages.getString("InitView.16"), 20); //$NON-NLS-1$
		status = new JLabel(" "); //$NON-NLS-1$
		login = new JButton(Messages.getString("InitView.4")); //$NON-NLS-1$
		help = new JButton(Messages.getString("InitView.5")); //$NON-NLS-1$
		existing = new JRadioButton(Messages.getString("InitView.15"));
		generate = new JRadioButton(Messages.getString("InitView.14"));
		profile = new ButtonGroup();
		profile.add(generate);
		profile.add(existing);
		generate.setSelected(true);
		JLabel instr = new JLabel(Messages.getString("InitView.6")); //$NON-NLS-1$

		JPanel container = new JPanel();
		container.add(help);
		container.add(login);

		setLayout(new GridBagLayout());
		GridBagConstraints gbc = new GridBagConstraints();

		gbc.insets = new Insets(2, 2, 2, 2);
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.gridwidth = 2;
		gbc.gridheight = 1;
		gbc.weightx = 2;
		gbc.weighty = 2;
		gbc.anchor = GridBagConstraints.CENTER;
		add(instr, gbc);

		gbc.gridy = 1;
		gbc.gridwidth = 1;
		gbc.weightx = 1;
		gbc.weighty = 1;
		gbc.fill = GridBagConstraints.BOTH;
		JPanel outer = new JPanel(); // GridBaglayout...
		outer.add(createCredentials());
		add(outer, gbc);

		outer = new JPanel();
		outer.add(status);
		gbc.gridy = 2;
		add(outer, gbc);

		gbc.gridy = 3;
		add(container, gbc);

		setBorder(BorderFactory.createEmptyBorder(50, 100, 50, 100));
	}

	private JPanel createCredentials() {
		JPanel ret = new JPanel();
		ret.setLayout(new GridBagLayout());
		ret.setBorder(BorderFactory.createTitledBorder(Messages.getString("InitView.7"))); //$NON-NLS-1$
		GridBagConstraints gbc = new GridBagConstraints();
		JPanel cont = new JPanel();
		cont.add(generate);
		cont.add(existing);
		gbc.insets = new Insets(2, 2, 2, 2);
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.weightx = 0;
		ret.add(new JLabel(Messages.getString("InitView.8")), gbc); //$NON-NLS-1$

		gbc.gridx = 1;
		gbc.gridy = 0;
		gbc.weightx = 1;
		ret.add(userId, gbc);

		gbc.gridx = 0;
		gbc.gridy = 1;
		gbc.weightx = 0;
		ret.add(new JLabel(Messages.getString("InitView.9")), gbc); //$NON-NLS-1$

		gbc.gridx = 1;
		gbc.gridy = 1;
		gbc.weightx = 1;
		ret.add(password, gbc);

		gbc.gridx = 0;
		gbc.gridy = 2;
		gbc.insets = new Insets(15, 0, 0, 0);
		ret.add(new JLabel(), gbc);

		gbc.gridx = 1;
		gbc.gridy = 2;
		gbc.weightx = 0;
		gbc.insets = new Insets(15, 0, 0, 0);
		ret.add(cont, gbc);

		gbc.gridx = 0;
		gbc.gridy = 3;
		gbc.weightx = 0;
		gbc.insets=new Insets(0,0,0,0);
		ret.add(new JLabel(Messages.getString("InitView.10")), gbc); //$NON-NLS-1$

		gbc.gridx = 1;
		gbc.gridy = 3;
		gbc.weightx = 1;
		ret.add(androidId, gbc);
		return ret;
	}

	public static InitView create(MainActivity mainActivity, Archive archive) {
		InitView ret = new InitView(mainActivity, archive);
		ret.login.addActionListener(ret);
		ret.help.addActionListener(ret);
		ret.existing.addActionListener(ret);
		ret.generate.addActionListener(ret);
		ret.androidId.setEditable(false);
		return ret;
	}

	public void actionPerformed(ActionEvent event) {
		Object src = event.getSource();
		if (src == login) {
			archive.setPassword(new String(password.getPassword()));
			archive.setUserId(userId.getText());
			if (existing.isSelected()) {
				archive.setAndroidId(androidId.getText());
			}
			login.setEnabled(false);
			new InitWorker(archive, this).execute();
		}
		if (src == help) {
			BrowseUtil.openUrl(Messages.getString("InitView.11")); //$NON-NLS-1$
		}
		if (src == existing || src == generate) {
			androidId.setEditable(existing.isSelected());
			if (existing.isSelected()) {
				androidId.setText("");
			}
			else {
				androidId.setText(Messages.getString("InitView.16"));
			}
		}
	}

	protected void doRemount() {
		mainActivity.doMount(archive);
	}

	protected void doErrorMessage() {
		status.setText(Messages.getString("InitView.12")); //$NON-NLS-1$
		login.setEnabled(true);
	}

	protected void doInProgress() {
		status.setText(Messages.getString("InitView.13")); //$NON-NLS-1$
	}

}
