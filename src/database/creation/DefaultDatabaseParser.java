package database.creation;

import database.creation.database_enums.Rarity;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 * Default implementation of the {@link DatabaseParser} interface.
 */
public class DefaultDatabaseParser extends DefaultDatabasePort implements DatabaseParser {

  /**
   * JSON object of set currently being read.
   */
  private JSONObject setBeingRead;

  /**
   * Shorthand name of set currently being processed.
   */
  private String shorthandSetName;

  /**
   * Creates a new MTG set in the CDDB based off the given info.
   * @param abbreviation shorthand name of the set
   * @param expansion full name of the set
   * @param block block the set is apart of
   * @throws IllegalArgumentException if any of the given parameters are null, or if CDDB fails
   *         add the given set info
   */
  private void addSet(String abbreviation, String expansion, String block) throws IllegalArgumentException {
    if (abbreviation == null || expansion == null || block == null) {
      throw new IllegalArgumentException("Given parameters can't be null!");
    }
    String insertStatement = "INSERT INTO Expansion(abbrv,expansion,block) VALUES (?,?,?)";

    try {
      PreparedStatement preparedStatement = connection.prepareStatement(insertStatement);
      preparedStatement.setString(1, abbreviation);
      preparedStatement.setString(2, expansion);
      preparedStatement.setString(3, block);
      preparedStatement.executeUpdate();
    }
    catch (SQLException e) {
      System.out.println("Failed to add set!");
      System.out.println(e.getMessage());
    }
  }

  @Override
  public void parse(Path path) throws IllegalArgumentException, IllegalStateException {
    if (connection == null) {
      throw new IllegalStateException("Database hasn't been connected yet!");
    }
    else if (path == null) {
      throw new IllegalArgumentException("Given path can't be null!");
    }
    else if (!Files.exists(path)) {
      throw new IllegalArgumentException("Given path doesn't exist!");
    }

    try {
      String fileAsString = pathToString(path);
      setBeingRead = new JSONObject(fileAsString);
    }
    catch (IOException e) {
      System.out.println(e.getMessage());
      throw new IllegalArgumentException("Given path failed to be successfully read!");
    }

    //System.out.println(setBeingRead.getString("code") + setBeingRead.getString("name") + setBeingRead.getString("block"));
    addSet(setBeingRead.getString("code"),
           setBeingRead.getString("name"),
           setBeingRead.getString("block"));

    shorthandSetName = setBeingRead.getString("code");
    JSONArray cards = setBeingRead.getJSONArray("cards");

    int length = cards.length();
    for (int i = 0; i < length; i += 1) {
      JSONObject card = cards.getJSONObject(i);
      addCard(card);
    }
  }

  /**
   * Given the JSON object of a MTG card, from JSON file from MTGJSON, adds it to the CDDB as
   * appropriate.
   * @param card card to add
   * @throws IllegalArgumentException if given JSON object is null, or isn't a JSONObject of a card
   *         as per MTGJSON documentation
   */
  private void addCard(JSONObject card) throws IllegalArgumentException {
    if (card == null) {
      throw new IllegalArgumentException("Give card can't be null!");
    }

    String cardName = card.getString("name");

    boolean cardAdded = true;
    try {
      // See if card has already been added to the CDDB
      String selectStatement = "SELECT name FROM Card WHERE name=?";
      PreparedStatement preparedStatement = connection.prepareStatement(selectStatement);
      preparedStatement.setString(1, cardName);
      ResultSet result = preparedStatement.executeQuery();

      // Resulting table should either have 0 or 1 rows, if 0 then card hasn't been added, if 1 then
      // card has been added
      if (!result.first()) {
        cardAdded = false;
      }
      // See if version of this card from set currently being read has already been added
    }
    catch (SQLException e) {
      System.out.print(e);
      throw new IllegalStateException("Failed to is see if card " + cardName + " is in CDDB!");
    }

    // Card hasn't been added, add card, super types, types, and subtypes, mana types,
    if (!cardAdded) {
      try {
        String insertStatement = "INSERT INTO Card(name,text,cmc) VALUES (?,?,?)";
        PreparedStatement preparedStatement = connection.prepareStatement(insertStatement);
        String cardText = card.getString("text");
        int cardCMC = (int)card.getFloat("convertedManaCost");
        preparedStatement.setString(1, cardName);
        preparedStatement.setString(2, cardText);
        preparedStatement.setInt(3, cardCMC);
        preparedStatement.executeUpdate();
      }
      catch (SQLException e) {
        System.out.println(e);
        throw new IllegalStateException("Failed to add card " + cardName + "!");
      }
    }


    // Card is for sure in database, add relevant set
    try {
      String insertStatement
          = "INSERT INTO CardExpansion(name,expansion,rarity,flavor_text,artist) VALUES (?,?,?,?,?)";
      PreparedStatement preparedStatement = connection.prepareStatement(insertStatement);
      preparedStatement.setString(1, cardName);
      preparedStatement.setString(2, shorthandSetName);
      preparedStatement.setInt(3, Rarity.matches(card.getString("rarity")).getDatabaseID());
      preparedStatement.setString(4, card.getString("flavorText"));
      preparedStatement.setString(5, card.getString("artist"));
      preparedStatement.executeUpdate();
    }
    catch (SQLException e) {
      System.out.println(e);
      throw new IllegalStateException("Failed to add card " + cardName + " and set info!");
    }
  }

  /**
   * Given a file {@link Path} to a JSON file of a MTG set from MTGJSON, attempts to convert that object
   * into a String.
   * @param toConvert path to convert
   * @return String of the contents of the given path
   */
  private String pathToString(Path toConvert) throws IOException {
    File converedFile = toConvert.toFile();
    BufferedReader reader = new BufferedReader(new FileReader(converedFile));

    try {
      StringBuilder toReturn = new StringBuilder();
      String currentLine = reader.readLine();

      while (currentLine != null) {
        toReturn.append(currentLine);
        currentLine = reader.readLine();
      }
      return toReturn.toString();
    }
    finally {
      reader.close();
    }
  }
}
