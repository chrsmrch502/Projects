package model;

abstract public class Account {
	private int id;
	private String username;
	private String password;

	/**
	 * Create an Account object with userame and password
	 * 
	 * @param username - The username of the account.
	 * @param password - The password of the account.
	 * @throws IllegalArgumentException if username or password strings are not
	 *                                  valid
	 */
	protected Account(String username, String password) throws IllegalArgumentException {
		setUsername(username);
		setPassword(password);
	}

	/**
	 * Create an Account object with id, userame and password
	 * 
	 * @param id       - The id of the account.
	 * @param username - The username of the account.
	 * @param password - The password of the account.
	 * @throws IllegalArgumentException if username or password strings are not
	 *                                  valid
	 */
	protected Account(int id, String username, String password) throws IllegalArgumentException {
		setId(id);
		setUsername(username);
		setPassword(password);
	}

	/**
	 * Returns the id of the account
	 * 
	 * @return the id of the account
	 */
	public int getId() {
		return id;
	}

	/**
	 * Set the id of the account
	 * 
	 * @param id - the id of the account
	 */
	public void setId(int id) {
		this.id = id;
	}

	/**
	 * Returns the username of the account
	 * 
	 * @return the username of the account
	 */
	public String getUsername() {
		return username;
	}

	/**
	 * Set the username of the account
	 * 
	 * @param username - the username of the account
	 * @throws IllegalArgumentException, if username is longer than 50 or empty
	 */
	public void setUsername(String username) throws IllegalArgumentException {
		if (username == null || username.length() > 50 || username.length() == 0) {
			throw new IllegalArgumentException("Username has invalid length!");
		}
		this.username = username;
	}

	/**
	 * Returns the password of the account
	 * 
	 * @return the password of the account
	 */
	public String getPassword() {
		return password;
	}

	/**
	 * Set the password of the account
	 * 
	 * @param password - the password of the account
	 * @throws IllegalArgumentException, if password is longer than 255 or empty
	 */
	public void setPassword(String password) throws IllegalArgumentException {
		if (username == null || username.length() > 255 || username.length() == 0) {
			throw new IllegalArgumentException("Password has invalid length!");
		}
		this.password = password;
	}

}
