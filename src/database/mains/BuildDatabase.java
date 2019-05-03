package database.mains;

import database.DatabasePort;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.SQLException;

public class BuildDatabase {

  public static void main(String[] args) throws SQLException {
    Path pathToDatabase = Paths.get("resources\\test.db").toAbsolutePath();
    Path pathToInitFile = Paths.get("resources\\database_init.txt");
    DatabasePort port = new DatabasePort(pathToDatabase, pathToInitFile);
  }

}
