package pt.ulisboa.tecnico.learnjava.sibs.domain;

import pt.ulisboa.tecnico.learnjava.bank.exceptions.AccountException;

public class RetryingD extends State {
	private int count;

	public RetryingD() {
		super();
		count = 3;
	}

	@Override
	public void process(TransferOperation transfer, Sibs sibs) throws AccountException {
		try {
			sibs.services.withdraw(transfer.getSourceIban(), transfer.commission());
			transfer.setState(new Completed());
			sibs.removeUnprocessedOp();
		} catch (AccountException e) {
			if (count != 0) {
				count--;
				this.process(transfer, sibs);
			}

			else {
				transfer.setState(new ErrorState());
				sibs.services.withdraw(transfer.getTargetIban(), transfer.getValue());
				sibs.services.deposit(transfer.getSourceIban(), transfer.getValue());
				sibs.removeUnprocessedOp();
			}
		}
	}

	@Override
	public void cancel(TransferOperation transfer, Sibs sibs) throws AccountException {
		sibs.services.withdraw(transfer.getTargetIban(), transfer.getValue());
		sibs.services.deposit(transfer.getSourceIban(), transfer.getValue());
		transfer.setState(new Canceled());
		sibs.removeUnprocessedOp();
	}

	@Override
	public int getCount() {
		return count;
	}

}
