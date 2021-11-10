package dao;

import com.db4o.query.Query;

import java.util.List;

import modelo.Convidado;



public class DAOConvidado extends DAO<Convidado>{
	@Override
	public Convidado read (Object chave) {
		String nome = (String) chave;	//casting para o tipo da chave
		Query q = manager.query();
		q.constrain(Convidado.class);
		q.descend("nome").constrain(nome);
		List<Convidado> resultados = q.execute();
		if (resultados.size()>0)
			return resultados.get(0);
		else
			return null;
	}

}
