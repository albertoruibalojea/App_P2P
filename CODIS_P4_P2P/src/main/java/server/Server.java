package server;

import java.io.IOException;
import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.Scanner;

public class Server {

    public static void main(String[] args) throws IOException {
        try {
            String portNum = null, registryURL;
            Scanner sc = new Scanner(System.in);
            System.setProperty("java.rmi.server.hostname", "localhost");
            System.out.println("Introduce el puerto de RMIregistry:");
            //portNum = sc.nextLine();
            portNum = "1024";
            int RMIPortNum = Integer.parseInt(portNum);

            /*Creacion del RMI Registry en el puerto correspondiente*/
            startRegistry(RMIPortNum);

            ServerImplements exportedObj = new ServerImplements();
            registryURL = "rmi://localhost:" + portNum + "/messaging";
            /*Registro del objeto en el RMI registry*/
            Naming.rebind(registryURL, exportedObj);

        } catch (RemoteException e) {
            System.out.println("Error" + e);
        } catch (MalformedURLException e) {
            System.out.println("Error" + e);
        }
    }

    private static void startRegistry(int RMIPortNum) throws RemoteException {
        try {
            Registry registry = LocateRegistry.getRegistry(RMIPortNum);
            registry.list();  // This call will throw an exception
            // if the registry does not already exist
        } catch (RemoteException e) {
            // No valid registry at that port.
            System.out.println("RMI registry cannot be located at port " + RMIPortNum);
            Registry registry = LocateRegistry.createRegistry(RMIPortNum);
            System.out.println("RMI registry created at port " + RMIPortNum);
        }
    }
}
