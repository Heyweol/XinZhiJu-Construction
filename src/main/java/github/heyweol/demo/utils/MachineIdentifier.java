package github.heyweol.demo.utils;

import java.net.NetworkInterface;
import java.security.MessageDigest;
import java.util.UUID;

public class MachineIdentifier {
  public static String getUniqueIdentifier() {
    try {
      String osName = System.getProperty("os.name");
      String osVersion = System.getProperty("os.version");
      String username = System.getProperty("user.name");
      String macAddress = getMacAddress();
      
      String combined = osName + osVersion + username + macAddress;
      
      MessageDigest md = MessageDigest.getInstance("SHA-256");
      byte[] digest = md.digest(combined.getBytes());
      
      return UUID.nameUUIDFromBytes(digest).toString();
    } catch (Exception e) {
      e.printStackTrace();
      return UUID.randomUUID().toString();
    }
  }
  
  private static String getMacAddress() throws Exception {
    NetworkInterface network = NetworkInterface.getByInetAddress(java.net.InetAddress.getLocalHost());
    byte[] mac = network.getHardwareAddress();
    StringBuilder sb = new StringBuilder();
    for (int i = 0; i < mac.length; i++) {
      sb.append(String.format("%02X%s", mac[i], (i < mac.length - 1) ? "-" : ""));
    }
    return sb.toString();
  }
}