package pt.ulisboa.tecnico.learnjava.sibs.sibs;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import pt.ulisboa.tecnico.learnjava.bank.domain.Bank;
import pt.ulisboa.tecnico.learnjava.bank.domain.CheckingAccount;
import pt.ulisboa.tecnico.learnjava.bank.domain.Client;
import pt.ulisboa.tecnico.learnjava.bank.exceptions.AccountException;
import pt.ulisboa.tecnico.learnjava.bank.exceptions.BankException;
import pt.ulisboa.tecnico.learnjava.bank.exceptions.ClientException;
import pt.ulisboa.tecnico.learnjava.bank.services.Services;
import pt.ulisboa.tecnico.learnjava.sibs.domain.Completed;
import pt.ulisboa.tecnico.learnjava.sibs.domain.Deposited;
import pt.ulisboa.tecnico.learnjava.sibs.domain.Operation;
import pt.ulisboa.tecnico.learnjava.sibs.domain.Registered;
import pt.ulisboa.tecnico.learnjava.sibs.domain.Sibs;
import pt.ulisboa.tecnico.learnjava.sibs.domain.TransferOperation;
import pt.ulisboa.tecnico.learnjava.sibs.domain.Withdrawn;
import pt.ulisboa.tecnico.learnjava.sibs.exceptions.OperationException;
import pt.ulisboa.tecnico.learnjava.sibs.exceptions.SibsException;

public class TransferMethodTest {
	private static final String ADDRESS = "Ave.";
	private static final String PHONE_NUMBER = "987654321";
	private static final String NIF = "123456789";
	private static final String LAST_NAME = "Silva";
	private static final String FIRST_NAME = "António";

	private Sibs sibs;
	private Bank sourceBank;
	private Bank targetBank;
	private Client sourceClient;
	private Client targetClient;
	private Services services;

	@Before
	public void setUp() throws BankException, AccountException, ClientException {
		this.services = new Services();
		this.sibs = new Sibs(100, services);
		this.sourceBank = new Bank("CGD");
		this.targetBank = new Bank("BPI");
		this.sourceClient = new Client(this.sourceBank, FIRST_NAME, LAST_NAME, NIF, PHONE_NUMBER, ADDRESS, 33);
		this.targetClient = new Client(this.targetBank, FIRST_NAME, LAST_NAME, NIF, PHONE_NUMBER, ADDRESS, 22);
	}

	@Test
	public void successDifferentBanks()
			throws BankException, AccountException, SibsException, OperationException, ClientException {
		String sourceIban = this.sourceBank.createAccount(Bank.AccountType.CHECKING, this.sourceClient, 1000, 0);
		String targetIban = this.targetBank.createAccount(Bank.AccountType.CHECKING, this.targetClient, 1000, 0);

		this.sibs.transfer(sourceIban, targetIban, 100);
		sibs.processOperations();
		assertEquals(6, sibs.getOperation(0).commission());
		assertEquals(894, this.services.getAccountByIban(sourceIban).getBalance());// desconta comissao
		assertEquals(1100, this.services.getAccountByIban(targetIban).getBalance());
		assertEquals(1, this.sibs.getNumberOfOperations());
		assertEquals(100, this.sibs.getTotalValueOfOperations());
		assertEquals(100, this.sibs.getTotalValueOfOperationsForType(Operation.OPERATION_TRANSFER));
		assertEquals(0, this.sibs.getTotalValueOfOperationsForType(Operation.OPERATION_PAYMENT));
	}

	@Test
	public void successSameBank()
			throws BankException, AccountException, SibsException, OperationException, ClientException {
		Client targetSameBankClient = new Client(this.sourceBank, FIRST_NAME, LAST_NAME, "987654321", PHONE_NUMBER,
				ADDRESS, 22);
		String sourceIban = this.sourceBank.createAccount(Bank.AccountType.CHECKING, this.sourceClient, 1000, 0);
		String targetIban = this.sourceBank.createAccount(Bank.AccountType.CHECKING, targetSameBankClient, 1000, 0);

		this.sibs.transfer(sourceIban, targetIban, 100);
		sibs.processOperations();

		assertEquals(0, sibs.getOperation(0).commission());
		assertEquals(900, this.services.getAccountByIban(sourceIban).getBalance());// nao desconta comissao
		assertEquals(1100, this.services.getAccountByIban(targetIban).getBalance());
		assertEquals(1, this.sibs.getNumberOfOperations());
		assertEquals(100, this.sibs.getTotalValueOfOperations());
		assertEquals(100, this.sibs.getTotalValueOfOperationsForType(Operation.OPERATION_TRANSFER));
		assertEquals(0, this.sibs.getTotalValueOfOperationsForType(Operation.OPERATION_PAYMENT));
	}

	@Test
	public void sucessDiferentBanksMock()
			throws SibsException, AccountException, OperationException, BankException, ClientException {
		Services serviceMock = mock(Services.class);
		Sibs sibs = new Sibs(100, serviceMock);

		String sourceIban = "CGDCK1";
		String targetIban = "BPICK2";
		int amount = 100;

		when(serviceMock.getAccountByIban(sourceIban)).thenReturn(mock(CheckingAccount.class));
		when(serviceMock.getAccountByIban(targetIban)).thenReturn(mock(CheckingAccount.class));
		when(serviceMock.getAccountByIban(sourceIban).getBalance()).thenReturn(10000);
		sibs.transfer(sourceIban, targetIban, amount);

		verify(serviceMock, never()).deposit(targetIban, amount);
		verify(serviceMock, never()).withdraw(sourceIban, amount);
		verify(serviceMock, never()).withdraw(sourceIban, 6);
		Operation op = sibs.getOperation(0);
		assert (op.getState() instanceof Registered);
		//
		op.getState().process((TransferOperation) op, sibs);
		assert (op.getState() instanceof Withdrawn);
		verify(serviceMock).withdraw(sourceIban, amount);
		//
		op.getState().process((TransferOperation) op, sibs);
		verify(serviceMock).deposit(targetIban, amount);
		assert (op.getState() instanceof Deposited);
		//
		op.getState().process((TransferOperation) op, sibs);
		verify(serviceMock).withdraw(sourceIban, 6);
		assert (op.getState() instanceof Completed);
	}

	@Test
	public void sucessSameBanksMock() throws SibsException, AccountException, OperationException, BankException {
		Services serviceMock = mock(Services.class);
		Sibs sibs = new Sibs(100, serviceMock);

		String sourceIban = "CGDCK1";
		String targetIban = "CGDCK2";
		int amount = 100;

		when(serviceMock.getAccountByIban(sourceIban)).thenReturn(mock(CheckingAccount.class));
		when(serviceMock.getAccountByIban(targetIban)).thenReturn(mock(CheckingAccount.class));
		when(serviceMock.getAccountByIban(sourceIban).getBalance()).thenReturn(10000);
		sibs.transfer(sourceIban, targetIban, amount);
		verify(serviceMock, never()).deposit(targetIban, amount);
		verify(serviceMock, never()).withdraw(sourceIban, amount);
		verify(serviceMock, never()).withdraw(sourceIban, 6);
		Operation op = sibs.getOperation(0);
		assert (op.getState() instanceof Registered);
		//
		op.getState().process((TransferOperation) op, sibs);
		assert (op.getState() instanceof Withdrawn);
		verify(serviceMock).withdraw(sourceIban, amount);
		//
		op.getState().process((TransferOperation) op, sibs);
		verify(serviceMock).deposit(targetIban, amount);
		assert (op.getState() instanceof Completed);

	}

	@Test
	public void transferFailCuzAccountDontExist()
			throws SibsException, AccountException, OperationException, BankException {
		Services serviceMock = mock(Services.class);
		Sibs sibs = mock(Sibs.class);
		doThrow(new AccountException()).when(sibs).transfer("123456789", "987654321", 100);
		verify(serviceMock, never()).deposit("987654321", 100);
		verify(serviceMock, never()).withdraw("123456789", 100);

	}

	@After
	public void tearDown() {
		Bank.clearBanks();
	}

}
