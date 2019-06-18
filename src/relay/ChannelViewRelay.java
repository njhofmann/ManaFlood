package relay;

import database.access.DatabaseChannel;
import view.DatabaseView;

/**
 * Controller handling data requests and deliveries between a {@link DatabaseChannel} and a
 * {@link DatabaseView}.
 */
public interface ChannelViewRelay {

  /**
   * Begins
   */
  void start();

}
