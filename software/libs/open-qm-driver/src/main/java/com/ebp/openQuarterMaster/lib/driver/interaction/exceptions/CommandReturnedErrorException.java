package com.ebp.openQuarterMaster.lib.driver.interaction.exceptions;

import com.ebp.openQuarterMaster.lib.driver.interaction.command.commands.complex.ErrorCommand;
import lombok.Getter;

public class CommandReturnedErrorException extends IllegalStateException {
	
	@Getter
	private final ErrorCommand errorCommand;
	
	public CommandReturnedErrorException(ErrorCommand errorCommand, String s) {
		super(s);
		this.errorCommand = errorCommand;
	}
	
	public CommandReturnedErrorException(ErrorCommand errorCommand, String message, Throwable cause) {
		super(message, cause);
		this.errorCommand = errorCommand;
	}
	
	public CommandReturnedErrorException(ErrorCommand errorCommand, Throwable cause) {
		super(cause);
		this.errorCommand = errorCommand;
	}
}
