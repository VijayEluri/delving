package eu.europeana.controller.util;

import freemarker.template.Configuration;
import freemarker.template.DefaultObjectWrapper;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.mail.BodyPart;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import org.springframework.mail.MailPreparationException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessagePreparator;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Reader;
import java.io.StringWriter;
import java.util.Locale;
import java.util.Map;

/**
 * Handle all email sending
 *
 * @author Gerald de Jong <geralddejong@gmail.com>
 * @author Borys Omelayenko
 * @author Lucien van Wouw
 *
 */

public class EmailSender {

    private static final String TEMPLATE_NAME_AFFIX_TEXT = ".txt.ftl";
    private static final String TEMPLATE_NAME_AFFIX_HTML = ".html.ftl";

    private JavaMailSender mailSender;
    private String template;

    public void setMailSender(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    public void setTemplate(String templateName) {
        this.template = templateName;
    }

    public void sendEmail(String toEmail, String fromEmail, String subject, Map<String,Object> model) throws IOException, TemplateException {

        final String toEmailFinal = toEmail;
        final String fromEmailFinal = fromEmail;
        final String subjectFinal = subject;
        final Map<String,Object> modelFinal = model;

        MimeMessagePreparator preparator = new MimeMessagePreparator() {
            public void prepare(MimeMessage mimeMessage) throws MessagingException, IOException {
                mimeMessage.setRecipient(Message.RecipientType.TO, new InternetAddress(toEmailFinal));
                mimeMessage.setFrom(new InternetAddress(fromEmailFinal));
                mimeMessage.setSubject(subjectFinal);

                Multipart mp = new MimeMultipart("alternative");

                // Create a "text" Multipart message
                BodyPart textPart = new MimeBodyPart();
                Template textTemplate = getResourceTemplate(template + TEMPLATE_NAME_AFFIX_TEXT);
                final StringWriter textWriter = new StringWriter();
                try {
                    textTemplate.process(modelFinal, textWriter);
                } catch (TemplateException e) {
                    throw new MailPreparationException("Can't generate text subscription mail", e);
                }
                textPart.setDataHandler(new DataHandler(new DataSource() {
                    public InputStream getInputStream() throws IOException {
                        return new ByteArrayInputStream(textWriter.toString().getBytes("utf-8"));
                    }
                    public OutputStream getOutputStream() throws IOException {
                        throw new IOException("Read-only data");
                    }
                    public String getContentType() {
                        return "text/plain";
                    }
                    public String getName() {
                        return "main";
                    }
                }));
                mp.addBodyPart(textPart);

                // Create a "HTML" Multipart message
                Multipart htmlContent = new MimeMultipart("related");
                BodyPart htmlPage = new MimeBodyPart();
                Template htmlTemplate = getResourceTemplate(template + TEMPLATE_NAME_AFFIX_HTML);
                final StringWriter htmlWriter = new StringWriter();
                try {
                    htmlTemplate.process(modelFinal, htmlWriter);
                } catch (TemplateException e) {
                    throw new MailPreparationException("Can't generate HTML subscription mail", e);
                }
                htmlPage.setDataHandler(new DataHandler(new DataSource() {
                    public InputStream getInputStream() throws IOException {
                        return new ByteArrayInputStream(htmlWriter.toString().getBytes("utf-8"));
                    }
                    public OutputStream getOutputStream() throws IOException {
                        throw new IOException("Read-only data");
                    }
                    public String getContentType() {
                        return "text/html";
                    }
                    public String getName() {
                        return "main";
                    }
                }));
                htmlContent.addBodyPart(htmlPage);
                BodyPart htmlPart = new MimeBodyPart();
                htmlPart.setContent(htmlContent);
                mp.addBodyPart(htmlPart);

                mimeMessage.setContent(mp);
            }
        };

        try {
            mailSender.send(preparator);
        } catch (Exception e) {
            throw new IOException("Unable to send email" + e);
        }
        /*
           Multipart mp = new MimeMultipart("alternative");

           // plain text email
           Template templateText = getResourceTemplate(template + TEMPLATE_NAME_AFFIX_TEXT);
           BodyPart textPart = new MimeBodyPart();
           textPart.setDataHandler(new DataHandler(new DataSource() {
               public InputStream getInputStream() throws IOException {
                   return new StringBufferInputStream(textWriter.toString());
               }
               public OutputStream getOutputStream() throws IOException {
                   throw new IOException("Read-only data");
               }
               public String getContentType() {
                   return "text/plain";
               }
               public String getName() {
                   return "main";
               }
           }));

           mp.addBodyPart(textPart);

           // html email
           try {
               Template templateHtml = getResourceTemplate(template + TEMPLATE_NAME_AFFIX_TEXT);
               Multipart htmlContent = new MimeMultipart("related");
               BodyPart htmlPage = new MimeBodyPart();
               htmlPage.setDataHandler(new DataHandler(new DataSource() {
                   public InputStream getInputStream() throws IOException {
                       return new StringBufferInputStream(htmlWriter.toString());
                   }
                   public OutputStream getOutputStream() throws IOException {
                       throw new IOException("Read-only data");
                   }
                   public String getContentType() {
                       return "text/html";
                   }
                   public String getName() {
                       return "main";
                   }
               }));
               htmlContent.addBodyPart(htmlPage);
               BodyPart htmlPart = new MimeBodyPart();
               htmlPart.setContent(htmlContent);
               mp.addBodyPart(htmlPart);
           } catch (Exception e) {
               // TODO: log if no html template found
           }

           mimeMessage.setContent(mp);


           SimpleMailMessage message = new SimpleMailMessage();
           message.setSubject(subject);
           message.setFrom(fromEmail);
           message.setTo(toEmail);
           String emailText = createEmailText(model);
           message.setText(emailText);
           mailSender.send(message);
        */
    }
  /*
    private String createEmailText(Map<String,Object> model) throws IOException, TemplateException {
        StringWriter out = new StringWriter();
        template.process(model, out);
        return out.toString();
    }
               */

    protected Template getResourceTemplate(String fileName) throws IOException {
        return getTemplate(fileName, new InputStreamReader(getClass().getResourceAsStream(fileName)));
    }

    private Template getTemplate(String name, Reader reader) throws IOException {
        Configuration configuration = new Configuration();
        configuration.setLocale(new Locale("nl"));
        configuration.setObjectWrapper(new DefaultObjectWrapper());
        return new Template(name, reader, configuration);
    }
}