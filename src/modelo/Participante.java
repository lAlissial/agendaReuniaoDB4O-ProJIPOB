package modelo;

import java.util.ArrayList;

/**********************************
 * IFPB - Curso Superior de Tec. em Sist. para Internet
 * Programação Orientada a Objetos
 * Prof. Fausto Maranhão Ayres
 **********************************/

public class Participante {
	private String nome; 
	private String email;
	private ArrayList <Reuniao> reunioes = new ArrayList <Reuniao> ();

	public Participante(String nome, String email) 	{
		super();
		this.nome = nome;
		this.email = email;
	}

	public String getNome() {
		return nome;
	}

	public void setNome(String nome) {
		this.nome = nome;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}


	public ArrayList<Reuniao> getReunioes() 	{
		return reunioes;
	}

	public void adicionar(Reuniao r)	{
		reunioes.add(r);
	}

	public void remover(Reuniao r)	{
		reunioes.remove(r);
	}


	public int getTotalReunioes() 	{
		return reunioes.size();
	}

	@Override
	public String toString() 	{
		String texto = "Nome: " + nome + ", email: " + email + ", Reuniões: ";
		
		if (reunioes.isEmpty())
			texto += " sem reunião";
		else 	
			for(Reuniao r : reunioes) 	
				texto += " " + r.getId();

		return texto;
	}
}

