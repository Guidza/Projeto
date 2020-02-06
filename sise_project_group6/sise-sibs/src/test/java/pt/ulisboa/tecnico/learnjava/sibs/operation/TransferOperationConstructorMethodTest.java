package pt.ulisboa.tecnico.learnjava.sibs.operation;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;

import org.junit.Test;

import pt.ulisboa.tecnico.learnjava.sibs.domain.Operation;
import pt.ulisboa.tecnico.learnjava.sibs.domain.Sibs;
import pt.ulisboa.tecnico.learnjava.sibs.domain.TransferOperation;
import pt.ulisboa.tecnico.learnjava.sibs.exceptions.OperationException;

public class TransferOperationConstructorMethodTest {
	private static final String SOURCE_IBAN = "SourceIban";
	private static final String TARGET_IBAN = "TargetIban";
	private static final int VALUE = 100;

	@Test
	public void success() throws OperationException {
		Sibs sibs = mock(Sibs.class);
		TransferOperation operation = new TransferOperation(SOURCE_IBAN, TARGET_IBAN, VALUE, sibs);

		assertEquals(Operation.OPERATION_TRANSFER, operation.getType());
		assertEquals(100, operation.getValue());
		assertEquals(SOURCE_IBAN, operation.getSourceIban());
		assertEquals(TARGET_IBAN, operation.getTargetIban());
	}

	@Test(expected = OperationException.class)
	public void nonPositiveValue() throws OperationException {
		Sibs sibs = mock(Sibs.class);
		new TransferOperation(SOURCE_IBAN, TARGET_IBAN, 0, sibs);
	}

	@Test(expected = OperationException.class)
	public void nullSourceIban() throws OperationException {
		Sibs sibs = mock(Sibs.class);
		new TransferOperation(null, TARGET_IBAN, 100, sibs);
	}

	@Test(expected = OperationException.class)
	public void emptySourceIban() throws OperationException {
		Sibs sibs = mock(Sibs.class);
		new TransferOperation("", TARGET_IBAN, 100, sibs);
	}

	@Test(expected = OperationException.class)
	public void nullTargetIban() throws OperationException {
		Sibs sibs = mock(Sibs.class);
		new TransferOperation(SOURCE_IBAN, null, 100, sibs);
	}

	@Test(expected = OperationException.class)
	public void emptyTargetIban() throws OperationException {
		Sibs sibs = mock(Sibs.class);
		new TransferOperation(SOURCE_IBAN, "", 100, sibs);
	}

}
