-- -- -- -- -- -- -- -- -- -- -- -- --
--   CREATE DATABSE: LNU_GOURMEET   --
-- -- -- -- -- -- -- -- -- -- -- -- --

CREATE DATABASE IF NOT EXISTS `lnu_gourmeet`;
USE `lnu_gourmeet`;


-- -- -- -- -- -- -- -- -- -- -- -- --
--           DROP TABLES            --
-- -- -- -- -- -- -- -- -- -- -- -- --

SET FOREIGN_KEY_CHECKS = 0;
DROP TABLE IF EXISTS `user_ingredients`;
DROP TABLE IF EXISTS `recipe_ingredients`;
DROP TABLE IF EXISTS `recipe_steps`;
DROP TABLE IF EXISTS `recipes`;
DROP TABLE IF EXISTS `users`;
SET FOREIGN_KEY_CHECKS = 1;


-- -- -- -- -- -- -- -- -- -- -- -- --
--        CREATE TABLE: USERS       --
-- -- -- -- -- -- -- -- -- -- -- -- --

CREATE TABLE `users` (
    `id` int AUTO_INCREMENT PRIMARY KEY,
    `username` varchar(50) NOT NULL UNIQUE,
    `password` binary(60) NOT NULL
);


-- -- -- -- -- -- -- -- -- -- -- -- -- --
--    CREATE TABLE: USER_INGREDIENTS   --
-- -- -- -- -- -- -- -- -- -- -- -- -- --

CREATE TABLE `user_ingredients` (
    `id` int AUTO_INCREMENT PRIMARY KEY,
    `user_id` int NOT NULL,
    CONSTRAINT `fk_ingredient_user`
		FOREIGN KEY (user_id)
		REFERENCES users(id)
        ON UPDATE CASCADE
        ON DELETE CASCADE,
    `name` varchar(50) NOT NULL,
    `quantity` smallint NOT NULL,
    `unit` varchar(50)
);


-- -- -- -- -- -- -- -- -- -- -- -- --
--      CREATE TABLE: RECIPES       --
-- -- -- -- -- -- -- -- -- -- -- -- --

CREATE TABLE `recipes` (
    `id` int AUTO_INCREMENT PRIMARY KEY,
    `user_id` int NOT NULL,
    CONSTRAINT `fk_recipe_user`
		FOREIGN KEY (user_id)
		REFERENCES users(id)
        ON UPDATE CASCADE
        ON DELETE CASCADE,
    `title` varchar(50) NOT NULL,
    `description` varchar(1024) NOT NULL,
	`is_public` bool DEFAULT false,
    `calories` mediumint,
    `tot_time_in_min` int NOT NULL,
    `servings` tinyint NOT NULL,
    `difficulty` enum('EASY', 'MEDIUM', 'HARD') NOT NULL
);


-- -- -- -- -- -- -- -- -- -- -- -- --
--    CREATE TABLE: RECIPE_STEPS    --
-- -- -- -- -- -- -- -- -- -- -- -- --

CREATE TABLE `recipe_steps` (
    `id` int AUTO_INCREMENT PRIMARY KEY,
    `recipe_id` int NOT NULL,
    CONSTRAINT `fk_step_recipe`
		FOREIGN KEY (recipe_id)
		REFERENCES recipes(id)
        ON UPDATE CASCADE
        ON DELETE CASCADE,
    `description` varchar(1024) NOT NULL,
    `order` tinyint NOT NULL
);


-- -- -- -- -- -- -- -- -- -- -- -- -- --
--  CREATE TABLE: RECIPE_INGREDIENTS   --
-- -- -- -- -- -- -- -- -- -- -- -- -- --

CREATE TABLE `recipe_ingredients` (
    `id` int AUTO_INCREMENT PRIMARY KEY,
    `recipe_id` int NOT NULL,
    CONSTRAINT `fk_ingredient_recipe`
		FOREIGN KEY (recipe_id)
		REFERENCES recipes(id)
        ON UPDATE CASCADE
        ON DELETE CASCADE,
    `name` varchar(50) NOT NULL,
    `quantity` smallint NOT NULL,
    `unit` varchar(50)
);
