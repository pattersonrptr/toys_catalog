package br.app.bean;

import java.math.BigDecimal;

public class Brinquedo {

	private long cod, codCategoria, qtdEstoque;
	private String descricao, marca, imgUrl, detalhes;
	private BigDecimal preco;
	
	public Brinquedo(long cod, long codCategoria, String descricao, String marca, String imgUrl, String detalhes,
			BigDecimal preco) {
		super();
		this.cod = cod;
		this.codCategoria = codCategoria;
		this.descricao = descricao;
		this.marca = marca;
		this.imgUrl = imgUrl;
		this.detalhes = detalhes;
		this.preco = preco;
	}

	public Brinquedo(long cod, long codCategoria, String descricao, String marca, String imgUrl, String detalhes,
			BigDecimal preco, long qtdEstoque) {
		super();
		this.cod = cod;
		this.codCategoria = codCategoria;
		this.descricao = descricao;
		this.marca = marca;
		this.imgUrl = imgUrl;
		this.detalhes = detalhes;
		this.preco = preco;
		this.qtdEstoque = qtdEstoque;
	}
	
	public long getCod() {
		return cod;
	}
	public void setCod(long cod) {
		this.cod = cod;
	}
	public long getCodCategoria() {
		return codCategoria;
	}
	public void setCodCategoria(long codCategoria) {
		this.codCategoria = codCategoria;
	}
	public String getDescricao() {
		return descricao;
	}
	public void setDescricao(String descricao) {
		this.descricao = descricao;
	}
	public String getMarca() {
		return marca;
	}
	public void setMarca(String marca) {
		this.marca = marca;
	}
	public String getImgUrl() {
		return imgUrl;
	}
	public void setImgUrl(String imgUrl) {
		this.imgUrl = imgUrl;
	}
	public String getDetalhes() {
		return detalhes;
	}
	public void setDetalhes(String detalhes) {
		this.detalhes = detalhes;
	}
	public BigDecimal getPreco() {
		return preco;
	}
	public void setPreco(BigDecimal preco) {
		this.preco = preco;
	}

	public long getQtdEstoque() {
		return qtdEstoque;
	}

	public void setQtdEstoque(long qtdEstoque) {
		this.qtdEstoque = qtdEstoque;
	}
}
