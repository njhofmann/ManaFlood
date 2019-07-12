package view;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.stream.Collectors;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.CheckMenuItem;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.MenuButton;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import relay.DatabaseViewConnection;
import value_objects.card.Card;
import value_objects.card.query.CardQuery;
import value_objects.card.query.SearchOption;
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

  private VBox cardSelectionResultDisplay;

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

    addNewDeckButton = new Button("New Deck");

    availableDecksDisplay = new VBox();

    cardSelectionDisplay = new VBox();

    cardSelectionResultDisplay = new VBox();

    deckAndCardSelectionPane = new VBox(deckAndCardSelectionPaneButtons, availableDecksDisplay);

    // Set button such that it makes available decks display visible to the user
    viewAvailableDecksButton.setOnAction(actionEvent -> {
      List<Node> children = deckAndCardSelectionPane.getChildren();
      System.out.println(children.size());
      if (children.size() > 1) {
        children.remove(children.size() - 1);
        children.add(availableDecksDisplay);
      }
    });

    // Set button such that it makes card selection display visible to the user
    cardSelectionButton.setOnAction(actionEvent -> {
      List<Node> children = deckAndCardSelectionPane.getChildren();
      if (children.size() > 1) {
        children.remove(children.size() - 1);
        children.add(cardSelectionDisplay);
      }
    });

    // Set up the deck display area
    deckNameDisplay = new TextField();

    deckDespButton = new Button("Description");
    deckDespButton.setMaxWidth(Double.MAX_VALUE);
    deckHistoryButton = new Button("History");
    deckHistoryButton.setMaxWidth(Double.MAX_VALUE);
    VBox deckInfoButtons = new VBox(deckDespButton, deckHistoryButton);

    mockDeckHandButton = new Button("Mock Hand");
    mockDeckHandButton.setMaxWidth(Double.MAX_VALUE);
    deckStatsButton = new Button("Stats");
    deckStatsButton.setMaxWidth(Double.MAX_VALUE);
    VBox deckStatButtons = new VBox(mockDeckHandButton, deckStatsButton);

    saveDeckInstanceButton = new Button("Save");
    saveDeckInstanceButton.setMaxWidth(Double.MAX_VALUE);
    exportDeckButton = new Button("Export");
    exportDeckButton.setMaxWidth(Double.MAX_VALUE);
    VBox deckSaveButtons = new VBox(saveDeckInstanceButton, exportDeckButton);

    deckDisplayHeaderArea = new HBox(deckNameDisplay, deckInfoButtons, deckStatButtons, deckSaveButtons);

    deckInfoDisplayArea = new HBox(new Button("Foo"));

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
    List<Node> deckDisplayAreaChildren = availableDecksDisplay.getChildren();
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

      Button deckDeleteButton = new Button("Delete");
      deckDeleteButton.setOnAction(actionEvent -> {
        setDeckToDelete(deckId);
        runAssociatedRelayRunnable(DatabaseViewConnection.DeleteDeck);
        resetDeckToDelete();
      });

      deckDisplayAreaChildren.add(new HBox(deckButton, deckDeleteButton));
    }
  }

  @Override
  public void acceptCardQuery(CardQuery cardQuery) throws IllegalArgumentException,
      SQLException {
    haveRelayRunnablesBeenAssigned();
    setCardQuery(cardQuery);

    //TODO setup card query as needed

    // by name
    SearchOptionVBox nameOptionVBox = new DynamicSearchOptionVBox();

    // by text
    SearchOptionVBox textOptionVBox = new DynamicSearchOptionVBox();

    // by color
    SearchOptionVBox colorOptionVBox = new StaticSearchOptionVBox(cardQuery.getAvailableColors());

    // by color identity
    SearchOptionVBox colorIdentityOptionVBox = new StaticSearchOptionVBox(cardQuery.getAvailableColors());

    // by supertype
    SearchOptionVBox supertypeOptionVBox = new StaticSearchOptionVBox(cardQuery.getAvailableSupertypes());

    // by type
    SearchOptionVBox typeOptionVBox = new StaticSearchOptionVBox(cardQuery.getAvailableTypes());

    // by subtype
    SearchOptionVBox subtypeOptionVBox = new StaticSearchOptionVBox(cardQuery.getAvailableSubtypes());

    // by block
    SearchOptionVBox blockOptionVBox = new StaticSearchOptionVBox(cardQuery.getAvailableBlocks());

    // by set
    SearchOptionVBox setOptionVBox = new StaticSearchOptionVBox(cardQuery.getAvailableSets());

    // by artist
    SearchOptionVBox artistOptionVBox = new StaticSearchOptionVBox(cardQuery.getAvailableArtists());

    // by rarity
    SearchOptionVBox rarityOptionVBox = new StaticSearchOptionVBox(cardQuery.getAvailableRarityTypes());

    // by flavor text
    SearchOptionVBox flavorTextOptionVBox = new DynamicSearchOptionVBox();

    // by stat

    //by stat vs stat

    // by mana type

    // submit button


  }

  private abstract class SearchOptionVBox extends VBox {

    protected final Label mustIncludeLabel;

    protected final Label oneOfLabel;

    protected final Label notIncludeLabel;

    private SearchOptionVBox() {
      super();
      mustIncludeLabel = new Label("Must Include");
      oneOfLabel = new Label("One Of");
      notIncludeLabel = new Label("Not Include");
    }

    abstract Set<String> getMustIncludeParams();

    abstract Set<String> getOneOfParams();

    abstract Set<String> getNotParams();
  }

  private class DynamicSearchOptionVBox extends SearchOptionVBox {

    private final TextField mustIncludeTextField;

    private final TextField oneOfTextField;

    private final TextField notIncludeTextField;

    private DynamicSearchOptionVBox() {
      super();
      mustIncludeTextField = new TextField();
      oneOfTextField = new TextField();
      notIncludeTextField = new TextField();
      HBox mustIncludeHBox = new HBox(mustIncludeLabel, mustIncludeTextField);
      HBox oneOfHBox = new HBox(oneOfLabel, oneOfTextField);
      HBox notIncludeHBox = new HBox(notIncludeLabel, notIncludeTextField);
      getChildren().addAll(mustIncludeHBox, oneOfHBox, notIncludeHBox);
    }

    private Set<String> textFieldToStringSet(TextField textField) {
      if (textField == null) {
        throw new IllegalArgumentException("Given array of strings can't be null!");
      }
      return Arrays.stream(textField.getText().split("\\s+"))
          .collect(Collectors.toCollection(TreeSet::new));
    }

    @Override
    Set<String> getMustIncludeParams() {
      return textFieldToStringSet(mustIncludeTextField);
    }

    @Override
    Set<String> getOneOfParams() {
      return textFieldToStringSet(oneOfTextField);
    }

    @Override
    Set<String> getNotParams() {
      return textFieldToStringSet(notIncludeTextField);
    }
  }

  private class StaticSearchOptionVBox extends SearchOptionVBox {

    private final MenuButton mustIncludeMenuButton;

    private final List<CheckMenuItem> mustIncludeCheckMenuItems;

    private final MenuButton oneOfMenuButton;

    private final List<CheckMenuItem> oneOfCheckMenuItems;

    private final MenuButton notIncludeMenuButton;

    private final List<CheckMenuItem> notIncludeCheckMenuItems;

    private StaticSearchOptionVBox(Collection<String> searchOptions) {
      super();
      if (searchOptions == null || searchOptions.isEmpty()) {
        throw new IllegalArgumentException("Given set of search options can't be null or empty!");
      }

      mustIncludeCheckMenuItems = searchOptionsToCheckMenuItemList(searchOptions);
      mustIncludeMenuButton = new MenuButton();
      mustIncludeMenuButton.getItems().addAll(mustIncludeCheckMenuItems);

      oneOfCheckMenuItems = searchOptionsToCheckMenuItemList(searchOptions);
      oneOfMenuButton = new MenuButton();
      oneOfMenuButton.getItems().addAll(oneOfCheckMenuItems);

      notIncludeCheckMenuItems = searchOptionsToCheckMenuItemList(searchOptions);
      notIncludeMenuButton = new MenuButton();
      notIncludeMenuButton.getItems().addAll(notIncludeCheckMenuItems);

      HBox mustIncludeHBox = new HBox(mustIncludeLabel, mustIncludeMenuButton);
      HBox oneOfHBox = new HBox(oneOfLabel, oneOfMenuButton);
      HBox notIncludeHBox = new HBox(notIncludeLabel, notIncludeMenuButton);

      getChildren().addAll(mustIncludeHBox, oneOfHBox, notIncludeHBox);
    }

    private List<CheckMenuItem> searchOptionsToCheckMenuItemList(Collection<String> searchOptions) {
      return searchOptions.stream().map(CheckMenuItem::new).collect(Collectors.toList());
    }

    private Set<String> getSelectedItems(List<CheckMenuItem> checkMenuItems) {
      return checkMenuItems.stream().filter(
          CheckMenuItem::isSelected).map(MenuItem::getText)
          .collect(Collectors.toCollection(TreeSet::new));
    }

    @Override
    Set<String> getMustIncludeParams() {
      return getSelectedItems(mustIncludeCheckMenuItems);
    }

    @Override
    Set<String> getOneOfParams() {
      return getSelectedItems(oneOfCheckMenuItems);
    }

    @Override
    Set<String> getNotParams() {
      return getSelectedItems(notIncludeCheckMenuItems);
    }
  }

  private class EnumeratedVBoxTriple<A, B, C> extends VBox {

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
