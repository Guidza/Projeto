package pt.ulisboa.tecnico.learnjava.sibs.domain;

import pt.ulisboa.tecnico.learnjava.bank.exceptions.AccountException;

public class Withdrawn extends State {

	public Withdrawn() {
		super();
		// TODO Auto-generated constructor stub
	}

	@Override
	public void process(TransferOperation transfer, Sibs sibs) throws AccountException {
		try {
			success(transfer, sibs);
		} catch (AccountException e) {
			transfer.setState(new RetryingR(new Withdrawn()));

		}
	}

	public void cancel(TransferOperation transfer, Sibs sibs) throws AccountException {
		sibs.services.deposit(transfer.getSourceIban(), transfer.getValue());
		transfer.setState(new Canceled());
		sibs.removeUnprocessedOp();
	}

	@Override
	public void errorState(TransferOperation transfer, Sibs sibs) throws AccountException {
		transfer.setState(new ErrorState());
		sibs.services.deposit(transfer.getSourceIban(), transfer.getValue());
		sibs.removeUnprocessedOp();
	}

	@Override
	public void success(TransferOperation transfer, Sibs sibs) throws AccountException {
		sibs.services.deposit(transfer.getTargetIban(), transfer.getValue());
		if (transfer.getSourceIban().substring(0, 3).equals(transfer.getTargetIban().substring(0, 3))) {
			transfer.setState(new Completed());
			sibs.removeUnprocessedOp();
		} else {
			transfer.setState(new Deposited());
		}
	}
}
