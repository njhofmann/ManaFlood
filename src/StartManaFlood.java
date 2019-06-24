import database.access.DatabaseChannel;
import database.access.DefaultDatabaseChannel;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.SQLException;
import relay.ChannelViewRelay;
import relay.DefaultChannelViewRelay;
import view.DatabaseView;
import view.GUIView;
import view.TerminalView;

/**
 * Starts up the ManaFlood application as either a terminal or GUI display.
 */
public class StartManaFlood {

  private static final Path pathToDatabase = Paths.get("resources\\cddb.db").toAbsolutePath();

  /**
   * Starts up the ManaFlood application based on the given String parameters.
   * @param args parameters to use for starting up the application
   * @throws IllegalArgumentException if given set of parameters do no constitute a valid parameter
   * set - i.e. they don't contain all requried arguments, they contain formed arguments, they
   * contain unsupported arguments, etc.
   */
  public static void main(String[] args) {
    if (args == null || args.length == 0) {
      throw new IllegalArgumentException("Given set of parameters can't be null!");
    }
    else if (args.length != 1) {
      throw new IllegalArgumentException("Given set of parameters must contain only one pararmeter!");
    }

    DatabaseView databaseView;
    String param = args[0].toLowerCase();
    if (param.equals("terminal")) {
      databaseView = new TerminalView();
    }
    else if (param.equals("gui")) {
      databaseView = new GUIView();
    }
    else {
      throw new IllegalArgumentException("Diplay parameter doesn't contain a valid type of display "
          + "to start the application with!");
    }

    DatabaseChannel databaseChannel = null;
    try {
      databaseChannel = new DefaultDatabaseChannel(StartManaFlood.pathToDatabase);
    }
    catch (SQLException e) {
      System.out.println("Failed to start ManaFlood application!");
      System.out.println(e.getSQLState());
      System.exit(1);
    }
    ChannelViewRelay channelViewRelay = new DefaultChannelViewRelay(databaseChannel, databaseView);
    channelViewRelay.start();
  }
}
