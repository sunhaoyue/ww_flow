package com.wwgroup.common.exceptions;

public class MailException extends Exception{
	private static final long serialVersionUID = -4324496475442438866L;

	public MailException() {
		super();
		// TODO Auto-generated constructor stub
	}

	public MailException(String message, Throwable cause) {
		super(message, cause);
		// TODO Auto-generated constructor stub
	}

	public MailException(String message) {
		super(message);
	}

	public MailException(Throwable cause) {
		super(cause);
		// TODO Auto-generated constructor stub
	}
}
