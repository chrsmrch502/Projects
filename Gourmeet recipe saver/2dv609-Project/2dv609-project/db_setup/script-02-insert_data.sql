-- -- -- -- -- -- -- -- -- -- -- -- --
--      USE THE LNU_GOURMEET DB     --
-- -- -- -- -- -- -- -- -- -- -- -- --

USE lnu_gourmeet;


-- -- -- -- -- -- -- -- -- -- -- -- --
--           CREATE USERS           --
-- -- -- -- -- -- -- -- -- -- -- -- --

INSERT INTO `users` (username, password)
VALUES
	-- User #1 (password: lnu_password)
	('john', '$2a$10$bd6MxwVZJ1xTBcatJKxnt.wQP9qiWJQ9vkCSYOsgjnEVxL66nc1Ye'),
    -- User #2
    ('mary', '$2a$10$bd6MxwVZJ1xTBcatJKxnt.wQP9qiWJQ9vkCSYOsgjnEVxL66nc1Ye'),
    -- User #3
    ('stephen', '$2a$10$bd6MxwVZJ1xTBcatJKxnt.wQP9qiWJQ9vkCSYOsgjnEVxL66nc1Ye'),
    -- User #4
    ('charles', '$2a$10$bd6MxwVZJ1xTBcatJKxnt.wQP9qiWJQ9vkCSYOsgjnEVxL66nc1Ye'),
    -- User #5
    ('vanessa', '$2a$10$bd6MxwVZJ1xTBcatJKxnt.wQP9qiWJQ9vkCSYOsgjnEVxL66nc1Ye');


-- -- -- -- -- -- -- -- -- -- -- -- --
--           CREATE RECIPES         --
-- -- -- -- -- -- -- -- -- -- -- -- --

INSERT INTO `recipes` (
	user_id,
    title,
    description,
    is_public,
    calories,
    tot_time_in_min,
    servings,
    difficulty
) VALUES
	-- Recipe #1-2 (belonging to User #1)
	(1, 'Simple Biscuits', 'Create amazing biscuits in no time.', true, 100, 30, 10, 'EASY'),
    (1, 'Couscous with quick preserved lemon', 'The tastiest couscous dish ever.', true, 70, 30, 8, 'EASY'),
    
    -- Recipe #3 (belonging to User #2)
    (2, 'Italian Tomato Soup', 'A tomato soup that is to die for.', false, 14, 22, 4, 'MEDIUM'),
    
    -- Recipe #4-5 (belonging to User #3)
    (3, 'Thai Chicken Curry Using Roast Chicken', 'Tasty thai chicken.', true, 800, 25, 4, 'HARD'),
    (3, 'Couscous and lemon dish', 'The tastiest couscous dish ever.', true, 70, 30, 8, 'MEDIUM'),
    
    -- Recipe #6 (belonging to User #4)
    (4, 'Soup De Italia', 'A tomato soup that is to die for.', true, 14, 22, 4, 'EASY');


INSERT INTO `recipe_steps` (
	recipe_id,
    description,
    `order`
) VALUES
	-- Steps for Recipe #1
	(1, 'Preheat the oven to 190°C. Grease several baking sheets and/or line them with baking paper.', 1),
    (1, 'Cut the margarine into small pieces.', 2),
    (1, 'Wrap the dough in cling film and leave to rest and chill for 30 minutes in the fridge.', 3),
    (1, 'Dust the work surface with a little flour and roll out the dough until it is about 5mm thick. Cut out shapes with cookie cutters and place on the baking tray. Alternatively you can just roll the dough into balls and press them down lightly on the baking tray to make simple round biscuits.', 4),
    (1, 'Bake in the oven for 15-20 minutes until cooked and lightly browned. Leave the biscuits to cool on the baking tray.', 5),
    
    -- Steps for Recipe #2
    (2, 'If the couscous is instant, follow the instructions on the packet.', 1),
    (2, 'For non-instant couscous, place the couscous into a large bowl. Fill a stock pot or large pasta pot 3/4 of the way with water. Bring water to a boil. Pour the couscous into the boiling water and stir. Cook for 7 to 8 mins until the couscous is tender. Drain the couscous and toss in preserved lemon and coriander. Season with salt and pepper.', 2),
    (2, 'For the quick preserved lemon: Slice 2 lemons into 0.5cm slices, lay out on a plate and salt on 1 side. Turn over and salt the other. Stack on top of themselves and let sit for 20 mins in a baking dish. Rinse gently with cold water and dice into 1cm pieces. ', 3),
    
    -- Steps for Recipe #3
    (3, 'Warm the olive oil in a large saucepan or soup pot over a medium-high heat. Add the carrots, onion and garlic and saute until soft, about two minutes.', 1),
    (3, 'Add the jar of tomato sauce, chicken stock, cannellini beans, chilli flakes, pasta, salt and pepper. Simmer for ten minutes. Ladle into bowls and serve.', 2),
    
    -- Steps for Recipe #4
    (4, 'Cook the rice to pack instructions. Drain and set aside.', 1),
    (4, 'Meanwhile, heat the oil in a large wok or frying pan, add the peppers and onion and stir-fry for 5 minutes.', 2),
    (4, 'Stir in the curry paste and sugar and cook for 1 minute until fragrant. Pour in the coconut milk, bring to the boil and simmer for 5 minutes until the liquid is reduced by a third.', 3),
    (4, 'Add the green beans and chicken and cook gently for 5 minutes, until the chicken is piping hot.', 4),
    (4, 'Divide the rice and curry between 4 plates and serve with lime cheeks and a sprig of coriander.', 5),
    
    -- Steps for Recipe #5
    (5, 'If the couscous is instant, follow the instructions on the packet.', 1),
    (5, 'For non-instant couscous, place the couscous into a large bowl. Fill a stock pot or large pasta pot 3/4 of the way with water. Bring water to a boil. Pour the couscous into the boiling water and stir. Cook for 7 to 8 mins until the couscous is tender. Drain the couscous and toss in preserved lemon and coriander. Season with salt and pepper.', 2),
    (5, 'For the quick preserved lemon: Slice 2 lemons into 0.5cm slices, lay out on a plate and salt on 1 side. Turn over and salt the other. Stack on top of themselves and let sit for 20 mins in a baking dish. Rinse gently with cold water and dice into 1cm pieces. ', 3),
    
    -- Steps for Recipe #6
    (6, 'Warm the olive oil in a large saucepan or soup pot over a medium-high heat. Add the carrots, onion and garlic and saute until soft, about two minutes.', 1),
    (6, 'Add the jar of tomato sauce, chicken stock, cannellini beans, chilli flakes, pasta, salt and pepper. Simmer for ten minutes. Ladle into bowls and serve.', 2);


INSERT INTO `recipe_ingredients` (
	recipe_id,
    name,
    quantity,
    unit
) VALUES
	-- Ingredients for Recipe #1
    (1, 'margarine', 200, 'g'),
    (1, 'flour', 250, 'g'),
    (1, 'baking powder', 5, 'g'),
    (1, 'sugar', 100, 'g'),
    (1, 'egg yolks', 2, null),
    
    -- Ingredients for Recipe #2
    (2, 'couscous', 1, 'kg'),
    (2, 'water', 1, 'pot'),
    (2, 'coriander leaves', 35, 'g'),
    (2, 'lemons', 4, null),
    
    -- Ingredients for Recipe #3
    (3, 'olive oil', 3, 'table spoons'),
    (3, 'carrots', 2, null),
    (3, 'onions', 1, null),
    (3, 'garlic', 1, 'clove'),
    (3, 'tomato sauce', 730, 'g'),
    (3, 'chicken stock', 800, 'ml'),
    (3, 'tinned cannelli beans', 425, 'g'),
    (3, 'red peppar flakes', 1, 'table spoon'),
    (3, 'pasta', 100, 'g'),
    (3, 'salt', 1, 'table spoon'),
    (3, 'black pepper', 1, 'table spoon'),
    
    -- Ingredients for Recipe #4
    (4, 'long grain white rice', 300, 'g'),
    (4, 'sunflower oil', 1, 'table spoon'),
    (4, 'red peppers', 2, null),
    (4, 'red onion', 1, null),
    (4, 'Thai red curry paste', 3, 'table spoons'),
    (4, 'white granulated sugar', 1, 'table spoon'),
    (4, 'coconut milk', 400, 'ml'),
    (4, 'frozen whole green beans', 200, 'g'),
    (4, 'roast chicken', 300, 'g'),
    (4, 'lime cheeks', 1, null),
    (4, 'coriander', 15, 'g'),
    
    -- Ingredients for Recipe #5
    (5, 'couscous', 1, 'kg'),
    (5, 'water', 1, 'pot'),
    (5, 'coriander leaves', 35, 'g'),
    (5, 'lemons', 4, null),
    
    -- Ingredients for Recipe #6
    (6, 'olive oil', 3, 'table spoons'),
    (6, 'carrots', 2, null),
    (6, 'onions', 1, null),
    (6, 'garlic', 1, 'clove'),
    (6, 'tomato sauce', 730, 'g'),
    (6, 'chicken stock', 800, 'ml'),
    (6, 'tinned cannelli beans', 425, 'g'),
    (6, 'red peppar flakes', 1, 'table spoon'),
    (6, 'pasta', 100, 'g'),
    (6, 'salt', 1, 'table spoon'),
    (6, 'black pepper', 1, 'table spoon');


-- -- -- -- -- -- -- -- -- -- -- -- --
--     CREATE USER INGREDIENTS      --
-- -- -- -- -- -- -- -- -- -- -- -- --

INSERT INTO `user_ingredients` (
	user_id,
    name,
    quantity,
    unit
) VALUES
	-- Ingredients for User #1
	(1, 'beans', 300, 'ml'),
    (1, 'pasta', 1, 'kg'),
    (1, 'garlic', 3, null),
    
    -- Ingredients for User #2
	(2, 'couscous', 3, 'kg'),
    (2, 'lemons', 3, null),
    
    -- Ingredients for User #4
	(4, 'red pepper', 200, 'g'),
    (4, 'flour', 2, 'kg'),
    (4, 'pasta', 500, 'g'),
    
    -- Ingredients for User #5
	(5, 'coriander', 150, 'g'),
    (5, 'tomato sauce', 1, 'kg'),
    (5, 'garlic', 10, null),
    (5, 'eggs', 20, null);
    
    
    
    