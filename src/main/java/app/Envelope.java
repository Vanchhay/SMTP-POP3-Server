package app;

import java.util.ArrayList;
import java.util.List;

public class Envelope {

	private String mailFrom;
	private String subject;
	private String message = "";
	private String status;
	private String header = "";
	private String uid;

	private  List<String> mailTo = new ArrayList<>();

	protected final String MAIL_FROM = "MAIL FROM";
	protected final String RCPT_TO = "RCPT TO";
	protected final String DATA = "DATA";
	protected final String SENT = "SENT";

	public Envelope(String uid, String header, String subject, String message, String mailFrom, List<String> mailTo) {
		this.uid = uid;
		this.header = header;
		this.subject = subject;
		this.message = message;
		this.mailFrom = mailFrom;
		this.mailTo = mailTo;
	}

	public Envelope() {
		this.status = MAIL_FROM;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public String getMailFrom() {
		return mailFrom;
	}

	public void setMailFrom(String mailFrom) {
		this.mailFrom = mailFrom;
	}

	public String getSubject() {
		return subject;
	}

	public void setSubject(String subject) {
		this.subject = subject;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public List<String> getMailTo() {
		return mailTo;
	}

	public void setMailTo(List mailTo) {
		this.mailTo = mailTo;
	}

	public String getHeader() {
		return header;
	}

	public void setHeader(String header) {
		this.header = header;
	}

	public String getUid() {
		return uid;
	}

	public void setUid(String uid) {
		this.uid = uid;
	}

}
