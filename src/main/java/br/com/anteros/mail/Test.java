package br.com.anteros.mail;

import java.util.Properties;

import javax.mail.MessagingException;
import javax.mail.NoSuchProviderException;

public class Test {

	public static void main(String[] args) throws NoSuchProviderException, MessagingException {
		EmailManager manager = new EmailManager(new Properties(), "box.crmgazin.com.br", new EmailAuthenticator("marketing@crmgazin.com.br", "Anteros@72720456789"), true, 587);
		EmailMessage message = new EmailMessage();
		message.setEmailFrom("marketing@crmgazin.com.br");
		message.setEmailTo("eduardo@ajrorato.ind.br");
		message.setSubject("P I N D U B A");
		message.setTextMessage("O BICHO DA GOIABA.");
		manager.send(message);
	}

}
