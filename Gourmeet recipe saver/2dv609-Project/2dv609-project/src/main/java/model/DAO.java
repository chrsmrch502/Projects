package model;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class DAO {
  private final String DB_NAME = "lnu_gourmeet";
  private final String DB_TABLE_USERS = "users";
  private final String DB_TABLE_RECIPES = "recipes";
  private final String DB_TABLE_RECIPE_STEPS = "recipe_steps";
  private final String DB_TABLE_RECIPE_INGREDIENTS = "recipe_ingredients";
  private final String DB_TABLE_USER_INGREDIENTS = "user_ingredients";
  private final String DB_URL_OPTIONS = "rewriteBatchedStatements=true&useUnicode=true&useJDBCCompliantTimezoneShift=true&useLegacyDatetimeCode=false&serverTimezone=UTC";
  private String dbHost;
  private int dbPort;
  private String dbUser;
  private String dbPassword;
  private Connection connection;
  private PreparedStatement statement;
  private ResultSet res;

  /**
   * Create a data access object using MySQL default settings.
   * (host = localhost, port = 3306, user = root)
   *
   * @param dbPassword - The password for the database connection.
   */
  public DAO(String dbPassword) {
    final String DEFAULT_DB_HOST = "localhost";
    final int DEFAULT_DB_PORT = 3306;
    final String DEFAULT_DB_USER = "root";

    this.dbHost = DEFAULT_DB_HOST;
    this.dbPort = DEFAULT_DB_PORT;
    this.dbUser = DEFAULT_DB_USER;
    this.dbPassword = dbPassword;
  }

  /**
   * Create a data access object.
   *
   * @param dbHost - The database host.
   * @param dbPort - The database port.
   * @param dbUser - The database user.
   * @param dbPassword - The database password.
   */
  public DAO(String dbHost, int dbPort, String dbUser, String dbPassword) {
    this.dbHost = dbHost;
    this.dbPort = dbPort;
    this.dbUser = dbUser;
    this.dbPassword = dbPassword;
  }

  /**
   * Get a user from the database.
   *
   * @param username - The username.
   * @return The user with id, username, and password.
   */
  public User getUser(String username) {
    if (username == null)
      throw new IllegalArgumentException("Username must contain at least 1 character.");

    String sql = "SELECT id, username, password FROM " + DB_TABLE_USERS + " WHERE username = ?";

    try {
      connection = connect();
      statement = connection.prepareStatement(sql);
      statement.setString(1, username);

      res = statement.executeQuery();
      if (res.next())
        return new User(
                res.getInt("id"),
                res.getString("username"),
                res.getString("password")
        );

      return null;
    }
    catch (SQLException e) {
      throw new RuntimeException(e);
    }
    finally {
      close();
    }
  }

  /**
   * Get a user from the database.
   *
   * @param username - The username.
   * @return The user with all attributes.
   */
  public User getFullUser(String username) {
    User user = getUser(username);
    if (user == null)
      return null;

    List<Ingredient> storedIngredients = getUserIngredients(user.getId());
    for (Ingredient ingredient : storedIngredients)
      user.addStoredIngredient(ingredient);

    return user;
  }

  /**
   * Add a user to the database.
   *
   * @param user - The user to be added.
   */
  public void addUser(User user) {
    if (user == null)
      throw new IllegalArgumentException("Argument must be a valid user.");

    if (userExists(user.getUsername()))
      throw new IllegalStateException("The username is already taken.");  // TODO: create custom exception?

    String sql = "INSERT INTO " + DB_TABLE_USERS + " (username, password) VALUES (?, ?)";

    try {
      connection = connect();
      statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
      statement.setString(1, user.getUsername());
      statement.setString(2, user.getPassword());

      statement.executeUpdate();

      res = statement.getGeneratedKeys();
      if (res.next())
        user.setId(res.getInt(1));
    }
    catch (SQLException e) {
      throw new RuntimeException(e);
    }
    finally {
      close();
    }
  }

  /**
   * Add a user ingredient to the database.
   *
   * @param ingredient - The ingredient.
   * @param username - The username.
   */
  public void addUserIngredient(Ingredient ingredient, String username) {
    if (ingredient == null)
      throw new IllegalArgumentException("Ingredient must be a valid ingredient.");

    User user = getUser(username);
    if (user == null)
      throw new IllegalStateException("The user with the given username does not exist.");

    String sql = "INSERT INTO " + DB_TABLE_USER_INGREDIENTS + " (user_id, name, quantity, unit) VALUES (?, ?, ?, ?)";

    try {
      connection = connect();
      statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
      statement.setInt(1, user.getId());
      statement.setString(2, ingredient.getName());
      statement.setInt(3, ingredient.getQuantity());
      statement.setString(4, ingredient.getUnit());

      statement.executeUpdate();

      res = statement.getGeneratedKeys();
      if (res.next())
        ingredient.setId(res.getInt(1));
    }
    catch (SQLException e) {
      throw new RuntimeException(e);
    }
    finally {
      close();
    }
  }

  /**
   * Delete a user ingredient from the database.
   *
   * @param id - The id of the user ingredient.
   */
  public void deleteUserIngredient(int id) {
    deleteById("user_ingredients", id);
  }

  /**
   * Get a user's recipe IDs from the database.
   *
   * @return The list of recipe IDs.
   */
  public List<Integer> getUserRecipeIds(String username) {
    User user = getUser(username);
    if (user == null)
      throw new IllegalStateException("The user with the given username does not exist.");

    String sql = "SELECT id FROM " + DB_TABLE_RECIPES + " WHERE user_id = ?";

    try {
      connection = connect();
      statement = connection.prepareStatement(sql);
      statement.setInt(1, user.getId());

      res = statement.executeQuery();

      List<Integer> ids = new ArrayList<>();
      while (res.next())
        ids.add(res.getInt("id"));

      return ids;
    }
    catch (SQLException e) {
      throw new RuntimeException(e);
    }
    finally {
      close();
    }
  }

  /**
   * Get all recipes from the database.
   *
   * @return The list of recipes.
   */
  public List<Recipe> getAllRecipes() {
    String sql = "SELECT * FROM " + DB_TABLE_RECIPES;

    Connection localConnection = null;
    PreparedStatement localStatement = null;
    ResultSet localRes = null;

    try {
      localConnection = connect();
      localStatement = localConnection.prepareStatement(sql);

      localRes = localStatement.executeQuery();

      List<Recipe> recipes = new ArrayList<>();
      while (localRes.next())
        recipes.add(new Recipe(
                localRes.getInt("id"),
                localRes.getString("title"),
                localRes.getString("description"),
                localRes.getBoolean("is_public"),
                localRes.getInt("calories"),
                localRes.getInt("tot_time_in_min"),
                localRes.getInt("servings"),
                getRecipeIngredients(localRes.getInt("id")),
                getRecipeSteps(localRes.getInt("id")),
                Difficulty.valueOf(localRes.getString("difficulty"))
        ));

      return recipes;
    }
    catch (SQLException e) {
      throw new RuntimeException(e);
    }
    finally {
      close(localRes, localStatement, localConnection);
    }
  }

  private List<RecipeStep> getRecipeSteps(int recipeId) {
    String sql = "SELECT * FROM " + DB_TABLE_RECIPE_STEPS + " WHERE recipe_id = ? ORDER BY `order`";

    try {
      connection = connect();
      statement = connection.prepareStatement(sql);
      statement.setInt(1, recipeId);

      res = statement.executeQuery();

      List<RecipeStep> steps = new ArrayList<>();
      while (res.next())
        steps.add(new RecipeStep(
                res.getInt("id"),
                res.getString("description"),
                res.getInt("order")
        ));

      return steps;
    }
    catch (SQLException e) {
      throw new RuntimeException(e);
    }
    finally {
      close();
    }
  }

  private List<Ingredient> getRecipeIngredients(int recipeId) {
    return getIngredients(DB_TABLE_RECIPE_INGREDIENTS, "recipe_id", recipeId);
  }

  private List<Ingredient> getUserIngredients(int userId) {
    return getIngredients(DB_TABLE_USER_INGREDIENTS, "user_id", userId);
  }

  private List<Ingredient> getIngredients(String table, String column, int id) {
    String sql = "SELECT * FROM " + table + " WHERE " + column + " = ?";

    try {
      connection = connect();
      statement = connection.prepareStatement(sql);
      statement.setInt(1, id);

      res = statement.executeQuery();

      List<Ingredient> ingredients = new ArrayList<>();
      while (res.next())
        ingredients.add(new Ingredient(
                res.getInt("id"),
                res.getString("name"),
                res.getInt("quantity"),
                res.getString("unit")
        ));

      return ingredients;
    }
    catch (SQLException e) {
      throw new RuntimeException(e);
    }
    finally {
      close();
    }
  }

  /**
   * Delete a recipe from the database.
   *
   * @param id - The recipe id.
   */
  public void deleteRecipe(int id) {
    deleteById(DB_TABLE_RECIPES, id);
  }

  /**
   * Add a recipe to the database.
   *
   * @param recipe - The recipe.
   * @param username - The username of the owner of the recipe.
   */
  public void addRecipe(Recipe recipe, String username) {
    if (recipe == null)
      throw new IllegalArgumentException("Recipe must be a valid recipe.");

    User user = getUser(username);
    if (user == null)
      throw new IllegalStateException("The user with the given username does not exist.");

    String sql = "INSERT INTO " + DB_TABLE_RECIPES + " (user_id, title, description, is_public, calories, tot_time_in_min, servings, difficulty) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

    Connection localConnection = null;
    PreparedStatement localStatement = null;
    ResultSet localRes = null;

    try {
      localConnection = connect();
      localStatement = localConnection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
      localStatement.setInt(1, user.getId());
      localStatement.setString(2, recipe.getTitle());
      localStatement.setString(3, recipe.getDescription());
      localStatement.setBoolean(4, recipe.isPublic());
      localStatement.setInt(5, recipe.getCalories());
      localStatement.setInt(6, recipe.getTotTimeInMin());
      localStatement.setInt(7, recipe.getServings());
      localStatement.setString(8, recipe.getDifficulty().name());

      localStatement.executeUpdate();

      localRes = localStatement.getGeneratedKeys();
      if (!localRes.next())
        return;

      int recipeId = localRes.getInt(1);
      recipe.setId(recipeId);
      addRecipeSteps(recipe.getSteps(), recipeId);
      addRecipeIngredients(recipe.getIngredients(), recipeId);
    }
    catch (SQLException e) {
      throw new RuntimeException(e);
    }
    finally {
      close(localRes, localStatement, localConnection);
    }
  }

  private void addRecipeSteps(List<RecipeStep> steps, int recipeId) {
    if (steps == null)
      return;

    String sql = "INSERT INTO " + DB_TABLE_RECIPE_STEPS + " (recipe_id, description, `order`) VALUES (?, ?, ?)";

    try {
      connection = connect();
      statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);

      int count = 0;
      for (RecipeStep step : steps) {
        statement.setInt(1, recipeId);
        statement.setString(2, step.getDescription());
        statement.setInt(3, step.getOrder());
        statement.addBatch();

        count++;
        if (shouldExecuteBatch(count, steps.size()))
          statement.executeBatch();
      }

      res = statement.getGeneratedKeys();

      // set the ids on the ingredients
      int idx = 0;
      while (res.next())
        steps.get(idx++).setId(res.getInt(1));
    }
    catch (SQLException e) {
      throw new RuntimeException(e);
    }
    finally {
      close();
    }
  }

  private void addRecipeIngredients(List<Ingredient> ingredients, int recipeId) {
    if (ingredients == null)
      return;

    String sql = "INSERT INTO " + DB_TABLE_RECIPE_INGREDIENTS + " (recipe_id, name, quantity, unit) VALUES (?, ?, ?, ?)";

    try {
      connection = connect();
      statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);

      int count = 0;
      for (Ingredient ingredient : ingredients) {
        statement.setInt(1, recipeId);
        statement.setString(2, ingredient.getName());
        statement.setInt(3, ingredient.getQuantity());
        statement.setString(4, ingredient.getUnit());
        statement.addBatch();

        count++;
        if (shouldExecuteBatch(count, ingredients.size()))
          statement.executeBatch();
      }

      res = statement.getGeneratedKeys();

      // set the ids on the ingredients
      int idx = 0;
      while (res.next())
        ingredients.get(idx++).setId(res.getInt(1));
    }
    catch (SQLException e) {
      throw new RuntimeException(e);
    }
    finally {
      close();
    }
  }

  /**
   * Update a recipe in the database.
   *
   * @param recipe - The updated recipe.
   */
  public void updateRecipe(Recipe recipe) {
    if (recipe == null)
      throw new IllegalArgumentException("Recipe must be a valid recipe.");

    String sql = "UPDATE " + DB_TABLE_RECIPES + " SET title = ?, description = ?, is_public = ?, calories = ?, tot_time_in_min = ?, servings = ?, difficulty = ? WHERE id = ?";

    Connection localConnection = null;
    PreparedStatement localStatement = null;
    ResultSet localRes = null;

    try {
      localConnection = connect();
      localStatement = localConnection.prepareStatement(sql);
      localStatement.setString(1, recipe.getTitle());
      localStatement.setString(2, recipe.getDescription());
      localStatement.setBoolean(3, recipe.isPublic());
      localStatement.setInt(4, recipe.getCalories());
      localStatement.setInt(5, recipe.getTotTimeInMin());
      localStatement.setInt(6, recipe.getServings());
      localStatement.setString(7, recipe.getDifficulty().name());
      localStatement.setInt(8, recipe.getId());

      localStatement.executeUpdate();

      updateRecipeSteps(recipe.getSteps());
      updateRecipeIngredients(recipe.getIngredients());
    }
    catch (SQLException e) {
      throw new RuntimeException(e);
    }
    finally {
      close(localRes, localStatement, localConnection);
    }
  }

  private void updateRecipeSteps(List<RecipeStep> steps) {
    if (steps == null)
      return;

    String sql = "UPDATE " + DB_TABLE_RECIPE_STEPS + " SET description = ?, `order` = ? WHERE id = ?";

    try {
      connection = connect();
      statement = connection.prepareStatement(sql);

      int count = 0;
      for (RecipeStep step : steps) {
        statement.setString(1, step.getDescription());
        statement.setInt(2, step.getOrder());
        statement.setInt(3, step.getId());
        statement.addBatch();

        count++;
        if (shouldExecuteBatch(count, steps.size()))
          statement.executeBatch();
      }
    }
    catch (SQLException e) {
      throw new RuntimeException(e);
    }
    finally {
      close();
    }
  }

  private void updateRecipeIngredients(List<Ingredient> ingredients) {
    if (ingredients == null)
      return;

    String sql = "UPDATE " + DB_TABLE_RECIPE_INGREDIENTS + " SET name = ?, quantity = ?, unit = ? WHERE id = ?";

    try {
      connection = connect();
      statement = connection.prepareStatement(sql);

      int count = 0;
      for (Ingredient ingredient : ingredients) {
        statement.setString(1, ingredient.getName());
        statement.setInt(2, ingredient.getQuantity());
        statement.setString(3, ingredient.getUnit());
        statement.setInt(4, ingredient.getId());
        statement.addBatch();

        count++;
        if (shouldExecuteBatch(count, ingredients.size()))
          statement.executeBatch();
      }
    }
    catch (SQLException e) {
      throw new RuntimeException(e);
    }
    finally {
      close();
    }
  }

  private void deleteById(String table, int id) {
    String sql = "DELETE FROM " + table + " WHERE id = ?";

    try {
      connection = connect();
      statement = connection.prepareStatement(sql);
      statement.setInt(1, id);

      statement.executeUpdate();
    }
    catch (SQLException e) {
      throw new RuntimeException(e);
    }
    finally {
      close();
    }
  }

  private boolean shouldExecuteBatch(int currentCount, int totalCount) {
    return hasReachedBatchLimit(currentCount) || currentCount == totalCount;
  }

  private boolean hasReachedBatchLimit(int currentCount) {
    final int BATCH_LIMIT = 1000;

    return currentCount % BATCH_LIMIT == 0;
  }

  /**
   * Check if a user exists in the database.
   *
   * @param username - The username.
   * @return Whether or not it exists.
   */
  public boolean userExists(String username) {
    return getUser(username) != null;
  }

  /**
   * Get the number of total recipes in the database.
   *
   * @return The count.
   */
  public int countRecipes() {
    String sql = "SELECT COUNT(*) AS count FROM " + DB_TABLE_RECIPES;

    try {
      connection = connect();
      statement = connection.prepareStatement(sql);

      res = statement.executeQuery();

      return res.next() ? res.getInt("count") : 0;
    }
    catch (SQLException e) {
      throw new RuntimeException(e);
    }
    finally {
      close();
    }
  }

  private Connection connect() throws SQLException {
    String dbUrl = "jdbc:mysql://" + dbHost + ":" + dbPort + "/" + DB_NAME + "?" + DB_URL_OPTIONS;

    return DriverManager.getConnection(dbUrl, dbUser, dbPassword);
  }

  private void close() {
    close(res, statement, connection);

    res = null;
    statement = null;
    connection = null;
  }

  private void close(ResultSet res, Statement statement, Connection connection) {
    try {
      if (res != null)
        res.close();

      if (statement != null)
        statement.close();

      if (connection != null)
        connection.close();
    }
    catch (SQLException e) {
      throw new RuntimeException(e);
    }
  }
}
