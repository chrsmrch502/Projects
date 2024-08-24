import controller.Controller;
import model.*;
import view.IView;
import view.Console;

public class Gourmeet {

	public static void main(String[] args) {
		if (!(args.length == 1 || args.length == 4)) {
			System.out.println("Wrong launch parameters.\n");
			System.out.println("Parameters option 1 (will use default DB_HOST \"localhost\", DB_PORT 3306, DB_USER \"root\")");
			System.out.println("\tDB_PASSWORD\n");
			System.out.println("Parameters option 2");
			System.out.println("\tDB_HOST DB_PORT DB_USER DB_PASSWORD");
			System.exit(1);
		}
		DAO dao;
		if (args.length == 1)
			dao = new DAO(args[0]);
		else
			dao = new DAO(args[0], Integer.parseInt(args[1]), args[2], args[3]);

		try {
			IView view = new Console(System.in, System.out);
			AuthService auth = new AuthService(dao);
			RecipeCollection collection = new RecipeCollection(dao);
			Controller controller = new Controller(view, auth, collection);
			controller.startMenu();
		} catch (RuntimeException e) {
			System.out.println(e.getMessage());
		}

	}
}
