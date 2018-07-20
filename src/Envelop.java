import java.util.ArrayList;
import java.util.List;

public class Envelop {

	private String mailFrom;
	private String subject;
	private String message = "";
	private String status;
	private String header = "";
	private String meetingID;

	private  List<String> mailTo = new ArrayList<>();
//	, mailCc, mailBcc;

	protected final String MAIL_FROM = "MAIL FROM";
	protected final String RCPT_TO = "RCPT TO";
	protected final String DATA = "DATA";
	protected final String SENT = "SENT";

	public Envelop(String meetingID, String header, String subject, String message, String mailFrom, List<String> mailTo) {
		this.meetingID = meetingID;
		this.header = header;
		this.subject = subject;
		this.message = message;
		this.mailFrom = mailFrom;
		this.mailTo = mailTo;
	}

	public Envelop() {
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

	public void setMailTo(String mailTo) {
		this.mailTo.add(mailTo);
	}

	public String getHeader() {
		return header;
	}

	public void setHeader(String header) {
		this.header = header;
	}

	public String getMeetingID() {
		return meetingID;
	}

	public void setMeetingID(String meetingID) {
		this.meetingID = meetingID;
	}

//	public List<String> getMailCc() {
//		return mailCc;
//	}
//
//	public void setMailCc(String mailCc) {
//		this.mailCc.add(mailCc);
//	}
//
//	public List<String> getMailBcc() {
//		return mailBcc;
//	}
//
//	public void setMailBcc(String mailBcc) {
//		this.mailBcc.add(mailBcc);
//	}
}
