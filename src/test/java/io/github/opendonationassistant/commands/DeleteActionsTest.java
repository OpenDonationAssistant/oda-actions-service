package io.github.opendonationassistant.commands;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import io.github.opendonationassistant.api.DeleteActionsApi.DeleteActionsCommand;
import io.github.opendonationassistant.repository.Action;
import io.github.opendonationassistant.repository.ActionData;
import io.github.opendonationassistant.repository.ActionRepository;
import io.micronaut.security.authentication.Authentication;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import org.instancio.junit.Given;
import org.instancio.junit.InstancioExtension;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(InstancioExtension.class)
public class DeleteActionsTest {

  ActionRepository repository = mock(ActionRepository.class);
  DeleteActions controller = new DeleteActions(repository);

  @Test
  public void testDeleteByIdsDoesNotQueryAllActions(
    @Given Authentication auth,
    @Given ActionData data
  ) throws Exception {
    var action = deletableAction(data);
    when(repository.findByIdAndRecipientId("id1", "testuser"))
      .thenReturn(Optional.of(action));

    var response = controller
      .execute(new DeleteActionsCommand(null, null, List.of("id1")), auth)
      .get();

    assertEquals(200, response.code());
    verify(action).delete();
    verify(repository, never()).findAll(any());
  }

  @Test
  public void testDeleteByCategoryUsesFilterSpec(
    @Given Authentication auth,
    @Given ActionData data
  ) throws Exception {
    var action = deletableAction(data);
    when(repository.findAll(any())).thenReturn(List.of(action));

    var response = controller
      .execute(new DeleteActionsCommand("category", null, null), auth)
      .get();

    assertEquals(200, response.code());
    verify(repository).findAll(any());
    verify(action).delete();
    verify(repository, never()).findByIdAndRecipientId(any(), any());
  }

  @Test
  public void testDeleteByProviderUsesFilterSpec(
    @Given Authentication auth,
    @Given ActionData data
  ) throws Exception {
    var action = deletableAction(data);
    when(repository.findAll(any())).thenReturn(List.of(action));

    var response = controller
      .execute(new DeleteActionsCommand(null, "provider", null), auth)
      .get();

    assertEquals(200, response.code());
    verify(repository).findAll(any());
    verify(action).delete();
  }

  @Test
  public void testDeleteWithoutSelectorsDeletesNothing(
    @Given Authentication auth
  ) throws Exception {
    var response = controller
      .execute(new DeleteActionsCommand(null, null, null), auth)
      .get();

    assertEquals(200, response.code());
    verify(repository, never()).findAll(any());
    verify(repository, never()).findByIdAndRecipientId(any(), any());
  }

  @Test
  public void testDeleteWithoutPreferredUsernameIsUnauthorized() {
    var auth = Authentication.build("user", Map.of());

    var response = controller
      .execute(new DeleteActionsCommand(null, null, List.of("id1")), auth)
      .join();

    assertEquals(401, response.code());
    verify(repository, never()).findByIdAndRecipientId(any(), any());
    verify(repository, never()).findAll(any());
  }

  private Action deletableAction(ActionData data) {
    var action = mock(Action.class);
    when(action.data()).thenReturn(data);
    when(action.delete()).thenReturn(CompletableFuture.completedFuture(null));
    return action;
  }
}
