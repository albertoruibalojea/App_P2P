package server;

import java.io.Serializable;
import java.util.ArrayList;
import client.IChat;

public class User implements Serializable {

    private String name;
    //This array isn´t returned when the user is passed by reference
    private ArrayList<User> friends;
    private IChat iface;

    public User(String name) {
        this.name = name;
        this.friends = null;
        this.iface = null;
    }

    public String getName() {
        return this.name;
    }

    public ArrayList<User> getFriends() {
        return this.friends;
    }

    public void setFriends(ArrayList<User> friends) {
        this.friends = friends;
    }

    public IChat getIface() {
        return this.iface;
    }

    public void setIface(IChat iface) {
        this.iface = iface;
    }

    public void addFriend(User friend) {
        this.friends.add(friend);
    }

    //Shows if a friend is connected
    public boolean isMyFriendConnected(String friend) {
        for (User user : this.friends) {
            if (user.getName().equals(friend) && user.getIface() != null) {
                return true;
            }
        }
        return false;
    }

    //Adds the interface of a connected friend
    public void updateFriendInterface(String name, IChat friendInterface) {
        //The interface has to be of a friend in this case
        if (this.friends.contains(new User(name))) {
            for (User u : this.friends) {
                if (u.name.equals(name)) {
                    u.setIface(friendInterface);
                }
            }
        } //If it is a new friend
        else {
            User a = new User(name);
            a.setIface(friendInterface);
            this.friends.add(a);
        }
    }

    //Deletes the interface of a disconnected friend
    public void disconnectFriend(String name) {
        for (User u : this.friends) {
            if (u.name.equals(name)) {
                u.setIface(null);
            }
        }
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof User) {
            return (this.name.equals(((User) obj).getName()));
        }
        return false;
    }

}
