package pt.ulisboa.tecnico.learnjava.sibs.sibs;

import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import org.junit.Test;

import pt.ulisboa.tecnico.learnjava.bank.exceptions.AccountException;
import pt.ulisboa.tecnico.learnjava.bank.services.Services;
import pt.ulisboa.tecnico.learnjava.sibs.domain.Canceled;
import pt.ulisboa.tecnico.learnjava.sibs.domain.Completed;
import pt.ulisboa.tecnico.learnjava.sibs.domain.Deposited;
import pt.ulisboa.tecnico.learnjava.sibs.domain.Registered;
import pt.ulisboa.tecnico.learnjava.sibs.domain.RetryingR;
import pt.ulisboa.tecnico.learnjava.sibs.domain.Sibs;
import pt.ulisboa.tecnico.learnjava.sibs.domain.TransferOperation;
import pt.ulisboa.tecnico.learnjava.sibs.domain.Withdrawn;
import pt.ulisboa.tecnico.learnjava.sibs.exceptions.OperationException;
import pt.ulisboa.tecnico.learnjava.sibs.exceptions.SibsException;
import pt.ulisboa.tecnico.learnjava.sibs.exceptions.StateException;

public class TransferStateTest {
	@Test
	public void baseStateIsRegistered() throws OperationException, SibsException {
		Sibs sibs = mock(Sibs.class);

		TransferOperation transfer = new TransferOperation("123456789", "987654332", 100, sibs);
		assert (transfer.getState() instanceof Registered);

	}

	@Test
	public void statusChangedToWithdrawn() throws OperationException, AccountException {
		Services service = mock(Services.class);
		Sibs sibs = new Sibs(3, service);
		TransferOperation transfer = new TransferOperation("123456789", "987654332", 100, sibs);
		transfer.getState().process(transfer, sibs);
		verify(service).withdraw("123456789", 100);
		assert (transfer.getState() instanceof Withdrawn);
	}

	@Test
	public void notEnoughtToWithdrawStatusChangedToRetryingR() throws OperationException, AccountException {
		Services service = mock(Services.class);
		Sibs sibs = new Sibs(3, service);
		TransferOperation transfer = new TransferOperation("123456789", "987654332", 100, sibs);
		doThrow(new AccountException()).when(service).withdraw("123456789", 100);
		transfer.getState().process(transfer, sibs);
		assert (transfer.getState() instanceof RetryingR);

	}

	@Test
	public void statusChangedToDeposited() throws OperationException, AccountException {
		Services service = mock(Services.class);
		Sibs sibs = new Sibs(3, service);
		TransferOperation transfer = new TransferOperation("123456789", "987654332", 100, sibs);
		transfer.setState(new Withdrawn());
		transfer.getState().process(transfer, sibs);
		verify(service).deposit("987654332", 100);
		assert (transfer.getState() instanceof Deposited);
	}

	@Test
	public void statusChangedToRetryingWDueTOAccountNotExisting() throws OperationException, AccountException {
		Services service = mock(Services.class);
		Sibs sibs = new Sibs(3, service);
		TransferOperation transfer = new TransferOperation("123456789", "987654332", 100, sibs);
		transfer.setState(new Withdrawn());
		doThrow(new AccountException()).when(service).deposit("987654332", 100);
		transfer.getState().process(transfer, sibs);
		verify(service, never()).deposit("123456789", 100);
		assert (transfer.getState() instanceof RetryingR);
	}

	@Test
	public void transferInTheSameBank() throws OperationException, AccountException {
		Services service = mock(Services.class);
		Sibs sibs = new Sibs(3, service);
		TransferOperation transfer = new TransferOperation("CGDCK1", "CGDCK2", 100, sibs);
		transfer.setState(new Withdrawn());
		transfer.getState().process(transfer, sibs);
		verify(service).deposit("CGDCK2", 100);
		assert (transfer.getState() instanceof Completed);
	}

	@Test
	public void depositToCompleted() throws OperationException, AccountException {
		Services service = mock(Services.class);
		Sibs sibs = new Sibs(3, service);
		TransferOperation transfer = new TransferOperation("CGDCK1", "BPICK2", 100, sibs);
		transfer.setState(new Deposited());
		transfer.getState().process(transfer, sibs);
		verify(service).withdraw("CGDCK1", transfer.commission());
		assert (transfer.getState() instanceof Completed);
	}

	@Test
	public void notEnoughToChargeComissionStateChangedTORetryingD() throws OperationException, AccountException {
		Services service = mock(Services.class);
		Sibs sibs = new Sibs(3, service);
		TransferOperation transfer = new TransferOperation("CGDCK1", "BPICK2", 100, sibs);
		transfer.setState(new Deposited());
		doThrow(new AccountException()).when(service).withdraw("CGDCK1", transfer.commission());
		transfer.getState().process(transfer, sibs);
		verify(service, never()).deposit("CGDCK1", 100);
		verify(service, never()).withdraw("BPICK2", 100);
		assert (transfer.getState() instanceof RetryingR);
	}

	@Test
	public void registeredStateCancel() throws OperationException, AccountException, StateException {
		Services service = mock(Services.class);
		Sibs sibs = new Sibs(3, service);
		TransferOperation transfer = new TransferOperation("CGDCK1", "BPICK2", 100, sibs);
		transfer.getState().cancel(transfer, sibs);
		assert (transfer.getState() instanceof Canceled);
	}

	@Test
	public void withdrawnStateCanceled() throws OperationException, AccountException, StateException {
		Services service = mock(Services.class);
		Sibs sibs = new Sibs(3, service);
		TransferOperation transfer = new TransferOperation("CGDCK1", "CGDCK2", 100, sibs);
		transfer.setState(new Withdrawn());
		transfer.getState().cancel(transfer, sibs);
		verify(service).deposit("CGDCK1", 100);
		verify(service, never()).deposit("CGDCK2", 100);
		assert (transfer.getState() instanceof Canceled);
	}

	@Test
	public void depositedStateCanceled() throws OperationException, AccountException, StateException {
		Services service = mock(Services.class);
		Sibs sibs = new Sibs(3, service);
		TransferOperation transfer = new TransferOperation("CGDCK1", "BPICK2", 100, sibs);
		transfer.setState(new Deposited());
		transfer.getState().cancel(transfer, sibs);
		verify(service).deposit("CGDCK1", 100);
		verify(service).withdraw("BPICK2", 100);
		assert (transfer.getState() instanceof Canceled);
	}

	@Test
	public void completedStateCantBeCanceled() throws OperationException, AccountException, StateException {
		Services service = mock(Services.class);
		Sibs sibs = new Sibs(3, service);
		TransferOperation transfer = new TransferOperation("CGDCK1", "BPICK2", 100, sibs);
		transfer.setState(new Completed());
		transfer.getState().cancel(transfer, sibs);
		assert (transfer.getState() instanceof Completed);
	}

	@Test
	public void stateEvolutionBeginToEndDifferentBanks() throws OperationException, AccountException {
		Services service = mock(Services.class);
		Sibs sibs = new Sibs(3, service);
		TransferOperation transfer = new TransferOperation("CGDCK1", "BPICK2", 100, sibs);
		assert (transfer.getState() instanceof Registered);
		transfer.getState().process(transfer, sibs);
		assert (transfer.getState() instanceof Withdrawn);
		transfer.getState().process(transfer, sibs);
		assert (transfer.getState() instanceof Deposited);
		transfer.getState().process(transfer, sibs);
		assert (transfer.getState() instanceof Completed);
		transfer.getState().process(transfer, sibs);
		assert (transfer.getState() instanceof Completed);

	}

	@Test
	public void stateEvolutionBeginToEndSameBank() throws AccountException, OperationException {
		Services service = mock(Services.class);
		Sibs sibs = new Sibs(3, service);
		TransferOperation transfer = new TransferOperation("CGDCK1", "CGDCK2", 100, sibs);
		assert (transfer.getState() instanceof Registered);
		transfer.getState().process(transfer, sibs);
		assert (transfer.getState() instanceof Withdrawn);
		transfer.getState().process(transfer, sibs);
		assert (transfer.getState() instanceof Completed);
		transfer.getState().process(transfer, sibs);
		assert (transfer.getState() instanceof Completed);
	}

}
