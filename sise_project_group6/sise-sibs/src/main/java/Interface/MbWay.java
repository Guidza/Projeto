//Carlos Morais nº3
//Guilherme Costa nº5
//grupo 6
package Interface;

import java.util.HashSet;
import java.util.NoSuchElementException;
import java.util.Set;

import pt.ulisboa.tecnico.learnjava.sibs.exceptions.MbWayException;

public class MbWay {
	public static Set<MbwayAccount> accounts = new HashSet<MbwayAccount>();

	public static MbwayAccount getAccount(String n) throws MbWayException {
		MbwayAccount account;
		try {
			account = accounts.stream().filter(e -> e.getPhoneNumber().equals(n)).findAny().get();
		} catch (NoSuchElementException e) {
			throw new MbWayException();
		}
		return account;
	}

}
