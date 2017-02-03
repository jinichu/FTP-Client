
import java.io.BufferedReader;
import java.io.Console;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.System;
import java.net.Socket;
import java.io.InputStreamReader;

//
// This is an implementation of a simplified version of a command
// line ftp client. The program always takes two arguments
//


public class CSftp {
  static final int MAX_LEN = 255;
  static final int ARG_CNT = 2;

  static Socket client;
  static PrintWriter out = null;
  static BufferedReader in = null;

  public static void main(String [] args) {
    // Get command line arguments and connected to FTP
    // If the arguments are invalid or there aren't enough of them
    // then exit.

    if (args.length != ARG_CNT) {
      System.out.print("Usage: cmd ServerAddress ServerPort\n");
      return;
    }

    try {
      client = new Socket(args[0], Integer.parseInt(args[1]));
      out = new PrintWriter(client.getOutputStream(), true);
      in = new BufferedReader(new InputStreamReader(client.getInputStream()));
    } catch (Exception exception) {
      System.err.println("0x398 Failed to open socket to server");
      System.exit(1);
    }

    System.out.println(readMessage(""));

    Console c = System.console();
    if (c == null) {
      System.err.println("0xFFFE Input error while reading commands, terminating.");
      System.exit(1);
    }

    outer:
    while (true) {
      String line = c.readLine("csftp> ");
      String parts[] = line.split(" ");
      if (parts.length == 0) {
        System.out.println("0x001 Invalid command.");
        continue;
      }
      String cmd = parts[0];
      switch (cmd) {
        case "quit":
          System.out.println("Goodbye.");
          System.exit(0);
          break;
        case "user":
          if (parts.length != 2) {
            System.out.println("0x002 Incorrect number of arguments.");
            continue outer;
          } else {
            System.out.println(sendSimpleCommand("USER "+parts[1]));
            continue outer;
          }
        case "pw":
          if (parts.length != 2) {
            System.out.println("0x002 Incorrect number of arguments.");
            continue outer;
          } else {
            System.out.println(sendSimpleCommand("PASS "+parts[1]));
            continue outer;
          }
        case "get":
          if (parts.length != 2) {
            System.out.println("0x002 Incorrect number of arguments.");
            continue outer;
          } else {
            System.out.println("TODO: get stuff");
            continue outer;
          }
        case "features":
          System.out.println(sendSimpleCommand("FEAT", "211 End"));
          continue outer;
        case "cd":
          if (parts.length != 2) {
            System.out.println("0x002 Incorrect number of arguments.");
            continue outer;
          } else {
            System.out.println("TODO: cd stuff");
            continue outer;
          }
        case "dir":
          Socket sock = PASV();
          System.out.println(sendSimpleCommand("LIST"));
          try {
            BufferedReader in = new BufferedReader(new InputStreamReader(sock.getInputStream()));
            String line2;
            while ((line2 = in.readLine()) != null) {
              System.out.println(line2);
            }
          } catch (IOException err) {
            System.out.println("0x3A7 Data transfer connection I/O error, closing data connection.");
          }
          continue outer;
        default:
          System.out.println("0x001 Invalid command.");
      }
    }
  }

  private static Socket PASV() {
    String resp = sendSimpleCommand("PASV");
    System.out.println(resp);
    int start = resp.indexOf("(");
    int end = resp.indexOf(")");
    if (start == -1 || end == -1) {
      throw new Error("0xFFFF Processing error. Response missing (): "+resp);
    }
    String portRegion = resp.substring(start+1, end);
    String[] parts = portRegion.split(",");
    if (parts.length != 6) {
      throw new Error("0xFFFF Processing error. Invalid parts length: "+resp);
    }

    // Build IP into usable format.
    String ip = parts[0];
    for (int i=1;i<4;i++) {
      ip += "." + parts[i];
    }

    int port = Integer.parseInt(parts[4])*256 + Integer.parseInt(parts[5]);
    System.out.println("IP: "+ip+", port: "+port);

    try {
      return new Socket(ip, port);
    } catch (Exception exception) {
      throw new Error("0x3A2 Data transfer connection to "+ip+" on port "+port+" failed to open.");
    }
  }

  // sendSimpleCommand sends the command and returns the first line of the
  // response.
  private static String sendSimpleCommand(String cmd) {
    return sendSimpleCommand(cmd, "");
  }

  // sendSimpleCommand sends the command and returns the response until end is
  // seen, or the first line if end is empty.
  private static String sendSimpleCommand(String cmd, String end) {
    out.print(cmd+"\r\n");
    out.flush();
    return readMessage(end);
  }

  private static String readMessage(String end) {
    String output = "";
    String line;
    try {
      while ((line = in.readLine()) != null) {
        output += line+"\n";
        if (line.equals(end) || end.length() == 0) {
          break;
        }
      }
    } catch (IOException e) {
      System.out.println("TODO error reading from server");
      System.exit(1);
    }
    return output;
  }
}
