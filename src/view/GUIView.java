package view;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
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

  private HBox rootPane;

  private VBox deckAndCardSelectionPane;

  private Button viewAvailableDecksButton;

  private Button cardSelectionButton;

  private Button addNewDeckButton;

  private VBox availableDecksDisplay;

  private VBox cardSelectionDisplay;

  private VBox deckDisplayArea;

  private HBox deckDisplayHeaderArea;

  private TextField deckNameDisplay;

  private Button deckDespButton;

  private Button deckHistoryButton;

  private Button mockDeckHandButton;

  private Button deckStatsButton;

  private Button saveDeckInstanceButton;

  private Button exportDeckButton;

  private HBox deckInfoDisplayArea;

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

    // Set up the deck display area
    deckNameDisplay = new TextField();

    deckDespButton = new Button("Description");
    deckHistoryButton = new Button("History");
    VBox deckSpecificButtons = new VBox(deckDespButton, deckHistoryButton);

    mockDeckHandButton = new Button("Mock Hand");
    deckStatsButton = new Button("Stats");
    VBox statsButtons = new VBox(mockDeckHandButton, deckStatsButton);

    saveDeckInstanceButton = new Button("Save");
    exportDeckButton = new Button("Export");
    VBox dataSavingButtons = new VBox(saveDeckInstanceButton, exportDeckButton);

    deckDisplayHeaderArea = new HBox(deckNameDisplay, deckSpecificButtons,
        statsButtons, dataSavingButtons);

    deckInfoDisplayArea = new HBox();

    deckDisplayArea = new VBox(deckDisplayHeaderArea, deckInfoDisplayArea);

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
        setSelectedDeckId(deckId);
        runAssociatedRelayRunnable(DatabaseViewConnection.RetrieveDeckInfo);
      });
      deckDisplayAreaChildren.add(deckButton);
    }
  }

  @Override
  public void acceptCardQuery(CardQuery cardQuery) throws IllegalArgumentException {

  }

  @Override
  public void acceptCards(SortedSet<Card> cards) throws IllegalArgumentException {
    /**
     * List images of each card displayed in given set in cardSelectionDisplay
     * - Include option to add to deck (if opened)
     * - Sort by options
     * - New search option
     */
  }

  @Override
  public void acceptDeckInfo(Deck deck) throws IllegalArgumentException {

  }

  @Override
  public int deckToRetrieveInfoOn() throws IllegalStateException {
    return 0;
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
  public Parent asParent() {
    return rootPane;
  }
}
