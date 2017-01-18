
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
      System.err.println("998 Input error while reading commands, terminating.");
      System.exit(1);
    }

    outer:
    while (true) {
      String line = c.readLine("csftp> ");
      if (line.length() == 0) {
        System.out.println("900 Invalid command.");
        continue;
      }
      String parts[] = line.split(" ");
      if (parts.length == 0) {
        System.out.println("900 Invalid command.");
        continue;
      }
      String cmd = parts[0];
      switch (cmd) {
        case "quit":
        System.out.println("TODO");
        continue outer;
        default:
        System.out.println("900 Invalid command.");
      }
    }
  }
}
