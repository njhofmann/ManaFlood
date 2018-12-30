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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
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

  List<PreparedStatement> multifacedCards = new ArrayList<>();


  /**
   * Given a JSONObject of a MTG set from MTGJSON, adds its info to the CDB. If set has already been
   * added, does nothing.
   * @param set JSON object of a MTG set to add to
   * @throws IllegalArgumentException if given set is null, doesn't have one of the required /
   *         documented fields, or fails to be added / queried to the CDDB
   */
  private void addSet(JSONObject set) throws IllegalArgumentException {
    if (set == null) {
      throw new IllegalArgumentException("Given set can't be null!");
    }

    PreparedStatement preparedStatement;

    String code;
    try {
      code = set.getString("code").toUpperCase();
    }
    catch (JSONException e) {
      System.out.println(e);
      throw new IllegalArgumentException("Given JSONObject isn't a set, doesn't have attribute 'code'!");
    }

    // Check if set has been added
    boolean setAdded = false;
    try {
      String checkForSet = "SELECT abbrv FROM Expansion WHERE abbrv = ?";
      preparedStatement = connection.prepareStatement(checkForSet);
      preparedStatement.setString(1, code);
      ResultSet result = preparedStatement.executeQuery();

      if (!result.next()) {
        setAdded = false;
      }
    }
    catch (SQLException e) {
      System.out.println(e);
      throw new IllegalArgumentException("Failed to query for set " + code + "!");
    }

    if (!setAdded) {
      String name;
      try {
        name = set.getString("name");
      }
      catch (JSONException e) {
        System.out.println(e);
        throw new IllegalArgumentException("Given JSONObject isn't a set, doesn't have attribute 'name'!");
      }

      String block;
      try {
        block = set.getString("block");
      }
      catch (JSONException e) {
        System.out.println(e);
        throw new IllegalArgumentException("Given JSONObject isn't a set, doesn't have attribute 'block'!");
      }

      int size;
      try {
        size = set.getInt("totalSetSize");
      }
      catch (JSONException e) {
        System.out.println(e);
        throw new IllegalArgumentException("Given JSONObject isn't a set, doesn't have attribute 'size'!");
      }

      try {
        String insertStatement = "INSERT INTO Expansion(abbrv,expansion,block,size) VALUES (?,?,?,?)";
        preparedStatement = connection.prepareStatement(insertStatement);
        preparedStatement.setString(1, code);
        preparedStatement.setString(2, name);
        preparedStatement.setString(3, block);
        preparedStatement.setInt(4, size);
        preparedStatement.executeUpdate();
      }
      catch (SQLException e) {
        System.out.println(e.getMessage());
        throw new IllegalArgumentException("Failed to add given set!");
      }
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

    JSONObject setBeingRead;
    try {
      String fileAsString = pathToString(path);
      setBeingRead = new JSONObject(fileAsString);
    }
    catch (IOException e) {
      System.out.println(e.getMessage());
      throw new IllegalArgumentException("Given path failed to be successfully read!");
    }

    addSet(setBeingRead);

    shorthandSetName = setBeingRead.getString("code").toUpperCase();

    JSONArray cards;
    try {
      cards = setBeingRead.getJSONArray("cards");
    }
    catch (JSONException e) {
      System.out.println(e);
      throw new IllegalArgumentException("Given JSONObject isn't a set, doesn't have attribute 'cards'!");
    }

    multifacedCards = new ArrayList<>();

    int length = cards.length();
    for (int i = 0; i < length; i += 1) {
      JSONObject card = cards.getJSONObject(i);
      addCard(card);
    }

    // Add multifaced cards
    try {
      for (PreparedStatement preparedStatement : multifacedCards) {
        preparedStatement.executeUpdate();
      }
    }
    catch (SQLException e) {
      System.out.println(e);
      throw new IllegalArgumentException("Failed to add multifaced cards!");
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

      // Resulting table should either have 0 or 1 rows, if 0 then card hasn't been added, if 1 then
      // card has been added
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

        String cardCMCQuery;
        if (card.has("faceConvertedManaCost")) {
          cardCMCQuery = "faceConvertedManaCost";
        }
        else {
          cardCMCQuery = "convertedManaCost";
        }

        int cardCMC = (int) card.getFloat(cardCMCQuery);

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

      if (card.has("manaCost")) {
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

            int quantity =  uniqueManaCosts.get(manaCost);

            String withOutBrackets = manaCost.substring(1, manaCost.length() - 1);
            if (withOutBrackets.matches("\\d+")) {
              manaCost = "{1}";
              quantity = Integer.parseInt(withOutBrackets);
            }

            preparedStatement = connection.prepareStatement(manaCostInsert);
            preparedStatement.setString(1, cardName);
            preparedStatement.setString(2, manaCost);
            preparedStatement.setInt(3, quantity);
            preparedStatement.executeUpdate();
          }
        }
        catch (SQLException e) {
          System.out.println(e);
          throw new IllegalStateException("Failed to add mana costs for " + cardName + "!");
        }
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

    // If card is multifaced, and all its components have been added, add multifaced relation to set
    if (card.has("layout")) {
      String layout = card.getString("layout");
      if (layout.matches("flip|split|transform")) {
        String[] names = JSONArrayToStringArray(card.getJSONArray("names"));
        String cardA = names[0];
        String cardB = names[1];

        // Check that each card has been added
        boolean cardsAdded = true;
        for (String name : names) {
          if (!cardAdded(name)) {
            cardsAdded = false;
          }
        }

        if (cardsAdded) {
          // Check that the query hasn't been added
          boolean relationshipAdded = true;
          try {
            String multifaceQuery = "SELECT * FROM TwoCards WHERE card_a=? AND card_b=?";
            PreparedStatement preparedStatement = connection.prepareStatement(multifaceQuery);
            preparedStatement.setString(1, cardA);
            preparedStatement.setString(2, cardB);
            ResultSet result = preparedStatement.executeQuery();

            if (!result.next()) {
              relationshipAdded = false;
            }
          }
          catch (SQLException e) {
            e.printStackTrace();
            throw new IllegalStateException("Failed to query for relationship between " +
                cardA + " and " + cardB +"!");
          }

          if (!relationshipAdded) {
            try {
              String multifaceUpdate = "INSERT INTO TwoCards(card_a,card_b,type,total_cmc) "
                  + "VALUES (?,?,?,?)";
              PreparedStatement preparedStatement = connection.prepareStatement(multifaceUpdate);
              preparedStatement.setString(1, cardA);
              preparedStatement.setString(2, cardB);
              preparedStatement.setString(3, layout);
              preparedStatement.setInt(4, (int) card.getFloat("convertedManaCost"));
              preparedStatement.executeUpdate();
            }
            catch (SQLException e) {
              e.printStackTrace();
              throw new IllegalStateException("Failed to query for relationship between " +
                  cardA + " and " + cardB +"!");
            }
          }
        }
      }
      else if (layout.matches("meld")) {
        String[] names = JSONArrayToStringArray(card.getJSONArray("names"));
        String cardA = names[0];
        String cardB = names[1];
        String cardC = names[2];

        // Check that each card has been added
        boolean cardsAdded = true;
        for (String name : names) {
          if (!cardAdded(name)) {
            cardsAdded = false;
          }
        }

        if (cardsAdded) {
          // Check that the query hasn't been added
          boolean relationshipAdded = true;
          try {
            String multifaceQuery = "SELECT * FROM ThreeCards WHERE card_a=? AND card_b=? AND card_c=?";
            PreparedStatement preparedStatement = connection.prepareStatement(multifaceQuery);
            preparedStatement.setString(1, cardA);
            preparedStatement.setString(2, cardB);
            preparedStatement.setString(3, cardC);
            ResultSet result = preparedStatement.executeQuery();

            if (!result.next()) {
              relationshipAdded = false;
            }
          }
          catch (SQLException e) {
            e.printStackTrace();
            throw new IllegalStateException("Failed to query for relationship between " +
                cardA + "," + cardB + " and " + cardC + "!");
          }

          if (!relationshipAdded) {
            try {
              String multifaceUpdate = "INSERT INTO ThreeCards(card_a,card_b,card_c,type,total_cmc) "
                  + "VALUES (?,?,?,?,?)";
              PreparedStatement preparedStatement = connection.prepareStatement(multifaceUpdate);
              preparedStatement.setString(1, cardA);
              preparedStatement.setString(2, cardB);
              preparedStatement.setString(3, cardC);
              preparedStatement.setString(4, layout);
              preparedStatement.setInt(5, (int) card.getFloat("convertedManaCost"));
              preparedStatement.executeUpdate();
            }
            catch (SQLException e) {
              e.printStackTrace();
              throw new IllegalStateException("Failed to query for relationship between " +
                  cardA + "," + cardB + " and " + cardC + "!");
            }
          }
        }
      }
    }

    // Card is for sure in database, add relevant set
    try {
      // See if card has already been added to the CDDB
      String selectStatement = "SELECT card_name, expansion, number FROM CardExpansion "
          + "WHERE card_name = ?"
          + "AND expansion = ?"
          + "AND number = ?";
      PreparedStatement preparedStatement = connection.prepareStatement(selectStatement);
      preparedStatement.setString(1, cardName);
      preparedStatement.setString(2, shorthandSetName);
      preparedStatement.setInt(3, Integer.parseInt(card.getString("number")));
      ResultSet result = preparedStatement.executeQuery();
      int numOfRows = result.getRow();
      // Resulting table should either have 0 or 1 rows, if 0 then card hasn't been added, if 1 then
      // card has been added
      if (!result.next()) {
        String insertStatement
            = "INSERT INTO CardExpansion(card_name,expansion,number,rarity,flavor_text,artist) VALUES (?,?,?,?,?,?)";
        preparedStatement = connection.prepareStatement(insertStatement);
        preparedStatement.setString(1, cardName);
        preparedStatement.setString(2, shorthandSetName);
        preparedStatement.setInt(3, Integer.parseInt(card.getString("number")));
        preparedStatement.setString(4, card.getString("rarity"));

        String flavorText = "";
        if (card.has("flavorText")) {
          flavorText = card.getString("flavorText");
        }

        preparedStatement.setString(5, flavorText);
        preparedStatement.setString(6, card.getString("artist"));
        preparedStatement.executeUpdate();
      }
    }
    catch (SQLException e) {
      System.out.println(e);
      System.out.println(shorthandSetName);
      System.out.println(card.getString("rarity"));
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
   * Given the name of a card, checks if it has been added to the CDDB under the 'Card' table.
   * @param toCheck name of card to check
   * @return if card is in CDDB
   * @throws IllegalArgumentException if given string is null
   * @throws IllegalStateException failure to query CDDB
   */
  private boolean cardAdded(String toCheck) {
    if (toCheck == null) {
      throw new IllegalArgumentException("Given string can't be null!");
    }
    String cardAddedQuery = "SELECT name FROM Card WHERE name=?";

    try {
      PreparedStatement preparedStatement = connection.prepareStatement(cardAddedQuery);
      preparedStatement.setString(1, toCheck);
      ResultSet result = preparedStatement.executeQuery();

      return result.next();
    }
    catch (SQLException e) {
      e.printStackTrace();
      throw new IllegalStateException("Failed to query database for card " + toCheck + "!");
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
