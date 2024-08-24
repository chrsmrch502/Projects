package model;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RecipeCollection {
	private List<Recipe> allRecipes;
	private List<Recipe> publicRecipes;
	private DAO dao;

	/**
	 * Create a recipe collection.
	 * 
	 * @param dao - The data access object.
	 */
	public RecipeCollection(DAO dao){
		this.dao = dao;
		populateCollection();
	}

	private void populateCollection(){
		allRecipes = dao.getAllRecipes();
		publicRecipes = new ArrayList<>();

		for (Recipe recipe : allRecipes)
			if (recipe.isPublic())
				publicRecipes.add(recipe);
	}

	/**
	 * Get all public recipes.
	 *
	 * @return The recipes.
	 */
	public List<Recipe> browseAll() {
		return new ArrayList<>(publicRecipes);
	}

	/**
	 * Get a user's recipes.
	 *
	 * @param username - The username of the user.
	 * @return The user's recipes.
	 */
	public List<Recipe> browseByUser(String username) {
		List<Integer> recipeIds = dao.getUserRecipeIds(username);
		List<Recipe> result = new ArrayList<>();

		for (Recipe recipe : allRecipes)
			if (recipeIds.contains(recipe.getId()))
				result.add(recipe);

		return result;
	}

	/**
	 * Get all public recipes with a certain difficulty.
	 *
	 * @param difficulty - The difficulty level.
	 * @return The recipes.
	 */
	public List<Recipe> browseByDifficulty(Difficulty difficulty) {
		List<Recipe> result = new ArrayList<>();

		for (Recipe recipe : publicRecipes)
			if (recipe.getDifficulty().equals(difficulty))
				result.add(recipe);

		return result;
	}

	/**
	 * Get all public recipes containing a maximum number of calories.
	 *
	 * @param maxCalories - The maximum calories allowed.
	 * @return The recipes.
	 */
	public List<Recipe> browseByMaxCalories(int maxCalories) {
		List<Recipe> result = new ArrayList<>();

		for (Recipe recipe : publicRecipes)
			if (recipe.getCalories() <= maxCalories)
				result.add(recipe);

		return result;
	}

	/**
	 * Get all public recipes taking no longer than a certain number of minutes.
	 *
	 * @param min - The maximum cooking time in minutes.
	 * @return The recipes.
	 */
	public List<Recipe> browseByTotTimeInMin(int min) {
		List<Recipe> result = new ArrayList<>();

		for (Recipe recipe : publicRecipes)
			if (recipe.getTotTimeInMin() <= min)
				result.add(recipe);

		return result;
	}

	/**
	 * Get all public recipes having at least the specified ingredients.
	 *
	 * @param ingredients - The names of the ingredients.
	 * @return The recipes.
	 */
	public List<Recipe> browseByIngredients(List<String> ingredients) {
		List<Recipe> result = new ArrayList<>();

		for (Recipe recipe : publicRecipes) {
			if (ingredients.size() > recipe.getNumIngredients())
				continue;

			int numIngredientsFound = 0;
			boolean allIngredientsFound = false;

			for (Ingredient existingIngredient : recipe.getIngredients()) {
				for (String wantedIngredient : ingredients) {
					Pattern toSearchFor = Pattern.compile(wantedIngredient, Pattern.CASE_INSENSITIVE);
					Matcher searchable = toSearchFor.matcher(existingIngredient.getName());
					if (searchable.find()) {
						allIngredientsFound = ++numIngredientsFound == ingredients.size();
						break;
					}
				}

				if (allIngredientsFound) {
					result.add(recipe);
					break;
				}
			}
		}

		return result;
	}

	/**
	 * Add a recipe to the collection.
	 *
	 * @param recipe   - The recipe to add.
	 * @param username - The username of the recipe owner.
	 */
	public void addRecipe(Recipe recipe, String username) {
		dao.addRecipe(recipe, username);
		allRecipes.add(recipe);
		if (recipe.isPublic())
			publicRecipes.add(recipe);
	}

	/**
	 * Get a recipe from the collection.
	 *
	 * @param id - The recipe id.
	 * @return The recipe.
	 */
	public Recipe getRecipe(int id) {
		for (Recipe recipe : allRecipes)
			if (recipe.getId() == id)
				return recipe;

		return null;
	}

	/**
	 * Update a recipe.
	 *
	 * @param updatedRecipe - The updated recipe.
	 */
	public void updateRecipe(Recipe updatedRecipe) {
		dao.updateRecipe(updatedRecipe);

		if (updatedRecipe.isPublic()) {
			if (!publicRecipes.contains(updatedRecipe))
				publicRecipes.add(updatedRecipe);
		} else {
			if (publicRecipes.contains(updatedRecipe))
				publicRecipes.remove(updatedRecipe);
		}
	}

	/**
	 * Remove a recipe.
	 *
	 * @param id - The recipe id.
	 */
	public void removeRecipe(int id) {
		dao.deleteRecipe(id);

		for (int i = 0; i < allRecipes.size(); i++) {
			if (allRecipes.get(i).getId() == id) {
				allRecipes.remove(i);
				break;
			}
		}

		for (int i = 0; i < publicRecipes.size(); i++) {
			if (publicRecipes.get(i).getId() == id) {
				publicRecipes.remove(i);
				break;
			}
		}
	}
}
