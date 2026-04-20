-- seed data for local development and demo — remove before deploying to staging/prod

INSERT INTO theatres (name, city, address, total_seats) VALUES
    ('PVR Cinemas', 'Mumbai', 'Phoenix Mall, Lower Parel, Mumbai 400013', 200),
    ('INOX Multiplex', 'Bangalore', 'Garuda Mall, MG Road, Bangalore 560001', 150)
ON CONFLICT DO NOTHING;

INSERT INTO shows (movie_name, theatre_id, show_time, ticket_price, available_seats) VALUES
    ('Dune: Part Two',  1, NOW() + INTERVAL '1 day' + TIME '10:00:00', 350.00, 20),
    ('Dune: Part Two',  1, NOW() + INTERVAL '1 day' + TIME '14:30:00', 350.00, 20),
    ('Oppenheimer',     2, NOW() + INTERVAL '1 day' + TIME '11:00:00', 300.00, 20),
    ('Oppenheimer',     2, NOW() + INTERVAL '1 day' + TIME '15:00:00', 300.00, 20)
ON CONFLICT DO NOTHING;

-- 20 seats per show (rows A-D, cols 1-5) — show 1
INSERT INTO seats (show_id, seat_number, status) VALUES
(1,'A1','AVAILABLE'),(1,'A2','AVAILABLE'),(1,'A3','AVAILABLE'),(1,'A4','AVAILABLE'),(1,'A5','AVAILABLE'),
(1,'B1','AVAILABLE'),(1,'B2','AVAILABLE'),(1,'B3','AVAILABLE'),(1,'B4','AVAILABLE'),(1,'B5','AVAILABLE'),
(1,'C1','AVAILABLE'),(1,'C2','AVAILABLE'),(1,'C3','AVAILABLE'),(1,'C4','AVAILABLE'),(1,'C5','AVAILABLE'),
(1,'D1','AVAILABLE'),(1,'D2','AVAILABLE'),(1,'D3','AVAILABLE'),(1,'D4','AVAILABLE'),(1,'D5','AVAILABLE')
ON CONFLICT DO NOTHING;

-- show 2
INSERT INTO seats (show_id, seat_number, status) VALUES
(2,'A1','AVAILABLE'),(2,'A2','AVAILABLE'),(2,'A3','AVAILABLE'),(2,'A4','AVAILABLE'),(2,'A5','AVAILABLE'),
(2,'B1','AVAILABLE'),(2,'B2','AVAILABLE'),(2,'B3','AVAILABLE'),(2,'B4','AVAILABLE'),(2,'B5','AVAILABLE'),
(2,'C1','AVAILABLE'),(2,'C2','AVAILABLE'),(2,'C3','AVAILABLE'),(2,'C4','AVAILABLE'),(2,'C5','AVAILABLE'),
(2,'D1','AVAILABLE'),(2,'D2','AVAILABLE'),(2,'D3','AVAILABLE'),(2,'D4','AVAILABLE'),(2,'D5','AVAILABLE')
ON CONFLICT DO NOTHING;

-- show 3
INSERT INTO seats (show_id, seat_number, status) VALUES
(3,'A1','AVAILABLE'),(3,'A2','AVAILABLE'),(3,'A3','AVAILABLE'),(3,'A4','AVAILABLE'),(3,'A5','AVAILABLE'),
(3,'B1','AVAILABLE'),(3,'B2','AVAILABLE'),(3,'B3','AVAILABLE'),(3,'B4','AVAILABLE'),(3,'B5','AVAILABLE'),
(3,'C1','AVAILABLE'),(3,'C2','AVAILABLE'),(3,'C3','AVAILABLE'),(3,'C4','AVAILABLE'),(3,'C5','AVAILABLE'),
(3,'D1','AVAILABLE'),(3,'D2','AVAILABLE'),(3,'D3','AVAILABLE'),(3,'D4','AVAILABLE'),(3,'D5','AVAILABLE')
ON CONFLICT DO NOTHING;

-- show 4
INSERT INTO seats (show_id, seat_number, status) VALUES
(4,'A1','AVAILABLE'),(4,'A2','AVAILABLE'),(4,'A3','AVAILABLE'),(4,'A4','AVAILABLE'),(4,'A5','AVAILABLE'),
(4,'B1','AVAILABLE'),(4,'B2','AVAILABLE'),(4,'B3','AVAILABLE'),(4,'B4','AVAILABLE'),(4,'B5','AVAILABLE'),
(4,'C1','AVAILABLE'),(4,'C2','AVAILABLE'),(4,'C3','AVAILABLE'),(4,'C4','AVAILABLE'),(4,'C5','AVAILABLE'),
(4,'D1','AVAILABLE'),(4,'D2','AVAILABLE'),(4,'D3','AVAILABLE'),(4,'D4','AVAILABLE'),(4,'D5','AVAILABLE')
ON CONFLICT DO NOTHING;
