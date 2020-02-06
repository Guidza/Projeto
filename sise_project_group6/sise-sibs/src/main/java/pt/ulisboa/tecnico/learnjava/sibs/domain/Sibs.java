//Carlos Morais nº3
//Guilherme Costa nº5
//grupo 6
package pt.ulisboa.tecnico.learnjava.sibs.domain;

import java.util.LinkedList;

import pt.ulisboa.tecnico.learnjava.bank.domain.Account;
import pt.ulisboa.tecnico.learnjava.bank.exceptions.AccountException;
import pt.ulisboa.tecnico.learnjava.bank.exceptions.BankException;
import pt.ulisboa.tecnico.learnjava.bank.services.Services;
import pt.ulisboa.tecnico.learnjava.sibs.exceptions.OperationException;
import pt.ulisboa.tecnico.learnjava.sibs.exceptions.SibsException;
import pt.ulisboa.tecnico.learnjava.sibs.exceptions.StateException;

public class Sibs {
	final Operation[] operations;
	Services services;
	private LinkedList<TransferOperation> unprocessedOp;

	public Sibs(int maxNumberOfOperations, Services services) {
		this.operations = new Operation[maxNumberOfOperations];
		this.services = services;
		unprocessedOp = new LinkedList<TransferOperation>();//
	}

	public void transfer(String sourceIban, String targetIban, int amount)
			throws SibsException, AccountException, OperationException, BankException {
		Account target = services.getAccountByIban(targetIban);
		if (services.getAccountByIban(targetIban) == null || services.getAccountByIban(sourceIban) == null) {
			throw new AccountException();
		}

		int position = addOperation(Operation.OPERATION_TRANSFER, sourceIban, targetIban, amount);
		TransferOperation op = (TransferOperation) this.getOperation(position);
		unprocessedOp.add(op);

	}

	public void cancelOperation(int position) throws AccountException, StateException {
		TransferOperation transfer = (TransferOperation) this.operations[position];
		unprocessedOp.stream().filter(e -> e.equals(transfer)).findAny().get().getState().cancel(transfer, this);
	}

	public void processOperations() throws AccountException {
		while (!unprocessedOp.isEmpty()) {
			TransferOperation op = this.getUnprocessedOp();
			this.getUnprocessedOp().getState().process(op, this);
		}
	}

	public LinkedList<TransferOperation> getQueue() {
		return this.unprocessedOp;
	}

	public TransferOperation removeUnprocessedOp() {
		return unprocessedOp.poll();
	}

	public TransferOperation getUnprocessedOp() {
		return unprocessedOp.peek();
	}

	public int addOperation(String type, String sourceIban, String targetIban, int value)
			throws OperationException, SibsException {
		int position = -1;
		for (int i = 0; i < this.operations.length; i++) {
			if (this.operations[i] == null) {
				position = i;
				break;
			}
		}

		if (position == -1) {
			throw new SibsException();
		}

		Operation operation;
		if (type.equals(Operation.OPERATION_TRANSFER)) {
			operation = new TransferOperation(sourceIban, targetIban, value, this);

		} else {
			operation = new PaymentOperation(targetIban, value, this);
		}

		this.operations[position] = operation;
		return position;
	}

	public void removeOperation(int position) throws SibsException {
		if (position < 0 || position > this.operations.length) {
			throw new SibsException();
		}
		this.operations[position] = null;
	}

	public Operation getOperation(int position) throws SibsException {
		if (position < 0 || position > this.operations.length) {
			throw new SibsException();
		}
		return this.operations[position];
	}

	public int getNumberOfOperations() {
		int result = 0;
		for (int i = 0; i < this.operations.length; i++) {
			if (this.operations[i] != null) {
				result++;
			}
		}
		return result;
	}

	public int getTotalValueOfOperations() {
		int result = 0;
		for (int i = 0; i < this.operations.length; i++) {
			if (this.operations[i] != null) {
				result = result + this.operations[i].getValue();
			}
		}
		return result;
	}

	public Operation[] getOperations() {
		return operations;
	}

	public int getTotalValueOfOperationsForType(String type) {
		int result = 0;
		for (int i = 0; i < this.operations.length; i++) {
			if (this.operations[i] != null && this.operations[i].getType().equals(type)) {
				result = result + this.operations[i].getValue();
			}
		}
		return result;
	}
}
