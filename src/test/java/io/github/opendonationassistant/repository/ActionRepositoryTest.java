package io.github.opendonationassistant.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.github.opendonationassistant.commons.Amount;
import io.micronaut.context.annotation.Property;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import jakarta.inject.Inject;
import java.util.Map;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

@MicronautTest(environments = "test", transactional = false)
@Property(name = "datasources.default.db-type", value = "postgres")
public class ActionRepositoryTest {

  private static final String ACTION_ID = "action-repository-test-id";
  private static final String RECIPIENT_ID = "action-repository-test-recipient";

  @Inject
  ActionRepository repository;

  @Inject
  ActionDataRepository dataRepository;

  private ActionData actionData;

  @BeforeEach
  public void setUp() {
    actionData = new ActionData(
      ACTION_ID,
      RECIPIENT_ID,
      "category",
      "provider",
      "name",
      new Amount(0, 0, "RUB"),
      "game",
      true,
      Map.of("key", "value")
    );
    dataRepository.save(actionData);
  }

  @AfterEach
  public void tearDown() {
    dataRepository.deleteById(ACTION_ID);
  }

  @Test
  public void findByIdAndRecipientIdReturnsPersistedActionForOwner() {
    var found = repository.findByIdAndRecipientId(ACTION_ID, RECIPIENT_ID);

    assertTrue(found.isPresent());
    assertEquals(actionData, found.get().data());
  }

  @Test
  public void findByIdAndRecipientIdReturnsEmptyForForeignRecipient() {
    assertTrue(
      repository
        .findByIdAndRecipientId(ACTION_ID, "foreign-recipient")
        .isEmpty()
    );
  }
}
