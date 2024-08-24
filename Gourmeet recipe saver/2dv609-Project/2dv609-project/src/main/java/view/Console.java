package view;

import java.io.InputStream;
import java.io.PrintStream;
import java.util.List;
import java.util.Scanner;
import java.util.Stack;

import model.*;

public class Console implements IView {

	private InputStream in;
	private PrintStream out;
	private StringBuilder stringB = new StringBuilder();
	private Scanner sc;

	private Stack<String> breadcrumbStack = new Stack<String>();

	public Console(InputStream in, PrintStream out) {
		this.in = in;
		this.out = out;
		this.sc = new Scanner(this.in);
	}

	// ==========================================================================

	/*
	 * All of the 'template' strings. Each are 50 characters in length, 52 for
	 * tables, excluding escape sequences. For copy+paste
	 * "                                                  \n"
	 */

	private String gourmeet = "" + "   _____                                      _   \n"
			+ "  / ____|                                    | |  \n"
			+ " | |  __  ___  _   _ _ __ _ __ ___   ___  ___| |_ \n"
			+ " | | |_ |/ _ \\| | | | '__| '_ ` _ \\ / _ \\/ _ \\ __|\n"
			+ " | |__| | (_) | |_| | |  | | | | | |  __/  __/ |_ \n"
			+ "  \\_____|\\___/ \\____|_|  |_| |_| |_|\\___|\\___|\\__|\n";

	private String div = "====================================================\n";
	private String border = "||                                                ||\n";
	// ==========================================================================

	/*
	 * Formatting methods. These are used to fill in bodies, handle options,
	 * descriptions, etc.
	 */

	private String bodyFormatter(String text) {
		StringBuilder formatted = new StringBuilder();
		formatted.append(border); // Starts with a border
		int loops = 0; // Keeps track of new borders created
		int padding = 4;
		int padAndNewline = (2 * padding) + 1;
		int borderContainer = 48;

		for (int i = 0; i < text.length(); i++) {
			if ((i + (padding * (loops + 1))) % borderContainer == 0) { // If the characters exceed the borders (+
																		// padding)
				loops++;
				formatted.append(border); // Adds another border
			}
			formatted.setCharAt((i + padding + (padAndNewline * loops)), text.charAt(i)); // Replaces characters within
																							// the border and its
			// padding
		}
		return formatted.toString();
	}

	private String optionFormatter(int depth) {
		StringBuilder formatted = new StringBuilder();
		for (int i = 0; i < depth; i++) // Appends "> " repeatedly, depending on depth
			formatted.append("> ");
		return formatted.toString();
	}

	private String breadcrumbs() {
		String bc = "";
		for (int i = 0; i < breadcrumbStack.size(); i++)
			bc += optionFormatter(1) + breadcrumbStack.get(i) + " ";
		return bodyFormatter(bc);
	}

	// ==========================================================================

	/*
	 * Screen and Writing methods. 
	 * These are used for printing object information
	 * using the format methods above.
	 */

	/*
	 * Screens
	 */

	public void mainMenuScreen() {
		breadcrumbStack.clear();
		breadcrumbStack.add("Main Menu");

		stringB.append(gourmeet + div + breadcrumbs() + div + border);

		stringB.append(bodyFormatter(optionFormatter(1) + "1. Create a Recipe"));
		stringB.append(bodyFormatter(optionFormatter(1) + "2. View My Recipes"));
		stringB.append(bodyFormatter(optionFormatter(1) + "3. View All Public Recipes"));
		stringB.append(bodyFormatter(optionFormatter(1) + "4. View Filtered Recipes"));
		stringB.append(bodyFormatter(optionFormatter(1) + "5. My Ingredients"));
		stringB.append(bodyFormatter(optionFormatter(1) + "6. Log Out"));

		stringB.append(border + div);
		write(stringB.toString());
	}

	public void createARecipeScreen() {
		if (!breadcrumbStack.contains("Create a Recipe"))
			breadcrumbStack.add("Create a Recipe");

		stringB.append(gourmeet + div + breadcrumbs() + div + border);
		write(stringB.toString());
	}

	public void deleteARecipeScreen() { 
		if (!breadcrumbStack.contains("Delete a Recipe"))
			breadcrumbStack.add("Delete a Recipe");

		stringB.append(gourmeet + div + breadcrumbs() + div + border);

		stringB.append(bodyFormatter("Are you sure you want to delete this recipe?" + "Confirm - Y, Cancel - X"));

		stringB.append(border + div);
		write(stringB.toString());
	}

	public void showARecipeScreenWithOptions(Recipe rec) { 
		if (!breadcrumbStack.contains(rec.getTitle()))
			breadcrumbStack.add(rec.getTitle());

		stringB.append(gourmeet + div + breadcrumbs() + div + border);
		write(stringB.toString());

		writeRecipeTitle(rec);
		writeRecipeServings(rec);
		writeRecipeIngredientsList(rec);
		writeRecipeDescription(rec);
		writeRecipeSteps(rec);
		writeRecipeCalories(rec);
		writeRecipeTotalTimeInMinutes(rec);
		writeRecipeDifficulty(rec);
		writeRecipeVisibility(rec);

		stringB.append(border + bodyFormatter("D - Delete, E - Edit, P - Set public/Private, B - Back") + border + div);
		write(stringB.toString());
	}

	public void showNoOwnedRecipesScreen() {
		stringB.append(gourmeet + div + breadcrumbs() + div + border);
		stringB.append(bodyFormatter("You have no owned recipes to view."));
		stringB.append(bodyFormatter(optionFormatter(1) + "Press any key to go back to Main Menu.") + border + div);
		write(stringB.toString());
	}

	public void confirmedDeletedRecipeScreen() {
		stringB.append(gourmeet + div + breadcrumbs() + div + border);
		stringB.append(bodyFormatter("Recipe successfully deleted"));
		stringB.append(bodyFormatter(optionFormatter(1) + "Press any key to return to the Main Menu.") + border + div);
		write(stringB.toString());
	}

	public void showARecipeScreenNoOptions(Recipe rec) { 
		if (!breadcrumbStack.contains(rec.getTitle()))
			breadcrumbStack.add(rec.getTitle());

		stringB.append(gourmeet + div + breadcrumbs() + div + border);
		write(stringB.toString());

		writeRecipeTitle(rec);
		writeRecipeServings(rec);
		writeRecipeIngredientsList(rec);
		writeRecipeDescription(rec);
		writeRecipeSteps(rec);
		writeRecipeCalories(rec);
		writeRecipeTotalTimeInMinutes(rec);
		writeRecipeDifficulty(rec);
		writeRecipeVisibility(rec);

		stringB.append(border + bodyFormatter("B - Back") + border + div);
		write(stringB.toString());
	}

	public void editARecipeScreen(Recipe rec) { 
		if (!breadcrumbStack.contains("Edit a Recipe"))
			breadcrumbStack.add("Edit a Recipe");

		stringB.append(gourmeet + div + breadcrumbs() + div + border);
		write(stringB.toString());

		stringB.append("Change Title - T");
		write(stringB.toString());
		writeRecipeTitle(rec);

		stringB.append("Change Servings - S");
		write(stringB.toString());
		writeRecipeServings(rec);

		stringB.append("Change Ingredients - I");
		write(stringB.toString());
		writeRecipeIngredientsList(rec);

		stringB.append("Change Description - D");
		write(stringB.toString());
		writeRecipeDescription(rec);

		stringB.append("Change Steps - A");
		write(stringB.toString());
		writeRecipeSteps(rec);

		stringB.append("Change Calories - C");
		write(stringB.toString());
		writeRecipeCalories(rec);

		stringB.append("Change Time (in Minutes) - M");
		write(stringB.toString());
		writeRecipeTotalTimeInMinutes(rec);

		stringB.append("Change Difficulty - F");
		write(stringB.toString());
		writeRecipeDifficulty(rec);

		stringB.append(border + bodyFormatter("B - Back") + border + div);
		write(stringB.toString());
	}

	public void showPublicRecipesScreen(List<Recipe> recipes) {
		if (!breadcrumbStack.contains("Public Recipes"))
			breadcrumbStack.add("Public Recipes");

		stringB.append(gourmeet + div + breadcrumbs() + div + border);
		write(stringB.toString());

		writeListOfRecipes(recipes);

		stringB.append(border + div);
		write(stringB.toString());
	}

	public void showMyRecipesScreen(List<Recipe> recipes) {
		if (!breadcrumbStack.contains("My Recipes"))
			breadcrumbStack.add("My Recipes");

		stringB.append(gourmeet + div + breadcrumbs() + div + border);
		write(stringB.toString());

		writeListOfRecipes(recipes);

		stringB.append(border + div);
		write(stringB.toString());
	}

	public void filteredRecipesScreen() {
		if (!breadcrumbStack.contains("Filtered Recipes"))
			breadcrumbStack.add("Filtered Recipes");

		stringB.append(gourmeet + div + breadcrumbs() + div + border);
		write(stringB.toString());

		stringB.append(bodyFormatter("Filter by:"));
		stringB.append(bodyFormatter(optionFormatter(1) + "1. View Recipes by Own Ingredients"));
		stringB.append(bodyFormatter(optionFormatter(1) + "2. View Recipes by Calories"));
		stringB.append(bodyFormatter(optionFormatter(1) + "3. View Recipes by Cooking Time"));
		stringB.append(bodyFormatter(optionFormatter(1) + "4. View Recipes by Difficulty"));
		stringB.append(bodyFormatter(optionFormatter(1) + "5. View Recipes by Specific Ingredients"));
		stringB.append(bodyFormatter(optionFormatter(1) + "B. Go Back"));

		stringB.append(border + div);
		write(stringB.toString());
	}

	public void showFilteredOwnIngredientsScreen(RecipeCollection rc, User user) {
		stringB.append(gourmeet + div + breadcrumbs() + div + border);
		write(stringB.toString());

		writeListOfRecipesByOwnIngredients(rc, user);

		stringB.append(border + div);
		write(stringB.toString());
	}

	public void showFilteredCaloriesScreen(RecipeCollection rc, int calories) {
		stringB.append(gourmeet + div + breadcrumbs() + div + border);
		write(stringB.toString());

		writeListOfRecipesByCalories(rc, calories);

		stringB.append(border + div);
		write(stringB.toString());
	}

	public void showFilteredCookingTimeScreen(RecipeCollection rc, int cookingTime) {
		stringB.append(gourmeet + div + breadcrumbs() + div + border);
		write(stringB.toString());

		writeListOfRecipesByCookingTime(rc, cookingTime);

		stringB.append(border + div);
		write(stringB.toString());
	}

	public void showFilteredDifficultyScreen() {
		if (!breadcrumbStack.contains("Difficulty"))
			breadcrumbStack.add("Difficulty");

		stringB.append(gourmeet + div + breadcrumbs() + div + border);
		write(stringB.toString());

		stringB.append(bodyFormatter("Pick Difficulty:"));
		stringB.append(bodyFormatter(optionFormatter(1) + "1. Easy"));
		stringB.append(bodyFormatter(optionFormatter(1) + "2. Medium"));
		stringB.append(bodyFormatter(optionFormatter(1) + "3. Hard"));
		stringB.append(bodyFormatter(optionFormatter(1) + "B. Go Back"));

		stringB.append(border + div);
		write(stringB.toString());
	}

	public void showOwnIngredientsScreen(User user) {
		if (!breadcrumbStack.contains("My Ingredients"))
			breadcrumbStack.add("My Ingredients");

		stringB.append(gourmeet + div + breadcrumbs() + div + border);
		write(stringB.toString());

		writeListOfOwnIngredients(user);

		stringB.append(
				border + bodyFormatter("B - Go Back, D - Delete, E - Edit, N - Add New Ingredient") + border + div);
		write(stringB.toString());
	}

	public void createAccountOrLogInScreen() {
		// No need for a breadcrumb here
		stringB.append(gourmeet + div + border);

		stringB.append(bodyFormatter(optionFormatter(1) + "1. Log In"));
		stringB.append(bodyFormatter(optionFormatter(1) + "2. Create Account")); 
		stringB.append(bodyFormatter(optionFormatter(1) + "Q. Quit"));

		stringB.append(border + div);
		write(stringB.toString());
	}

	public void quitScreen() {
		stringB.append(gourmeet + div + border);

		stringB.append(bodyFormatter("Goodbye!"));

		stringB.append(border + div);
		write(stringB.toString());
	}

	/*
	 * Writings
	 */

	public void writeListOfRecipesByOwnIngredients(RecipeCollection rc, User user) {
		stringB.append(bodyFormatter(optionFormatter(1) + "Recipes by Ingredients you have: "));
		for (Recipe recipe : rc.browseByIngredients(user.getStoredIngredientNames())) {
			stringB.append(bodyFormatter(optionFormatter(2) + "(ID: " + recipe.getId() + ") " + recipe.getTitle()));
		}
		write(stringB.toString());
	}

	public void writeListOfRecipesByCalories(RecipeCollection rc, int calories) {
		stringB.append(bodyFormatter(optionFormatter(1) + "Recipes by Specified Calories: "));
		for (Recipe recipe : rc.browseByMaxCalories(calories)) {
			stringB.append(bodyFormatter(optionFormatter(2) + "(ID: " + recipe.getId() + ") " + recipe.getTitle()));
		}
		write(stringB.toString());
	}

	public void writeListOfRecipesByCookingTime(RecipeCollection rc, int cookingTime) {
		stringB.append(bodyFormatter(optionFormatter(1) + "Recipes by Specified Cooking Time: "));
		for (Recipe recipe : rc.browseByTotTimeInMin(cookingTime)) {
			stringB.append(bodyFormatter(optionFormatter(2) + "(ID: " + recipe.getId() + ") " + recipe.getTitle()));
		}
		write(stringB.toString());
	}

	public void writeListOfRecipesByDifficulty(RecipeCollection rc, Difficulty difficulty) {
		stringB.append(gourmeet + div + breadcrumbs() + div + border);
		write(stringB.toString());

		stringB.append(bodyFormatter(optionFormatter(1) + "Recipes by Specified Difficulty: "));
		for (Recipe recipe : rc.browseByDifficulty(difficulty)) {
			stringB.append(bodyFormatter(optionFormatter(2) + "(ID: " + recipe.getId() + ") " + recipe.getTitle()));
		}
		write(stringB.toString());

		stringB.append(border + div);
		write(stringB.toString());
	}

	public void writeListOfRecipesBySpecifiedIngredients(RecipeCollection rc, List<String> ingredients) {
		stringB.append(gourmeet + div + breadcrumbs() + div + border);
		write(stringB.toString());

		stringB.append(bodyFormatter(optionFormatter(1) + "Recipes by Specified Ingredients: "));
		for (Recipe recipe : rc.browseByIngredients(ingredients)) {
			stringB.append(bodyFormatter(optionFormatter(2) + "(ID: " + recipe.getId() + ") " + recipe.getTitle()));
		}
		write(stringB.toString());
	}

	public void writeListOfOwnIngredients(User user) {
		stringB.append(bodyFormatter(optionFormatter(1) + "Your Available Ingredients: "));
		for (Ingredient ing : user.getStoredIngredients()) {
			stringB.append(bodyFormatter(optionFormatter(2) + "(ID: " + ing.getId() + ") " + ing.getName()));
			if (ing.getUnit() != null && !ing.getUnit().isEmpty())
				stringB.append(bodyFormatter(
						optionFormatter(3) + "Quantity: " + ing.getQuantity() + " Unit: " + ing.getUnit()));
			else
				stringB.append(bodyFormatter(optionFormatter(3) + "Quantity: " + ing.getQuantity()));
		}
		write(stringB.toString());
	}

	public void writeListOfRecipes(List<Recipe> recipes) {
		stringB.append(bodyFormatter(optionFormatter(1) + "Enter ID of recipe below to view details:"));
		for (Recipe rec : recipes) {
			stringB.append(bodyFormatter(optionFormatter(2) + "(ID: " + rec.getId() + ") " + rec.getTitle()));
		}
		write(stringB.toString());
	}

	public void writeRecipeTitle(Recipe rec) {
		stringB.append(bodyFormatter(optionFormatter(1) + rec.getTitle()));
		write(stringB.toString());
	}

	public void writeRecipeServings(Recipe rec) {
		stringB.append(bodyFormatter(optionFormatter(1) + "Servings: " + rec.getServings()));
		write(stringB.toString());
	}

	public void writeRecipeIngredientsList(Recipe rec) {
		stringB.append(bodyFormatter(optionFormatter(1) + "Ingredients:"));

		for (Ingredient ingredient : rec.getIngredients()) {

			if (ingredient.getUnit() == null)
				stringB.append(bodyFormatter(optionFormatter(2) + ingredient.getName() + " - " + ingredient.getQuantity()));
			else
				stringB.append(bodyFormatter(optionFormatter(2) + ingredient.getName() + " - " + ingredient.getQuantity()
					+ " " + ingredient.getUnit()));
		}

		write(stringB.toString());
	}

	public void writeRecipeDescription(Recipe rec) {
		stringB.append(bodyFormatter(optionFormatter(1) + "Description:"));
		stringB.append(bodyFormatter(optionFormatter(1) + rec.getDescription()));
		write(stringB.toString());
	}

	public void writeRecipeSteps(Recipe rec) {
		stringB.append(bodyFormatter(optionFormatter(1) + "Steps:"));

		for (RecipeStep step : rec.getSteps()) {
			stringB.append(bodyFormatter(optionFormatter(2) + "Step " + step.getOrder()));
			stringB.append(bodyFormatter(step.getDescription()));
		}

		write(stringB.toString());
	}

	public void writeRecipeCalories(Recipe rec) {
		stringB.append(bodyFormatter(optionFormatter(1) + "Calories: " + rec.getCalories()));
		write(stringB.toString());
	}

	public void writeRecipeTotalTimeInMinutes(Recipe rec) {
		stringB.append(bodyFormatter(optionFormatter(1) + "Time (in minutes): " + rec.getTotTimeInMin()));
		write(stringB.toString());
	}

	public void writeRecipeDifficulty(Recipe rec) {
		stringB.append(bodyFormatter(optionFormatter(1) + "Difficulty: " + rec.getDifficulty().toString()));
		write(stringB.toString());
	}

	public void writeRecipeVisibility(Recipe rec) {
		if (rec.isPublic())
			stringB.append(bodyFormatter(optionFormatter(1) + "Visibility: Public"));
		else
			stringB.append(bodyFormatter(optionFormatter(1) + "Visibility: Private"));
		write(stringB.toString());
	}

	// ==========================================================================

	/*
	 * Get methods. Complex inputs are yielded here.
	 */

	public String getUserStringInput() {
		return sc.nextLine();
	}

	public int getUserIntInput() {

		int returnValue;
		while (true) {
			try {
				String input = sc.nextLine();
				returnValue = Integer.parseInt(input);
				return returnValue;
			} catch (NumberFormatException e) {
				stringB.append(bodyFormatter("Input invalid, try again"));
				write(stringB.toString());
			}
		}
	}

	public void readMenuSelection() {
		String input = sc.nextLine();
		while (input.length() < 1) {
			stringB.append(bodyFormatter("Enter key is an invalid input, try again"));
			write(stringB.toString());
			input = sc.nextLine();
		}
		try {
			lastInput = Integer.parseInt(input);
		} catch (NumberFormatException | StringIndexOutOfBoundsException e) {
			lastInput = input.charAt(0);
		}
	}

	// ==========================================================================

	/*
	 * Ask methods. Methods that prompts the user for a requested, complex input of
	 * sorts.
	 *
	 * Values are returned using get methods.
	 */

	public void askUsername() {
		stringB.append(bodyFormatter(optionFormatter(1) + "Enter your Username: "));
		write(stringB.toString());
	}

	public void askPassword() {
		stringB.append(bodyFormatter(optionFormatter(1) + "Enter your Password: "));
		write(stringB.toString());
	}

	public void askRecipeTitle() {
		stringB.append(bodyFormatter(optionFormatter(1) + "Enter Recipe Title: "));
		write(stringB.toString());
	}

	public void askRecipeDescription() {
		stringB.append(bodyFormatter(optionFormatter(1) + "Enter Recipe Description: "));
		write(stringB.toString());
	}

	public void askRecipeCalories() {
		stringB.append(bodyFormatter(optionFormatter(1) + "Enter Recipe Calories: "));
		write(stringB.toString());
	}

	public void askToMakePublic() {
		stringB.append(bodyFormatter(optionFormatter(1) + "Should the recipe be public? 1-Yes, 0-No"));
		write(stringB.toString());
	}

	public void askTotTimeInMin() {
		stringB.append(bodyFormatter(optionFormatter(1) + "Enter Total Time in Minutes: "));
		write(stringB.toString());
	}

	public void askServings() {
		stringB.append(bodyFormatter(optionFormatter(1) + "Enter No. of Servings: "));
		write(stringB.toString());
	}

	public void askNumberOfIngredients() {
		stringB.append(bodyFormatter(optionFormatter(1) + "Enter Number of Ingredients: "));
		write(stringB.toString());
	}

	public void askIngredientName() {
		stringB.append(bodyFormatter(optionFormatter(1) + "Enter Ingredient Name: "));
		write(stringB.toString());
	}

	public void askIngredientQuantity() {
		stringB.append(bodyFormatter(optionFormatter(1) + "Enter Ingredient Quantity: "));
		write(stringB.toString());
	}

	public void askIngredientUnit() {
		stringB.append(bodyFormatter(optionFormatter(1) + "Enter Ingredient Unit (Optional): "));
		write(stringB.toString());
	}

	public void askNumberOfSteps() {
		stringB.append(bodyFormatter(optionFormatter(1) + "Enter Number of Steps: "));
		write(stringB.toString());
	}

	public void askStepDescription() {
		stringB.append(bodyFormatter(optionFormatter(1) + "Enter Step Description: "));
		write(stringB.toString());
	}

	public void askDifficulty() {
		stringB.append(bodyFormatter(optionFormatter(1) + "Enter Difficulty: (0-Easy, 1-Medium, 2-Hard)"));
		write(stringB.toString());
	}

	public void askOwnIngredientID() {
		stringB.append(bodyFormatter(optionFormatter(1) + "Enter Ingredient ID: "));
		write(stringB.toString());
	}

	public void askRecipeID() {
		stringB.append(bodyFormatter(optionFormatter(1) + "Enter Recipe ID to View: "));
		write(stringB.toString());
	}

	public void askCalories() {
		stringB.append(bodyFormatter(optionFormatter(1) + "Enter Maximum Calories: "));
		write(stringB.toString());
	}

	public void askCookingTime() {
		stringB.append(bodyFormatter(optionFormatter(1) + "Enter Maximum Cooking Time (in minutes): "));
		write(stringB.toString());
	}

	public void askSpecificIngredients() {
		stringB.append(bodyFormatter(optionFormatter(1) + "Enter Specified Ingredients (Separated by ','): "));
		write(stringB.toString());
	}

	// ==========================================================================

	/*
	 * Wants methods. 
	 * These methods will return a boolean to be used by the
	 * controller. For values, refer to the ask (and get) methods.
	 */

	private int lastInput;

	public boolean wantsToEdit() {
		return Character.toUpperCase(lastInput) == 'E';
	}

	public boolean wantsToDelete() {
		return Character.toUpperCase(lastInput) == 'D';
	}

	public boolean wantsToSetPublic() {
		return Character.toUpperCase(lastInput) == 'P';
	}

	public boolean wantsToGoBack() {
		if (!breadcrumbStack.isEmpty())
			breadcrumbStack.pop();
		return Character.toUpperCase(lastInput) == 'B';
	}

	public boolean wantsToLogIn() {
		return lastInput == 1;
	}

	public boolean wantsToCreateAccount() {
		return lastInput == 2;
	}

	public boolean wantsToQuit() {
		return Character.toUpperCase(lastInput) == 'Q';
	}

	public boolean wantsToCreateRecipe() {
		return lastInput == 1;
	}

	public boolean wantsToViewOwnRecipes() {
		return lastInput == 2;
	}

	public boolean wantsToViewAllPublicRecipes() {
		return lastInput == 3;
	}

	public boolean wantsToViewFilteredRecipes() {
		return lastInput == 4;
	}

	public boolean wantsToViewOwnIngredients() {
		return lastInput == 5;
	}

	public boolean wantsToLogOut() {
		return lastInput == 6;
	}

	public boolean wantsToEditOwnIngredient() {
		return Character.toUpperCase(lastInput) == 'E';
	}

	public boolean wantsToDeleteOwnIngredient() {
		return Character.toUpperCase(lastInput) == 'D';
	}

	public boolean wantsToAddNewOwnIngredient() {
		return Character.toUpperCase(lastInput) == 'N';
	}

	// View your own or public recipe options

	public boolean wantsToDeleteOwnRecipe() {
		return Character.toUpperCase(lastInput) == 'D';
	}

	public boolean wantsToEditRecipe() {
		return Character.toUpperCase(lastInput) == 'E';
	}

	public boolean wantsToMakePrivateOrPublic() {
		return Character.toUpperCase(lastInput) == 'P';
	}

	public boolean wantsToConfirmQuit() {
		return Character.toUpperCase(lastInput) == 'Y';
	}

	public boolean wantsToMakePublic() {
		return lastInput == 1;
	}

	// View filtered recipes options

	public boolean wantsToViewRecipesByOwnIngredients() {
		return lastInput == 1;
	}

	public boolean wantsToViewRecipesByCalories() {
		return lastInput == 2;
	}

	public boolean wantsToViewRecipesByCookingTime() {
		return lastInput == 3;
	}

	public boolean wantsToViewRecipesByDifficulty() {
		return lastInput == 4;
	}

	public boolean wantsToViewRecipesBySpecifiedIngredients() {
		return lastInput == 5;
	}

	public boolean wantsEasyDifficulty() {
		return lastInput == 1;
	}

	public boolean wantsMediumDifficulty() {
		return lastInput == 2;
	}

	public boolean wantsHardDifficulty() {
		return lastInput == 3;
	}

	@Override
	public void resetMenuSelection() {
		lastInput = '_';
	}

	// ==========================================================================

	/*
	 * Input and output methods. 
	 * Used for printing the resulting strings 
	 * or taking input from the user.
	 */

	private void write(String text) {
		out.print(text);
		stringB.setLength(0);
	}

	/*
	 * Error and Exception messages. 
	 * Custom error messages using console formatting.
	 */
	public void errorMessage(String errorMessage) {
		stringB.append(div + bodyFormatter(errorMessage) + div);
		write(stringB.toString());
	}

	public void exceptionMessage(Exception e) {
		stringB.append(div + bodyFormatter("Something went wrong!") + div);
		write(stringB.toString());
		e.printStackTrace();
	}

	public void confirmationMessage(String confirmation) {
		stringB.append(div + bodyFormatter(confirmation) + div);
		write(stringB.toString());
	}
}
