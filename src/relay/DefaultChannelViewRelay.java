package relay;

import database.access.DatabaseChannel;
import java.util.EnumMap;
import java.util.SortedSet;
import value_objects.card.query.CardQuery;
import java.sql.SQLException;
import javafx.event.EventHandler;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import value_objects.deck.Deck;
import value_objects.deck.instance.DeckInstance;
import value_objects.utility.Pair;
import view.DatabaseView;
import value_objects.card.Card;

/**
 * Default implementation of {@link ChannelViewRelay}, implements data querying between a given
 * channel to the Card and Deck Database (CDDB) in the form of a {@link DatabaseChannel} and a
 * display given to a user in the form of a {@link DatabaseView}. In addition, assigns
 * {@link EventHandler}s to give to the given DatabaseView.
 */
public class DefaultChannelViewRelay {

  /**
   * The {@link DatabaseChannel} this controller uses to interact with the CDDB.
   */
  private final DatabaseChannel databaseChannel;

  /**
   * The {@link DatabaseView} this controller uses for display info to a user.
   */
  private final DatabaseView databaseView;

  /**
   * Creates a {@link ChannelViewRelay} by setting up connections for and relaying information
   * between a {@link DatabaseChannel} (channel to the CDDB) and a {@link DatabaseView} (displays
   * info to a user).
   * @param databaseChannel DatabaseChannel to the CDDB to use
   * @param databaseView DatabaseView for displaying info to the user
   * @throws IllegalArgumentException if any given param is null
   */
  public DefaultChannelViewRelay(DatabaseChannel databaseChannel, DatabaseView databaseView) {
    if (databaseChannel == null || databaseView == null) {
      throw new IllegalArgumentException("Given database parameters can't be null!");
    }
    this.databaseChannel = databaseChannel;
    this.databaseView = databaseView;
    setUpConnections();
  }

  /**
   * Sets up a starting relationship with this {@link ChannelViewRelay}'s {@link DatabaseView}.
   * Specifically gives a starting {@link CardQuery}, the EventHandler's in this controller,
   * info from the CDDB such as card types and color types, and the {@link Deck}s (in the form of
   * IDs and names) currently stored in the CDDB.
   */
  private void setUpConnections() {
    // Giving available deck info
    retrieveAvailableDecksInfo();

    // Give starting card query
    giveNewCardQuery();

    // Give "enum" info related to cards from the CDDB
    setUpCardInfo();

    // Give mapping of Runnables by respective enum
    setUpRelayRunnables();
  }

  /**
   * Gives {@link Card} info from this controller's {@link DatabaseChannel} to its
   * {@link DatabaseView} via {@link DatabaseView#acceptInfo(String, SortedSet)}.
   */
  private void setUpCardInfo() {
    try {
      databaseView.acceptInfo("artist", databaseChannel.getArtists());
      databaseView.acceptInfo("block", databaseChannel.getBlocks());
      databaseView.acceptInfo("color", databaseChannel.getColors());
      databaseView.acceptInfo("mana", databaseChannel.getManaTypes());
      databaseView.acceptInfo("rarity", databaseChannel.getRarityTypes());
      databaseView.acceptInfo("set", databaseChannel.getSets());
      databaseView.acceptInfo("supertype", databaseChannel.getSupertypes());
      databaseView.acceptInfo("type", databaseChannel.getTypes());
      databaseView.acceptInfo("subtype", databaseChannel.getSubtypes());
    }
    catch (SQLException e) {
      Alert error = new Alert(AlertType.ERROR);
      error.setHeaderText("Type Info Failure");
      error.setContentText(e.getMessage());
      error.show();
    }
  }

  /**
   * Sets up and gives the {@link Runnable}s linked to each {@link DatabaseViewConnection} for
   * this controller to this controller's {@link DatabaseView} via
   * {@link DatabaseView#acceptRelayRunnables(EnumMap)}
   */
  private void setUpRelayRunnables() {
    EnumMap<DatabaseViewConnection, Runnable> relayRunnables = new EnumMap<>(DatabaseViewConnection.class);
    relayRunnables.put(DatabaseViewConnection.RetrieveDeckInfo, new RetrieveDeckInfo());
    relayRunnables.put(DatabaseViewConnection.DeleteDeck, new DeleteDeck());
    relayRunnables.put(DatabaseViewConnection.QueryCards, new QueryCards());
    relayRunnables.put(DatabaseViewConnection.NewDeck, new AddDeck());
    relayRunnables.put(DatabaseViewConnection.NewDeckInstance, new AddDeckInstance());
    relayRunnables.put(DatabaseViewConnection.EditDeckName, new ChangeDeckName());
    relayRunnables.put(DatabaseViewConnection.EditDeckDesp, new ChangeDeckDescription());
    databaseView.acceptRelayRunnables(relayRunnables);
  }

  /**
   * Assigns a new {@link CardQuery} to the {@link DatabaseView} from the {@link DatabaseChannel}.
   */
  private void giveNewCardQuery() {
    databaseView.acceptCardQuery(databaseChannel.getQuery());
  }

  /**
   * Sends a new list of decks available for selection from the CDDB, from the
   * {@link DatabaseChannel} to the {@link DatabaseView}.
   */
  private void retrieveAvailableDecksInfo() {
    try {
      databaseView.acceptAvailableDecksInfo(databaseChannel.getDecks());
    }
    catch (SQLException e) {
      Alert error = new Alert(AlertType.ERROR);
      error.setHeaderText("Decks Info Retrieval Failure");
      error.setContentText(e.getMessage());
      error.show();
    }
  }

  /**
   * Sends a new info associated with a Deck ID selected by the {@link DatabaseView} from the
   * {@link DatabaseChannel} to the {@link DatabaseView}.
   */
  private void updateSelectedDeckInfo() {
    try {
      databaseView.acceptDeckInfo(databaseChannel.getDeck(databaseView.deckToRetrieveInfoOn()));
    }
    catch (SQLException e) {
      Alert error = new Alert(AlertType.ERROR);
      error.setHeaderText("Deck Retrieval Failure");
      error.setContentText(e.getMessage());
      error.show();
    }
  }

  /**
   * {@link Runnable} for retrieving the ID of a {@link Deck} from the {@link DatabaseView}
   * to retrieve info on from the {@link DatabaseChannel}, then sending that {@link Deck} back to
   * DatabaseView.
   */
  private class RetrieveDeckInfo implements Runnable {
    @Override
    public void run() {
      updateSelectedDeckInfo();
    }
  }

  /**
   * {@link Runnable} for retrieving {@link Card}s from the {@link DatabaseChannel}, as given
   * by the {@link CardQuery} currently residing in the {@link DatabaseView}, giving them to the
   * DatabaseView again, then reseting the CardQuery in the DatabaseView by assigning it a new
   * one from the DatabaseChannel.
   */
  private class QueryCards implements Runnable {
    @Override
    public void run() {
      try {
        // Get card info
        databaseView.acceptCards(databaseChannel.queryCards(databaseView.getCardQuery()));

        // Give a new card query object to the view
        giveNewCardQuery();
      }
      catch (SQLException e) {
        Alert error = new Alert(AlertType.ERROR);
        error.setHeaderText("Card Query Retrieval Failure");
        error.setContentText(e.getMessage());
        error.show();
      }
    }
  }


  /**
   * {@link Runnable} for retrieving the ID of a Deck from the {@link DatabaseView} to delete
   * from the CDDB through the {@link DatabaseChannel}.
   */
  private class DeleteDeck implements Runnable {
    @Override
    public void run() {
      try {
        // Delete deck
        databaseChannel.deleteDeck(databaseView.deckToDelete());
        // Update list of available decks
        updateSelectedDeckInfo();
      }
      catch (SQLException e) {
        Alert error = new Alert(AlertType.ERROR);
        error.setHeaderText("Deck Deletion Failure");
        error.setContentText(e.getMessage());
        error.show();
      }
    }
  }

  /**
   * {@link Runnable} for retrieving a new {@link Deck} from the {@link DatabaseView}
   * and adding it in the CDDB through the {@link DatabaseChannel}.
   */
  private class AddDeck implements Runnable {
    @Override
    public void run() {
      try {
        // Add deck
        databaseChannel.addDeck(databaseView.deckToAdd());

        // Update list of available decks
        retrieveAvailableDecksInfo();

        // Update selected deck info
        updateSelectedDeckInfo();
      }
      catch (SQLException e) {
        Alert error = new Alert(AlertType.ERROR);
        error.setHeaderText("Deck Addition Failure");
        error.setContentText(e.getMessage());
        error.show();
      }
    }
  }

  /**
   * {@link Runnable} for retrieving a new {@link DeckInstance} from the {@link DatabaseView}
   * and adding it in the CDDB through the {@link DatabaseChannel}.
   */
  private class AddDeckInstance implements Runnable {
    @Override
    public void run() {
      try {
        // Add deck instance
        databaseChannel.addDeckInstance(databaseView.deckInstanceToAdd());

        // Update selected deck info
        updateSelectedDeckInfo();
      }
      catch (SQLException e) {
        Alert error = new Alert(AlertType.ERROR);
        error.setHeaderText("Deck Instance Addition Failure");
        error.setContentText(e.getMessage());
        error.show();
      }
    }
  }

  /**
   * {@link Runnable} for retrieving a new name of a Deck from the {@link DatabaseView}
   * and its associated ID, and updating it in the CDDB through the {@link DatabaseChannel}.
   */
  private class ChangeDeckName implements Runnable {
    @Override
    public void run() {
      try {
        // Update deck name
        Pair<Integer, String> nameInfo = databaseView.newDeckName();
        databaseChannel.updateDeckName(nameInfo.getA(), nameInfo.getB());

        // Update available decks info
        retrieveAvailableDecksInfo();

        // Update selected deck info
        updateSelectedDeckInfo();
      }
      catch (SQLException e) {
        Alert error = new Alert(AlertType.ERROR);
        error.setHeaderText("Deck Name Update Failure");
        error.setContentText(e.getMessage());
        error.show();
      }
    }
  }

  /**
   * {@link Runnable} for retrieving a new description of a Deck from the {@link DatabaseView}
   * and its associated ID, and updating it in the CDDB through the {@link DatabaseChannel}.
   */
  private class ChangeDeckDescription implements Runnable {
    @Override
    public void run() {
      try {
        // Update deck desp
        Pair<Integer, String> despInfo = databaseView.newDeckDesp();
        databaseChannel.updateDeckDesp(despInfo.getA(), despInfo.getB());

        // Update available decks info
        retrieveAvailableDecksInfo();

        // Update selected deck info
        updateSelectedDeckInfo();
      }
      catch (SQLException e) {
        Alert error = new Alert(AlertType.ERROR);
        error.setHeaderText("Deck Name Update Failure");
        error.setContentText(e.getMessage());
        error.show();
      }
    }
  }
}
