package pt.ulisboa.tecnico.learnjava.sibs.sibs;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.Test;

import pt.ulisboa.tecnico.learnjava.bank.domain.CheckingAccount;
import pt.ulisboa.tecnico.learnjava.bank.exceptions.AccountException;
import pt.ulisboa.tecnico.learnjava.bank.exceptions.BankException;
import pt.ulisboa.tecnico.learnjava.bank.services.Services;
import pt.ulisboa.tecnico.learnjava.sibs.domain.Completed;
import pt.ulisboa.tecnico.learnjava.sibs.domain.ErrorState;
import pt.ulisboa.tecnico.learnjava.sibs.domain.Registered;
import pt.ulisboa.tecnico.learnjava.sibs.domain.Sibs;
import pt.ulisboa.tecnico.learnjava.sibs.domain.TransferOperation;
import pt.ulisboa.tecnico.learnjava.sibs.exceptions.OperationException;
import pt.ulisboa.tecnico.learnjava.sibs.exceptions.SibsException;

public class ProcessOperationsMethodTest {
	private String sourceIban = "CGDCK1";
	private String targetIban = "BPICK2";

	@Test
	public void unprocessedTransferOpIsAddedToQueue()
			throws OperationException, AccountException, SibsException, BankException {
		Services service = mock(Services.class);
		Sibs sibs = new Sibs(3, service);
		when(service.getAccountByIban(sourceIban)).thenReturn(mock(CheckingAccount.class));
		when(service.getAccountByIban(targetIban)).thenReturn(mock(CheckingAccount.class));
		when(service.getAccountByIban(sourceIban).getBalance()).thenReturn(10000);
		//
		sibs.transfer(sourceIban, targetIban, 10);
		assertEquals(1, sibs.getQueue().size());
		assertEquals(1, sibs.getNumberOfOperations());
		assert (sibs.getUnprocessedOp() instanceof TransferOperation);
		assert (sibs.getUnprocessedOp().getState() instanceof Registered);
	}

	@Test
	public void opIsProcessedUntilFinalStateCompleted()
			throws SibsException, AccountException, OperationException, BankException {
		Services service = mock(Services.class);
		Sibs sibs = new Sibs(3, service);
		when(service.getAccountByIban(sourceIban)).thenReturn(mock(CheckingAccount.class));
		when(service.getAccountByIban(targetIban)).thenReturn(mock(CheckingAccount.class));
		when(service.getAccountByIban(sourceIban).getBalance()).thenReturn(10000);
		//
		sibs.transfer(sourceIban, targetIban, 10);
		assert (sibs.getOperation(0).getState() instanceof Registered);
		assertEquals(1, sibs.getNumberOfOperations());
		assertEquals(1, sibs.getQueue().size());
		//
		sibs.processOperations();
		assertEquals(1, sibs.getNumberOfOperations());
		assertEquals(0, sibs.getQueue().size());
		assert (sibs.getOperation(0).getState() instanceof Completed);
	}

	@Test
	public void opIsProcessedUntilFInalStateError()
			throws AccountException, SibsException, OperationException, BankException {
		Services service = mock(Services.class);
		Sibs sibs = new Sibs(3, service);
		when(service.getAccountByIban(sourceIban)).thenReturn(mock(CheckingAccount.class));
		when(service.getAccountByIban(targetIban)).thenReturn(mock(CheckingAccount.class));
		doThrow(new AccountException()).when(service).withdraw(sourceIban, 10);
		//
		sibs.transfer(sourceIban, targetIban, 10);
		assert (sibs.getOperation(0).getState() instanceof Registered);
		assertEquals(1, sibs.getNumberOfOperations());
		assertEquals(1, sibs.getQueue().size());
		//
		sibs.processOperations();
		assertEquals(1, sibs.getNumberOfOperations());
		assertEquals(0, sibs.getQueue().size());
		assert (sibs.getOperation(0).getState() instanceof ErrorState);
	}

	@Test
	public void processSeveralAccounts() throws SibsException, AccountException, OperationException, BankException {
		Services service = mock(Services.class);
		Sibs sibs = new Sibs(3, service);
		String sourceIban2 = "CGDCK2";
		String targetIban2 = "CGDCK3";
		//
		when(service.getAccountByIban(sourceIban)).thenReturn(mock(CheckingAccount.class));
		when(service.getAccountByIban(targetIban)).thenReturn(mock(CheckingAccount.class));
		when(service.getAccountByIban(sourceIban2)).thenReturn(mock(CheckingAccount.class));
		when(service.getAccountByIban(targetIban2)).thenReturn(mock(CheckingAccount.class));
		when(service.getAccountByIban(sourceIban).getBalance()).thenReturn(10000);
		when(service.getAccountByIban(sourceIban2).getBalance()).thenReturn(10000);
		//
		sibs.transfer(sourceIban, targetIban, 10);
		sibs.transfer(sourceIban2, targetIban, 10);
		sibs.transfer(sourceIban, targetIban2, 10);
		//
		assertEquals(3, sibs.getNumberOfOperations());
		assertEquals(3, sibs.getQueue().size());
		assert (sibs.getOperation(0).getState() instanceof Registered);
		assert (sibs.getOperation(1).getState() instanceof Registered);
		assert (sibs.getOperation(2).getState() instanceof Registered);
		//
		sibs.processOperations();
		assertEquals(3, sibs.getNumberOfOperations());
		assertEquals(0, sibs.getQueue().size());
		assert (sibs.getOperation(0).getState() instanceof Completed);
		assert (sibs.getOperation(1).getState() instanceof Completed);
		assert (sibs.getOperation(2).getState() instanceof Completed);

	}

	@Test
	public void processSeveralAccountsWithOneError()
			throws AccountException, SibsException, OperationException, BankException {
		Services service = mock(Services.class);
		Sibs sibs = new Sibs(3, service);
		String sourceIban2 = "CGDCK2";
		String targetIban2 = "CGDCK3";
		//
		when(service.getAccountByIban(sourceIban)).thenReturn(mock(CheckingAccount.class));
		when(service.getAccountByIban(targetIban)).thenReturn(mock(CheckingAccount.class));
		when(service.getAccountByIban(sourceIban2)).thenReturn(mock(CheckingAccount.class));
		when(service.getAccountByIban(targetIban2)).thenReturn(mock(CheckingAccount.class));
		when(service.getAccountByIban(sourceIban).getBalance()).thenReturn(10000);
		doThrow(new AccountException()).when(service).withdraw(sourceIban2, 10);
		//
		sibs.transfer(sourceIban, targetIban, 10);
		sibs.transfer(sourceIban2, targetIban, 10);
		sibs.transfer(sourceIban, targetIban2, 10);
		//
		assertEquals(3, sibs.getNumberOfOperations());
		assertEquals(3, sibs.getQueue().size());
		assert (sibs.getOperation(0).getState() instanceof Registered);
		assert (sibs.getOperation(1).getState() instanceof Registered);
		assert (sibs.getOperation(2).getState() instanceof Registered);
		//
		sibs.processOperations();
		assertEquals(3, sibs.getNumberOfOperations());
		assertEquals(0, sibs.getQueue().size());
		assert (sibs.getOperation(0).getState() instanceof Completed);
		assert (sibs.getOperation(1).getState() instanceof ErrorState);
		assert (sibs.getOperation(2).getState() instanceof Completed);
	}
}
