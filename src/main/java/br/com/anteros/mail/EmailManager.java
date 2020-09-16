package br.com.anteros.mail;

import java.io.UnsupportedEncodingException;
import java.util.Date;
import java.util.Properties;

import javax.activation.DataHandler;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.NoSuchProviderException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.InternetHeaders;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import javax.mail.util.ByteArrayDataSource;

public class EmailManager {

	private Properties properties;
	private EmailAuthenticator authenticator;
	private EmailMessage emailMessage;
	private boolean confirmationReceipt = false;
	private boolean readingConfirmation = false;
	private boolean useTLS;
	private boolean debug;
	private int port;
	private String host;

	public EmailManager(Properties propriedades, EmailAuthenticator autenticadorEmail) {
		this.properties = propriedades;
		this.authenticator = autenticadorEmail;
	}

	public EmailManager(Properties propriedades, String host, EmailAuthenticator autenticadorEmail, boolean useTLS,
			int port, boolean debug) {
		this.properties = propriedades;
		this.authenticator = autenticadorEmail;
		this.useTLS = useTLS;
		this.port = port;
		this.host = host;
		this.debug = debug;
	}

	public EmailManager(Properties propriedades, EmailAuthenticator autenticadorEmail, boolean confirmationReceipt,
			boolean readingConfirmation) {
		this.properties = propriedades;
		this.authenticator = autenticadorEmail;
		this.confirmationReceipt = confirmationReceipt;
		this.readingConfirmation = readingConfirmation;
	}

	public void send(EmailMessage emailMessage)
			throws NoSuchProviderException, MessagingException, UnsupportedEncodingException {
		this.emailMessage = emailMessage;

		if (host.contains("gmail")) {
			properties.put("mail.smtp.host", "smtp.gmail.com");
			properties.put("mail.smtp.socketFactory.port", "465");
			properties.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
			properties.put("mail.smtp.auth", "true");
			properties.put("mail.smtp.port", "465");
			properties.setProperty("mail.debug", debug == true ? "true" : "false");
		} else if (host.contains("amazon")) {
			properties.put("mail.transport.protocol", "smtp");
			properties.put("mail.smtp.host", host);
			properties.put("mail.smtp.port", 587); 
			properties.put("mail.smtp.starttls.enable", "true");
			properties.put("mail.smtp.auth", "true");
			properties.setProperty("mail.debug", debug == true ? "true" : "false");
		} else { 
			if (useTLS) {
				if (properties == null)
					properties = new Properties();
				properties.setProperty("mail.smtp.host", host);
				properties.setProperty("mail.imap.ssl.enable", "true");
				properties.setProperty("mail.imap.ssl.socketFactory.class",
						"br.com.anteros.mail.AnterosSSLSocketFactory");
				properties.setProperty("mail.imap.ssl.socketFactory.fallback", "false");
				properties.setProperty("mail.smtp.port", "" + port);
				properties.setProperty("mail.smtp.auth", "true");
				properties.setProperty("mail.smtp.socketFactory.port", "" + port);
				properties.setProperty("mail.smtp.EnableSSL.enable", "true");
				properties.setProperty("mail.debug", debug == true ? "true" : "false");
				properties.setProperty("mail.smtp.starttls.required", "true");
				properties.setProperty("mail.smtp.ssl.trust", host);

			} else {
				if (properties == null)
					properties = new Properties();
				properties.setProperty("mail.smtp.host", host);
				properties.setProperty("mail.smtp.port", "" + port);
				properties.setProperty("mail.smtp.auth", "true");
				properties.setProperty("mail.debug", debug == true ? "true" : "false");
			}
		}

		Session session = Session.getInstance(properties, authenticator);

		MimeMessage message = new MimeMessage(session);
		if (this.getEmailMessage().getName() != null) {
			message.setFrom(
					new InternetAddress(this.getEmailMessage().getEmailFrom(), this.getEmailMessage().getName()));
		} else {
			message.setFrom(new InternetAddress(this.getEmailMessage().getEmailFrom()));
		}

		InternetAddress[] to = this.emailMessage.getEmailTO();
		InternetAddress[] cc = this.emailMessage.getEmailCC();
		if (readingConfirmation)
			message.addHeader("Disposition-Notification-To", this.emailMessage.getEmailFrom());
		if (confirmationReceipt)
			message.addHeader("Return-Receipt-To", this.emailMessage.getEmailFrom());

		message.setRecipients(Message.RecipientType.TO, to);
		if (cc != null)
			message.setRecipients(javax.mail.Message.RecipientType.CC, cc);

		message.setSubject(this.emailMessage.getSubject());
		message.setSentDate(new Date());

		Multipart body = new MimeMultipart();

		MimeBodyPart bodyText = new MimeBodyPart();
		bodyText.setContent(this.getEmailMessage().getTextMessage(), "text/html; charset=utf-8");

		body.addBodyPart(bodyText);

		if (this.emailMessage.getAttachments().size() > 0) {

			InternetHeaders headers = new InternetHeaders();
			for (EmailAttachment anexo : this.emailMessage.getAttachments()) {
				headers.addHeader("Content-Type", anexo.getContentType());
				MimeBodyPart attach = new MimeBodyPart();
				attach.setDataHandler(
						new DataHandler(new ByteArrayDataSource(anexo.getContent(), anexo.getContentType())));
				attach.setFileName(anexo.getName());
				body.addBodyPart(attach);
			}
		}

		message.setContent(body);

		Transport.send(message);
	}

	public void showMessage() {
		StringBuffer sb = new StringBuffer();
		sb.append("De: ");
		sb.append(emailMessage.getEmailFrom());
		sb.append("\n");
		sb.append("Para: ");
		sb.append(emailMessage.getEmailTo());
		sb.append("\n");
		sb.append("Assunto: ");
		sb.append("\n\n");
		sb.append("Conte\372do: <OK>");
		for (EmailAttachment anexo : emailMessage.getAttachments())
			sb.append("  Anexo-> " + anexo.toString());
	}

	public Properties getProperties() {
		return properties;
	}

	public void setProperties(Properties properties) {
		this.properties = properties;
	}

	public boolean isConfirmationReceipt() {
		return confirmationReceipt;
	}

	public void setConfirmationReceipt(boolean confirmationReceipt) {
		this.confirmationReceipt = confirmationReceipt;
	}

	public boolean isReadingConfirmation() {
		return readingConfirmation;
	}

	public void setReadingConfirmation(boolean readingConfirmation) {
		this.readingConfirmation = readingConfirmation;
	}

	public EmailAuthenticator getAuthenticator() {
		return authenticator;
	}

	public void setAuthenticator(EmailAuthenticator authenticator) {
		this.authenticator = authenticator;
	}

	public EmailMessage getEmailMessage() {
		return emailMessage;
	}

	public void setEmailMessage(EmailMessage emailMessage) {
		this.emailMessage = emailMessage;
	}

}