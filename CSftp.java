
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

    System.out.println(readMessage());

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
          System.out.println(sendSimpleCommand("FEAT"));
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
          System.out.println("TODO: fEaTuREs");
          continue outer;
        default:
          System.out.println("0x001 Invalid command.");
      }
    }
  }

  private static String sendSimpleCommand(String cmd) {
    out.print(cmd+"\r\n");
    out.flush();
    return readMessage();
  }

  private static String readMessage() {
    try {
      return in.readLine();
    } catch (IOException e) {
      System.out.println("TODO error reading from server");
      System.exit(1);
    }
    return "";
  }
}
