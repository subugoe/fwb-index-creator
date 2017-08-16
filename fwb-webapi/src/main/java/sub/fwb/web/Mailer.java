package sub.fwb.web;

import org.apache.commons.mail.Email;
import org.apache.commons.mail.EmailException;
import org.apache.commons.mail.SimpleEmail;

public class Mailer {

	private LogAccess logAccess = new LogAccess();
	private Email email = new SimpleEmail();
	private Environment env = new Environment();

	public void sendLog(String mailAddress) {
		try {
			String user = env.getVariable("MAIL_USER");
			String password = env.getVariable("MAIL_PASSWORD");
			if (notEmpty(user) && notEmpty(password)) {
				email.setAuthentication(user, password);
			}
			email.setHostName(env.getVariable("MAIL_HOST"));
			email.setSmtpPort(587);
			email.setFrom("no-reply@fwb-online.de");
			email.setDebug(false);
			email.setSubject("FWB-Importer Logausgabe");
			email.setMsg(logAccess.getLogContents());
			String[] addresses = mailAddress.split("[,;]");
			for (String address : addresses) {
				email.addTo(address);
			}
			email.send();
		} catch (EmailException e) {
			e.printStackTrace();
			System.out.println("Could not send mail to " + mailAddress + "(" + e.getMessage() + ")");
		}
	}

	private boolean notEmpty(String s) {
		return s != null && !s.isEmpty();
	}

}
