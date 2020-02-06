//Carlos Morais nº3
//Guilherme Costa nº5
//grupo 6
package Interface;

import pt.ulisboa.tecnico.learnjava.bank.exceptions.AccountException;
import pt.ulisboa.tecnico.learnjava.bank.services.Services;

public class MbwayAccount {
	private String phoneNumber;
	private Integer code;
	private String iban;
	private boolean state;

	public MbwayAccount(String phoneNumber, String iban, Services service) throws AccountException {

		if (service.getAccountByIban(iban) == null) {
			throw new AccountException();
		}
		this.phoneNumber = phoneNumber;
		this.code = null;
		this.iban = iban;
		this.state = false;
		MbWay.accounts.add(this);
	}

	public String getPhoneNumber() {
		return phoneNumber;
	}

	public void setPhoneNumber(String phoneNumber) {
		this.phoneNumber = phoneNumber;
	}

	public Integer getCode() {
		return code;
	}

	public void setCode(Integer code) {
		this.code = code;
	}

	public String getIban() {
		return iban;
	}

	public void setIban(String iban) {
		this.iban = iban;
	}

	public boolean getState() {
		return state;
	}

	public void setState(boolean state) {
		this.state = state;
	}

}
