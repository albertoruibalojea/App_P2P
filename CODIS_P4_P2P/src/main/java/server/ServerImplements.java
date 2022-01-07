package server;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;
import client.IChat;
import java.io.UnsupportedEncodingException;
import java.security.GeneralSecurityException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import javax.crypto.spec.SecretKeySpec;

public class ServerImplements extends UnicastRemoteObject implements IServer {

    private ArrayList<User> connectedPeople;
    private File dbUsers;
    private EncryptData ed;
    private SecretKeySpec key;
    private ArrayList<String[]> requests; //[0] -> remitente, [1] -> destinatario

    public ServerImplements() throws java.rmi.RemoteException, IOException {
        super();
        try {
            this.requests = new ArrayList();
            this.connectedPeople = new ArrayList();
            this.dbUsers = new File(System.getProperty("user.dir") + "//src//main//java//server//users.txt");
            
            //Tratado del encriptado del archivo de configuración
            String password = "secret";
            // The salt (probably) can be stored along with the encrypted data
            byte[] salt = new String("12345678").getBytes();

            // Decreasing this speeds down startup time and can be useful during testing, but it also makes it easier for brute force attackers
            int iterationCount = 40000;
            int keyLength = 128;
            //Clave de desencriptado para las contraseñas
            key = EncryptData.createSecretKey(password.toCharArray(), salt, iterationCount, keyLength);
            
        } catch (NoSuchAlgorithmException | InvalidKeySpecException  ex) {
            Logger.getLogger(ServerImplements.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @Override
    public void updateUser(User user, String password) throws RemoteException, GeneralSecurityException {
        if(this.getUser(user.getName(), password) != null)
            this.notifyFriendships();
    }

    /*
    First, we update the user´s friend list adding the interfaces of whom is connected of his friends,
     letting the friends know that he is connected adding it to connectedPeople arraylist
     and finally updating the list of connectedPeople of the Chat interface with the connected friends
     */
    @Override
    public void registerCallback(User user, String password) throws RemoteException, GeneralSecurityException {
        if(this.getUser(user.getName(), password) != null){
           this.getConnectedFriends(user, password);
            this.notifyFriendsOnline(user, password);
            this.connectedPeople.add(user);
            System.out.println(user.getName() + " is connected."); 
        }
    }

    /*
    In this case, we renove the user from the connectedPeople arraylist
     and then we notify friends that he is not online anymore
     */
    @Override
    public void deleteCallback(User user, String password) throws RemoteException, GeneralSecurityException {
        if(this.getUser(user.getName(), password) != null){
            this.connectedPeople.remove(user);
            this.notifyFriendsOffline(user, password);
            System.out.println(user.getName() + " exited system.");
        }
    }

    /**
     * Gets the user with his connected friends
     *
     * @param name
     * @param password
     * @return
     * @throws java.rmi.RemoteException
     */
    @Override
    public User login(String name, String password) throws RemoteException {
        try {
            return this.getUser(name, password);
        } catch (GeneralSecurityException ex) {
            Logger.getLogger(ServerImplements.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }

    /**
     * Registers an user
     *
     * @param name
     * @param password
     * @return
     * @throws java.rmi.RemoteException
     */
    @Override
    public User register(String name, String password) throws RemoteException {
        try {
            if (!this.existsUser(name)) {
                FileWriter fw = new FileWriter(this.dbUsers, true);
                BufferedWriter bw = new BufferedWriter(fw);
                bw.append(name + "," + EncryptData.encrypt(password, key) + "\n");
                bw.close();
                fw.close();
                User user = new User(name);
                return user;
            }
        } catch (FileNotFoundException ex) {
            System.out.println("Error registering user: " + ex);
        } catch (IOException ex) {
            System.out.println("Error registering user: " + ex);
        } catch (GeneralSecurityException ex) {
            Logger.getLogger(ServerImplements.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }

    /**
     * Gets an user interface to communicate
     *
     * @param name
     * @return
     * @throws java.rmi.RemoteException
     */
    @Override
    public IChat getUserInterface(String name) throws RemoteException {
        for (User user : this.connectedPeople) {
            if (user.getName().equals(name)) {
                return user.getIface();
            }
        }
        return null;
    }

    /**
     * If this FriendRequest hasn´t been sent previously, it´s added to the
     * pending requests of the user and we update the pending requests to notify
     *
     * @param user
     * @param password
     * @param source
     * @param destiny
     * @return
     * @throws java.rmi.RemoteException
     */
    @Override
    public boolean friendRequest(User user, String password, String source, String destiny) throws RemoteException, GeneralSecurityException {
        if(this.getUser(user.getName(), password) != null){
            boolean result = false;
            String friendship[] = new String[2];
            String oppositeFriendship[] = new String[2];
            friendship[0] = source;
            oppositeFriendship[0] = destiny;
            friendship[1] = destiny;
            oppositeFriendship[1] = source;

            //We hace to make sure that the friend exists
            if (this.existsUser(destiny)) {
                //If we haven´t sent that request previously, we send it
                if (!this.requests.contains(friendship) && !this.requests.contains(oppositeFriendship)) {
                    this.requests.add(friendship);
                    this.notifyFriendships();
                }
            }
            return result;
        }
        
        return false;
    }

    /**
     * Method to update the password of the user
     *
     * @param user
     * @param password
     * @throws java.rmi.RemoteException
     */
    @Override
    public void changePassword(User user, String password) throws RemoteException {
        try {
            StringBuffer input = null;
            try (BufferedReader file = new BufferedReader(new FileReader(this.dbUsers))) {
                input = new StringBuffer();
                String line;
                String aux[];
                while ((line = file.readLine()) != null) {
                    aux = line.split(",");
                    if (aux[0].equals(user.getName())) {
                        line = "";
                        aux[1] = EncryptData.encrypt(password, key);
                        line = line.concat(aux[0]);
                        for (int i = 1; i < aux.length; i++) {
                            line = line.concat("," + aux[i]);
                        }
                        input.append(line).append('\n');
                    } else {
                        input.append(line).append('\n');
                    }
                }
            } catch (GeneralSecurityException | UnsupportedEncodingException ex) {
                Logger.getLogger(ServerImplements.class.getName()).log(Level.SEVERE, null, ex);
            }
            try (FileOutputStream fileOut = new FileOutputStream(this.dbUsers)) {
                fileOut.write(input.toString().getBytes());
            }
        } catch (FileNotFoundException ex) {
            System.out.println("Error finding user: " + ex);
        } catch (IOException ex) {
            Logger.getLogger(ServerImplements.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * Method to send the pending friend requests
     */
    private void notifyFriendships() {
        //The user who´s gonna receive it has to be connected
        for (String[] s : this.requests) {
            for (User u : this.connectedPeople) {
                if (s[1].equals(u.getName())) {
                    try {
                        //With this method, we invoke the FriendRequest class at that friend interface
                        u.getIface().friendshipRequest(s[0]);
                    } catch (RemoteException ex) {
                        Logger.getLogger(ServerImplements.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            }
        }
    }

    /**
     * If the friend accepts the petition, we update the interfaces to show if
     * they are connected and we delete the friend request
     * @param user
     * @param password
     * @param friend
     * @param answer
     * @throws java.rmi.RemoteException
     * @throws java.security.GeneralSecurityException
     */
    @Override
    public void friendshipResponse(User user, String password, User friend, boolean answer) throws RemoteException, GeneralSecurityException {
        if(this.getUser(user.getName(), password) != null){
            String request[] = {user.getName(), friend.getName()};
            String requestReverse[] = {friend.getName(), user.getName()};
            if (answer) {
                //We add the friendship to the db
                this.addFriendship(user, password, user.getName(), friend.getName());

                //If they are connected
                if (this.connectedPeople.contains(new User(user.getName())) && this.connectedPeople.contains(new User(friend.getName()))) {
                    User source = this.connectedPeople.get(this.connectedPeople.indexOf(new User(user.getName())));
                    User destiny = this.connectedPeople.get(this.connectedPeople.indexOf(new User(friend.getName())));
                    source.getIface().connectFriend(friend.getName(), destiny.getIface());
                    destiny.getIface().connectFriend(user.getName(), source.getIface());
                }
            }

            //Deleting the friend request
            ArrayList<String[]> rm = new ArrayList();
            for (String[] aux : this.requests) {
                if ((aux[0].equals(request[0]) && aux[1].equals(request[1])) || (aux[0].equals(request[1]) && aux[1].equals(request[0]))) {
                    rm.add(aux);
                }
            }
            for (String[] aux : rm) {
                this.requests.remove(aux);
            }
        }
    }

    /**
     * Adds a friend (both sides) to the db
     */
    private void addFriendship(User user, String password, String source, String destiny) throws GeneralSecurityException {
        
        if(this.getUser(user.getName(), password) != null){
            try {
                BufferedReader file = new BufferedReader(new FileReader(this.dbUsers));
                StringBuffer input = new StringBuffer();
                String line;
                String aux[];
                while ((line = file.readLine()) != null) {
                    aux = line.split(",");
                    if (aux[0].equals(source)) {
                        line = line.concat("," + destiny);
                        input.append(line).append('\n');
                    } else if (aux[0].equals(destiny)) {
                        line = line.concat("," + source);
                        input.append(line).append('\n');
                    } else {
                        input.append(line).append('\n');
                    }
                }
                file.close();
                FileOutputStream fileOut = new FileOutputStream(this.dbUsers);
                fileOut.write(input.toString().getBytes());
                fileOut.close();
            } catch (FileNotFoundException ex) {
                System.out.println("Error finding the user: " + ex);
            } catch (IOException ex) {
                Logger.getLogger(ServerImplements.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    /**
     * This method is only used when you logon, using notifyFriendsOnline after
     * that
     */
    private void getConnectedFriends(User user, String password) throws GeneralSecurityException {
        if(this.getUser(user.getName(), password) != null){
            if (user.getFriends() != null) {
                for (User user2 : user.getFriends()) {
                    for (User j : this.connectedPeople) {
                        if (user2.equals(j)) {
                            try {
                                user.getIface().connectFriend(j.getName(), j.getIface());
                            } catch (RemoteException ex) {
                                System.out.println("Error getting connected friends: " + ex);
                            }
                        }
                    }
                }
            } else {
                user.setFriends(new ArrayList());
            }
        }
    }

    /**
     * When the user is online, notifies that USER is connected only to the
     * connected friends
     */
    private void notifyFriendsOnline(User u, String password) throws GeneralSecurityException {
        if(this.getUser(u.getName(), password) != null){
            for (User i : this.connectedPeople) {
                for (User j : u.getFriends()) {
                    if (i.equals(j)) {
                        try {
                            i.getIface().connectFriend(u.getName(), u.getIface());
                        } catch (RemoteException ex) {
                            System.out.println("Error connecting with the friend: " + ex);
                        }
                    }
                }
            }
        }
    }

    /**
     * Notifies that the user is offline
     */
    private void notifyFriendsOffline(User u, String password) throws GeneralSecurityException {
        if(this.getUser(u.getName(), password) != null){
            for (User i : this.connectedPeople) {
                for (User j : u.getFriends()) {
                    if (i.equals(j)) {
                        try {
                            i.getIface().connectFriend(u.getName(), null);
                        } catch (RemoteException ex) {
                            System.out.println("Error disconnecting the friend: " + ex);
                        }
                    }
                }
            }
        }
    }

    /**
     * First, it compares the name and password with the actual name and
     * password If it is true, creates the friends on his arraylist Else, return
     * null
     */
    private User getUser(String name, String password) throws GeneralSecurityException {
        try {
            Scanner sc = new Scanner(this.dbUsers);
            String line = "";
            String aux[];
            User user;
            ArrayList<User> friends = new ArrayList();
            while (sc.hasNextLine()) {
                line = sc.nextLine();
                aux = line.split(",");
                if (aux[0].equals(name) && EncryptData.decrypt(aux[1], key).equals(password)) {
                    for (int i = 2; i < aux.length; i++) {
                        User friend = new User(aux[i]);
                        friends.add(friend);
                    }
                    user = new User(aux[0]);
                    user.setFriends(friends);

                    return user;
                }
            }
        } catch (IOException | GeneralSecurityException ex) {
            Logger.getLogger(ServerImplements.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }

    /**
     * Checks if this user exists
     */
    private boolean existsUser(String name) {
        try {
            Scanner sc = new Scanner(this.dbUsers);
            String line = "";
            String aux[];
            while (sc.hasNextLine()) {
                line = sc.nextLine();
                aux = line.split(",");
                if (aux[0].equals(name)) {
                    return true;
                }
            }
        } catch (FileNotFoundException ex) {
            System.out.println("Error al buscar el usuario: " + ex);
        }
        return false;
    }
}
