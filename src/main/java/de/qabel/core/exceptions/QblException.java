package de.qabel.core.exceptions;

public abstract class QblException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = 4261199805687953191L;


	public QblException() {
	}

	public QblException(String msg) {
		super(msg);
	}
}
