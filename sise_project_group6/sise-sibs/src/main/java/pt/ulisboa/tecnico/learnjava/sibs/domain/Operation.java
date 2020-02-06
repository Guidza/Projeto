package pt.ulisboa.tecnico.learnjava.sibs.domain;

import pt.ulisboa.tecnico.learnjava.sibs.exceptions.OperationException;

public abstract class Operation {
	public static final String OPERATION_TRANSFER = "transfer";
	public static final String OPERATION_PAYMENT = "payment";

	private final String type;
	private final int value;

	public Operation(String type, int value, Sibs sibs) throws OperationException {
		checkParameters(type, value);
		this.type = type;
		this.value = value;

	}

	private void checkParameters(String type, int value) throws OperationException {
		if (type == null || !type.equals(OPERATION_TRANSFER) && !type.equals(OPERATION_PAYMENT)) {
			throw new OperationException(type);
		}

		if (value <= 0) {
			throw new OperationException(value);
		}
	}

	public int commission() {
		return 1;
	}

	public String getType() {
		return this.type;
	}

	public int getValue() {
		return this.value;
	}

	public void setState(State state) {
		// TODO Auto-generated method stub

	}

	public State getState() {
		// TODO Auto-generated method stub
		return null;
	}

}
