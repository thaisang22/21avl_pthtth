package bai4;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.HashMap;
import java.util.Map;

public class ContactServiceImpl extends UnicastRemoteObject implements ContactService {
    private final Map<String, String> contacts;

    public ContactServiceImpl() throws RemoteException {
        super();
        this.contacts = new HashMap<>();
        // Dữ liệu mẫu ban đầu
        this.contacts.put("Nguyen Van A", "0901234567");
        this.contacts.put("Tran Thi B", "0912345678");
    }

    @Override
    public synchronized void addContact(String name, String phone) throws RemoteException {
        contacts.put(name.trim(), phone.trim());
        System.out.printf("Added/Updated contact: %s -> %s\n", name, phone);
    }

    @Override
    public synchronized String findContact(String name) throws RemoteException {
        System.out.println("Finding contact for: " + name);
        return contacts.get(name.trim());
    }

    @Override
    public synchronized boolean deleteContact(String name) throws RemoteException {
        System.out.println("Deleting contact: " + name);
        return contacts.remove(name.trim()) != null;
    }
}
