USE explore_db;

-- Отключаем проверку внешних ключей
SET FOREIGN_KEY_CHECKS = 0;

-- Очищаем основные таблицы
TRUNCATE TABLE users;
TRUNCATE TABLE events;
TRUNCATE TABLE category;

-- Включаем проверку внешних ключей обратно
SET FOREIGN_KEY_CHECKS = 1;
