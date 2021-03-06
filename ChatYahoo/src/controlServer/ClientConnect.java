package controlServer;

import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.Dictionary;
import java.util.Hashtable;
import java.util.Vector;

import javax.imageio.ImageIO;

import model.ChatHistory;
import model.Message;
import model.Setting;
import model.SmileIcon;
import model.User;
import controlClient.ImageManager;
import controlRMI.RMILoginInterface;

public class ClientConnect extends Thread {

	private ObjectInputStream ois;
	private ObjectOutputStream oos;
	ServerTCPControl serverTCP;
	private RMILoginInterface rmiServer;
	private Registry registry;
	private String rmiService = "rmitcpLoginServer";
	private int serverRMIPort = 3535;
	private Vector<String> vecChatting;
	Vector<Vector<Message>> vecOffline;
	private String serverRMIHost = "localhost";

	public ClientConnect(ObjectInputStream ois, ObjectOutputStream oos,
			ServerTCPControl serverTCP) {
		// TODO Auto-generated constructor stub
		this.ois = ois;
		this.oos = oos;
		this.serverTCP = serverTCP;
		vecChatting = new Vector<>();
		vecOffline = new Vector<>();
		bindingRMI();
		this.start();
	}

	private void bindingRMI() {
		try {
			// lay the dang ki
			registry = LocateRegistry.getRegistry(serverRMIHost, serverRMIPort);
			// tim kiem RMI server
			rmiServer = (RMILoginInterface) (registry.lookup(rmiService));
		} catch (RemoteException e) {
			e.printStackTrace();
		} catch (NotBoundException e) {
			e.printStackTrace();
		}
	}

	public Message readMsg() {
		Message msg = null;
		try {
			msg = (Message) ois.readObject();
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return msg;
	}

	public void sendMessage(Message msg) {
		try {
			oos.writeObject(msg);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public boolean testMessageinVecMsg(Message msg, Vector<Message> vec) {
		Message tmp = vec.get(0);
		if(tmp.getRecipient().equals(msg.getRecipient())){
			return true;
		}
		return false;
	}
	
	public void addOff(Message msg, Vector<Vector<Message>> vec) {
		int ok = 1;
		for (int i = 0; i < vec.size(); i++) {
			if(testMessageinVecMsg(msg, vec.get(i))){
				ok = 0;
				vec.get(i).add(msg);
				break;
			}
		}
		if(ok == 1){
			Vector<Message> vecmsg = new Vector<>();
			vecmsg.add(msg);
			vec.add(vecmsg);
		}
	}
	
	public Vector<Message> searchVecHistoryOff(Message msg, Vector<Vector<Message>> vec) {
		Vector<Message> result = new Vector<>();
		for (Vector<Message> vecmessage : vec) {
			Message tmp = vecmessage.get(0);
			if(msg.getRecipient().equals(tmp.getRecipient())){
				result = vecmessage;
				break;
			}
		}
		return result;
	}

	public void run() {
		while (true) {
			try {
				Message msg = (Message) ois.readObject();
				int flag = msg.getType();
				switch (flag) {
				case Setting.REQUEST_LOGIN:
					User user = (User) msg.getObj();
					user = rmiServer.searchUser(user.getUserName());
					try {
						String result = rmiServer.checkLogin(user);
						if (result.equals("YES")) {
							rmiServer.updateOnl(user, 1);
							serverTCP.hash.put(user.getUserName(), this);
							System.out.println(serverTCP.getVecOnline().size());
						}
						msg.setType(Setting.RESPONSE_LOGIN);
						sendMessage(msg);
						sendString(result);
						serverTCP.sendtoUser(user.getUserName(), new Message(Setting.RESPONSE_OFFLINEMESSAGE, searchVecHistoryOff(msg, vecOffline), null, null));
						for (String string: serverTCP.getVecOnline()) {
							Vector<String> vec = new Vector<>();
							for (String string2 : rmiServer.vecFriend(rmiServer.searchUser(string))) {
								if (rmiServer.searchUser(string2).getIsOnline() == 0) {
									vec.add(string2 + "       -        offline"); 
								} else {
									vec.add(string2 + "       -        online");
								}
							}
							serverTCP.sendtoUser(string, new Message(Setting.RESPNONSE_ALL_ONLINE, vec, null, null));
						}

					} catch (RemoteException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}

					break;

				case Setting.REQUEST_ACCESS_DATABASE:
					try {
						User userA = rmiServer.searchUser(String.valueOf(msg
								.getObj()));
						System.out.println(userA.getUserName());
						Message msgFlag = new Message(
								Setting.RESPONSE_ACCESS_DATABASE, null, null,
								null);
						sendMessage(msgFlag);
						Message msgArespone = new Message(
								Setting.RESPONSE_ACCESS_DATABASE, userA, null,
								userA.getUserName());
						sendMessage(msgArespone);
						Message msgB = readMsg();
						User userB = rmiServer.searchUser(String.valueOf(msgB
								.getObj()));
						Message msgBrespone = new Message(
								Setting.RESPONSE_ACCESS_DATABASE, userB, null,
								userA.getUserName());
						sendMessage(msgBrespone);
						System.out.println(userB.getUserName());
					} catch (RemoteException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					break;
				case Setting.REQUEST_REGISTER:
					User u = (User) msg.getObj();
					boolean b = false;
					try {
						b = rmiServer.addNewUser(u);
					} catch (RemoteException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					Message response = new Message();
					response.setType(Setting.RESPONSE_REGISTER);
					response.setSender(null);
					response.setRecipient(null);
					if (b) {
						response.setObj("Ok");
					} else
						response.setObj("Fail");
					sendMessage(response);
					break;
				case Setting.REQUEST_CHAT:
					try {
						rmiServer.insertHistory(msg);
						ChatHistory history = rmiServer.searchHistory(msg);
						User userA = history.getUserA();
						User userB = history.getUserB();
						Message msgSendUserB = new Message(
								Setting.RESPONSE_CHAT, history,
								userA.getUserName(), userB.getUserName());
						serverTCP.sendtoUser(msg.getRecipient(), msgSendUserB);
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					break;
				case Setting.REQUEST_SIGNOUT:
					User userout = (User) msg.getObj();
					userout = rmiServer.searchUser(userout.getUserName());
					rmiServer.updateOnl(userout, 0);
					serverTCP.removeUserOut(userout);
					for (String string: serverTCP.getVecOnline()) {
						Vector<String> vec = new Vector<>();
						for (String string2 : rmiServer.vecFriend(rmiServer.searchUser(string))) {
							if (rmiServer.searchUser(string2).getIsOnline() == 0) {
								vec.add(string2 + "       -        offline"); 
							} else {
								vec.add(string2 + "       -        online");
							}
						}
						serverTCP.sendtoUser(string, new Message(Setting.RESPNONSE_ALL_ONLINE, vec, null, null));
					}
					break;
				case Setting.REQUEST_ADDFRIEND:
					String userNameUserSend = (String) msg.getSender();
					String userNameFriendAdd = (String) msg.getObj();
					
					if (rmiServer.searchUser(userNameFriendAdd) == null) {
						String result = "NO";
						serverTCP.sendtoUser(msg.getSender(), new Message(
								Setting.RESPONSE_ADDFRIEND, result, null,
								userNameUserSend));
					} else {
						serverTCP.sendtoUser(userNameFriendAdd, new Message(
								Setting.REQUEST_ACCEPTADDFRIEND, msg,
								userNameUserSend, userNameFriendAdd));
					}
					break;
				case Setting.RESPONSE_DECLINEADDFRIEND:
					serverTCP.sendtoUser(msg.getRecipient(), msg);
					break;
				case Setting.RESPONSE_ACCEPTADDFRIEND:
					rmiServer.insertFriendList(msg);
					serverTCP.sendtoUser(msg.getRecipient(), msg);
					for (String string : serverTCP.getVecOnline()) {
						Vector<String> vec = new Vector<>();
						for (String string2 : rmiServer.vecFriend(rmiServer
								.searchUser(string))) {
							if (rmiServer.searchUser(string2).getIsOnline() == 0) {
								vec.add(string2 + "       -        offline");
							} else {
								vec.add(string2 + "       -        online");
							}
						}
						serverTCP.sendtoUser(string, new Message(
								Setting.RESPNONSE_ALL_ONLINE, vec, null, null));
					}
					break;

				case Setting.REQUEST_AVATAR:
					user = (User) msg.getObj();
					Dictionary<String, String> dic = new Hashtable<String,String>();
					Message avatarMessage = new Message();
					avatarMessage.setType(Setting.RESPONSE_AVATAR);
					Vector<User> userList = rmiServer.listUser();
					for (User usr:userList) {
						File file = new File(usr.getUserName() + ".jpg");
						BufferedImage bimg = null; 
						if (file.exists()) {
							bimg = ImageIO.read(file);
							dic.put(usr.getUserName(), ImageManager.encodeToString(bimg, "jpg"));
						} else {
							dic.put(usr.getUserName(), "NO_IMG");
						}
					}
					avatarMessage.setObj(dic);
					avatarMessage.setSender(msg.getSender());
					avatarMessage.setRecipient(msg.getRecipient());
					sendMessage(avatarMessage);
					break;
				case Setting.REQUEST_UPLOAD_IMAGE:
					String imagestr = (String) msg.getObj();
					BufferedImage bufferedImage = ImageManager.decodeToImage(imagestr);
					System.out.println(msg.getSender());
					File file2 = new File(msg.getSender() + ".jpg");
					ImageIO.write(bufferedImage, "jpg", file2);
					break;
				case Setting.REQUEST_SMILE_ICON:
					Vector<SmileIcon> smileList = rmiServer.listSmileIcon();
					System.out.println("smilelist size: "+smileList.size());
					Message smileMessage = new Message();
					smileMessage.setType(Setting.RESPONSE_SMILE_ICON);
					smileMessage.setObj(smileList);
					sendMessage(smileMessage);
					break;
					
				default:
					break;
				}
			} catch (Exception e) {
				// TODO: handle exception
				System.out.println(e.toString());
				break;
			}
		}
	}

	public void sendString(String s) {
		try {
			oos.writeObject(s);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
