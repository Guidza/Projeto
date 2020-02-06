package pt.ulisboa.tecnico.learnjava.sibs.domain;

import pt.ulisboa.tecnico.learnjava.bank.exceptions.AccountException;

public class Registered extends State {

	public Registered() {
		super();
	}

	@Override
	public void process(TransferOperation transfer, Sibs sibs) throws AccountException {
		try {
			sibs.services.withdraw(transfer.getSourceIban(), transfer.getValue());
			transfer.setState(new Withdrawn());
		} catch (AccountException e) {
			transfer.setState(new RetryingR());

		}

	}

	@Override
	public void cancel(TransferOperation transfer, Sibs sibs) {
		transfer.setState(new Canceled());
		sibs.removeUnprocessedOp();
	}
}
