package sub.fwb.web;

import org.apache.commons.mail.Email;
import org.apache.commons.mail.EmailException;
import org.apache.commons.mail.SimpleEmail;

public class Mailer {

	private LogAccess logAccess = new LogAccess();
	private Email email = new SimpleEmail();

	public void sendLog(String mailAddress) {
		try {
			email.setHostName("localhost");
			email.setFrom("no-reply@fwb-online.de");
			email.setSubject("FWB-Importer Logausgabe");
			email.setMsg(logAccess.getLogContents());
			email.addTo(mailAddress);
			email.send();
		} catch (EmailException e) {
			e.printStackTrace();
			System.out.println("Could not send mail to " + mailAddress + "(" + e.getMessage() + ")");
		}
	}

}
