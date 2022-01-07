package client;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

public class ChatImplements extends UnicastRemoteObject implements IChat {

    private Chat client;

    public ChatImplements(Chat client) throws RemoteException {
        super();
        this.client = client;
    }

    /**
     * Sends a message to this client
     *
     * @param source
     * @param message
     * @throws RemoteException
     */
    @Override
    public void sendMessage(String source, String message) throws RemoteException {
        client.receiveMessage(source, message);
    }

    /**
     * Registers a friend interface to communicate
     *
     * @param name
     * @param friend
     * @throws RemoteException
     */
    @Override
    public void connectFriend(String name, IChat friend) throws RemoteException {
        this.client.getUser().updateFriendInterface(name, friend);
        this.client.updateFriendsList();
    }

    /**
     * Send a friendship request
     *
     * @param source
     * @throws RemoteException
     */
    @Override
    public void friendshipRequest(String source) throws RemoteException {
        this.client.createFriendRequest(source);
    }

}
