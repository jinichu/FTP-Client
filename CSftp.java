
import java.lang.System;
import java.io.IOException;
import java.io.Console;
import java.net.Socket;

//
// This is an implementation of a simplified version of a command
// line ftp client. The program always takes two arguments
//


public class CSftp {

  static final int MAX_LEN = 255;
  static final int ARG_CNT = 2;

  public static void main(String [] args) {
    // Get command line arguments and connected to FTP
    // If the arguments are invalid or there aren't enough of them
    // then exit.

    if (args.length != ARG_CNT) {
      System.out.print("Usage: cmd ServerAddress ServerPort\n");
      return;
    }

    Socket client;

    try {
      client = new Socket(args[0], Integer.parseInt(args[1]));
    } catch (Exception exception) {
      System.err.println("0x398 Failed to open socket to server");
      System.exit(1);
    }

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
            System.out.println("TODO: user stuff");
            continue outer;
          }
        case "pw":
          if (parts.length != 2) {
            System.out.println("0x002 Incorrect number of arguments.");
            continue outer;
          } else {
            System.out.println("TODO: pw stuff");
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
          System.out.println("TODO: fEaTuREs");
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
}
