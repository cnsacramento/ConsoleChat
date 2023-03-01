package es.iespuertodelacruz.cns.server.database;

import java.util.Map;
import java.util.TreeMap;

public class UsersBD {
    private static Map<String,String> bdUsers = new TreeMap<>();

    static {
        bdUsers.put("1234", "1234");
        bdUsers.put("chris", "chris");
        bdUsers.put("user", "user");
        bdUsers.put("admin", "admin");
    }

    public static boolean checkPassword(String user, String pass) {
        return pass.equals(bdUsers.get(user));
    }

    public static Map<String, String> getBdUsers() {
        return bdUsers;
    }

}