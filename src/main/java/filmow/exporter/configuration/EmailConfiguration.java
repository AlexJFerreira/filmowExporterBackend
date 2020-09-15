package filmow.exporter.configuration;

import java.util.Properties;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;

@Configuration
@ComponentScan(basePackages = { "filmow.exporter" })
public class EmailConfiguration {

    @Bean
    public static JavaMailSender getJavaMailSender() {
        JavaMailSenderImpl mailSender = new JavaMailSenderImpl();
        
        mailSender.setHost("in-v3.mailjet.com");
        mailSender.setPort(587);
        
        mailSender.setUsername("55ca75b0202fcd3518f0d5e2cd137524");
        mailSender.setPassword("3181180fa341d6b41e146bb2c3def309");
        
        Properties props = mailSender.getJavaMailProperties();
        props.put("mail.transport.protocol", "smtp");
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "false");
        props.put("mail.debug", "true");
        
        return mailSender;
    }
    

}