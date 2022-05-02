package com.ebp.openQuarterMaster.lib.driver.interaction.exceptions;

public class SerialInteractionUnlockedException extends IllegalStateException {
	
	public SerialInteractionUnlockedException() {
	}
	
	public SerialInteractionUnlockedException(String s) {
		super(s);
	}
	
	public SerialInteractionUnlockedException(String message, Throwable cause) {
		super(message, cause);
	}
	
	public SerialInteractionUnlockedException(Throwable cause) {
		super(cause);
	}
}
