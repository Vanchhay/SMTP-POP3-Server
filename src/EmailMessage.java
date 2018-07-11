public class EmailMessage {

	private String mailFrom, mailTo;
	private String subject;
	private String message;
	private String status;

	protected final String MAIL_FROM = "MAIL FROM";
	protected final String RCPT_TO = "RCPT TO";
	protected final String DATA = "DATA";
	protected final String SENT = "SENT";

	public EmailMessage() {
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

	public String getMailTo() {
		return mailTo;
	}

	public void setMailTo(String mailTo) {
		this.mailTo = mailTo;
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
}
