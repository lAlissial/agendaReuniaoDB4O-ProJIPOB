package fachada;
/**********************************
 * IFPB - Curso Superior de Tec. em Sist. para Internet
 * Programação Orientada a Objetos
 * Prof. Fausto Maranhão Ayres
 * Grupo: Adriana, Alíssia e Vanessa.
 * Setembro 2021
 **********************************/

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport; // send comentado a pedido do professor para fazer o teste.
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import dao.DAO;
import dao.DAOParticipante;
import dao.DAOReuniao;
import dao.DAOConvidado;
import modelo.Convidado;
import modelo.Participante;
import modelo.Reuniao;

public class Fachada {
	private static String emailOrigem ;				//email de origem 
	private static String senhaOrigem ;				//senha do email de origem
	private static boolean emailDesabilitado ;		//desabilitar envio?
	
	private static DAOParticipante daoparticipante= new DAOParticipante();  
	private static DAOReuniao daoreuniao = new DAOReuniao();  
	private static DAOConvidado daoconvidado = new DAOConvidado();  

	private static DateTimeFormatter formatadorDT = DateTimeFormatter.ofPattern("dd/MM/yyyy");
	private static DateTimeFormatter formatadorDTH = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

	public static void setEmailSenha(String email, String senha) {
		emailOrigem = email;
		senhaOrigem = senha;
	}
	
	public static void desabilitarEmail(boolean status) {
		emailDesabilitado = status;
	}

	public static void inicializar()  {
		DAO.open();
	}

	public static void	finalizar() {
		DAO.close();
	}

	public static Participante criarParticipante(String nome, String email) throws Exception {
		nome = nome.trim();
		email = email.trim();
		
		//inicio da transacao
		DAO.begin();
		
		//Verificar se o participande existe
		Participante p = daoparticipante.read(nome);//localizarParticipante
		
		if (p!=null){
			throw new Exception("Participante " + nome + " ja cadastrado(a)");
		} else {
			//Cadastrar participante na reunião
			p = new Participante(nome,email);
			
			//persistir novo participante
			daoparticipante.create(p);
			
			//fim da transacao
			DAO.commit();
			
			return p;
		}
		
	}	

	public static Convidado criarConvidado(String nome, String email, String empresa) throws Exception{
		nome = nome.trim();
		email = email.trim();
		empresa = empresa.trim();
		
		//inicio da transacao
		DAO.begin();
		
		//Verificar se o convidado existe
		Convidado c = daoconvidado.read(nome); //localizar Convidado

		if (c!=null) {
			throw new Exception("Convidado " + nome + " ja cadastrado(a)");
		}
		
		//Cadastrar convidado na reunião
		Convidado conv = new Convidado(nome, email, empresa);

		//persistir novo convidado no banco
		daoconvidado.create(conv);
		//...
		
		//fim da transacao
		DAO.commit();
		
		return conv;	
	}	

	public static Reuniao criarReuniao (String datahora, String assunto, ArrayList<String> nomes) throws Exception{
		assunto = assunto.trim();

		String pegandoexc1="";
		int seraqchegou1=0;
		String pegandoexc2="";
		int seraqchegou2=0;
		String pegandoexc3="";
		int seraqchegou3=0;
		

		int contador1 = 0; //conta participantes invalidos
		
		//inicio da transacao
		DAO.begin();
		
		
		LocalDateTime dth;
		
		try {
			DateTimeFormatter parser = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
			dth  = LocalDateTime.parse(datahora, parser); 
		}
		catch(DateTimeParseException e) {
			throw new Exception ("Reuniao com formato de data invalido");
		}

		
		//Verificar o tamanho da lista de participantes se é > 2
		if (nomes.size()<2) {
			throw new Exception("Reuniao sem quórum mínimo de dois participantes");
		} else {
			Reuniao r = new Reuniao(dth, assunto);	
			
			//persistir nova reuniao no banco
			daoreuniao.create(r);
			//...

			for(String nomezito: nomes) {
				try {
					adicionarParticipanteReuniao(nomezito, r.getId());
				} catch (Exception e) {
					if (e.getMessage().contains("não consta no cadastro")){
						contador1++;
						seraqchegou1 = 1;
						pegandoexc1 = e.getMessage();
					}

					if (e.getMessage().contains("adicionar novamente - participante")){
						contador1++;
						seraqchegou2 = 1;
						pegandoexc2 = e.getMessage();

					}
					if (e.getMessage().contains("já está em outra reunião nesse horárioo")) {
						contador1++;
						seraqchegou3 = 1;
						pegandoexc3 = e.getMessage();
					}
				}
			}


			if ((nomes.size()-contador1)<2){
				cancelarReuniao(r.getId());
				throw new Exception("Reuniao será cancelada: sem quórum mínimo de dois participantes");
			}
			
			//fim da transacao
			DAO.commit();


			if(seraqchegou1 == 1){
				throw new Exception(pegandoexc1);
			}
			if (seraqchegou2 == 1) {
				throw new Exception(pegandoexc2);
			}
			if (seraqchegou3 == 1) {
				throw new Exception(pegandoexc3);
			}

			return r;
		}
		
	}

	public static void 	adicionarParticipanteReuniao(String nome, int id) throws Exception 	{
		DateTimeFormatter formatador = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
		Duration duracao;
		long horas;

		nome = nome.trim();
		
		//inicio da transacao
		DAO.begin();

		//Verificar se o participante existe
		Participante p = daoparticipante.read(nome);//localizarParticipante
		
		
		if(p==null){
			throw new Exception("Participante " + nome + " não consta no cadastro");
		}

		//Verificar de a reuniaão existe no repositório
		Reuniao r = daoreuniao.read(id);//localizarReuniao

		if(r==null){
			throw new Exception("Reuniao " + id + " não cadastrada");
		}


		for (Participante partnome: r.getParticipantes()){
			if (nome.equalsIgnoreCase(partnome.getNome())){
				throw new Exception(String.format("Nao pode adicionar novamente - participante %s ja incluso",nome));
			}
		}


		if (p.getReunioes().isEmpty()){
			r.adicionar(p);
			p.adicionar(r);
		} else {
			
			//Verificar se o participante já está em outra reunião no mesmo horário
			for (Reuniao reuni : p.getReunioes()) {
				duracao = Duration.between(r.getDatahora(), reuni.getDatahora());
				horas = duracao.toHours();

				if (Math.abs(horas) < 2) {
					throw new Exception("Participante já está em outra reunião nesse horário");
				}
			}
			r.adicionar(p);
			p.adicionar(r);
		}
		
		//atualizar reuniao no banco
		daoreuniao.update(r);
		//...

		
		//fim da transacao
		DAO.commit();

		//enviarEmail(p.getEmail(), "Adicionado a reuniao", String.format("Voce foi adicionado(a) a reuniao %d que ocorrera em %s",r.getId(),r.getDatahora().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")).toString()));
	}

	public static void 	removerParticipanteReuniao(String nome, int id) throws Exception	{
		nome = nome.trim();
		
		//inicio da transacao
		DAO.begin();

		//Verificar se o participante existe
		Participante p = daoparticipante.read(nome);//localizarParticipante

		if(p==null){
			throw new Exception("Participante " + nome + " não consta no cadastro");
		}
		
		//Verificar se a reunião está cadastrada
		Reuniao r = daoreuniao.read(id);//localizarReuniao 
		
		if(r==null){
			throw new Exception("Reuniao " + id + " não cadastrada");
		}

		int controle=0;
		for (Participante part: r.getParticipantes()){
			if (nome.equalsIgnoreCase(part.getNome())) {
				//Remover participante da reunião 
				//atualizar reuniao no banco
				r.remover(part);
				daoreuniao.update(r);
				
				p.remover(r);
				daoparticipante.update(p);
				
				controle++;
				break;
			}
		}

		//fim da transacao
		DAO.commit();
		
		//enviar email para o  participante removido
		//enviarEmail(p.getEmail(), "participante removido", "Você foi removido da reunião na data:"+r.getDatahora()+" e assunto:"+r.getAssunto());

		if (controle==1){
			if(r.getParticipantes().size()<2){
				cancelarReuniao(id);
				//throw new Exception("Reuniao será cancelada: nao ha participantes suficientes");
			}
		}

		if (controle<1){
			throw new Exception("Nao se pode remover o que nao existe - participante nao esta nem incluso");
		}
		
	}

	public static void cancelarReuniao(int id) throws Exception	{
		//inicio da transacao
		DAO.begin();

		//Verificar se a reunião está cadastrada
		Reuniao r = daoreuniao.read(id);//repositorio.localizarReuniao(id);
		
		if(r==null){
			throw new Exception("Reuniao " + id + " não cadastrada");
		}
		
		//Remover participante da reunião
		for(Participante p: r.getParticipantes()){
			//enviarEmail(p.getEmail(), "reuniao cancelada", "data:+"+r.getDatahora()+" e assunto:"+r.getAssunto());
			p.remover(r);
			daoparticipante.update(p);
		}

		//apagar reunião no banco
		daoreuniao.delete(r);
		
		//fim da transacao
		DAO.commit();

		
	}

	public static void apagarParticipante(String nome) throws Exception {
		nome = nome.trim();
		
		ArrayList<Reuniao> guardandoReunioeszitas = new ArrayList<Reuniao>();

		//inicio da transacao
		DAO.begin();
		
		//Verificar se o participande existe
		Participante p = daoparticipante.read(nome);//localizarParticipante
		
		if (p==null) {
			throw new Exception("Participante " + nome + " nao cadastrado(a)");
		}
		
		//remover o participante das reunioes que participa
		for (Reuniao reun: p.getReunioes()){
			reun.remover(p);
			daoreuniao.update(reun);

			if(reun.getTotalParticipantes()<2){
				 guardandoReunioeszitas.add(reun);
				
			}
		}

		//apagar participante do banco
		daoparticipante.delete(p);
		//...
		
		
		//fim da transacao
		DAO.commit();
		
		//cancelando as reunioes que ficam com menos de 2 participantes
		if (!guardandoReunioeszitas.isEmpty()) {
			for (Reuniao reuniaozita: guardandoReunioeszitas) {
				cancelarReuniao(reuniaozita.getId());
			}
		}

		//enviar email para o participante apagado
		//enviarEmail(p.getEmail()," descadastro ",  "vc foi excluido da agenda");
	}	

	public static List<Participante> listarParticipantes() {
		return daoparticipante.readAll(); //...
	}
	
	public static List<Convidado> listarConvidados() {
		return daoconvidado.readAll(); //...
	}
	
	public static List<Reuniao> listarReunioes() 	{
		return daoreuniao.readAll(); //...
	}
	
	public static List<Participante> consultaA(String nome, int mes) 	{
		return daoparticipante.consultaA(nome, mes); //...
	}
	
	public static List<Reuniao> consultaB() 	{
		return daoreuniao.consultaB(); //...
	}


	/*
	 * ********************************************************
	 * Obs: lembrar de desligar antivirus e 
	 * de ativar "Acesso a App menos seguro" na conta do gmail
	 * 
	 * biblioteca java.mail 1.6.2
	 * ********************************************************
	 */
	public static void enviarEmail(String emaildestino, String assunto, String mensagem) {
		try {
			if (Fachada.emailDesabilitado)
				return;

			String emailorigem = Fachada.emailOrigem;
			String senhaorigem = Fachada.senhaOrigem;

			//configurar email de origem
			Properties props = new Properties();
			props.put("mail.smtp.starttls.enable", "true");
			props.put("mail.smtp.host", "smtp.gmail.com");
			props.put("mail.smtp.port", "587");
			props.put("mail.smtp.auth", "true");
			Session session;
			session = Session.getInstance(props,
					new javax.mail.Authenticator() 	{
				protected PasswordAuthentication getPasswordAuthentication() 	{
					return new PasswordAuthentication(emailorigem, senhaorigem);
				}
			});

			//criar e enviar email
			MimeMessage message = new MimeMessage(session);
			message.setSubject(assunto);		
			message.setFrom(new InternetAddress(emailorigem));
			message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(emaildestino));
			message.setText(mensagem);   // usar "\n" para quebrar linhas
			//Transport.send(message);
		} 
		catch (MessagingException e) {
			System.out.println(e.getMessage());
		} 
		catch (Exception e) {
			System.out.println(e.getMessage());
		}
	}
}
