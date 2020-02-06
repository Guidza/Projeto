package pt.ulisboa.tecnico.learnjava.sibs.sibs;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.Test;

import pt.ulisboa.tecnico.learnjava.bank.domain.CheckingAccount;
import pt.ulisboa.tecnico.learnjava.bank.exceptions.AccountException;
import pt.ulisboa.tecnico.learnjava.bank.exceptions.BankException;
import pt.ulisboa.tecnico.learnjava.bank.services.Services;
import pt.ulisboa.tecnico.learnjava.sibs.domain.Canceled;
import pt.ulisboa.tecnico.learnjava.sibs.domain.Completed;
import pt.ulisboa.tecnico.learnjava.sibs.domain.Deposited;
import pt.ulisboa.tecnico.learnjava.sibs.domain.Registered;
import pt.ulisboa.tecnico.learnjava.sibs.domain.Sibs;
import pt.ulisboa.tecnico.learnjava.sibs.domain.TransferOperation;
import pt.ulisboa.tecnico.learnjava.sibs.domain.Withdrawn;
import pt.ulisboa.tecnico.learnjava.sibs.exceptions.OperationException;
import pt.ulisboa.tecnico.learnjava.sibs.exceptions.SibsException;
import pt.ulisboa.tecnico.learnjava.sibs.exceptions.StateException;

public class CancelOperationMethodTest {
	private String sourceIban = "CGDCK1";
	private String targetIban = "BPICK2";

	@Test
	public void registeredTransferOpIsCanceled()
			throws OperationException, AccountException, SibsException, BankException, StateException {
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
		//
		sibs.cancelOperation(0);
		assertEquals(0, sibs.getQueue().size());
		assertEquals(1, sibs.getNumberOfOperations());
		assert (sibs.getOperation(0).getState() instanceof Canceled);
	}

	@Test
	public void withdrawnTransferOpIsCanceled()
			throws OperationException, AccountException, SibsException, BankException, StateException {
		Services service = mock(Services.class);
		Sibs sibs = new Sibs(3, service);
		when(service.getAccountByIban(sourceIban)).thenReturn(mock(CheckingAccount.class));
		when(service.getAccountByIban(targetIban)).thenReturn(mock(CheckingAccount.class));
		when(service.getAccountByIban(sourceIban).getBalance()).thenReturn(10000);
		//
		sibs.transfer(sourceIban, targetIban, 10);
		sibs.getUnprocessedOp().setState(new Withdrawn());
		//
		assertEquals(1, sibs.getQueue().size());
		assertEquals(1, sibs.getNumberOfOperations());
		assert (sibs.getUnprocessedOp() instanceof TransferOperation);
		assert (sibs.getUnprocessedOp().getState() instanceof Withdrawn);
		//
		sibs.cancelOperation(0);
		verify(service).deposit(sourceIban, 10);
		assertEquals(0, sibs.getQueue().size());
		assertEquals(1, sibs.getNumberOfOperations());
		assert (sibs.getOperation(0).getState() instanceof Canceled);
	}

	@Test
	public void depositedTransferOpIsCanceled()
			throws OperationException, AccountException, SibsException, BankException, StateException {
		Services service = mock(Services.class);
		Sibs sibs = new Sibs(3, service);
		when(service.getAccountByIban(sourceIban)).thenReturn(mock(CheckingAccount.class));
		when(service.getAccountByIban(targetIban)).thenReturn(mock(CheckingAccount.class));
		when(service.getAccountByIban(sourceIban).getBalance()).thenReturn(10000);
		//
		sibs.transfer(sourceIban, targetIban, 10);
		sibs.getUnprocessedOp().setState(new Deposited());
		assertEquals(1, sibs.getQueue().size());
		assertEquals(1, sibs.getNumberOfOperations());
		assert (sibs.getUnprocessedOp() instanceof TransferOperation);
		assert (sibs.getUnprocessedOp().getState() instanceof Deposited);
		//
		sibs.cancelOperation(0);
		verify(service).deposit(sourceIban, 10);
		verify(service).withdraw(targetIban, 10);
		assertEquals(0, sibs.getQueue().size());
		assertEquals(1, sibs.getNumberOfOperations());
		assert (sibs.getOperation(0).getState() instanceof Canceled);
	}

	@Test
	public void CompletedTransferOpCannotBeCanceled()
			// este teste nao faz muito sentido pq uma transfer de estado completed ja foi
			// retirada da queue de unprocessedOperations.
			// serve so para ver se de facto nao muda o estado de completed para canceled.
			throws OperationException, AccountException, SibsException, BankException, StateException {
		Services service = mock(Services.class);
		Sibs sibs = new Sibs(3, service);
		when(service.getAccountByIban(sourceIban)).thenReturn(mock(CheckingAccount.class));
		when(service.getAccountByIban(targetIban)).thenReturn(mock(CheckingAccount.class));
		when(service.getAccountByIban(sourceIban).getBalance()).thenReturn(10000);
		//
		sibs.transfer(sourceIban, targetIban, 10);
		sibs.getUnprocessedOp().setState(new Completed());
		assertEquals(1, sibs.getQueue().size());
		assertEquals(1, sibs.getNumberOfOperations());
		assert (sibs.getOperation(0) instanceof TransferOperation);
		assert (sibs.getOperation(0).getState() instanceof Completed);
		//
		sibs.cancelOperation(0);
		assertEquals(1, sibs.getQueue().size());
		assertEquals(1, sibs.getNumberOfOperations());
		assert (sibs.getOperation(0).getState() instanceof Completed);
	}
}
