import java.io.BufferedReader;
import java.io.Console;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.lang.System;
import java.net.Socket;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

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
      System.out.println("0x398 Failed to open socket to server");
      System.exit(1);
    }

    System.out.println(readMessage(""));

    Console c = System.console();
    if (c == null) {
      System.err.println("0xFFFE Input error while reading commands, terminating.");
      System.exit(1);
    }

    // Loop to get further commands from console, unless otherwise
    // (such as errors, quitting, etc.)
    outer:
    while (true) {
      String line = c.readLine("csftp> ");
      // Split commands at whitespaces
      String parts[] = line.split(" ");
      if (parts.length == 0) {
        System.out.println("0x001 Invalid command.");
        continue;
      }
      /* Command will always be the first argument from console input
      Some commands will require two parts, so we check for correct
      number of arguments. */
      String cmd = parts[0];
      switch (cmd) {
        // We send QUIT to the server using sendSimpleCommand
        case "quit":
          if (parts.length != 1) {
            System.out.println("0x002 Incorrect number of arguments.");
            continue outer;
          } else {
            System.out.println(sendSimpleCommand("QUIT"));
            try {
              client.close();
            } catch(IOException e) {
              System.out.println("0xFFFF Processing error. Failed to close control connection.");
            }
            System.exit(0);
            break;
          }
        // Second argument is the username
        // We send USER [username] to the server using sendSimpleCommand
        case "user":
          if (parts.length != 2) {
            System.out.println("0x002 Incorrect number of arguments.");
            continue outer;
          } else {
            System.out.println(sendSimpleCommand("USER "+parts[1]));
            continue outer;
          }
        // Second argument is the password
        // We send PASS [password] to the server using sendSimpleCommand
        case "pw":
          if (parts.length != 2) {
            System.out.println("0x002 Incorrect number of arguments.");
            continue outer;
          } else {
            System.out.println(sendSimpleCommand("PASS " + parts[1]));
            continue outer;
          }
        // Second argument is the file we want to retrieve
        // We send RETR [file] to the server using sendSimpleCommand
        case "get":
          if (parts.length != 2) {
            System.out.println("0x002 Incorrect number of arguments.");
            continue outer;
          } else {
            File f = new File(parts[1]);
            // Check if file is read-only, or if we have write permissions to it
            // If file is read-only, we throw an error
            try {
              f.createNewFile();
            } catch(IOException e){}
            if (!f.canWrite()) {
              System.out.println("0x38E Access to local file " + parts[1] + " denied.");
              continue outer;
            }
            // Open a new connection with the IP address given back by the server
            Socket sock;
            try {
              sock = PASV();
            } catch (Error e) {
              System.out.println(e.getMessage());
              continue outer;
            }
            // After we've opened a new connection, we send RETR [file] to the server
            String resp = sendSimpleCommand("RETR " + parts[1]);
            System.out.println(resp);
            // Response code 150 means it was successful
            if (responseCode(resp) != 150) {
              continue outer;
            }
            // Save file to local machine, overwriting existing file if needed
            try {
              Files.copy(sock.getInputStream(), f.toPath(), StandardCopyOption.REPLACE_EXISTING);
            } catch (IOException err) {
              System.out.println("0x3A7 Data transfer connection I/O error, closing data connection. "+err);
            }
            // Close socket
            try {
              sock.close();
            } catch (IOException err) {
              System.out.println("0xFFFF Processing error. Failed to close data connection.");
            }
            if (hasMultipleResp(resp)) {
              System.out.println(readMessage(""));
            }
            continue outer;
          }
        // We send FEAT 211 End to the server using sendSimpleCommand
        // Server returns a list of features
        case "features":
          if (parts.length != 1) {
            System.out.println("0x002 Incorrect number of arguments.");
            continue outer;
          } else {
            System.out.println(sendSimpleCommand("FEAT", "211 End"));
            continue outer;
          }
        // Second argument is the folder we want to change directory to
        // We send CWD [directory] to the server using sendSimpleCommand
        case "cd":
          if (parts.length != 2) {
            System.out.println("0x002 Incorrect number of arguments.");
            continue outer;
          } else {
            System.out.println(sendSimpleCommand("CWD " + parts[1]));
            continue outer;
          }
        // We send LIST to the server using sendSimpleCommand
        case "dir":
          if (parts.length != 1) {
            System.out.println("0x002 Incorrect number of arguments.");
            continue outer;
          } else {
            // Open a new connection with the IP address given back by the server
            Socket sock;
            try {
              sock = PASV();
            } catch (Error e) {
              System.out.println(e.getMessage());
              continue outer;
            }
            String resp = sendSimpleCommand("LIST");
            System.out.println(resp);
            // Read out list of directories given back by the server
            try {
              BufferedReader in = new BufferedReader(new InputStreamReader(sock.getInputStream()));
              String line2;
              while ((line2 = in.readLine()) != null) {
                System.out.println(line2);
              }
            } catch (IOException err) {
              System.out.println("0x3A7 Data transfer connection I/O error, closing data connection.");
            }
            // Close socket
            try {
              sock.close();
            } catch (IOException err) {
              System.out.println("0xFFFF Processing error. Failed to close data connection.");
            }
            if (hasMultipleResp(resp)) {
              System.out.println(readMessage(""));
            }
            continue outer;
          }
        default:
          System.out.println("0x001 Invalid command.");
      }
    }
  }
  // PASV opens a new passive data connection and returns the corresponding
  // socket.
  private static Socket PASV() {
    String resp = sendSimpleCommand("PASV");

    // Check for valid response code.
    int code = responseCode(resp);
    if (code != 227) {
      throw new Error(resp);
    }

    // IP address will be given in (h1,h2,h3,h4,p1,p2) format
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

    try {
      return new Socket(ip, port);
    } catch (Exception exception) {
      throw new Error("0x3A2 Data transfer connection to "+ip+" on port "+port+" failed to open.");
    }
  }
  // hasMultipleResp parses a response line and returns whether there will be
  // multiple lines returned.
  private static boolean hasMultipleResp(String line) {
    int code = responseCode(line);
    return code == 120 || code == 125 || code == 150;
  }

  // responseCode parses the response line and returns the response code.
  private static int responseCode(String line) {
    String code = line.substring(0, 3);
    return Integer.parseInt(code);
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

  // readMessage reads until end is reached, or the first line if end is empty.
  private static String readMessage(String end) {
    String output = "";
    String line;
    try {
      while ((line = in.readLine()) != null) {
        output += line;
        if (line.equals(end) || end.length() == 0) {
          break;
        }
        output += "\n";
      }
    } catch (IOException e) {
      System.out.println("0xFFFD Control connection I/O error, closing control connection.");
      try {
        client.close();
      } catch (IOException e2) {
        System.out.println("0xFFFF Processing error. Failed to close control connection.");
      }
      System.exit(1);
    }
    return output;
  }
}
