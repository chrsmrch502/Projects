USE lnu_gourmeet;

-- Get all recipes belonging to user 'john'
SELECT *
FROM recipes
WHERE user_id IN (
	SELECT id
    FROM users
    WHERE username = 'john'
);


-- Get all recipes with difficulty level 'MEDIUM' and add the user's username as a column
SELECT
	r.id AS recipe_id,
    r.user_id,
    u.username,
    r.title,
    r.description,
    r.is_public,
    r.calories,
    r.tot_time_in_min,
    r.servings,
    r.difficulty
FROM recipes r
JOIN users u
	ON u.id = r.user_id
WHERE difficulty = 'MEDIUM';


-- Get all ingredients of the recipe with ID 4
SELECT *
FROM recipe_ingredients
WHERE recipe_id = 4;


-- Get the total number of recipes for each user
SELECT
	user_id,
	count(*) AS num_recipes
FROM recipes
GROUP BY user_id;


-- Get the total number of recipes for each user and show the username and sort in descending order
SELECT
	u.username,
	count(*) AS num_recipes
FROM recipes r
JOIN users u
	ON r.user_id = u.id
GROUP BY user_id
ORDER BY num_recipes DESC;


-- Get all steps of the recipe with ID 4 and sort by the order in ascending order
SELECT
	recipe_id,
    description,
    `order`
FROM recipe_steps
WHERE recipe_id = 4
ORDER BY `order`;


-- Get all recipes that take less than or equal to 25 minutes
SELECT *
FROM recipes
WHERE tot_time_in_min <= 25;


-- Get all recipes containing pasta or chicken
SELECT
	r.id AS recipe_id,
    r.user_id,
    r.title,
    r.description,
    r.is_public,
    r.calories,
    r.tot_time_in_min,
    r.servings,
    r.difficulty
FROM recipes r
JOIN recipe_ingredients ri
	ON r.id = ri.recipe_id
WHERE ri.name LIKE '%pasta%' OR ri.name LIKE '%chicken%'
GROUP BY r.id;



