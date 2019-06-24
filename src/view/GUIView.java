package view;

import java.util.EnumMap;
import java.util.Map;
import java.util.SortedSet;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import relay.DatabaseViewConnection;
import value_objects.card.Card;
import value_objects.card.query.CardQuery;
import value_objects.deck.Deck;
import value_objects.deck.instance.DeckInstance;
import value_objects.utility.Pair;

/**
 * {@link DatabaseView} implementation that provides a graphical user interface for a user to
 * interact with via JavaFX.
 */
public class GUIView extends BaseView implements DatabaseView {

  private final BorderPane rootPane;

  public GUIView() {
    setUp();
  }

  private void setUp() {
    rootPane = new BorderPane();
  }

  @Override
  public void acceptRelayRunnables(EnumMap<DatabaseViewConnection, Runnable> relayRunnables) {
    checkRelayRunnables(relayRunnables);
    // TODO assign relay runnables
  }

  @Override
  public void acceptAvailableDecksInfo(Map<Integer, String> deckInfo)
      throws IllegalArgumentException {

  }

  @Override
  public void acceptInfo(String infoType, SortedSet<String> info) throws IllegalArgumentException {

  }

  @Override
  public void acceptCards(SortedSet<Card> cards) throws IllegalArgumentException {

  }

  @Override
  public void acceptDeckInfo(Deck deck) throws IllegalArgumentException {

  }

  @Override
  public int deckToDelete() throws IllegalStateException {
    return 0;
  }

  @Override
  public Deck deckToAdd() throws IllegalStateException {
    return null;
  }

  @Override
  public DeckInstance deckInstanceToAdd() throws IllegalStateException {
    return null;
  }

  @Override
  public Pair<Integer, String> newDeckName() throws IllegalStateException {
    return null;
  }

  @Override
  public Pair<Integer, String> newDeckDesp() throws IllegalStateException {
    return null;
  }

  @Override
  public void start() {
    /**
     * After this {@link GUIView} has been set up, starts a JavaFX {@link Application} to attach
     * this GUIView to - then shows the GUI.
     */
    class RunManaFloodGUI extends Application {

      @Override
      public void start(Stage stage) {
        Scene scene = new Scene(rootPane);
        stage.setTitle("ManaFlood");
        // TODO image header
        stage.setResizable(false);
        stage.setScene(scene);
        stage.show();
      }
    }
    RunManaFloodGUI.launch();
  }
}
