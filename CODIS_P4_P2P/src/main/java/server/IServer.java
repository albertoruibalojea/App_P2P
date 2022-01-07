package server;

import java.rmi.Remote;
import java.rmi.RemoteException;
import client.IChat;
import java.security.GeneralSecurityException;

public interface IServer extends Remote {

    //Updates this user
    public void updateUser(User user, String password) throws RemoteException, GeneralSecurityException;

    //Registers an user on the server with his interface
    public void registerCallback(User user, String password) throws RemoteException, GeneralSecurityException;

    //Deletes an user on the server with his interface
    public void deleteCallback(User user, String password) throws RemoteException, GeneralSecurityException;

    //Gets this user on the db with his friends
    public User login(String name, String password) throws RemoteException;

    //Creates a new user on the db
    public User register(String name, String password) throws RemoteException;

    //Gets the user interface
    public IChat getUserInterface(String name) throws RemoteException;

    //Regsterrs a friend request
    public boolean friendRequest(User user, String password, String source, String destiny) throws RemoteException, GeneralSecurityException;

    //Change password
    public void changePassword(User user, String password) throws RemoteException;

    //Gets the friend request answer
    public void friendshipResponse(User user, String password, User friend, boolean answer) throws RemoteException, GeneralSecurityException;
}
