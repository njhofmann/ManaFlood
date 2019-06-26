package view;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import relay.DatabaseViewConnection;
import value_objects.card.Card;
import value_objects.deck.Deck;
import value_objects.deck.instance.DeckInstance;
import value_objects.utility.Pair;

/**
 * {@link DatabaseView} implementation that provides a graphical user interface for a user to
 * interact with via JavaFX.
 */
public class GUIView extends BaseView implements DatabaseView {

  private final HBox rootPane;

  private final VBox deckAndCardSelectionPane;

  private final Button viewAvailableDecksButton;

  private final Button cardSelectionButton;

  private final Button addNewDeckButton;

  private final VBox availableDecksDisplay;

  private final VBox cardSelectionDisplay;

  private final VBox deckDisplayArea;

  private final HBox deckDisplayHeaderArea;

  private final TextField deckNameDisplay;

  private final Button deckDespButton;

  private final Button deckHistoryButton;

  private final Button mockDeckHandButton;

  private final Button deckStatsButton;

  private final Button saveDeckInstanceButton;

  private final Button exportDeckButton;

  public GUIView() {

    // Set up deck and card selection
    viewAvailableDecksButton = new Button("Decks");

    cardSelectionButton = new Button("Card Search");

    HBox deckAndCardSelectionPaneButtons = new HBox(viewAvailableDecksButton, cardSelectionButton);

    addNewDeckButton = new Button();

    availableDecksDisplay = new VBox(addNewDeckButton);

    cardSelectionDisplay = new VBox();

    deckAndCardSelectionPane = new VBox(deckAndCardSelectionPaneButtons, availableDecksDisplay);

    // Set button such that it makes available decks display visible to the user
    viewAvailableDecksButton.setOnAction(actionEvent -> {
      List<Node> children = deckAndCardSelectionPane.getChildren();
      children.remove(children.size() - 1);
      children.add(availableDecksDisplay);
    });

    // Set button such that it makes card selection display visible to the user
    cardSelectionButton.setOnAction(actionEvent -> {
      List<Node> children = deckAndCardSelectionPane.getChildren();
      children.remove(children.size() - 1);
      children.add(cardSelectionDisplay);
    });

    // Set up the deck display are
    deckDisplayArea = new VBox();

    rootPane = new HBox(deckAndCardSelectionPane, deckDisplayArea);
  }

  @Override
  public void acceptRelayRunnables(EnumMap<DatabaseViewConnection, Runnable> relayRunnables) {
    checkRelayRunnables(relayRunnables);
    // TODO assign relay runnables
  }

  @Override
  public void acceptAvailableDecksInfo(Map<Integer, String> deckInfo)
      throws IllegalArgumentException, IllegalStateException {
    haveRelayRunnablesBeenAssigned();

    if (deckInfo == null) {
      throw new IllegalArgumentException("Given mapping can't be null!");
    }

    // Clear deck display area of everything but the new deck button
    List<Node> deckDisplayAreaChildren = deckDisplayArea.getChildren();
    deckDisplayAreaChildren.clear();
    deckDisplayAreaChildren.add(addNewDeckButton);

    // For each deck (id and name), assign it a button with its deck name that when clicked,
    // retrieves the deck info associated with the deck from the CDDB
    for (int deckId : deckInfo.keySet()) {
      Button deckButton = new Button(deckInfo.get(deckId));
      deckButton.setOnAction(actionEvent -> {
        selectedDeckId = deckId;
        relayRunnables.get(DatabaseViewConnection.RetrieveDeckInfo).run();
      });
      deckDisplayAreaChildren.add(deckButton);
    }
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

  /**
   * Sets up the card query display by filling in the card display area pane.
   */
  @Override
  protected void setUpCardQueryDisplay() {
    // TODO set up card query display area
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
