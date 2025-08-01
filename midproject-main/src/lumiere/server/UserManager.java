package lumiere.server;

import lumiere.model.User;
import java.rmi.RemoteException;
import java.rmi.server.RemoteServer;
import java.rmi.server.ServerNotActiveException;
import java.rmi.server.UnicastRemoteObject;
import java.util.List;

public class UserManager extends UnicastRemoteObject implements UserService {
    private List<User> admins;
    private List<User> customers;
    private JsonHandler jsonHandler;

    public UserManager() throws RemoteException {
        super();
        this.jsonHandler = new JsonHandler();
        this.admins = jsonHandler.loadAdmins();
        this.customers = jsonHandler.loadCustomers();
    }

    @Override
    public boolean authenticate(String username, String password, String role) throws RemoteException {
        String clientIp = "UNKNOWN";
        try {
            clientIp = RemoteServer.getClientHost();
        } catch (ServerNotActiveException e) {
            e.printStackTrace();
        }

        boolean isAuthenticated = false;
        if ("admin".equalsIgnoreCase(role)) {
            isAuthenticated = admins.stream().anyMatch(u ->
                    u.getUsername().equals(username) && u.getPassword().equals(password)
            );
        } else if ("customer".equalsIgnoreCase(role)) {
            isAuthenticated = customers.stream().anyMatch(u ->
                    u.getUsername().equals(username) && u.getPassword().equals(password)
            );
        }

        ServerView.logUserConnection(clientIp, username, role, isAuthenticated ? "login success" : "login fail");
        return isAuthenticated;
    }

    @Override
    public boolean isAdmin(String username) throws RemoteException {
        return admins.stream().anyMatch(u -> u.getUsername().equals(username));
    }

    @Override
    public boolean registerCustomer(String username, String password) throws RemoteException {
        boolean exists = customers.stream().anyMatch(u -> u.getUsername().equalsIgnoreCase(username));
        if (exists) return false;

        User newUser = new User(username, password);
        customers.add(newUser);
        JsonHandler.saveCustomers(customers);
        return true;
    }

    @Override
    public void logout(String username, String role) throws RemoteException {
        String clientIp = "UNKNOWN";
        try {
            clientIp = RemoteServer.getClientHost();
        } catch (ServerNotActiveException e) {
            e.printStackTrace();
        }
        ServerView.logUserConnection(clientIp, username, role, "logout");
    }
}
