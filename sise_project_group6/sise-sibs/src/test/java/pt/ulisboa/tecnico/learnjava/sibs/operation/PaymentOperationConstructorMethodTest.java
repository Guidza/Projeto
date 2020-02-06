package pt.ulisboa.tecnico.learnjava.sibs.operation;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;

import org.junit.Test;

import pt.ulisboa.tecnico.learnjava.sibs.domain.Operation;
import pt.ulisboa.tecnico.learnjava.sibs.domain.PaymentOperation;
import pt.ulisboa.tecnico.learnjava.sibs.domain.Sibs;
import pt.ulisboa.tecnico.learnjava.sibs.exceptions.OperationException;

public class PaymentOperationConstructorMethodTest {
	private static final String TARGET_IBAN = "TargetIban";
	private static final int VALUE = 100;

	@Test
	public void success() throws OperationException {
		Sibs sibs = mock(Sibs.class);
		PaymentOperation operation = new PaymentOperation(TARGET_IBAN, VALUE, sibs);

		assertEquals(Operation.OPERATION_PAYMENT, operation.getType());
		assertEquals(VALUE, operation.getValue());
		assertEquals(TARGET_IBAN, operation.getTargetIban());
	}

	@Test(expected = OperationException.class)
	public void nullTargetIban() throws OperationException {
		Sibs sibs = mock(Sibs.class);
		new PaymentOperation(null, VALUE, sibs);
	}

	@Test(expected = OperationException.class)
	public void emptyTargetIban() throws OperationException {
		Sibs sibs = mock(Sibs.class);
		new PaymentOperation("", VALUE, sibs);
	}

}
