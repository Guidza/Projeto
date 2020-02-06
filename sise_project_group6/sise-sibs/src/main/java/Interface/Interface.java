//Carlos Morais nº3
//Guilherme Costa nº5
//grupo 6
package Interface;

import java.io.BufferedInputStream;
import java.util.Random;
import java.util.Scanner;

import pt.ulisboa.tecnico.learnjava.bank.domain.Bank;
import pt.ulisboa.tecnico.learnjava.bank.domain.Bank.AccountType;
import pt.ulisboa.tecnico.learnjava.bank.domain.Client;
import pt.ulisboa.tecnico.learnjava.bank.exceptions.AccountException;
import pt.ulisboa.tecnico.learnjava.bank.exceptions.BankException;
import pt.ulisboa.tecnico.learnjava.bank.exceptions.ClientException;
import pt.ulisboa.tecnico.learnjava.bank.services.Services;
import pt.ulisboa.tecnico.learnjava.sibs.domain.Sibs;
import pt.ulisboa.tecnico.learnjava.sibs.exceptions.MbWayException;
import pt.ulisboa.tecnico.learnjava.sibs.exceptions.OperationException;
import pt.ulisboa.tecnico.learnjava.sibs.exceptions.SibsException;

public class Interface {

	public static void main(String[] args)
			throws BankException, ClientException, AccountException, MbWayException, SibsException, OperationException {
		Services service = new Services();
		Sibs sibs = new Sibs(10000, service);
		Bank bank1 = new Bank("CGD");
		Bank bank2 = new Bank("CTT");
		Client client1 = new Client(bank1, "Dorian", "Grey", "123456789", "000000000", "casa pia", 200);
		Client client2 = new Client(bank1, "Luke", "Skywalker", "234567891", "917722338", "Tatooine", 20);
		Client client3 = new Client(bank2, "Tom", "Riddle", "345678912", "666666666", "Hogwarts", 60);
		bank1.createAccount(AccountType.CHECKING, client1, 10000, 1000); // Iban CGDCK1
		bank1.createAccount(AccountType.CHECKING, client2, 10000, 1000); // Iban CGDCK2
		bank2.createAccount(AccountType.CHECKING, client3, 10000, 1000); // Iban CTTCK3
		MbwayAccount mbway;

		// TODO Auto-generated method stub
		while (true) {
			String str[] = lerInput();
			String comando = str[0];

			switch (comando) {
			case "exit":
				System.exit(0);
			case "associate-mbway":
				String iban = str[1];
				String phoneNumber = str[2];

				mbway = createMbWay(phoneNumber, iban, service);
				if (mbway == null) {
					break;
				}

				Random rand = new Random();
				Integer code = 1000000 + rand.nextInt(8999999);

				mbway.setCode(code);
				System.out.println("Code: " + code + " (Dont share it with anyone)");
				break;
			case "confirm-mbway":
				String phone = str[1];
				int cod = Integer.parseInt(str[2]);
				MbwayAccount mb = MbWay.getAccount(phone);

				if (mb.getCode().equals(cod)) {
					mb.setState(true);
					System.out.println("MBWay association confirmed successfully!");

				} else {
					System.out.println("Wrong Confirmation Code!");
				}
				break;
			case "mbway-transfer":
				String sourcePhone = str[1];
				String targetPhone = str[2];
				int amount = Integer.parseInt(str[3]);
				MbwayAccount targetMb;
				MbwayAccount sourceMb;
				try {
					sourceMb = MbWay.getAccount(sourcePhone);
				} catch (MbWayException e) {
					System.out.println("Could not complete transfer!");
					break;
				}
				try {
					targetMb = MbWay.getAccount(targetPhone);
				} catch (MbWayException e) {
					System.out.println("Could not complete transfer!");
					break;
				}
				if (targetMb.getState() == false || sourceMb.getState() == false) {
					System.out.println("Could not complete transfer!");
				}
				sibs.transfer(sourceMb.getIban(), targetMb.getIban(), amount);
				System.out.println("Transfer successful!");
				break;
			case "mbway-split-bill":
				int nrFriends = Integer.parseInt(str[1]);
				int amountToSplit = Integer.parseInt(str[2]);
				int addedFriends = 0;
				MbwayAccount[] friends = new MbwayAccount[nrFriends];
				int[] amounts = new int[nrFriends];

				Boolean o = true;
				int friendsAmount = 0;
				int i = 0;
				while (o == true) {
					String str1[] = Interface.lerInput();
					String comand = str1[0];
					if (comand.equals("friend")) {
						try {
							friends[i] = MbWay.getAccount(str1[1]);
							amounts[i] = Integer.parseInt((str1[2]));
							++i;
							addedFriends++;
							friendsAmount += Integer.parseInt(str1[2]);
						} catch (MbWayException e) {
							System.out.println("Friend does not have a MBway account!");
							o = false;
							break;
						}
						if (service.getAccountByIban((MbWay.getAccount(str1[1]).getIban())).getBalance() < Integer
								.parseInt(str1[2])) {
							System.out.println("Oh no! One of your friends does not have money to pay!");
							o = false;
							break;
						}

					} else if (comand.equals("end")) {
						o = false;
					} else {
						System.out.println("It was not a friend command!");
					}

				}
				if (addedFriends != nrFriends) {
					if (addedFriends < nrFriends) {
						System.out.println("Oh no! One friend is missing.");
						break;
					} else if (addedFriends > nrFriends) {
						System.out.println("Oh no! Too many friends.");
						break;
					}
				}
				if (amountToSplit != friendsAmount) {
					System.out.println("Something is wrong. Did you set the bill amount right?");
					break;
				}
				String target = friends[0].getIban();
				for (int j = 1; j < addedFriends; j++) {
					String source = friends[j].getIban();
					int share = amounts[j];
					sibs.transfer(source, target, share);
				}
				break;
			}

		}
	}

	public static String[] lerInput() {
		Scanner input = new Scanner(new BufferedInputStream(System.in));
		String linha = input.nextLine();
		String str[] = linha.split(" ");
		return str;
	}

	public static MbwayAccount createMbWay(String phoneNumber, String iban, Services service) {
		try {
			MbwayAccount mb = new MbwayAccount(phoneNumber, iban, service);
			return mb;
		} catch (AccountException e) {
			System.out.println("No account with such IBAN");
			return null;
		}

	}

}
