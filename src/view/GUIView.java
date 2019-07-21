package view;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collection;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.CheckMenuItem;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.MenuButton;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import relay.DatabaseViewConnection;
import value_objects.card.Card;
import value_objects.card.query.CardQuery;
import value_objects.card.query.Comparison;
import value_objects.card.query.Stat;
import value_objects.deck.Deck;
import value_objects.deck.instance.DeckInstance;
import value_objects.utility.Pair;
import value_objects.utility.Triple;

/**
 * {@link DatabaseView} implementation that provides a graphical user interface for a user to
 * interact with via JavaFX.
 */
public class GUIView extends BaseView implements DatabaseView {

  private static final int minSearchOptionNumber = -2;

  private static final int maxSearchOptionNumber = 21;

  private Pair<String, String> newDeckNameAndDesp;

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
    addNewDeckButton.setOnAction(actionEvent -> {
      getSetAndAddNewDeckNameAndDesp();
    });

    availableDecksDisplay = new VBox();

    cardSelectionDisplay = new VBox();

    cardSelectionResultDisplay = new VBox();

    deckAndCardSelectionPane = new VBox(deckAndCardSelectionPaneButtons, availableDecksDisplay);

    // Set button such that it makes available decks display visible to the user
    viewAvailableDecksButton.setOnAction(actionEvent -> {
      List<Node> children = deckAndCardSelectionPane.getChildren();
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

  /**
   * Opens a new window prompt for the user to enter the name and description for a new deck by
   * resetting the value of {@link GUIView#newDeckNameAndDesp}, then calling the
   * Runnable associated with the {@link DatabaseViewConnection#NewDeck}.
   */
  private void getSetAndAddNewDeckNameAndDesp() {
    Stage stage = new Stage();
    GridPane gridPane = new GridPane();

    Label nameLabel = new Label("Name:");
    gridPane.add(nameLabel, 0, 0);

    TextField nameField = new TextField();
    gridPane.add(nameField, 1, 0);

    Label despLabel = new Label("Description:");
    gridPane.add(despLabel, 0, 1);

    TextField despField = new TextField();
    gridPane.add(despField, 1, 1);

    Button submitButton = new Button("Submit");
    gridPane.add(submitButton, 0, 2);
    submitButton.setOnAction(actionEvent -> {
      if (!nameField.getText().isEmpty()) {
        newDeckNameAndDesp = new Pair<>(nameField.getText(), despField.getText());
        stage.close();
        runAssociatedRelayRunnable(DatabaseViewConnection.NewDeck);
      }
    });

    Button cancelButton = new Button("Cancel");
    gridPane.add(cancelButton, 1, 2);
    cancelButton.setOnAction(actionEvent -> {
      stage.close();
    });
    stage.setScene(new Scene(gridPane));
    stage.show();
  }

  @Override
  public void acceptCardQuery(CardQuery cardQuery) throws IllegalArgumentException {

    try {
      haveRelayRunnablesBeenAssigned();
      setCardQuery(cardQuery);

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
      GenericComparisonVBox<Stat, Integer> statOptionVBox = new GenericComparisonVBox<>(
          Arrays.asList(Stat.values()),
          IntStream.range(minSearchOptionNumber, maxSearchOptionNumber).boxed().collect(Collectors.toList()));

      //by stat vs stat
      GenericComparisonVBox<Stat, Stat> statVsStatOptionVBox = new GenericComparisonVBox<>(
          Arrays.asList(Stat.values()),
          Arrays.asList(Stat.values()));

      // by mana type
      GenericComparisonVBox<String, Integer> manaTypeOptionVBox = new GenericComparisonVBox<>(
          cardQuery.getAvailableManaTypes(),
          IntStream.range(minSearchOptionNumber, maxSearchOptionNumber).boxed().collect(Collectors.toList()));

      // submit button
      Button submitQuery = new Button("Submit");
      submitQuery.setOnAction(actionEvent -> {
        // add entered info into card query

        // enter stat info
        for (Triple<Stat, Comparison, Integer> statOption : statOptionVBox.getParams()) {
          cardQuery.byStat(statOption.getA(), statOption.getB(), statOption.getC());
        }
        // TODO add info to card query

        // submit for card query
        runAssociatedRelayRunnable(DatabaseViewConnection.QueryCards);
      });

      // display options
      ObservableList<Node> displayChildren = cardSelectionResultDisplay.getChildren();
      displayChildren.clear();
      displayChildren.addAll(nameOptionVBox, textOptionVBox, colorOptionVBox,
          colorIdentityOptionVBox, supertypeOptionVBox, typeOptionVBox,
          subtypeOptionVBox, blockOptionVBox, setOptionVBox,
          artistOptionVBox, rarityOptionVBox, flavorTextOptionVBox,
          statOptionVBox, statVsStatOptionVBox, manaTypeOptionVBox,
          submitQuery);
    }
    catch (SQLException e) {
      Alert error = new Alert(AlertType.ERROR);
      error.setHeaderText("Card Query Error");
      error.setContentText(e.getMessage());
      error.show();
    }
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

    private final Set<String> mustIncludeSelectedItems;

    private final MenuButton oneOfMenuButton;

    private final Set<String> oneOfSelectedItems;

    private final MenuButton notIncludeMenuButton;

    private final Set<String> notIncludeSelectedItems;

    private StaticSearchOptionVBox(Collection<String> searchOptions) {
      super();
      if (searchOptions == null || searchOptions.isEmpty()) {
        throw new IllegalArgumentException("Given set of search options can't be null or empty!");
      }

      mustIncludeSelectedItems = new HashSet<>();
      mustIncludeMenuButton = new MenuButton();
      mustIncludeMenuButton.getItems().addAll(searchOptionsToCheckMenuItemList(searchOptions, mustIncludeSelectedItems));

      oneOfSelectedItems = new HashSet<>();
      oneOfMenuButton = new MenuButton();
      oneOfMenuButton.getItems().addAll(searchOptionsToCheckMenuItemList(searchOptions, oneOfSelectedItems));

      notIncludeSelectedItems = new HashSet<>();
      notIncludeMenuButton = new MenuButton();
      notIncludeMenuButton.getItems().addAll(searchOptionsToCheckMenuItemList(searchOptions, notIncludeSelectedItems));

      HBox mustIncludeHBox = new HBox(mustIncludeLabel, mustIncludeMenuButton);
      HBox oneOfHBox = new HBox(oneOfLabel, oneOfMenuButton);
      HBox notIncludeHBox = new HBox(notIncludeLabel, notIncludeMenuButton);

      getChildren().addAll(mustIncludeHBox, oneOfHBox, notIncludeHBox);
    }

    private List<CheckMenuItem> searchOptionsToCheckMenuItemList(Collection<String> searchOptions,
        Set<String> selectedCheckMenuItemValues) {
      List<CheckMenuItem> checkMenuItems  = searchOptions.stream().map(CheckMenuItem::new).collect(Collectors.toList());
      for (CheckMenuItem checkMenuItem : checkMenuItems) {
        checkMenuItem.setOnAction(actionEvent -> {
          if (checkMenuItem.isSelected()) {
            selectedCheckMenuItemValues.remove(checkMenuItem.getText());
          }
          else {
            selectedCheckMenuItemValues.add(checkMenuItem.getText());
          }
        });
      }
      return checkMenuItems;
    }

    @Override
    Set<String> getMustIncludeParams() {
      return mustIncludeSelectedItems;
    }

    @Override
    Set<String> getOneOfParams() {
      return oneOfSelectedItems;
    }

    @Override
    Set<String> getNotParams() {
      return notIncludeSelectedItems;
    }
  }

  private class GenericComparisonVBox<A, B> extends VBox {

    private static final String addButtonText = "Add";

    private static final String removeButtonText = "Remove";

    private final VBox holdingBox;

    private final Collection<A> aParams;

    private final Collection<B> bParams;

    private final Map<HBox, Triple<ChoiceBox<A>, ChoiceBox<Comparison>, ChoiceBox<B>>> optionBoxToChoiceBox;

    GenericComparisonVBox(Collection<A> aParams, Collection<B> bParams) {
      if (aParams == null || aParams.isEmpty() || bParams == null || bParams.isEmpty()) {
        throw new IllegalArgumentException("Given collections of params can't be null!");
      }
      optionBoxToChoiceBox = new HashMap<>();
      this.aParams = aParams;
      this.bParams = bParams;
      holdingBox = new VBox();
      addNewOption();
    }

    private <T> ChoiceBox<T> setUpChoiceBox(Collection<T> searchOptions) {
      if (searchOptions == null || searchOptions.isEmpty()) {
        throw new IllegalArgumentException("Given search options can't be null or empty!");
      }

      ChoiceBox<T> choiceBox = new ChoiceBox<T>();
      choiceBox.getItems().addAll(searchOptions);
      return choiceBox;
    }

    private void addNewOption() {
      ChoiceBox<A> aSelectionMenu = setUpChoiceBox(aParams);
      ChoiceBox<Comparison> comparisonSelectionMenu = setUpChoiceBox(Arrays.asList(Comparison.values()));
      ChoiceBox<B> bSelectionMenu = setUpChoiceBox(bParams);

      Button addRemoveOptionButton = new Button(GenericComparisonVBox.addButtonText);
      HBox optionBox = new HBox(aSelectionMenu, comparisonSelectionMenu,
          bSelectionMenu, addRemoveOptionButton);

      addRemoveOptionButton.setOnAction(actionEvent -> {
        // All associated choice boxes must have been selected
        if (addRemoveOptionButton.getText().equals(GenericComparisonVBox.addButtonText) &&
        hasChoiceBoxBeenSelected(aSelectionMenu) &&
        hasChoiceBoxBeenSelected(comparisonSelectionMenu) &&
        hasChoiceBoxBeenSelected(bSelectionMenu)) {
          // If so change to delete mode and add a new option
          addRemoveOptionButton.setText(GenericComparisonVBox.removeButtonText);
          addNewOption();
        }
        else {
          // In delete mode, only remove if there is at least one other option box
          ObservableList<Node> children = holdingBox.getChildren();
          if (children.size() > 1) {
            children.remove(optionBox);
            optionBoxToChoiceBox.remove(optionBox);
          }

          // TODO need to change remaining option box to add mode?
        }
      });

      optionBoxToChoiceBox.put(optionBox, new Triple<>(aSelectionMenu, comparisonSelectionMenu, bSelectionMenu));
      holdingBox.getChildren().addAll(optionBox);
    }

    private <T> boolean hasChoiceBoxBeenSelected(ChoiceBox<T> choiceBox) {
      if (choiceBox == null) {
        throw new IllegalArgumentException("Given choice box can't be null!");
      }
      return choiceBox.getValue() != null;
    }

    Set<Triple<A, Comparison, B>> getParams() {
      // TODO remove toggle group, switch mapping to radiobutton directly
      Set<Triple<A, Comparison, B>> selectedParams = new HashSet<>();
      for (HBox optionBox : optionBoxToChoiceBox.keySet()) {

        Triple<ChoiceBox<A>, ChoiceBox<Comparison>, ChoiceBox<B>> associatedChoiceBoxes =
            optionBoxToChoiceBox.get(optionBox);
        ChoiceBox<A> aChoiceBox = associatedChoiceBoxes.getA();
        ChoiceBox<Comparison> comparisonChoiceBox = associatedChoiceBoxes.getB();
        ChoiceBox<B> bChoiceBox = associatedChoiceBoxes.getC();

        if (hasChoiceBoxBeenSelected(aChoiceBox) &&
            hasChoiceBoxBeenSelected(comparisonChoiceBox) &&
            hasChoiceBoxBeenSelected(bChoiceBox)) {
          selectedParams.add(new Triple<>(aChoiceBox.getValue(),
              comparisonChoiceBox.getValue(),
              bChoiceBox.getValue()));
        }
      }
      return selectedParams;
    }
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
  public Pair<String, String> newDeckToAdd() throws IllegalStateException {
    if (newDeckNameAndDesp == null) {
      throw new IllegalStateException("Name and desp for a new deck have not been set!");
    }
    return newDeckNameAndDesp;
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
