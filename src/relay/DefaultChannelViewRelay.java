package relay;

import database.access.DatabaseChannel;
import value_objects.card.query.CardQuery;
import java.sql.SQLException;
import javafx.event.ActionEvent;
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

  private final DatabaseChannel databaseChannel;

  private final DatabaseView databaseView;

  /**
   *
   * @param databaseChannel
   * @param databaseView
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
    // TODO
    // Give mapping of EventHandler by respective enum
    //TODO
  }

  /**
   * {@link EventHandler} for retrieving the ID of a {@link Deck} from the {@link DatabaseView}
   * to retrieve info on from the {@link DatabaseChannel}, then sending that {@link Deck} back to
   * DatabaseView.
   */
  private class RetrieveDeckInfo implements EventHandler<ActionEvent> {
    @Override
    public void handle(ActionEvent actionEvent) {
      updateSelectedDeckInfo();
    }
  }

  /**
   * {@link EventHandler} for retrieving {@link Card}s from the {@link DatabaseChannel}, as given
   * by the {@link CardQuery} currently residing in the {@link DatabaseView}, giving them to the
   * DatabaseView again, then reseting the CardQuery in the DatabaseView by assigning it a new
   * one from the DatabaseChannel.
   */
  private class QueryCards implements EventHandler<ActionEvent> {
    @Override
    public void handle(ActionEvent actionEvent) {
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
   * {@link EventHandler} for retrieving the ID of a Deck from the {@link DatabaseView} to delete
   * from the CDDB through the {@link DatabaseChannel}.
   */
  private class DeleteDeck implements EventHandler<ActionEvent> {
    @Override
    public void handle(ActionEvent actionEvent) {
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
   * {@link EventHandler} for retrieving a new {@link Deck} from the {@link DatabaseView}
   * and adding it in the CDDB through the {@link DatabaseChannel}.
   */
  private class AddDeck implements EventHandler<ActionEvent> {
    @Override
    public void handle(ActionEvent actionEvent) {
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
   * {@link EventHandler} for retrieving a new {@link DeckInstance} from the {@link DatabaseView}
   * and adding it in the CDDB through the {@link DatabaseChannel}.
   */
  private class AddDeckInstance implements EventHandler<ActionEvent> {
    @Override
    public void handle(ActionEvent actionEvent) {
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
   * {@link EventHandler} for retrieving a new name of a Deck from the {@link DatabaseView}
   * and its associated ID, and updating it in the CDDB through the {@link DatabaseChannel}.
   */
  private class ChangeDeckName implements EventHandler<ActionEvent> {
    @Override
    public void handle(ActionEvent actionEvent) {
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
   * {@link EventHandler} for retrieving a new description of a Deck from the {@link DatabaseView}
   * and its associated ID, and updating it in the CDDB through the {@link DatabaseChannel}.
   */
  private class ChangeDeckDesp implements EventHandler<ActionEvent> {
    @Override
    public void handle(ActionEvent actionEvent) {
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
