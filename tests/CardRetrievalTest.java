import database.access.DefaultDatabaseChannel;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.SQLException;
import java.util.SortedSet;
import java.util.TreeSet;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import value_objects.card.Card;
import value_objects.query.CardQuery;

public class CardRetrievalTest {

  CardQuery cardQuery;
  SortedSet<Card> cardQueryResult;

  @BeforeAll
  public void init() throws SQLException {
    Path pathToDatabase = Paths.get("resources\\test.db").toAbsolutePath();
    DefaultDatabaseChannel defaultChannel = new DefaultDatabaseChannel(pathToDatabase);
    cardQuery = defaultChannel.getQuery();
    cardQueryResult = new TreeSet<>();
  }

  @AfterEach
  public void afterEach() {
    cardQuery.clear();
    cardQueryResult.clear();
  }

  // Single creature card from single expansion with no relationship

  // Single planeswalker card from single expansion with no relationship

  // Single non-creature, non-planeswalker card from single expansion with no relationship

  // Multiple cards from single expansion with no relationship

  // Single card from single expansion with two card relationship

  // Single card from single expansion with three card relationship

}
