package relay;

import database.access.DatabaseChannel;
import java.sql.SQLException;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import value_objects.deck.Deck;
import view.DatabaseView;

/**
 * Default implementation of {@link ChannelViewRelay}, implements data querying between a given
 * channel to the Card and Deck Database (CDDB) in the form of a {@link DatabaseChannel} and a
 * display given to a user in the form of a {@link DatabaseView} - as well as {@link EventHandler}s
 * to give to the given DatabaseView.
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
  }

  /**
   * {@link EventHandler} for retrieving the ID of a {@link Deck} from the {@link DatabaseView}
   * to retrieve info on from the {@link DatabaseChannel}, then sending that {@link Deck} back to
   * DatabaseView.
   */
  private class RetrieveDeckInfo implements EventHandler<ActionEvent> {

    @Override
    public void handle(ActionEvent actionEvent) {
      try {
        Deck toAdd = databaseChannel.getDeck(databaseView.deckToRetrieveInfo());
        databaseView.acceptDeck(toAdd);
      }
      catch (SQLException e) {
        Alert error = new Alert(AlertType.ERROR);
        error.setHeaderText("Deck Retrieval Failure");
        error.setContentText(e.getMessage());
        error.show();
      }
    }
  }
}
