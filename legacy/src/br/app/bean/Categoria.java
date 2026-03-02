package br.app.bean;

public class Categoria {
	private long id;
	private String nome;
	
	public Categoria(long id, String nome) {
		super();
		this.id = id;
		this.nome = nome;
	}
	// Getters & Setters
	public long getId() {
		return id;
	}
	public void setId(long id) {
		this.id = id;
	}
	public String getNome() {
		return nome;
	}
	public void setNome(String nome) {
		this.nome = nome;
	}
}
