package lumiere.server;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface UserService extends Remote {
    boolean authenticate(String username, String password, String role) throws RemoteException;
    boolean isAdmin(String username) throws RemoteException;
    boolean registerCustomer(String username, String password) throws RemoteException;
    void logout(String username, String role) throws RemoteException;
}