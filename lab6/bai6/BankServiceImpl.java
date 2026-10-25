package bai6;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

public class BankServiceImpl extends UnicastRemoteObject implements BankService {
    private double balance;

    public BankServiceImpl(double initialBalance) throws RemoteException {
        super();
        this.balance = initialBalance;
    }

    @Override
    public synchronized double getBalance() throws RemoteException {
        System.out.println("Client checked balance: " + balance);
        return this.balance;
    }

    @Override
    public synchronized void deposit(double amount) throws RemoteException {
        if (amount <= 0) {
            throw new IllegalArgumentException("Số tiền nạp phải lớn hơn 0!");
        }
        this.balance += amount;
        System.out.printf("Deposited: %.2f | New balance: %.2f\n", amount, this.balance);
    }

    @Override
    public synchronized boolean withdraw(double amount) throws RemoteException {
        if (amount <= 0) {
            throw new IllegalArgumentException("Số tiền rút phải lớn hơn 0!");
        }
        if (this.balance >= amount) {
            this.balance -= amount;
            System.out.printf("Withdrew: %.2f | Remaining balance: %.2f\n", amount, this.balance);
            return true;
        } else {
            System.out.printf("Withdraw failed (insufficient funds): Requested %.2f, Current %.2f\n", amount, this.balance);
            return false;
        }
    }
}
