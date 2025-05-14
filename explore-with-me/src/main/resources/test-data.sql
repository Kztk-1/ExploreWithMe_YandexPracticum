USE explore_db;

-- Очищаем таблицы перед вставкой тестовых данных
SET FOREIGN_KEY_CHECKS = 0;
TRUNCATE TABLE events;
TRUNCATE TABLE users;
TRUNCATE TABLE category;
SET FOREIGN_KEY_CHECKS = 1;

-- 5 тестовых категорий
INSERT INTO category (id, name) VALUES
                                    (1, 'Music'),
                                    (2, 'Sport'),
                                    (3, 'Theatre'),
                                    (4, 'Exhibition'),
                                    (5, 'Workshop');

-- -- 5 тестовых пользователей
-- INSERT INTO users (id, name, email, registration_date) VALUES
--                                                            (1, 'Alice Johnson', 'alice.johnson@example.com', '2025-01-15 10:20:30'),
--                                                            (2, 'Bob Smith',     'bob.smith@example.com',     '2025-02-20 14:05:00'),
--                                                            (3, 'Carol White',   'carol.white@example.com',   '2025-03-10 09:00:00'),
--                                                            (4, 'David Brown',   'david.brown@example.com',   '2025-04-01 18:45:10'),
--                                                            (5, 'Eva Green',     'eva.green@example.com',     '2025-04-25 12:30:00');

-- -- 5 тестовых событий
-- -- Предполагается, что в таблице events есть колонки lat и lon для @Embedded Location
-- INSERT INTO events (
--     id,
--     title,
--     annotation,
--     description,
--     event_date,
--     lat,
--     lon,
--     category_id,
--     initiator_id,
--     state,
--     created_on,
--     published_on,
--     paid,
--     participant_limit,
--     request_moderation,
--     confirmed_requests,
--     views
-- ) VALUES
--       (1,
--        'Jazz Night',
--        'An evening of smooth jazz.',
--        'Join us for a relaxed evening with live jazz bands.',
--        '2025-06-10 20:00:00',
--        52.3702, 4.8952,
--        1, 1, 'PUBLISHED',
--        '2025-05-01 08:00:00', '2025-05-02 09:00:00',
--        FALSE, 100, TRUE, 25, 150),
--
--       (2,
--        'City Marathon',
--        'Run through the city streets.',
--        'Annual city marathon open to all levels.',
--        '2025-07-05 09:00:00',
--        52.3676, 4.9041,
--        2, 2, 'PENDING',
--        '2025-05-03 10:15:00', NULL,
--        TRUE, 5000, FALSE, 0, 0),
--
--       (3,
--        'Shakespeare in the Park',
--        'Outdoor theatre performance.',
--        'Experience a classic Shakespeare play under the stars.',
--        '2025-08-12 19:30:00',
--        52.3584, 4.8811,
--        3, 3, 'PUBLISHED',
--        '2025-05-05 11:00:00', '2025-05-06 12:00:00',
--        FALSE, 200, TRUE, 50, 300),
--
--       (4,
--        'Modern Art Expo',
--        'Exhibition of contemporary artists.',
--        'A showcase of modern art across various mediums.',
--        '2025-09-01 10:00:00',
--        52.3791, 4.9003,
--        4, 4, 'PUBLISHED',
--        '2025-05-07 09:30:00', '2025-05-08 10:00:00',
--        FALSE, 0, TRUE, 0, 75),
--
--       (5,
--        'Photography Workshop',
--        'Learn DSLR basics.',
--        'Hands-on workshop teaching DSLR techniques.',
--        '2025-10-15 13:00:00',
--        52.3708, 4.8960,
--        5, 5, 'PENDING',
--        '2025-05-09 14:45:00', NULL,
--        TRUE, 20, TRUE, 0, 0;
