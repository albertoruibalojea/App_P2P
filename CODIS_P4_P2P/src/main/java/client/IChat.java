package client;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface IChat extends Remote {

    /*Enviar un mensaje*/
    public void sendMessage(String source, String message) throws RemoteException;

    /*Registrar la interface de un amigo conectado para comunicarse con el*/
    public void connectFriend(String name, IChat friend) throws RemoteException;

    /*Lanzar enviar solicitud de amistad*/
    public void friendshipRequest(String source) throws RemoteException;
}
