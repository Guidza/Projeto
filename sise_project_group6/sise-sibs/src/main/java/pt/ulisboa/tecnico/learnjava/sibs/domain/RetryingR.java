package pt.ulisboa.tecnico.learnjava.sibs.domain;

import pt.ulisboa.tecnico.learnjava.bank.exceptions.AccountException;

public class RetryingR extends State {
	private int count;

	public RetryingR() {
		super();
		count = 3;
	}

	@Override
	public void process(TransferOperation transfer, Sibs sibs) throws AccountException {
		try {
			sibs.services.withdraw(transfer.getSourceIban(), transfer.getValue());
			transfer.setState(new Withdrawn());
		} catch (AccountException e) {
			if (count != 0) {
				count--;
				this.process(transfer, sibs);
			}

			else {
				transfer.setState(new ErrorState());
				sibs.removeUnprocessedOp();
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
}
