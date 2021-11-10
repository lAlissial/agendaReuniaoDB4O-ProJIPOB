package dao;

import java.util.List;

import com.db4o.query.Query;

import modelo.Convidado;
import modelo.Reuniao;

public class DAOReuniao extends DAO<Reuniao> {
	
	public Reuniao read (Object chave) {
		int id = (Integer) chave;	
		Query q = manager.query();
		q.constrain(Reuniao.class);
		q.descend("id").constrain(id);
		List<Reuniao> resultados = q.execute();
		if (resultados.size()>0)
			return resultados.get(0);
		else
			return null;
	}

	public void create(Reuniao obj){
		Reuniao r = (Reuniao) obj;
		int id = super.getMaxId();
		id++;
		r.setId(id);
		manager.store( r );
	}
	
	/**********************************************************
	 * 
	 * CONSULTA DE REUNIAO
	 * 
	 **********************************************************/
	
	
	//B. Quais as reuniões que tem algum convidado?
	public List<Reuniao> consultaB() {
		Query querona = manager.query();
		querona.constrain(Reuniao.class);
		querona.descend("participantes").constrain(Convidado.class);
		List<Reuniao> resultinho = querona.execute();
		return resultinho;
	}
}
