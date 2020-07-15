package cl.gesvita.ws.enviocorreo;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
//import javax.servlet.ServletException;
//import javax.servlet.http.HttpServletRequest;
//import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;

//import com.google.gson.Gson;
import javax.mail.Address;

public class BusinessEnviarCorreo {
	static Logger logger = Logger.getLogger(BusinessEnviarCorreo.class);

	public void Enviar(String para, String titulo, String body) {
		Boolean archivor = false;
		Properties prop = new Properties();
		String archivoPro = System.getProperty("catalina.base") + "/webapps/propiedades/email.properties";
		try {
			prop.load(new FileInputStream(archivoPro));
			archivor = true;

		} catch (FileNotFoundException e1) {
			// TODO Auto-generated catch block
			// e1.printStackTrace();
			logger.error("Error al leer propiedades: " + e1.getMessage());
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			logger.error("Error al leer propiedades: " + e1.getMessage());
			// e1.printStackTrace();
		}

		/*
		 * final String username = "enviocorreo@actitudtecnologia.cl"; final String
		 * password = "Act1tud.V4ld1v1a.3018";
		 * 
		 * Properties props = new Properties(); props.put("mail.smtp.auth", "true");
		 * props.put("mail.smtp.starttls.enable", "true"); props.put("mail.smtp.host",
		 * "200.35.156.56"); props.put("mail.smtp.ssl.trust", "200.35.156.56");
		 * props.put("mail.smtp.port", "587");
		 */
		if (archivor) {
			final String username = prop.getProperty("username");
			final String password = prop.getProperty("password");

			Properties props = new Properties();
			props.put("mail.smtp.auth", prop.getProperty("authSmtp"));
			props.put("mail.smtp.starttls.enable", prop.getProperty("startTLS"));
			props.put("mail.smtp.host", prop.getProperty("host"));
			props.put("mail.smtp.ssl.trust", prop.getProperty("sslTrust"));
			props.put("mail.smtp.port", prop.getProperty("port"));
			// DUMMY
			/*
			 * body = "<html><head></head><body>"; body +=
			 * "<p style=\"text-align:center\"><span style=\"color:#2980b9\"><strong><img alt=\"\" src=\"http://190.215.53.149/codelco/docs/img/codelco_logo1.PNG\" style=\"height:80px; width:86px\" /><br /><span style=\"font-size:22px\">Notificaci&oacute;n-Creaci&oacute;n de Proyecto</span></strong></span></p> <p><span style=\"color:#2980b9\">Estimado</span></p> <p><span style=\"color:#2980b9\">Este mail es para notificar&nbsp;que el d&iacute;a&nbsp;<strong>{{FECHA_CREACION}}</strong>&nbsp;el Jefe de Proyecto&nbsp;<strong>{{NOMBRE_JEFEPROYECTO}}</strong>&nbsp;realiz&oacute; la creaci&oacute;n del &nbsp;Proyecto&nbsp;</span></p> <p><span style=\"color:#2980b9\"><strong>{{NOMBRE_PROYECTO}}</strong> perteneciente al centro de costo&nbsp;{{NOMBRE_PRESUPUESTO}}, el detalle es el siguiente:&nbsp;</span></p> <table align=\"center\" border=\"1\" style=\"width:800px\"> <caption> <p style=\"text-align:center\">&nbsp;</p> </caption> <tbody> <tr> <td style=\"text-align:center; width:250px\"><strong><em><span style=\"color:#0000ff\"><span style=\"background-color:#ffffff\">Nombre Proyecto</span></span></em></strong></td> <td style=\"text-align:center; width:100px\"><strong><em><span style=\"color:#000000\"><span style=\"background-color:#ffffff\">Jefe Proyecto</span></span></em></strong></td> <td style=\"text-align:center; width:40px\"><strong><em><span style=\"color:#000000\"><span style=\"background-color:#ffffff\">Magnitud</span></span></em></strong></td> <td style=\"text-align:center; width:80px\"><strong><em><span style=\"color:#000000\"><span style=\"background-color:#ffffff\">Area</span></span></em></strong></td> <td style=\"text-align:center; width:80px\"><strong><em><span style=\"color:#000000\"><span style=\"background-color:#ffffff\">Sponsor</span></span></em></strong></td> <td style=\"text-align:center; width:80px\"><strong><em><span style=\"color:#000000\"><span style=\"background-color:#ffffff\">Fecha Inicio</span></span></em></strong></td> <td style=\"text-align:center; width:80px\"><strong><em><span style=\"color:#000000\"><span style=\"background-color:#ffffff\">Fecha Fin</span></span></em></strong></td> </tr> <tr> <td style=\"text-align:center; width:146px\"><strong><span style=\"color:#2c3e50\"><span style=\"background-color:#ffffff\">{{NOMBRE_PROYECTO}}</span></span></strong></td> <td style=\"text-align:center; width:55px\"><strong><span style=\"color:#2c3e50\"><span style=\"background-color:#ffffff\">{{NOMBRE_USUARIO}}</span></span></strong></td> <td style=\"text-align:center; width:126px\"><strong><span style=\"color:#2c3e50\">{{MAGNITUD}}</span></strong></td> <td style=\"text-align:center; width:126px\"><strong><span style=\"color:#2c3e50\">{{AREA}}</span></strong></td> <td style=\"text-align:center; width:126px\"><strong><span style=\"color:#2c3e50\"><span style=\"background-color:#ffffff\">{{SPONSOR}}</span></span></strong></td> <td style=\"text-align:center; width:126px\"><strong><span style=\"color:#2c3e50\">{{FECHA_INI_REAL}}</span></strong></td> <td style=\"text-align:center; width:126px\"><strong><span style=\"color:#2c3e50\"><span style=\"background-color:#ffffff\">{{FECHA_FIN_REAL}}</span></span></strong></td> </tr> </tbody> </table> <p>&nbsp;</p> <ul> <li><span style=\"color:#3366ff\">Para visualizar la Gantt</span>: <a href=\"http://www.actitudtecnologia.cl/App/MonitorSodimac\" target=\"_blank\">Pinche Aqu&iacute;</a></li> </ul> <p><span style=\"color:#2980b9\">Saludos</span></p> <p><span style=\"color:#2980b9\">Atentamente Soporte ActtSPG</span></p> <p>&nbsp;</p> <p><span style=\"color:#2980b9\">Datos Adicionales:</span></p> <p><span style=\"color:#2980b9\">ALERTAS:</span></p> <p><span style=\"color:#2980b9\">=======</span></p> <p><span style=\"color:#2980b9\"><strong>Tipo Alerta: {{TIPO_ALERTA}}</strong>;</span></p> <p><span style=\"color:#2980b9\"><strong>Nombre : {{NOMBRE_ALERTA}}</strong>;</span></p> <p><span style=\"color:#2980b9\"><strong>DescripciÃn: {{DESCRIPCION_ALERTA}}</strong>;</span></p> <p><span style=\"color:#2980b9\"><strong>Severidad : {{SEVERIDAD_ALERTA}}</strong>;</span></p> <p><span style=\"color:#2980b9\">NOTFICACION:</span></p> <p><span style=\"color:#2980b9\">=======</span></p> <p><span style=\"color:#2980b9\"><strong>Origen : {{ORIGEN_NOTIFICACION}}</strong>;</span></p> <p><span style=\"color:#2980b9\"><strong>CÃdigo: {{CODI_NOTIFICACION}}</strong>;</span></p> <p><span style=\"color:#2980b9\"><strong>Asunto : {{ASUNTO_NOTIFICACION}}</strong>;</span></p> <p><span style=\"color:#2980b9\"><strong>DescripciÃn : {{DESCRIPCION_NOTIFICACION}}</strong>;</span></p> <p><span style=\"color:#2980b9\"><strong>Ocurrencia: {{NUMERO_OCURRENCIA}}</strong>;</span></p> <p><span style=\"color:#2980b9\">ORIGEN:</span></p> <p><span style=\"color:#2980b9\">=======</span></p> <p><span style=\"color:#2980b9\"><strong>PAIS: {{NOMBRE_PAIS}}</strong>;</span></p> <p><span style=\"color:#2980b9\"><strong>Empresa: {{NOMBRE_EMPRESA}}</strong>;</span></p> <p><span style=\"color:#2980b9\"><strong>Modulo: {{NOMBRE_MODULO}}</strong>;</span></p> <p><span style=\"color:#2980b9\"><strong>Submodulo: {{NOMBRE_SUBMODULO}}</strong>;</span></p> <p><span style=\"color:#2980b9\"><strong>Area: {{NOMBRE_AREA}}</strong>;</span></p> <p><span style=\"color:#2980b9\">Datos Proyecto:</span></p> <p><span style=\"color:#2980b9\">=======</span></p> <p><span style=\"color:#2980b9\"><strong>Tipo Proy: {{DESCRIPCION_TIPO}}</strong>;</span></p> <p><span style=\"color:#2980b9\"><strong>Estado: {{NOMBRE_ESTADO}}</strong>;</span></p> <p><span style=\"color:#2980b9\"><strong>Magnitud: {{NOMBRE_MAGNITUD}}</strong>;</span></p> <p><span style=\"color:#2980b9\"><strong>Prioridad : {{NOMBRE_PRIORIDAD}}</strong>;</span></p>"
			 * ; body += "</body>";
			 */
			Session session = Session.getInstance(props, new javax.mail.Authenticator() {
				protected PasswordAuthentication getPasswordAuthentication() {
					return new PasswordAuthentication(username, password);
				}
			});

			try {

				Message message = new MimeMessage(session);
				message.setHeader("content-type", "text/html");
				message.setFrom(new InternetAddress(username));
                                Message.RecipientType msgTo = Message.RecipientType.TO;
                                Address[] msgPara = InternetAddress.parse(para);
				message.setRecipients(msgTo, msgPara);
                                message.setSubject(titulo.replaceAll("\r|\n", ""));
                                // message.setText(body);
				message.setContent(body, "text/html;charset=UTF-8");

				Transport.send(message);

				System.out.println("Done");

			} catch (MessagingException e) {
				throw new RuntimeException(e);
			}
		}
	}
}
