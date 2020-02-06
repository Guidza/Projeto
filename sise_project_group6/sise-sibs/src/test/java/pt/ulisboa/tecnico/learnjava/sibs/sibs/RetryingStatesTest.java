package pt.ulisboa.tecnico.learnjava.sibs.sibs;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.Test;

import pt.ulisboa.tecnico.learnjava.bank.domain.CheckingAccount;
import pt.ulisboa.tecnico.learnjava.bank.exceptions.AccountException;
import pt.ulisboa.tecnico.learnjava.bank.exceptions.BankException;
import pt.ulisboa.tecnico.learnjava.bank.services.Services;
import pt.ulisboa.tecnico.learnjava.sibs.domain.Completed;
import pt.ulisboa.tecnico.learnjava.sibs.domain.Deposited;
import pt.ulisboa.tecnico.learnjava.sibs.domain.ErrorState;
import pt.ulisboa.tecnico.learnjava.sibs.domain.Registered;
import pt.ulisboa.tecnico.learnjava.sibs.domain.RetryingR;
import pt.ulisboa.tecnico.learnjava.sibs.domain.Sibs;
import pt.ulisboa.tecnico.learnjava.sibs.domain.TransferOperation;
import pt.ulisboa.tecnico.learnjava.sibs.domain.Withdrawn;
import pt.ulisboa.tecnico.learnjava.sibs.exceptions.OperationException;
import pt.ulisboa.tecnico.learnjava.sibs.exceptions.SibsException;

public class RetryingStatesTest {
	private String sourceIban = "CGDCK1";
	private String targetIban = "BPICK2";
	private String targetIban2 = "CGDCK3";

	@Test
	public void retryingR() throws AccountException, SibsException, OperationException, BankException {
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
		TransferOperation trans = (TransferOperation) sibs.getOperation(0);
		trans.getState().process(trans, sibs);
		assert (trans.getState() instanceof RetryingR);
		assertEquals(3, trans.getState().getCount());
		assertEquals(1, sibs.getNumberOfOperations());
		assertEquals(1, sibs.getQueue().size());
		//
		trans.getState().process(trans, sibs);
		assert (trans.getState() instanceof ErrorState);
		assertEquals(0, trans.getState().getCount());
		assertEquals(1, sibs.getNumberOfOperations());
		assertEquals(0, sibs.getQueue().size());
		//

	}

	@Test
	public void retryingW() throws AccountException, SibsException, OperationException, BankException {
		Services service = mock(Services.class);
		Sibs sibs = new Sibs(3, service);
		when(service.getAccountByIban(sourceIban)).thenReturn(mock(CheckingAccount.class));
		when(service.getAccountByIban(targetIban)).thenReturn(mock(CheckingAccount.class));
		doThrow(new AccountException()).when(service).deposit(targetIban, 10);
		//
		sibs.transfer(sourceIban, targetIban, 10);
		sibs.getOperation(0).setState(new Withdrawn());
		assert (sibs.getOperation(0).getState() instanceof Withdrawn);
		assertEquals(1, sibs.getNumberOfOperations());
		assertEquals(1, sibs.getQueue().size());
		//
		TransferOperation trans = (TransferOperation) sibs.getOperation(0);
		trans.getState().process(trans, sibs);
		assert (trans.getState() instanceof RetryingR);
		assertEquals(3, trans.getState().getCount());
		assertEquals(1, sibs.getNumberOfOperations());
		assertEquals(1, sibs.getQueue().size());
		//
		trans.getState().process(trans, sibs);
		assert (trans.getState() instanceof ErrorState);
		verify(service).deposit(sourceIban, 10);
		assertEquals(0, trans.getState().getCount());
		assertEquals(1, sibs.getNumberOfOperations());
		assertEquals(0, sibs.getQueue().size());

	}

	@Test
	public void retryingD() throws AccountException, SibsException, OperationException, BankException {
		Services service = mock(Services.class);
		Sibs sibs = new Sibs(3, service);
		when(service.getAccountByIban(sourceIban)).thenReturn(mock(CheckingAccount.class));
		when(service.getAccountByIban(targetIban)).thenReturn(mock(CheckingAccount.class));
		doThrow(new AccountException()).when(service).withdraw(sourceIban, 2);
		//
		sibs.transfer(sourceIban, targetIban, 10);
		sibs.getOperation(0).setState(new Deposited());
		assert (sibs.getOperation(0).getState() instanceof Deposited);
		assertEquals(1, sibs.getNumberOfOperations());
		assertEquals(1, sibs.getQueue().size());
		//
		TransferOperation trans = (TransferOperation) sibs.getOperation(0);
		trans.getState().process(trans, sibs);
		assert (trans.getState() instanceof RetryingR);
		assertEquals(3, trans.getState().getCount());
		assertEquals(1, sibs.getNumberOfOperations());
		assertEquals(1, sibs.getQueue().size());
		//
		trans.getState().process(trans, sibs);
		assert (trans.getState() instanceof ErrorState);
		verify(service).deposit(sourceIban, 10);
		verify(service).withdraw(targetIban, 10);
		assertEquals(0, trans.getState().getCount());
		assertEquals(1, sibs.getNumberOfOperations());
		assertEquals(0, sibs.getQueue().size());

	}

	@Test
	public void retryingRToWithdrawn() throws AccountException, SibsException, OperationException, BankException {
		Services service = mock(Services.class);
		Sibs sibs = new Sibs(3, service);
		when(service.getAccountByIban(sourceIban)).thenReturn(mock(CheckingAccount.class));
		when(service.getAccountByIban(targetIban)).thenReturn(mock(CheckingAccount.class));
		when(service.getAccountByIban(sourceIban).getBalance()).thenReturn(10000);
		//
		sibs.transfer(sourceIban, targetIban, 10);
		sibs.getOperation(0).setState(new RetryingR(sibs.getOperation(0).getState()));
		assertEquals(3, sibs.getOperation(0).getState().getCount());
		assertEquals(1, sibs.getNumberOfOperations());
		assertEquals(1, sibs.getQueue().size());
		//
		TransferOperation trans = (TransferOperation) sibs.getOperation(0);
		trans.getState().process(trans, sibs);
		assert (trans.getState() instanceof Withdrawn);
		verify(service).withdraw(sourceIban, 10);
		assertEquals(1, sibs.getNumberOfOperations());
		assertEquals(1, sibs.getQueue().size());

	}

	@Test
	public void retryingWToDeposited() throws AccountException, SibsException, OperationException, BankException {
		Services service = mock(Services.class);
		Sibs sibs = new Sibs(3, service);
		when(service.getAccountByIban(sourceIban)).thenReturn(mock(CheckingAccount.class));
		when(service.getAccountByIban(targetIban)).thenReturn(mock(CheckingAccount.class));
		when(service.getAccountByIban(sourceIban).getBalance()).thenReturn(10000);
		//
		sibs.transfer(sourceIban, targetIban, 10);
		sibs.getOperation(0).setState(new Withdrawn());
		sibs.getOperation(0).setState(new RetryingR(sibs.getOperation(0).getState()));
		assertEquals(3, sibs.getOperation(0).getState().getCount());
		assertEquals(1, sibs.getNumberOfOperations());
		assertEquals(1, sibs.getQueue().size());
		//
		TransferOperation trans = (TransferOperation) sibs.getOperation(0);
		trans.getState().process(trans, sibs);
		assert (trans.getState() instanceof Deposited);
		verify(service).deposit(targetIban, 10);
		assertEquals(1, sibs.getNumberOfOperations());
		assertEquals(1, sibs.getQueue().size());

	}

	@Test
	public void retryingWToCompleted() throws AccountException, SibsException, OperationException, BankException {
		Services service = mock(Services.class);
		Sibs sibs = new Sibs(3, service);
		when(service.getAccountByIban(sourceIban)).thenReturn(mock(CheckingAccount.class));
		when(service.getAccountByIban(targetIban2)).thenReturn(mock(CheckingAccount.class));
		when(service.getAccountByIban(sourceIban).getBalance()).thenReturn(10000);
		//
		sibs.transfer(sourceIban, targetIban2, 10);
		sibs.getOperation(0).setState(new Withdrawn());
		sibs.getOperation(0).setState(new RetryingR(sibs.getOperation(0).getState()));
		assertEquals(3, sibs.getOperation(0).getState().getCount());
		assertEquals(1, sibs.getNumberOfOperations());
		assertEquals(1, sibs.getQueue().size());
		//
		TransferOperation trans = (TransferOperation) sibs.getOperation(0);
		trans.getState().process(trans, sibs);
		assert (trans.getState() instanceof Completed);
		verify(service).deposit(targetIban2, 10);
		assertEquals(1, sibs.getNumberOfOperations());
		assertEquals(0, sibs.getQueue().size());

	}

	@Test
	public void retryingDToCompleted() throws AccountException, SibsException, OperationException, BankException {
		Services service = mock(Services.class);
		Sibs sibs = new Sibs(3, service);
		when(service.getAccountByIban(sourceIban)).thenReturn(mock(CheckingAccount.class));
		when(service.getAccountByIban(targetIban)).thenReturn(mock(CheckingAccount.class));
		when(service.getAccountByIban(sourceIban).getBalance()).thenReturn(10000);
		//
		sibs.transfer(sourceIban, targetIban, 10);
		sibs.getOperation(0).setState(new Deposited());
		sibs.getOperation(0).setState(new RetryingR(sibs.getOperation(0).getState()));
		assertEquals(3, sibs.getOperation(0).getState().getCount());
		assertEquals(1, sibs.getNumberOfOperations());
		assertEquals(1, sibs.getQueue().size());
		//
		TransferOperation trans = (TransferOperation) sibs.getOperation(0);
		trans.getState().process(trans, sibs);
		assert (trans.getState() instanceof Completed);
		verify(service).withdraw(sourceIban, 2);
		assertEquals(1, sibs.getNumberOfOperations());
		assertEquals(0, sibs.getQueue().size());

	}
}
