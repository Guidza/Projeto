package pt.ulisboa.tecnico.learnjava.sibs.domain;

import pt.ulisboa.tecnico.learnjava.bank.exceptions.AccountException;

public class Deposited extends State {

	public Deposited() {
		super();
		// TODO Auto-generated constructor stub
	}

	@Override
	public void process(TransferOperation transfer, Sibs sibs) throws AccountException {
		try {
			success(transfer, sibs);
		} catch (AccountException e) {
			transfer.setState(new RetryingR(transfer.getState()));
		}
	}

	@Override
	public void cancel(TransferOperation transfer, Sibs sibs) throws AccountException {
		sibs.services.withdraw(transfer.getTargetIban(), transfer.getValue());
		sibs.services.deposit(transfer.getSourceIban(), transfer.getValue());
		transfer.setState(new Canceled());
		sibs.removeUnprocessedOp();
	}

	public void errorState(TransferOperation transfer, Sibs sibs) throws AccountException {
		transfer.setState(new ErrorState());
		sibs.services.withdraw(transfer.getTargetIban(), transfer.getValue());
		sibs.services.deposit(transfer.getSourceIban(), transfer.getValue());
		sibs.removeUnprocessedOp();

	}

	public void success(TransferOperation transfer, Sibs sibs) throws AccountException {
		sibs.services.withdraw(transfer.getSourceIban(), transfer.commission());
		transfer.setState(new Completed());
		sibs.removeUnprocessedOp();

	}

}
