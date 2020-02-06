package pt.ulisboa.tecnico.learnjava.sibs.domain;

import pt.ulisboa.tecnico.learnjava.bank.exceptions.AccountException;

public class RetryingR extends State {
	private int count;
	public State prevState;

	public RetryingR(State state) {
		super();
		count = 3;
		this.prevState = state;
	}

	@Override
	public void process(TransferOperation transfer, Sibs sibs) throws AccountException {
		try {
			prevState.success(transfer, sibs);
		} catch (AccountException e) {
			if (count != 0) {
				count--;
				this.process(transfer, sibs);
			}

			else {
				prevState.errorState(transfer, sibs);
			}
		}
	}

	@Override
	public void cancel(TransferOperation transfer, Sibs sibs) {
		transfer.setState(new Canceled());
		sibs.removeUnprocessedOp();
	}

	@Override
	public int getCount() {
		return count;
	}

	@Override
	public void success(TransferOperation transfer, Sibs sibs) {
		if (prevState instanceof Registered) {

		}
	}

}
