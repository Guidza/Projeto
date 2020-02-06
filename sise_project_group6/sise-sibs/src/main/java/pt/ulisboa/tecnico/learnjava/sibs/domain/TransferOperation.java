package pt.ulisboa.tecnico.learnjava.sibs.domain;

import pt.ulisboa.tecnico.learnjava.sibs.exceptions.OperationException;

public class TransferOperation extends Operation {
	private final String sourceIban;
	private final String targetIban;
	private State state;
	private int value;
	private Sibs sibs;

	public TransferOperation(String sourceIban, String targetIban, int value, Sibs sibs) throws OperationException {
		super(Operation.OPERATION_TRANSFER, value, sibs);

		if (invalidString(sourceIban) || invalidString(targetIban)) {
			throw new OperationException();
		}

		this.sourceIban = sourceIban;
		this.targetIban = targetIban;
		this.state = new Registered();
		this.value = value;
		this.sibs = sibs;

	}

	@Override
	public State getState() {
		return state;
	}

	@Override
	public void setState(State state) {
		this.state = state;
	}

	private boolean invalidString(String name) {
		return name == null || name.length() == 0;
	}

	@Override
	public int commission() {
		if (sourceIban.substring(0, 3).equals(targetIban.substring(0, 3))) {
			return 0;
		}
		return (int) Math.round(super.commission() + getValue() * 0.05);

	}

	public String getSourceIban() {
		return this.sourceIban;
	}

	public String getTargetIban() {
		return this.targetIban;
	}

}
