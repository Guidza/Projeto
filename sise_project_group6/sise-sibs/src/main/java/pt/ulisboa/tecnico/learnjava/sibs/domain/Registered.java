package pt.ulisboa.tecnico.learnjava.sibs.domain;

import pt.ulisboa.tecnico.learnjava.bank.exceptions.AccountException;

public class Registered extends State {

	public Registered() {
		super();
	}

	@Override
	public void process(TransferOperation transfer, Sibs sibs) throws AccountException {
		try {
			success(transfer, sibs);
		} catch (AccountException e) {
			transfer.setState(new RetryingR(new Registered()));

		}

	}

	@Override
	public void cancel(TransferOperation transfer, Sibs sibs) {
		transfer.setState(new Canceled());
		sibs.removeUnprocessedOp();
	}

	@Override
	public void errorState(TransferOperation transfer, Sibs sibs) {
		transfer.setState(new ErrorState());
		sibs.removeUnprocessedOp();
	}

	@Override
	public void success(TransferOperation transfer, Sibs sibs) throws AccountException {
		sibs.services.withdraw(transfer.getSourceIban(), transfer.getValue());
		transfer.setState(new Withdrawn());
	}
}
