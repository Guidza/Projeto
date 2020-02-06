package pt.ulisboa.tecnico.learnjava.sibs.domain;

import pt.ulisboa.tecnico.learnjava.bank.exceptions.AccountException;

public class RetryingW extends State {
	private int count;

	public RetryingW() {
		super();
		count = 3;
	}

	@Override
	public void process(TransferOperation transfer, Sibs sibs) throws AccountException {
		try {
			sibs.services.deposit(transfer.getTargetIban(), transfer.getValue());
			if (transfer.getSourceIban().substring(0, 3).equals(transfer.getTargetIban().substring(0, 3))) {
				transfer.setState(new Completed());
				sibs.removeUnprocessedOp();
			} else {
				transfer.setState(new Deposited());
			}
		} catch (AccountException e) {
			if (count != 0) {
				count--;
				this.process(transfer, sibs);
			}

			else {
				transfer.setState(new ErrorState());
				sibs.services.deposit(transfer.getSourceIban(), transfer.getValue());
				sibs.removeUnprocessedOp();
			}
		}
	}

	@Override
	public void cancel(TransferOperation transfer, Sibs sibs) throws AccountException {
		sibs.services.deposit(transfer.getSourceIban(), transfer.getValue());
		transfer.setState(new Canceled());
		sibs.removeUnprocessedOp();
	}

	@Override
	public int getCount() {
		return count;
	}
}
