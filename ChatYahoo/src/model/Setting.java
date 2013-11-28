package model;

import java.io.Serializable;

public class Setting implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public static final int RESPONSE_CHAT = 1;
	public static final int RESPONSE_LOGIN = 2;
	public static final int REQUEST_CHAT = 3;
	public static final int REQUEST_LOGIN = 4;
	public static final int REQUEST_USER_ONLINE = 5;
	public static final int RESPONSE_USER_ONLINE = 6;
	public static final int RESPNONSE_ALL_ONLINE = 7;
	public static final int REQUEST_ALL_ONLINE = 8;
	public static final int REQUSET_ACCESS_DATABASE = 9;
	public static final int RESPONSE_ACCESS_DATABASE = 10;
}
