package io.github.opendonationassistant.repository;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import io.github.opendonationassistant.commons.Amount;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import jakarta.inject.Inject;
import java.util.Map;
import org.instancio.junit.InstancioExtension;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

@MicronautTest(environments = "allinone")
@ExtendWith(InstancioExtension.class)
public class ActionRepositoryTest {

  @Inject
  ActionRepository repository;

  @Test
  public void testAddingAction() {
    var action = repository.create(
      "recipient",
      "category",
      "provider",
      "name",
      new Amount(10, 0, "EUR"),
      "game",
      Map.of("key", "value")
    );
    action.save();
    var saved = repository.findByIdAndRecipientId(
      action.data().id(),
      "recipient"
    );
    assertTrue(saved.isPresent());
    assertEquals("game", action.data().game());
    assertEquals("category", action.data().category());
    assertEquals("provider", action.data().provider());
    assertEquals("recipient", action.data().recipientId());
    assertEquals(Map.of("key", "value"), action.data().payload());
    assertEquals(new Amount(10, 0, "EUR"), action.data().amount());
  }

  @Test
  public void testListingByRecipientId() {
    var id1 = repository
      .create(
        "recipient",
        "category",
        "provider",
        "name",
        new Amount(10, 0, "EUR"),
        "game",
        Map.of("key", "value")
      )
      .save()
      .data()
      .id();
    var id2 = repository
      .create(
        "recipient",
        "anotherCategory",
        "anotherProvider",
        "anotherName",
        new Amount(100, 0, "EUR"),
        "anotherGame",
        Map.of("anotherKey", "anotherValue")
      )
      .save()
      .data()
      .id();
    var actions = repository.list("recipient");
    assertEquals(2, actions.size());

    assertEquals(id1, actions.get(0).data().id());
    assertEquals("game", actions.get(0).data().game());
    assertEquals("category", actions.get(0).data().category());
    assertEquals("provider", actions.get(0).data().provider());
    assertEquals("recipient", actions.get(0).data().recipientId());
    assertEquals(Map.of("key", "value"), actions.get(0).data().payload());
    assertEquals(new Amount(10, 0, "EUR"), actions.get(0).data().amount());

    assertEquals(id2, actions.get(1).data().id());
    assertEquals("anotherGame", actions.get(1).data().game());
    assertEquals("anotherCategory", actions.get(1).data().category());
    assertEquals("anotherProvider", actions.get(1).data().provider());
    assertEquals("recipient", actions.get(1).data().recipientId());
    assertEquals(
      Map.of("anotherKey", "anotherValue"),
      actions.get(1).data().payload()
    );
    assertEquals(new Amount(100, 0, "EUR"), actions.get(1).data().amount());
  }
}
