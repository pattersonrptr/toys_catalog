-- V2__seed_data.sql
-- Dados iniciais migrados do projeto legado (2017)

INSERT INTO categories (name, description) VALUES
    ('Educativos',        'Jogos e brinquedos educativos'),
    ('Action Figure',     'Bonecos articulados e colecionáveis'),
    ('Jogos de Tabuleiro','Jogos de tabuleiro clássicos e modernos'),
    ('Jogos de Carta',    'Baralhos e jogos de cartas'),
    ('Bonecas',           'Bonecas e acessórios'),
    ('Bonecos',           'Bonecos colecionáveis e dinossauros'),
    ('Clássicos',         'Brinquedos clássicos atemporais'),
    ('Carrinhos',         'Veículos em miniatura');

INSERT INTO products (name, description, brand, image_url, price, stock_quantity, category_id, featured) VALUES
    ('Dohko de Libra - Action Figure',   'Boneco articulado série Saint Seiya - Dohko de Libra',    'TOEY',    'products/dohko.jpg',              195.98, 10, 2, true),
    ('Aiacos de Garuda - Action Figure', 'Boneco articulado série Saint Seiya - Aiacos de Garuda',  'TOEY',    'products/aiacos.jpg',             165.98, 8,  2, false),
    ('Alberich de Megres - Action Figure','Boneco articulado série Saint Seiya - Alberich de Megres','TOEY',   'products/alberich.jpg',           195.98, 5,  2, false),
    ('Baian de Cavalo Marinho - Action Figure','Boneco articulado Saint Seiya - Baian','TOEY',      'products/baian.jpg',              125.98, 7,  2, false),
    ('Banco Imobiliário',                'Jogo de tabuleiro clássico',                               'ESTRELA', 'products/bancoimobiliario.jpg',   150.98, 15, 3, true),
    ('Aprendendo Inglês',                'Jogo educativo que ensina inglês brincando',               'TOYSTER', 'products/aprendendo_ingles.jpg',   25.99, 20, 1, false),
    ('Velociraptor',                     'Boneco colecionável PAPO - Velociraptor',                  'PAPO',    'products/velociraptor.jpg',        75.99, 12, 6, false),
    ('Tyrannosaurus',                    'Boneco colecionável PAPO - Tyrannosaurus',                 'PAPO',    'products/tyrannosaurus.jpg',       75.99, 10, 6, true),
    ('Triceratops',                      'Boneco colecionável PAPO - Triceratops',                   'PAPO',    'products/triceratops.jpg',         75.99, 9,  6, false);
