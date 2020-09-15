package filmow.exporter.service;

import java.io.File;
import java.io.IOException;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
public class EmailService  {

	 
		@Autowired
	    private JavaMailSender javaMailSender;
		
		public void sendEmailWithAttachment(File csv) throws MessagingException, IOException {

	        final MimeMessage msg = javaMailSender.createMimeMessage();

	        MimeMessageHelper helper = new MimeMessageHelper(msg, true);
	        helper.setFrom("alex@gerador.dev.br");
	        helper.setTo("alexjtricolor76@gmail.com");

	        helper.setSubject("Lista de filmes filmow exportada para CSV");


	        helper.setText("<h1>Arquivo em anexo.</h1>", true);

	        helper.addAttachment("filmes.csv", csv);

	        javaMailSender.send(msg);

	    }
}
