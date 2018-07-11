package com.tfg.LibrairyConection;

public class EstructuraDelPDF {
	private String titulo;
	private String texto;
	public EstructuraDelPDF(String titulo, String texto) {
		super();
		this.titulo = titulo;
		this.texto = texto;
	}
	public String getTitulo() {
		return titulo;
	}
	public void setTitulo(String titulo) {
		this.titulo = titulo;
	}
	public String getTexto() {
		return texto;
	}
	public void setTexto(String texto) {
		this.texto = texto;
	}
	
}
