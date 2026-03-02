
DROP SCHEMA IF EXISTS brinquedos;
CREATE SCHEMA brinquedos;
-- GRANT ALL PRIVILEGES ON brinquedos.* TO 'patterson'@'%' WITH GRANT OPTION;
USE brinquedos;

CREATE TABLE IF NOT EXISTS categoria (
  categoria_id INT(11) NOT NULL AUTO_INCREMENT,
  categoria_nome VARCHAR(80) NOT NULL,
  PRIMARY KEY (categoria_id),
  UNIQUE INDEX categoria_nome_UNIQUE (categoria_nome ASC));


CREATE TABLE IF NOT EXISTS brinquedo (
  brinquedo_id INT(10) UNSIGNED NOT NULL AUTO_INCREMENT,
  brinquedo_descricao VARCHAR(200) NULL DEFAULT NULL,
  brinquedo_imagem_url VARCHAR(200) NOT NULL,
  brinquedo_preco DECIMAL(9,2) NOT NULL,
  brinquedo_detalhes VARCHAR(200) NULL DEFAULT NULL,
  brinquedo_categoria_id INT(11) NOT NULL,
  brinquedo_marca VARCHAR(45) NULL DEFAULT NULL,
  PRIMARY KEY (brinquedo_id),
  UNIQUE INDEX brinquedo_imagem_url_UNIQUE (brinquedo_imagem_url ASC),
  CONSTRAINT fk_brinquedo_categoria
    FOREIGN KEY (brinquedo_categoria_id)
    REFERENCES categoria (categoria_id)
    ON DELETE CASCADE
    ON UPDATE CASCADE);

CREATE TABLE IF NOT EXISTS estoque (
  estoque_id INT(11) NOT NULL AUTO_INCREMENT,
  estoque_qtd INT(10) UNSIGNED ZEROFILL NOT NULL,
  estoque_brinquedo_id INT(10) UNSIGNED NOT NULL,
  PRIMARY KEY (estoque_id),
  INDEX fk_estoque_brinquedo1_idx (estoque_brinquedo_id ASC),
  CONSTRAINT fk_estoque_brinquedo1
    FOREIGN KEY (estoque_brinquedo_id)
    REFERENCES brinquedo (brinquedo_id)
    ON DELETE CASCADE
    ON UPDATE CASCADE);
    
-- INSERINDO CATEGORIAS
INSERT INTO categoria (categoria_nome) VALUES ('educativos');
INSERT INTO categoria (categoria_nome) VALUES ('action figure');
INSERT INTO categoria (categoria_nome) VALUES ('jogos de taboleiro');
INSERT INTO categoria (categoria_nome) VALUES ('jogos de carta');
INSERT INTO categoria (categoria_nome) VALUES ('bonecas');
INSERT INTO categoria (categoria_nome) VALUES ('bonecos');
INSERT INTO categoria (categoria_nome) VALUES ('clássicos');
INSERT INTO categoria (categoria_nome) VALUES ('carrinhos');

-- INSERINDO BRINQUEDOS
INSERT INTO brinquedo (brinquedo_descricao, brinquedo_imagem_url, brinquedo_preco, brinquedo_detalhes, brinquedo_categoria_id, brinquedo_marca) 
VALUES ('Dohko de Libra - Action Figure', 'resources/pictures/dohko.jpg', '195.98', 'Boneco articulado série Saint Seiya Action Figures - Dohko de Libra', '2', 'TOEY');

INSERT INTO brinquedo (brinquedo_descricao, brinquedo_imagem_url, brinquedo_preco, brinquedo_detalhes, brinquedo_categoria_id, brinquedo_marca) 
VALUES ('Aiacos de Garuda - Action Figure', 'resources/pictures/aiacos.jpg', '165.98', 'Boneco articulado série Saint Seiya Action Figures - Aiacos de Garuda', '2', 'TOEY');

INSERT INTO brinquedo (brinquedo_descricao, brinquedo_imagem_url, brinquedo_preco, brinquedo_detalhes, brinquedo_categoria_id, brinquedo_marca) 
VALUES ('Alberich de Megres - Action Figure', 'resources/pictures/alberich.jpg', '195.98', 'Boneco articulado série Saint Seiya Action Figures - Alberich de Megres', '2', 'TOEY');

INSERT INTO brinquedo (brinquedo_descricao, brinquedo_imagem_url, brinquedo_preco, brinquedo_detalhes, brinquedo_categoria_id, brinquedo_marca) 
VALUES ('Baian de Cavalo Marinho - Action Figure', 'resources/pictures/baian.jpg', '125.98', 'Boneco articulado série Saint Seiya Action Figures - Baian de Cavalo Marinho', '2', 'TOEY');

INSERT INTO brinquedo (brinquedo_descricao, brinquedo_imagem_url, brinquedo_preco, brinquedo_detalhes, brinquedo_categoria_id, brinquedo_marca) 
VALUES ('Banco Imobiliário', 'resources/pictures/bancoimobiliario.jpg', '150.98', 'Jogo banco imobiliario', '3', 'ESTRELA');

INSERT INTO brinquedo (brinquedo_descricao, brinquedo_imagem_url, brinquedo_preco, brinquedo_detalhes, brinquedo_categoria_id, brinquedo_marca) 
VALUES ('Aprendendo inglês', 'resources/pictures/aprendendo_ingles.jpg', '25.99', 'Jogo educativo que ensina inglês brincando', '1', 'TOYSTER');

INSERT INTO brinquedo (brinquedo_descricao, brinquedo_imagem_url, brinquedo_preco, brinquedo_detalhes, brinquedo_categoria_id, brinquedo_marca) 
VALUES ('Velociraptor', 'resources/pictures/velociraptor.jpg', '75.99', 'Boneco colecionável Dinossauros PAPO - Velociraptor', '6', 'PAPO');

INSERT INTO brinquedo (brinquedo_descricao, brinquedo_imagem_url, brinquedo_preco, brinquedo_detalhes, brinquedo_categoria_id, brinquedo_marca) 
VALUES ('Tyrannosaurus', 'resources/pictures/tyrannosaurus.jpg', '75.99', 'Boneco colecionável PAPO - Tyrannosaurus', '6', 'PAPO');

INSERT INTO brinquedo (brinquedo_descricao, brinquedo_imagem_url, brinquedo_preco, brinquedo_detalhes, brinquedo_categoria_id, brinquedo_marca) 
VALUES ('Triceratops', 'resources/pictures/triceratops.jpg', '75.99', 'Boneco colecionável PAPO - Triceratops', '6', 'PAPO');

INSERT INTO brinquedo (brinquedo_descricao, brinquedo_imagem_url, brinquedo_preco, brinquedo_detalhes, brinquedo_categoria_id, brinquedo_marca) 
VALUES ('Brontosaurus', 'resources/pictures/brontosaurus.jpg', '75.99', 'Boneco colecionável PAPO - Brontosaurus', '6', 'PAPO');

INSERT INTO brinquedo (brinquedo_descricao, brinquedo_imagem_url, brinquedo_preco, brinquedo_detalhes, brinquedo_categoria_id, brinquedo_marca) 
VALUES ('Hot Wheels Dodg Viper Race', 'resources/pictures/viper.jpg', '15.99', 'Hot Wheels 2015 HW Race World SRT Dodge Viper GT-S GTS in Silver Grey Gray', '8', 'Hot Wheels');

INSERT INTO brinquedo (brinquedo_descricao, brinquedo_imagem_url, brinquedo_preco, brinquedo_detalhes, brinquedo_categoria_id, brinquedo_marca) 
VALUES ('ABC Xalingo', 'resources/pictures/abc_xalingo.png', '69.99', 'Brinquedo Educativo de madeira xalingo', '1', 'Xalingo');

INSERT INTO brinquedo (brinquedo_descricao, brinquedo_imagem_url, brinquedo_preco, brinquedo_detalhes, brinquedo_categoria_id, brinquedo_marca) 
VALUES ('Carro bate Pinos', 'resources/pictures/carro_bate_pinos.jpg', '75.99', 'Brinquedo Educativo de madeira xalingo', '1', 'Xalingo');

INSERT INTO brinquedo (brinquedo_descricao, brinquedo_imagem_url, brinquedo_preco, brinquedo_detalhes, brinquedo_categoria_id, brinquedo_marca) 
VALUES ('Associando números Miney', 'resources/pictures/associando_numeros_miney.jpg', '85.56', 'Brinquedo Educativo de madeira xalingo', '1', 'Xalingo');

INSERT INTO brinquedo (brinquedo_descricao, brinquedo_imagem_url, brinquedo_preco, brinquedo_detalhes, brinquedo_categoria_id, brinquedo_marca) 
VALUES ('Buslightyear', 'resources/pictures/buslightyear.jpg', '359.79', 'Boneco Toy Story Buslightyear', '6', 'Mattel');

INSERT INTO brinquedo (brinquedo_descricao, brinquedo_imagem_url, brinquedo_preco, brinquedo_detalhes, brinquedo_categoria_id, brinquedo_marca) 
VALUES ('Allosaurus', 'resources/pictures/allosaurus.jpg', '100.66', 'Boneco colecionável PAPO - Allosaurus', '6', 'PAPO');

INSERT INTO brinquedo (brinquedo_descricao, brinquedo_imagem_url, brinquedo_preco, brinquedo_detalhes, brinquedo_categoria_id, brinquedo_marca) 
VALUES ('Carnotaurus', 'resources/pictures/carnotaurus.jpg', '100.66', 'Boneco colecionável PAPO - carnotaurus', '6', 'PAPO');

INSERT INTO brinquedo (brinquedo_descricao, brinquedo_imagem_url, brinquedo_preco, brinquedo_detalhes, brinquedo_categoria_id, brinquedo_marca) 
VALUES ('Dilophosaurus', 'resources/pictures/dilophosaurus.jpg', '100.66', 'Boneco colecionável PAPO - dilophosaurus', '6', 'PAPO');

INSERT INTO brinquedo (brinquedo_descricao, brinquedo_imagem_url, brinquedo_preco, brinquedo_detalhes, brinquedo_categoria_id, brinquedo_marca) 
VALUES ('Estegosaurus', 'resources/pictures/estegosaurus.jpg', '100.66', 'Boneco colecionável PAPO - estegosaurus', '6', 'PAPO');

INSERT INTO brinquedo (brinquedo_descricao, brinquedo_imagem_url, brinquedo_preco, brinquedo_detalhes, brinquedo_categoria_id, brinquedo_marca) 
VALUES ('Spinosaurus', 'resources/pictures/spinosaurus.jpg', '100.66', 'Boneco colecionável PAPO - spinosaurus', '6', 'PAPO');

INSERT INTO brinquedo (brinquedo_descricao, brinquedo_imagem_url, brinquedo_preco, brinquedo_detalhes, brinquedo_categoria_id, brinquedo_marca) 
VALUES ('Annabelle', 'resources/pictures/annabelle.jpg', '56.45', 'Boneca Annabelle', '5', 'Mattel');

-- INSERINDO ESTOQUE
INSERT INTO estoque (estoque_qtd, estoque_brinquedo_id) VALUES ('50', '1');
INSERT INTO estoque (estoque_qtd, estoque_brinquedo_id) VALUES ('37', '2');
INSERT INTO estoque (estoque_qtd, estoque_brinquedo_id) VALUES ('40', '3');
INSERT INTO estoque (estoque_qtd, estoque_brinquedo_id) VALUES ('35', '4');
INSERT INTO estoque (estoque_qtd, estoque_brinquedo_id) VALUES ('17', '5');
INSERT INTO estoque (estoque_qtd, estoque_brinquedo_id) VALUES ('9', '6');
INSERT INTO estoque (estoque_qtd, estoque_brinquedo_id) VALUES ('11', '7');
INSERT INTO estoque (estoque_qtd, estoque_brinquedo_id) VALUES ('10', '8');
INSERT INTO estoque (estoque_qtd, estoque_brinquedo_id) VALUES ('3', '9');
INSERT INTO estoque (estoque_qtd, estoque_brinquedo_id) VALUES ('3', '10');
INSERT INTO estoque (estoque_qtd, estoque_brinquedo_id) VALUES ('150', '11');
INSERT INTO estoque (estoque_qtd, estoque_brinquedo_id) VALUES ('200', '12');
INSERT INTO estoque (estoque_qtd, estoque_brinquedo_id) VALUES ('202', '13');
INSERT INTO estoque (estoque_qtd, estoque_brinquedo_id) VALUES ('10', '14');
INSERT INTO estoque (estoque_qtd, estoque_brinquedo_id) VALUES ('6', '15');
INSERT INTO estoque (estoque_qtd, estoque_brinquedo_id) VALUES ('4', '16');
INSERT INTO estoque (estoque_qtd, estoque_brinquedo_id) VALUES ('8', '17');
INSERT INTO estoque (estoque_qtd, estoque_brinquedo_id) VALUES ('11', '18');

-- Um select simples em cada tabela
-- SELECT * FROM categoria;
-- SELECT * FROM brinquedo;
-- SELECT * FROM estoque;

-- Procedure brinquedoCategoria
-- Trazendo todas as tabelas com JOIN filtrando por categoria.

DELIMITER $$
DROP PROCEDURE IF EXISTS brinquedoCategoria$$
CREATE PROCEDURE brinquedoCategoria(id INT)
BEGIN
	SELECT 
	brinquedo_descricao as Brinquedo, CONCAT('R$ ', FORMAT(brinquedo_preco,2,'pt_BR')) as 'Preço', 
	categoria_nome as Categoria, estoque_qtd as 'Estoque qtd'
	FROM brinquedo
	INNER JOIN categoria ON brinquedo_categoria_id = categoria_id
	INNER JOIN estoque ON brinquedo_id = estoque_brinquedo_id
	WHERE brinquedo_categoria_id = id
	order by brinquedo_preco ASC;
END $$
DELIMITER ;

-- CALL brinquedoCategoria(2);

-- Procedure categoriaBrinquedoQtd
-- Mostra quantidade em estoque de um determinado brinquedo
DELIMITER $$
DROP PROCEDURE IF EXISTS categoriaBrinquedoQtd$$
CREATE PROCEDURE categoriaBrinquedoQtd()
BEGIN
	SELECT 
    CAP_FIRST(categoria_nome) as categoria_nome,
    sum(estoque_qtd) as brinquedo_categoria_qtd,
    brinquedo_imagem_url, categoria_id
    FROM categoria 
    INNER JOIN brinquedo ON categoria_id = brinquedo_categoria_id
    INNER JOIN estoque ON brinquedo_id = estoque_brinquedo_id
    WHERE estoque_qtd > 0
    GROUP BY categoria_id;
END
$$
DELIMITER ;

-- CALL categoriaBrinquedoQtd();

-- Trigger para inserir id do brinquedo no estoque com qtd 0 ao cadastrar novo brinquedo
DELIMITER $$
DROP TRIGGER IF EXISTS af_brinquedoInsert$$
CREATE TRIGGER af_brinquedoInsert
AFTER INSERT ON brinquedo
FOR EACH ROW 
BEGIN
	-- AO CADASTRAR NOVO BRINQUEDO, ADICIONA 1 NO ESTOQUE, APENAS PARA QUE ELE APAREÇA NO SITE
    -- O CERTO SERIA TER UM FORMULÁRIO DE ENTRADA DE MERCADORIA, MAS ISSO FOI PEDIDO.
	INSERT INTO estoque (estoque_qtd, estoque_brinquedo_id) VALUES ('1', NEW.brinquedo_id);
END $$
DELIMITER ;

-- Função que Capitaliza Strings
DELIMITER $$
DROP FUNCTION IF EXISTS CAP_FIRST$$
CREATE FUNCTION CAP_FIRST (input VARCHAR(255))
RETURNS VARCHAR(255)
DETERMINISTIC

BEGIN
	DECLARE len INT;
	DECLARE i INT;

	SET len   = CHAR_LENGTH(input);
	SET input = LOWER(input);
	SET i = 0;

	WHILE (i < len) DO
		IF (MID(input,i,1) = ' ' OR i = 0) THEN
			IF (i < len) THEN
				SET input = CONCAT(
					LEFT(input,i),
					UPPER(MID(input,i + 1,1)),
					RIGHT(input,len - i - 1)
				);
			END IF;
		END IF;
		SET i = i + 1;
	END WHILE;

	RETURN input;
END $$
DELIMITER ;






















    

