package model;

import java.util.ArrayList;
import java.util.List;

public class User extends Account {
	private List<Ingredient> ingredients = new ArrayList<>();
	private RecipeCollection recipeCollection;

	/**
	 * Create a user object with userame and password
	 * 
	 * @param username - The username of the user.
	 * @param password - The password of the user.
	 * @throws IllegalArgumentException if username or password strings are not valid
	 */
	public User(String username, String password) throws IllegalArgumentException{
		super(username, password);
		// TODO Auto-generated constructor stub
	}

	/**
	 * Create a user object with id, userame and password
	 * 
	 * @param id       - The id of the user.
	 * @param username - The username of the user.
	 * @param password - The password of the user.
	 * @throws IllegalArgumentException if username or password strings are not valid
	 */
	public User(int id, String username, String password) throws IllegalArgumentException{
		super(id, username, password);
	}

	/**
	 * Set the recipe collection object of the user
	 * 
	 * @param recipeCollection - The recipeCollecton provided to the user to make
	 *                         queries
	 */
	public void setRecipeCollection(RecipeCollection recipeCollection) {
		this.recipeCollection = recipeCollection;
	}

	/**
	 * Get the users recipes
	 * 
	 * @return The list of all the user's recipes
	 */
	public List<Recipe> getRecipes() {
		return recipeCollection.browseByUser(this.getUsername());
	}

	/**
	 * Get the users ingredients
	 * 
	 * @return The list of all the user's ingredients
	 */
	public List<Ingredient> getStoredIngredients() {
		return new ArrayList<>(ingredients);
	}

	/**
	 * Get the users ingredients' names
	 * 
	 * @return The list of the names of all stored ingredients
	 */
	public List<String> getStoredIngredientNames() {
		List<String> names = new ArrayList<>();
		for (Ingredient ingredient : ingredients)
			names.add(ingredient.getName());

		return names;
	}

	/**
	 * Add an ingredient to the user
	 * 
	 * @param ingredient - The ingredient to be added
	 */
	public void addStoredIngredient(Ingredient ingredient) {
		ingredients.add(ingredient);
	}

	/**
	 * Remove an ingredient from the user's stored ingredients.
	 *
	 * @param id - The id of the ingredient.
	 */
	public void removeStoredIngredient(int id) {
		for (int i = 0; i < ingredients.size(); i++) {
			if (ingredients.get(i).getId() == id) {
				ingredients.remove(i);
				return;
			}
		}
	}

	/**
	 * returns a string representation of the user
	 * 
	 * @return string representation of user of the form: "User ID: [ID]; Username: [username]"
	 */
	@Override
	public String toString() {
		return "User ID: " + getId() + "; Username: " + getUsername();
	}
}
