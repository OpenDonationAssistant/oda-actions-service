package io.github.opendonationassistant.commands;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import io.github.opendonationassistant.commands.DeleteActions.DeleteActionsCommand;
import io.github.opendonationassistant.commands.DeleteActions.DeleteActionsResult;
import io.github.opendonationassistant.repository.ActionData;
import io.github.opendonationassistant.repository.ActionDataRepository;
import io.github.opendonationassistant.repository.ActionRepository;
import io.micronaut.http.HttpResponse;
import io.micronaut.security.authentication.Authentication;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.instancio.junit.Given;
import org.instancio.junit.InstancioExtension;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(InstancioExtension.class)
public class DeleteActionsTest {

  ActionDataRepository dataRepository = mock(ActionDataRepository.class);
  ActionRepository repository = new ActionRepository(dataRepository);
  DeleteActions controller = new DeleteActions(repository);
  Authentication auth = mock(Authentication.class);

  @Test
  public void testDeleteActionsById(
    @Given ActionData first,
    @Given ActionData second,
    @Given ActionData third
  ) {
    when(auth.getAttributes()).thenReturn(
      Map.of("preferred_username", "recipient")
    );

    when(dataRepository.findById(first.id())).thenReturn(Optional.of(first));
    when(dataRepository.findById(second.id())).thenReturn(Optional.of(second));
    when(dataRepository.findById(third.id())).thenReturn(Optional.of(third));

    DeleteActionsCommand command = new DeleteActionsCommand(
      null,
      null,
      List.of(first.id(), second.id())
    );
    final HttpResponse<DeleteActionsResult> result = controller.deleteActions(
      command,
      auth
    );
    assertEquals(200, result.code());
    assertTrue(result.body().success());
    assertEquals(List.of(first.id(), second.id()), result.body().ids());
    verify(dataRepository).delete(first);
    verify(dataRepository).delete(second);
    // TODO check that third is not deleted
  }
}
