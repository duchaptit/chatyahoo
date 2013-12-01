package view;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingConstants;

import model.User;

public class MainViewYahoo extends JFrame {

	private JLabel lblAvatar, lblUserName;
	private PlaceholderTextField txtStatus, txtSearch;
	private JButton btnAddfriend, btnHistory;
	private JList<String> listFriends;
	private JLabel lblBanner;
	private DefaultListModel dlm;
	JComboBox<String> cbbUserName;
	private User user;
	private Image avatar;
	public MainViewYahoo(User user,Image avatar) {
		// TODO Auto-generated constructor stub
		super("ChatOnline");
		this.user = user;
		this.avatar = avatar;
		cbbUserName = new JComboBox<>();
		cbbUserName.addItem(user.getUserName());
		cbbUserName.addItem("Online");
		cbbUserName.addItem("Offline");
		MyPanel pnlChat = new MyPanel("backgroundview.jpg");
		pnlChat.setPreferredSize(new Dimension(250, 500));

		// ---------------------------avata+UserName--------------------------

		lblAvatar = new JLabel();
		lblAvatar.setBounds(25, 10, 50, 50);
		lblAvatar.setIcon(new ImageIcon("avatar.jpg"));
		pnlChat.add(lblAvatar);

		lblUserName = new JLabel(user.getUserName());
		lblUserName.setBounds(80, 10, 150, 30);
		lblUserName.setHorizontalAlignment(SwingConstants.CENTER);
		lblUserName.setForeground(Color.red);
		cbbUserName.setBounds(lblUserName.getBounds());
		pnlChat.add(cbbUserName);

		// --------------------------------status---------------------------------

		txtStatus = new PlaceholderTextField("");
		txtStatus.setPlaceholder("What is in your mind");
		txtStatus.setBounds(80, 40, 150, 30);
		pnlChat.add(txtStatus);
		txtStatus.setEditable(true);
		txtStatus.addKeyListener(new KeyAdapter() {
			public void keyReleased(KeyEvent e) {
				if (e.getKeyCode() == 10) {
					System.out.println(txtStatus.getText());
					txtStatus.requestFocus();
					txtStatus.getCaretPosition();
				}
			}
		});

		// ------------------------------------search-------------------------------

		txtSearch = new PlaceholderTextField();
		txtSearch.setPlaceholder("Type your key here");
		txtSearch.setBounds(new Rectangle(0, 100, 150, 30));
		pnlChat.add(txtSearch);

		btnAddfriend = new JButton("Add");
		btnAddfriend.setBounds(new Rectangle(150, 100, 100, 30));
		pnlChat.add(btnAddfriend);

		dlm = new DefaultListModel<>();
		listFriends = new JList(dlm);
		listFriends.setBounds(new Rectangle(10, 135, 230, 300));
		JScrollPane js = new JScrollPane(listFriends);
		js.setVerticalScrollBarPolicy ( ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS );
//		JPanel pnlChat = new JPanel();
//		pnlChat.add(js);
		js.setBounds(listFriends.getBounds());
		listFriends.setBorder(BorderFactory.createLineBorder(Color.black));
		pnlChat.add(js);
		this.setLocation(600, 200);
		this.setContentPane(pnlChat);
		this.pack();
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	}
	
	public String getInforAcombobox() {
		return (String) cbbUserName.getSelectedItem();
	}
	
	public void ChangeItemStatement(ActionListener act) {
		cbbUserName.addActionListener(act);
	}
	public User getUser() {
		return user;
	}

	public void setUser(User user) {
		this.user = user;
	}

	public void addFriendAction(ActionListener act) {
		btnAddfriend.addActionListener(act);
	}
	public void addActionListFriend(MouseAdapter act) {
		listFriends.addMouseListener(act);
	}
	public void addUsertoListFriends(String userName) {
		dlm.addElement(userName);
	}
	
	public String getUserNameA() {
		return lblUserName.getText();
	}
	public void UpdatelistOnline(Vector<String> vec) {
		dlm.removeAllElements();
		for (String string : vec) {
			dlm.addElement(string);
		}
	}
}
