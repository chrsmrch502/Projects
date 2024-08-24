package model;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

public class AuthService {
	private DAO dao;
	private PasswordEncoder encoder;

	/**
	 * Create an AuthService object with given DAO
	 * 
	 * @param dao - the database-access-object
	 */
	public AuthService(DAO dao) {
		this.dao = dao;

		final int LOG_ROUNDS = 10;
		encoder = new BCryptPasswordEncoder(LOG_ROUNDS);
	}

	/**
	 * checks if given combination of username & password exists in the database
	 * 
	 * @param username - the username of the user
	 * @param password - the password of the user
	 * @return True, if combination of username & password was found, False if not
	 */
	public boolean authenticate(String username, String password) {
		User user = dao.getUser(username);

		return user != null && encoder.matches(password, user.getPassword());
	}

	/**
	 * checks if username already exists in the database
	 * 
	 * @param username - the username to be checked
	 * @return True, if username already exists in the database, False if not
	 */
	public boolean checkExistingUser(String username) {
		return dao.userExists(username);
	}

	/**
	 * register a new user to the database
	 * 
	 * @param user - the user to be registered
	 */
	public void addUser(User user) {
		String hash = encoder.encode(user.getPassword());
		user.setPassword(hash);
		dao.addUser(user);
	}

	/**
	 * get the full user object from the database given a username
	 * 
	 * @param username - the username of desired user
	 * @return the full user object of desired user
	 */
	public User getFullUser(String username) {
		return dao.getFullUser(username);
	}

}
