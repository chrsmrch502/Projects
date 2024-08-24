package model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Recipe {
	private int id;
	private String title;
	private String description;
	private boolean isPublic;
	private int calories;
	private int totTimeInMin;
	private int servings;
	private List<Ingredient> ingredients;
	private List<RecipeStep> steps;
	private Difficulty difficulty;

	/**
	 * Create a Recipe object with id, title, description, public, calories,
	 * totTimeInMin, servings, ingredients, steps and difficulty
	 * 
	 * @param id           - The id of the recipe.
	 * @param title        - The title of the recipe.
	 * @param description  - The description of the recipe.
	 * @param isPublic     - boolean for visibility of recipe (True is public, False
	 *                     is private)
	 * @param calories     - The number of calories of the recipe.
	 * @param totTimeInMin - The total time required of the recipe.
	 * @param servings     - The number of servings of the recipe.
	 * @param ingredients  - The ingredients used in the recipe.
	 * @param steps        - The steps of the recipe.
	 * @param difficulty   - The difficulty of the recipe.
	 * @throws IllegalArgumentException if title, descriptions, calories or servings
	 *                                  are invalid
	 */
	public Recipe(int id, String title, String description, boolean isPublic, int calories, int totTimeInMin,
			int servings, List<Ingredient> ingredients, List<RecipeStep> steps, Difficulty difficulty)
			throws IllegalArgumentException {
		setId(id);
		setTitle(title);
		setDescription(description);
		this.isPublic = isPublic;
		setCalories(calories);
		setTotTimeInMin(totTimeInMin);
		setServings(servings);
		setIngredients(ingredients);
		setSteps(steps);
		setDifficulty(difficulty);
	}

	/**
	 * Create a Recipe object with title, description, public, calories,
	 * totTimeInMin, servings, ingredients, steps and difficulty
	 * 
	 * @param title        - The title of the recipe.
	 * @param description  - The description of the recipe.
	 * @param isPublic     - boolean for visibility of recipe (True is public, False
	 *                     is private)
	 * @param calories     - The number of calories of the recipe.
	 * @param totTimeInMin - The total time required of the recipe.
	 * @param servings     - The number of servings of the recipe.
	 * @param ingredients  - The ingredients used in the recipe.
	 * @param steps        - The steps of the recipe.
	 * @param difficulty   - The difficulty of the recipe.
	 * @throws IllegalArgumentException if title, descriptions, calories or servings
	 *                                  are invalid
	 */
	public Recipe(String title, String description, boolean isPublic, int calories, int totTimeInMin, int servings,
			List<Ingredient> ingredients, List<RecipeStep> steps, Difficulty difficulty)
			throws IllegalArgumentException {
		setTitle(title);
		setDescription(description);
		this.isPublic = isPublic;
		setCalories(calories);
		setTotTimeInMin(totTimeInMin);
		setServings(servings);
		setIngredients(ingredients);
		setSteps(steps);
		setDifficulty(difficulty);
	}

	/**
	 * Returns the id of the recipe
	 * 
	 * @return the id of the recipe
	 */
	public int getId() {
		return id;
	}

	/**
	 * Set the id of the recipe
	 * 
	 * @param id - the id of the recipe
	 */
	public void setId(int id) {
		this.id = id;
	}

	/**
	 * Returns the visibility of the recipe
	 * 
	 * @return the visibility of the recipe
	 */
	public boolean isPublic() {
		return this.isPublic;
	}

	/**
	 * Makes the recipe public
	 */
	public void makePublic() {
		this.isPublic = true;
	}

	/**
	 * Makes the recipe private
	 */
	public void makePrivate() {
		this.isPublic = false;
	}

	/**
	 * Returns the title of the recipe
	 * 
	 * @return the title of the recipe
	 */
	public String getTitle() {
		return title;
	}

	/**
	 * Set the title of the recipe
	 * 
	 * @param title - the id of the recipe
	 * @throws IllegalArgumentException if the title is longer than 50 or empty
	 */
	public void setTitle(String title) throws IllegalArgumentException {
		if (title == null || title.length() > 50 || title.length() == 0)
			throw new IllegalArgumentException("Recipe title is invalid!");
		this.title = title;
	}

	/**
	 * Returns the description of the recipe
	 * 
	 * @return the description of the recipe
	 */
	public String getDescription() {
		return description;
	}

	/**
	 * Set the description of the recipe
	 * 
	 * @param description - the id of the recipe
	 * @throws IllegalArgumentException if the description is longer than 1024 or
	 *                                  empty
	 */
	public void setDescription(String description) throws IllegalArgumentException {
		if (description == null || description.length() > 1024 || description.length() == 0)
			throw new IllegalArgumentException("Recipe description is invalid!");
		this.description = description;
	}

	/**
	 * Returns the calories of the recipe
	 * 
	 * @return the calories of the recipe
	 */
	public int getCalories() {
		return calories;
	}

	/**
	 * Set the calories of the recipe
	 * 
	 * @param calories - the id of the recipe
	 * @throws IllegalArgumentException if the calories is negative
	 */
	public void setCalories(int calories) throws IllegalArgumentException {
		if (calories < 0)
			throw new IllegalArgumentException("Calories amount cannot be negative!");
		this.calories = calories;
	}

	/**
	 * Returns the total time of the recipe
	 * 
	 * @return the total time of the recipe
	 */
	public int getTotTimeInMin() {
		return totTimeInMin;
	}

	/**
	 * Set the total time of the recipe
	 * 
	 * @param totTimeInMin - the total time of the recipe
	 */
	public void setTotTimeInMin(int totTimeInMin) {
		this.totTimeInMin = totTimeInMin;
	}

	/**
	 * Returns the number of servings of the recipe
	 * 
	 * @return the number of servings of the recipe
	 */
	public int getServings() {
		return servings;
	}

	/**
	 * Set the servings of the recipe
	 * 
	 * @param servings - the id of the recipe
	 * @throws IllegalArgumentException if the servings is negative
	 */
	public void setServings(int servings) throws IllegalArgumentException {
		if (servings < 0)
			throw new IllegalArgumentException("Number of servings cannot be negative!");
		this.servings = servings;
	}

	/**
	 * Returns the ingredients of the recipe
	 * 
	 * @return the ingredients of the recipe
	 */
	public List<Ingredient> getIngredients() {
		return new ArrayList<>(ingredients);
	}

	/**
	 * Set the ingredients of the recipe
	 * 
	 * @param ingredients - the ingredients of the recipe
	 */
	public void setIngredients(List<Ingredient> ingredients) {
		this.ingredients = ingredients;
	}

	/**
	 * Returns the steps of the recipe
	 * 
	 * @return the steps of the recipe
	 */
	public List<RecipeStep> getSteps() {
		Collections.sort(steps);

		return new ArrayList<>(steps);
	}

	/**
	 * Set the steps of the recipe
	 * 
	 * @param steps - the steps of the recipe
	 */
	public void setSteps(List<RecipeStep> steps) {
		this.steps = steps;
	}

	/**
	 * Returns the difficulty of the recipe
	 * 
	 * @return the difficulty of the recipe
	 */
	public Difficulty getDifficulty() {
		return difficulty;
	}

	/**
	 * Set the difficulty of the recipe
	 * 
	 * @param difficulty - the difficulty of the recipe
	 */
	public void setDifficulty(Difficulty difficulty) {
		this.difficulty = difficulty;
	}

	/**
	 * Returns the number of ingredients of the recipe
	 * 
	 * @return the number of ingredients of the recipe
	 */
	public int getNumIngredients() {
		return ingredients.size();
	}
}
