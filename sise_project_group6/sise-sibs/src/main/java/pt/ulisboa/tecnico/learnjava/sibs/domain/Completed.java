package pt.ulisboa.tecnico.learnjava.sibs.domain;

import pt.ulisboa.tecnico.learnjava.sibs.exceptions.StateException;

public class Completed extends State {

	public Completed() {
		super();
		// TODO Auto-generated constructor stub
	}

	@Override
	public void process(TransferOperation transfer, Sibs sibs) {

	}

	@Override
	public void cancel(TransferOperation transfer, Sibs sibs) throws StateException {

	}

}
