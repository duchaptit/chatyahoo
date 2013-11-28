package controlRMI;

import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import model.User;
import controlClient.DBConnection;

public class RMILoginServerControl extends UnicastRemoteObject implements
		RMILoginInterface {

	private int serverPort = 3535;
	private Registry registry;
	private String rmiService = "rmitcpLoginServer";

	protected RMILoginServerControl() throws RemoteException {
		super();
		// TODO Auto-generated constructor stub
		registry = LocateRegistry.createRegistry(serverPort);
		registry.rebind(rmiService, this);
	}

	@Override
	public String checkLogin(User user) throws RemoteException {
		// TODO Auto-generated method stub
		if (checkUser(user))
			return "YES";
		return "NO";
	}

	public boolean checkUser(User user) throws RemoteException {
		Connection conn = DBConnection.getConn();
		String sql = "select * from tbluser where username = ? and userpassword = ?";
		try {
			PreparedStatement pre = conn.prepareStatement(sql);
			pre.setString(1, user.getUserName());
			pre.setString(2, user.getUserpassword());
			ResultSet rs = pre.executeQuery();
			while (rs.next()) {
				return true;
			}
		} catch (Exception e) {
			// TODO: handle exceptionh
			e.printStackTrace();
		}
		return false;
	}

	@Override
	public User searchUser(String userName) throws RemoteException {
		// TODO Auto-generated method stub
		User user = new User();
		String  sql = "select * from tbluser where username = ?";
		Connection conn = DBConnection.getConn();
		try {
			PreparedStatement pre = conn.prepareStatement(sql);
			pre.setString(1, userName);
			ResultSet rs = pre.executeQuery();
			while (rs.next()) {
				user.setUserId(rs.getString("userid"));
				user.setUserName(rs.getString("username"));
				user.setUserpassword(rs.getString("userpassword"));
				user.setUserFirstName(rs.getString("userFirstName"));
				user.setUserLastName(rs.getString("userLastName"));
				user.setUserNickName(rs.getString("userNickname"));
				user.setUserEmail(rs.getString("useremail"));
				user.setUserPhoneNumber(rs.getString("userphone"));
				user.setUserDateofBirth(rs.getString("dateOfBirth"));
				user.setGender(rs.getInt("gender"));
				user.setIsOnline(rs.getInt("isOnline"));
				user.setUserStatusUpdate(rs.getString("statusUpdate"));
				user.setUserHost(rs.getString("userHost"));
				user.setUserPort(rs.getInt("userPort"));
				user.setUserAvartarURL(rs.getString("avatarUrl"));
				return user;
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return null;
	}
	public static void main(String[] args) {
		try {
			User user = new RMILoginServerControl().searchUser("a");
			System.out.println(user.getUserDateofBirth());
		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
