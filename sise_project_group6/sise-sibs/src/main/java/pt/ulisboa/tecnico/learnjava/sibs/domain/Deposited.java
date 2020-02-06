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
			sibs.services.withdraw(transfer.getSourceIban(), transfer.commission());
			transfer.setState(new Completed());
			sibs.removeUnprocessedOp();
		} catch (AccountException e) {
			transfer.setState(new RetryingD());
		}
	}

	@Override
	public void cancel(TransferOperation transfer, Sibs sibs) throws AccountException {
		sibs.services.withdraw(transfer.getTargetIban(), transfer.getValue());
		sibs.services.deposit(transfer.getSourceIban(), transfer.getValue());
		transfer.setState(new Canceled());
		sibs.removeUnprocessedOp();
	}

}
