CREATE TABLE IF NOT EXISTS produto (
    id                 BIGINT AUTO_INCREMENT PRIMARY KEY,
    nome               VARCHAR(150)   NOT NULL,
    descricao          VARCHAR(500),
    preco              DECIMAL(12, 2) NOT NULL,
    quantidade_estoque INT            NOT NULL
);
