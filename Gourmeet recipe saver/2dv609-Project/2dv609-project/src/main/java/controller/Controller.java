package controller;

import java.util.ArrayList;
import java.util.List;

import model.AuthService;
import model.Difficulty;
import model.Ingredient;
import model.Recipe;
import model.User;
import model.RecipeCollection;
import model.RecipeStep;
import view.IView;

public class Controller {
	private IView view;
	private AuthService authService;
	private RecipeCollection recipeCollection;
	private User loggedInUser;

	public Controller(IView view, AuthService authService, RecipeCollection recipeCollection) {
		this.view = view;
		this.authService = authService;
		this.recipeCollection = recipeCollection;
		loggedInUser = null;
	}

	public void startMenu() {
		while (true) {
			view.createAccountOrLogInScreen();
			view.readMenuSelection();
			if (view.wantsToCreateAccount()) {
				register();
				if (isLoggedIn())
					mainMenu();
				else
					continue;
			}
			else if (view.wantsToLogIn()) {
				logIn();
				if (isLoggedIn())
					mainMenu();
				else
					continue;
			}
			else if (view.wantsToQuit())
				break;
			else
				view.errorMessage("Please enter a valid option");
		}
	}

	public void logIn() {
		view.askUsername();
		String username = view.getUserStringInput();
		view.askPassword();
		String password = view.getUserStringInput();

		if (authService.authenticate(username, password)) {
			loggedInUser = authService.getFullUser(username);
			loggedInUser.setRecipeCollection(recipeCollection);
			// TODO create a success message after logged in or progress to next prompt
		}
		else
			view.errorMessage("Incorrect login details, try again");
	}

	/*
	 * Register a user by getting their username and password, creating a new User
	 * object and send the user to the database by calling addUser in the
	 * AuthService class.
	 */
	public void register() {
		view.askUsername();
		String username = view.getUserStringInput();
		view.askPassword();
		String password = view.getUserStringInput();

		if (authService.checkExistingUser(username)) {
			view.errorMessage("That user account already exists, try again");
			// TODO create a success message after registered or progress to next prompt
		}
		else {
			loggedInUser = new User(username, password);
			loggedInUser.setRecipeCollection(recipeCollection);
			authService.addUser(loggedInUser);
		}
	}

	public void mainMenu() {
		while (true) {
			view.mainMenuScreen();
			view.readMenuSelection();

			if (view.wantsToCreateRecipe()) {
				view.createARecipeScreen();
				createRecipe();
			}
			else if (view.wantsToViewOwnRecipes()) {
				if(recipeCollection.browseByUser(loggedInUser.getUsername()).isEmpty()) {
					view.showNoOwnedRecipesScreen();
					view.readMenuSelection();
				}
				else {
					view.showMyRecipesScreen(recipeCollection.browseByUser(loggedInUser.getUsername()));
					ownRecipesMenu();
				}
			}
			else if (view.wantsToViewAllPublicRecipes()) {
				view.showPublicRecipesScreen(recipeCollection.browseAll());
				allRecipesMenu();
			}
			else if (view.wantsToViewFilteredRecipes()) {
				filteredRecipesMenu();
				// add controller method for this
			}
			else if (view.wantsToViewOwnIngredients()) {
				ownIngredientsMenu();
				// add controller method for this
			}
			else if (view.wantsToLogOut())
				break;
			else
				view.errorMessage("Please enter a valid option");
		}
		logOut();
	}

	private void logOut() {
		loggedInUser = null;
	}

	private void createRecipe() {
		view.askRecipeTitle();
		String title = view.getUserStringInput();

		view.askRecipeDescription();
		String description = view.getUserStringInput();

		view.askToMakePublic();
		view.readMenuSelection();
		boolean isPublic = view.wantsToMakePublic();

		view.askRecipeCalories();
		int calories = view.getUserIntInput();

		view.askTotTimeInMin();
		int totTimeInMin = view.getUserIntInput();

		view.askServings();
		int servings = view.getUserIntInput();

		view.askNumberOfIngredients();
		int numberOfIngredients = view.getUserIntInput();
		List<Ingredient> ingredients = new ArrayList<Ingredient>();
		for (int i = 0; i < numberOfIngredients; ++i) {
			view.askIngredientName();
			String ingName = view.getUserStringInput();

			view.askIngredientQuantity();
			int ingQuant = view.getUserIntInput();

			view.askIngredientUnit();
			String ingUnit = view.getUserStringInput();
			ingredients.add(new Ingredient(ingName, ingQuant, ingUnit));
		}

		view.askNumberOfSteps();
		int numberOfSteps = view.getUserIntInput();
		List<RecipeStep> steps = new ArrayList<RecipeStep>();
		for (int i = 0; i < numberOfSteps; ++i) {
			view.askStepDescription();
			String stepDescription = view.getUserStringInput();
			steps.add(new RecipeStep(stepDescription, i + 1));
		}

		view.askDifficulty();
		int intDiff = view.getUserIntInput();
		Difficulty difficulty;
		if (intDiff == 0)
			difficulty = Difficulty.EASY;
		else if (intDiff == 2)
			difficulty = Difficulty.HARD;
		else
			difficulty = Difficulty.MEDIUM;

		Recipe recipe = new Recipe(title, description, isPublic, calories, totTimeInMin, servings, ingredients, steps,
				difficulty);
		recipeCollection.addRecipe(recipe, loggedInUser.getUsername());
	}

	private void ownIngredientsMenu() {
		while (true) {
			view.showOwnIngredientsScreen(loggedInUser);
			view.readMenuSelection();

			if (view.wantsToDeleteOwnIngredient()) {
				view.askOwnIngredientID();
				int pickedIngredientId = view.getUserIntInput();
				for (Ingredient ingredient : loggedInUser.getStoredIngredients()) {
					if (pickedIngredientId == ingredient.getId()) {
						loggedInUser.removeStoredIngredient(pickedIngredientId);
						break;
					}
				}
			}
			else if (view.wantsToEditOwnIngredient()) {
				view.askOwnIngredientID();
				int pickedIngredient = view.getUserIntInput();
				view.askIngredientName();
				String ingredientName = view.getUserStringInput();
				view.askIngredientQuantity();
				int ingredientQuantity = view.getUserIntInput();
				view.askIngredientUnit();
				String ingredientUnit = view.getUserStringInput();

				chosenIngredient(loggedInUser, pickedIngredient).setName(ingredientName);
				chosenIngredient(loggedInUser, pickedIngredient).setQuantity(ingredientQuantity);
				if (ingredientUnit != null)
					chosenIngredient(loggedInUser, pickedIngredient).setUnit(ingredientUnit);
			}
			else if (view.wantsToAddNewOwnIngredient()) {
				view.askIngredientName();
				String ingredientName = view.getUserStringInput();
				view.askIngredientQuantity();
				int ingredientQuantity = view.getUserIntInput();
				view.askIngredientUnit();
				String ingredientUnit = view.getUserStringInput();

				if (ingredientUnit != null) {
					Ingredient ingredient = new Ingredient(ingredientName, ingredientQuantity, ingredientUnit);
					loggedInUser.addStoredIngredient(ingredient);
				}
				else {
					Ingredient ingredient = new Ingredient(ingredientName, ingredientQuantity);
					loggedInUser.addStoredIngredient(ingredient);
				}
			}
			else if (view.wantsToGoBack())
				break;
			else
				view.errorMessage("Please enter a valid option");
		}
	}

	private Ingredient chosenIngredient(User user, int ingredientID) {
		for (Ingredient ingredient : user.getStoredIngredients())
			if (ingredientID == ingredient.getId())
				return ingredient;

		return null;
	}

	private Recipe chosenRecipe(RecipeCollection recipeCollection, int recipeID) {
		for (Recipe recipe : recipeCollection.browseAll())
			if (recipeID == recipe.getId())
				return recipe;

		return null;
	}

	private void filteredRecipesMenu() {
		while (true) {
			view.filteredRecipesScreen();
			view.readMenuSelection();

			if (view.wantsToViewRecipesByOwnIngredients()) {
				view.showFilteredOwnIngredientsScreen(recipeCollection, loggedInUser);
				view.askRecipeID();
				int pickedRecipe = view.getUserIntInput();
				Recipe recipe = chosenRecipe(recipeCollection, pickedRecipe);
				if(recipe == null) {
					view.errorMessage("Recipe not found!");
					continue;
				}
				recipeDetailsMenu(recipe);
			}
			else if (view.wantsToViewRecipesByCalories()) {
				view.askCalories();
				int calories = view.getUserIntInput();
				view.showFilteredCaloriesScreen(recipeCollection, calories);
				view.askRecipeID();
				int pickedRecipe = view.getUserIntInput();
				Recipe recipe = chosenRecipe(recipeCollection, pickedRecipe);
				if(recipe == null) {
					view.errorMessage("Recipe not found!");
					continue;
				}
				recipeDetailsMenu(recipe);
			}
			else if (view.wantsToViewRecipesByCookingTime()) {
				view.askCookingTime();
				int cookingTime = view.getUserIntInput();
				view.showFilteredCookingTimeScreen(recipeCollection, cookingTime);
				view.askRecipeID();
				int pickedRecipe = view.getUserIntInput();
				Recipe recipe = chosenRecipe(recipeCollection, pickedRecipe);
				if(recipe == null) {
					view.errorMessage("Recipe not found!");
					continue;
				}
				recipeDetailsMenu(recipe);
			}
			else if (view.wantsToViewRecipesByDifficulty()) {
				
				recipeDifficultyFilterMenu();
			}
			else if (view.wantsToViewRecipesBySpecifiedIngredients()) {
				view.askSpecificIngredients();
				String ingredientsTogether = view.getUserStringInput();
				String[] separatedIngredient = ingredientsTogether.split(",");
				ArrayList <String> specifiedIngredients = new ArrayList<>();
				for (String s : separatedIngredient) {
					specifiedIngredients.add(s.trim());
				}
				view.writeListOfRecipesBySpecifiedIngredients(recipeCollection, specifiedIngredients);
				view.askRecipeID();
				int pickedRecipe = view.getUserIntInput();
				Recipe recipe = chosenRecipe(recipeCollection, pickedRecipe);
				if(recipe == null) {
					view.errorMessage("Recipe not found!");
					continue;
				}
				recipeDetailsMenu(recipe);
			}
			else if (view.wantsToGoBack())
				break;
			else
				view.errorMessage("Please enter a valid option");
		}
	}
	
	
	private void recipeDetailsMenu(Recipe recipe) {
		view.showARecipeScreenNoOptions(recipe);
		while(!view.wantsToGoBack()) {
			view.readMenuSelection();
			if(view.wantsToGoBack()) {
				return;
			}
			else {
				view.errorMessage("Invalid menu selection");
			}
		}
	}

	private void recipeDifficultyFilterMenu() {
		while (true) {
			view.showFilteredDifficultyScreen();
			view.readMenuSelection();

			if (view.wantsEasyDifficulty()) {
				view.writeListOfRecipesByDifficulty(recipeCollection, Difficulty.EASY);
				view.askRecipeID();
				int pickedRecipe = view.getUserIntInput();
				recipeDetailsMenu(chosenRecipe(recipeCollection, pickedRecipe));
			}
			else if (view.wantsMediumDifficulty()) {
				view.writeListOfRecipesByDifficulty(recipeCollection, Difficulty.MEDIUM);
				view.askRecipeID();
				int pickedRecipe = view.getUserIntInput();
				recipeDetailsMenu(chosenRecipe(recipeCollection, pickedRecipe));
			}
			else if (view.wantsHardDifficulty()) {
				view.writeListOfRecipesByDifficulty(recipeCollection, Difficulty.HARD);
				view.askRecipeID();
				int pickedRecipe = view.getUserIntInput();
				recipeDetailsMenu(chosenRecipe(recipeCollection, pickedRecipe));
			}
			else if (view.wantsToGoBack())
				break;
			else
				view.errorMessage("Please enter a valid option");
		}
	}

	private void ownRecipesMenu() {
		int recipeIDChoice = view.getUserIntInput();
		Recipe recipe = recipeCollection.getRecipe(recipeIDChoice);
		if(recipe == null) {
			view.errorMessage("Recipe not found!");
			return;
		}
		view.showARecipeScreenWithOptions(recipe);
		while (true) {
			view.readMenuSelection();
			if (view.wantsToDeleteOwnRecipe()) {
				view.deleteARecipeScreen();
				view.readMenuSelection();
				if (view.wantsToConfirmQuit()) {
					recipeCollection.removeRecipe(recipeIDChoice);
					view.confirmedDeletedRecipeScreen();
					view.readMenuSelection();
					break;
				}
				view.showARecipeScreenWithOptions(recipeCollection.getRecipe(recipeIDChoice));
			}
			else if (view.wantsToEditRecipe()) {
				view.errorMessage("Not Yet Implemented");
			}
			else if (view.wantsToMakePrivateOrPublic()) {
				if (recipe.isPublic()) {
					recipe.makePrivate();
					recipeCollection.updateRecipe(recipe);
					view.confirmationMessage("Recipe is now private");
				}
				else {
					recipe.makePublic();
					recipeCollection.updateRecipe(recipe);
					view.confirmationMessage("Recipe is now public");
				}
			}
			else if (view.wantsToGoBack()) {
				return;
			}
			else
				view.errorMessage("Please enter a valid option");
		}
	}

	private void allRecipesMenu() {
		int recipeIDChoice = view.getUserIntInput();
		view.showARecipeScreenNoOptions(recipeCollection.getRecipe(recipeIDChoice));
		while (true) {
			view.readMenuSelection();
			if (view.wantsToGoBack())
				return;
			else
				view.errorMessage("Please enter a valid option");
		}
	}

	private boolean isLoggedIn() {
		return loggedInUser != null;
	}
}
