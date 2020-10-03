package filmow.exporter.service;

import java.io.File;
import java.io.IOException;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
public class EmailService  {

		@Autowired
	    private JavaMailSender javaMailSender;
		
		private static final Logger logger = LoggerFactory.getLogger(EmailService.class);

		
		public void sendEmailWithAttachment(File csv, String userEmail, String filmowUserName) throws MessagingException, IOException {

	        final MimeMessage msg = javaMailSender.createMimeMessage();

	        MimeMessageHelper helper = new MimeMessageHelper(msg, true);
	        helper.setFrom("exportacaofilmow@gerador.dev.br");
	        helper.setTo(userEmail);

	        helper.setSubject("Exportação Filmow");
	        helper.setText(getSubjectEmail(filmowUserName),true);

	        helper.addAttachment("filmes.csv", csv);

	        javaMailSender.send(msg);
			logger.info("Email enviado com sucesso para endereço: {}", userEmail);


	    }

		private String getSubjectEmail(String filmowUserName) {
			final StringBuilder subject = new StringBuilder();
			subject.append("<h1> Olá ").append(filmowUserName).append(". </h1>")
			.append(" <p>Obrigado por usar a ferramenta exportador filmow, segue em anexo o csv com seus filmes marcados como visto além das notas dadas.")
			.append(" Ele já vem no formato certo para ser importado no <a href=\"https://letterboxd.com/import/\">Letterboxd</a>")
			.append(" e em breve teremos mais recursos como exportar comentários e lista de filmes que você quer ver.</p>");
			return subject.toString();
		}
}
