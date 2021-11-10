package dao;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import com.db4o.query.Candidate;
import com.db4o.query.Evaluation;
import com.db4o.query.Query;

import modelo.Participante;
import modelo.Reuniao;

public class DAOParticipante extends DAO<Participante> {
	
	public Participante read (Object chave) {
		String nome = (String) chave;	//casting para o tipo da chave
		Query q = manager.query();
		q.constrain(Participante.class);    // select p from Participante p where nome=:nome
		q.descend("nome").constrain(nome);
		List<Participante> resultados = q.execute();
		if (resultados.size()>0)
			return resultados.get(0);
		else
			return null;
	}
	

	/**********************************************************
	 * 
	 * CONSULTA DE PARTICIPANTE
	 * 
	 **********************************************************/
	//A. Quais os participantes que tem reunião com participante de nome N no mês M?
	
	public List<Participante> consultaA(String nome, int mes) {
		Query querynha = manager.query();
		querynha.constrain(Participante.class);
		querynha.descend("reunioes").constrain(new Filtro(nome, mes));
		List<Participante> resultadozitinho = querynha.execute();
		return resultadozitinho;
	}

}

/*-------------------------------------------------*/
class Filtro implements Evaluation {
	private String nome;
	private int mes;
	
	public Filtro(String nome,int mes) {
		this.nome = nome;
		this.mes = mes;
		
	}
	public void evaluate(Candidate candidatinho) {
		Reuniao r = (Reuniao) candidatinho.getObject();
		boolean teste = false;
		for(Participante p : r.getParticipantes()) {
			if (r.getDatahora().getMonthValue() == mes && p.getNome().equalsIgnoreCase(nome)) {
				teste = true;
				break;
			}
		}
		candidatinho.include(teste);
	}
}





