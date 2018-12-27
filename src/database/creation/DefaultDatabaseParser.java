package database.creation;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Set;
import org.json.JSONArray;
import org.json.JSONException;
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
      String selectStatement = "SELECT name FROM Card WHERE name = ?";
      PreparedStatement preparedStatement = connection.prepareStatement(selectStatement);
      preparedStatement.setString(1, cardName);
      ResultSet result = preparedStatement.executeQuery();
      int numOfRows = result.getRow();
      // Resulting table should either have 0 or 1 rows, if 0 then card hasn't been added, if 1 then
      // card has been added
      System.out.println(cardName + numOfRows);
      if (!result.next()) {
        cardAdded = false;
      }
    }
    catch (SQLException e) {
      System.out.print(e);
      throw new IllegalStateException("Failed to is see if card " + cardName + " is in CDDB!");
    }

    // Card hasn't been added, add card, super types, types, and subtypes, mana types,
    if (!cardAdded) {
      PreparedStatement preparedStatement;
      try {

        // Add card
        String insertCard = "INSERT INTO Card(name,card_text,cmc) VALUES (?,?,?)";
        preparedStatement = connection.prepareStatement(insertCard);

        String cardText = "";
        if (card.has("text")) {
          cardText = card.getString("text");
        }

        int cardCMC = (int) card.getFloat("convertedManaCost");
        preparedStatement.setString(1, cardName);
        preparedStatement.setString(2, cardText);
        preparedStatement.setInt(3, cardCMC);
        preparedStatement.executeUpdate();
      }
      catch (SQLException e) {
        System.out.println(e);
        throw new IllegalStateException("Failed to add card " + cardName + "!");
      }

      try {
        // Add supertypes
        String[] supertypes = JSONArrayToStringArray(card.getJSONArray("supertypes"));
        for (String supertype : supertypes) {
          String insertSupertype = "INSERT INTO CardSupertype(card_name,supertype) VALUES (?,?)";
          preparedStatement = connection.prepareStatement(insertSupertype);
          preparedStatement.setString(1, cardName);
          preparedStatement.setString(2, supertype.toLowerCase());
          preparedStatement.executeUpdate();
        }
      }
      catch (SQLException e) {
        System.out.println(e);
        throw new IllegalStateException("Failed to add supertypes for " + cardName + "!");
      }

      if (card.has("types")) {
        try {
          //Add types
          String[] types = JSONArrayToStringArray(card.getJSONArray("types"));
          for (String type : types) {
            String insertType = "INSERT INTO CardType(card_name,type) VALUES (?,?)";
            preparedStatement = connection.prepareStatement(insertType);
            preparedStatement.setString(1, cardName);
            preparedStatement.setString(2, type.toLowerCase());
            preparedStatement.executeUpdate();
          }
        }
        catch (SQLException e) {
          System.out.println(e);
          throw new IllegalStateException("Failed to add types for " + cardName + "!");
        }
      }

      if (card.has("subtypes")) {
        try {
          // Add subtypes
          String[] subtypes = JSONArrayToStringArray(card.getJSONArray("subtypes"));
          for (String subtype : subtypes) {
            String insertSubtype = "INSERT INTO CardSubtype(card_name,subtype) VALUES (?,?)";
            preparedStatement = connection.prepareStatement(insertSubtype);
            preparedStatement.setString(1, cardName);
            preparedStatement.setString(2, subtype.toLowerCase());
            preparedStatement.executeUpdate();
          }
        }
        catch (SQLException e) {
          System.out.println(e);
          throw new IllegalStateException("Failed to add subtypes for " + cardName + "!");
        }
      }

      if (card.has("manaCost"))
      try {
        // Add mana costs
        String[] manaCosts = card.getString("manaCost").split("(?=\\{)");
        HashMap<String, Integer> uniqueManaCosts = new HashMap<>();
        for (String manaCost : manaCosts) {
          if (uniqueManaCosts.containsKey(manaCost)) {
            uniqueManaCosts.replace(manaCost, uniqueManaCosts.get(manaCost) + 1);
          } else {
            uniqueManaCosts.put(manaCost, 1);
          }
        }
        for (String manaCost : uniqueManaCosts.keySet()) {
          String manaCostInsert = "INSERT INTO CardMana(card_name,mana_type,quantity) VALUES (?,?,?)";
          preparedStatement = connection.prepareStatement(manaCostInsert);
          preparedStatement.setString(1, cardName);
          preparedStatement.setString(2, manaCost);
          preparedStatement.setInt(3, uniqueManaCosts.get(manaCost));
          preparedStatement.executeUpdate();
        }
      }
      catch (SQLException e) {
        System.out.println(e);
        throw new IllegalStateException("Failed to add mana costs for " + cardName + "!");
      }

      try {
        // If creature or vehicle, add p/t - if planeswalker add loyalty
        if (card.has("power") && card.has("toughness")) {
          String ptInsert = "INSERT INTO PTStats(card_name,power,toughness) VALUES (?,?,?)";
          preparedStatement = connection.prepareStatement(ptInsert);
          preparedStatement.setString(1, cardName);
          preparedStatement.setString(2, card.getString("power"));
          preparedStatement.setString(3, card.getString("toughness"));
          preparedStatement.executeUpdate();
        } else if (card.has("loyalty")) {
          String planeswalkerInsert = "INSERT INTO PlaneswalkerStats(card_name,loyalty) VALUES (?,?)";
          preparedStatement = connection.prepareStatement(planeswalkerInsert);
          preparedStatement.setString(1, cardName);
          preparedStatement.setString(2, card.getString("loyalty"));
          preparedStatement.executeUpdate();
        }
      }
      catch (SQLException e) {
        System.out.println(e);
        throw new IllegalStateException("Failed to add extra stats for  " + cardName + "!");
      }
    }


    // Card is for sure in database, add relevant set
    try {
      // See if card has already been added to the CDDB
      String selectStatement = "SELECT card_name, expansion FROM CardExpansion "
          + "WHERE card_name = ?"
          + "AND expansion = ?";
      PreparedStatement preparedStatement = connection.prepareStatement(selectStatement);
      preparedStatement.setString(1, cardName);
      preparedStatement.setString(2, shorthandSetName);
      ResultSet result = preparedStatement.executeQuery();
      int numOfRows = result.getRow();
      // Resulting table should either have 0 or 1 rows, if 0 then card hasn't been added, if 1 then
      // card has been added
      System.out.println(cardName + numOfRows);
      if (!result.next()) {
        String insertStatement
            = "INSERT INTO CardExpansion(card_name,expansion,rarity,flavor_text,artist) VALUES (?,?,?,?,?)";
        preparedStatement = connection.prepareStatement(insertStatement);
        preparedStatement.setString(1, cardName);
        preparedStatement.setString(2, shorthandSetName);
        preparedStatement.setString(3, card.getString("rarity"));

        String flavorText = "";
        if (card.has("flavorText")) {
          flavorText = card.getString("flavorText");
        }

        preparedStatement.setString(4, flavorText);
        preparedStatement.setString(5, card.getString("artist"));
        preparedStatement.executeUpdate();
      }
    }
    catch (SQLException e) {
      System.out.println(e);
      throw new IllegalStateException("Failed to add card " + cardName + " and set info!");
    }
  }

  /**
   * Given a JSONArray of Strings, converts it to an array of Strings.
   * @param toConvert JSONArray of Strings to convert
   * @return resulting String array
   * @throws IllegalArgumentException if given JSONArray is null, or isn't entirely made of Strings
   */
  private String[] JSONArrayToStringArray(JSONArray toConvert) throws IllegalArgumentException {
    if (toConvert == null) {
      throw new IllegalArgumentException("Give JSONArray can't be null!");
    }

    try {
      int length = toConvert.length();
      String[] toReturn = new String[length];
      for (int i = 0; i < length; i += 1) {
        toReturn[i] = toConvert.getString(i);
      }
      return toReturn;
    }
    catch (JSONException e) {
      System.out.println(e);
      throw new IllegalArgumentException("Given JSON array isn't entirely made of Strings!");
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
