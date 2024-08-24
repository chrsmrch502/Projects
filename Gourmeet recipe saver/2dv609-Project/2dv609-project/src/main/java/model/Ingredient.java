package model;

public class Ingredient {
	private int id;
	private String name;
	private int quantity;
	private String unit;

	/**
	 * Create a an ingredient with name and quantity
	 * 
	 * @param name     - The name of the ingredient.
	 * @param quantity - The quantity of the ingredient.
	 * @throws IllegalArgumentException if name or quantity are invalid
	 */
	public Ingredient(String name, int quantity) throws IllegalArgumentException{
		setName(name);
		setQuantity(quantity);
	}

	/**
	 * Create a an ingredient with name, quantity and unit.
	 * 
	 * @param name     - The name of the ingredient.
	 * @param quantity - The quantity of the ingredient.
	 * @param unit     - The unit of the ingredient.
	 * @throws IllegalArgumentException if name or quantity are invalid
	 */
	public Ingredient(String name, int quantity, String unit) throws IllegalArgumentException{
		setName(name);
		setQuantity(quantity);
		setUnit(unit);
	}

	/**
	 * Create a an ingredient id, name, quantity and unit
	 * 
	 * @param id       - The id of the ingredient.
	 * @param name     - The name of the ingredient.
	 * @param quantity - The quantity of the ingredient.
	 * @param unit     - The unit of the ingredient.
	 * @throws IllegalArgumentException if name, quantity or unit are invalid
	 */
	public Ingredient(int id, String name, int quantity, String unit) throws IllegalArgumentException{
		setId(id);
		setName(name);
		setQuantity(quantity);
		setUnit(unit);
	}

	/**
	 * Get the ingredient's id
	 * 
	 * @return The ingredient's id
	 */
	public int getId() {
		return id;
	}

	/**
	 * Set the ingredient's id
	 * 
	 * @param id - The ingredient's id
	 */
	public void setId(int id) {
		this.id = id;
	}

	/**
	 * Get the ingredient's name
	 * 
	 * @return The ingredient's name
	 */
	public String getName() {
		return name;
	}

	/**
	 * Set the ingredient's name
	 * 
	 * @param name - The ingredient's name
	 * @throws IllegalArgumentException if name is invalid
	 */
	public void setName(String name) throws IllegalArgumentException{
		if (name == null || name.length() > 50 || name.length() == 0) {
			throw new IllegalArgumentException("Ingredient name has invalid length!");
		}
		this.name = name;
	}

	/**
	 * Get the ingredient's quantity
	 * 
	 * @return The ingredient's quantity
	 */
	public int getQuantity() {
		return quantity;
	}

	/**
	 * Set the ingredient's quantity
	 * 
	 * @param quantity - The ingredient's quantity
	 * @throws IllegalArgumentException if quantity is negative
	 */
	public void setQuantity(int quantity) throws IllegalArgumentException{
		if (quantity < 0)
			throw new IllegalArgumentException("Quantity must be positive!");
		this.quantity = quantity;
	}

	/**
	 * Get the ingredient's unit
	 * 
	 * @return The ingredient's unit
	 */
	public String getUnit() {
		return unit;
	}

	/**
	 * Set the ingredient's unit
	 * 
	 * @param unit - The ingredient's unit
	 * @throws IllegalArgumentException if unit string is too long
	 */
	public void setUnit(String unit) throws IllegalArgumentException{
		if (unit != null && unit.length() > 255) {
			throw new IllegalArgumentException("Unit has invalid length!");
		}
		this.unit = unit;
	}
}
