package model;

public class RecipeStep implements Comparable<RecipeStep> {
	private int id;
	private String description;
	private int order;

	/**
	 * Create a recipe step object with id, description and order
	 * 
	 * @param id          - The id of the step.
	 * @param description - The description of the step.
	 * @param order       - The order of the step.
	 * @throws IllegalArgumentException if description is invalid
	 */
	public RecipeStep(int id, String description, int order) throws IllegalArgumentException{
		setId(id);
		setDescription(description);
		setOrder(order);
	}

	/**
	 * Create a recipe step object with description and order
	 * 
	 * @param description - The description of the step.
	 * @param order       - The order of the step.
	 * @throws IllegalArgumentException if description is invalid
	 */
	public RecipeStep(String description, int order) throws IllegalArgumentException{
		setDescription(description);
		setOrder(order);
	}

	/**
	 * Gets the id of the step
	 * 
	 * @return The id of the step
	 */
	public int getId() {
		return id;
	}

	/**
	 * Sets the id of the step
	 * 
	 * @param id - the id of the step
	 */
	public void setId(int id) {
		this.id = id;
	}

	/**
	 * Gets the description of the step
	 * 
	 * @return The description of the step
	 */
	public String getDescription() {
		return description;
	}

	/**
	 * Sets the description of the step
	 * 
	 * @param description - the description of the step
	 */
	public void setDescription(String description) throws IllegalArgumentException{
		if (description == null || description.length() > 1024 || description.length() == 0) {
			throw new IllegalArgumentException("Description has invalid length!");
		}
		this.description = description;
	}

	/**
	 * Gets the order of the step
	 * 
	 * @return The order of the step
	 */
	public int getOrder() {
		return order;
	}

	/**
	 * Sets the order of the step
	 * 
	 * @param order - the order of the step
	 */
	public void setOrder(int order) {
		this.order = order;
	}

	@Override
	public int compareTo(RecipeStep other) {
		return order - other.order;
	}
}
