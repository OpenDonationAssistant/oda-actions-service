package io.github.opendonationassistant.commands;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import io.github.opendonationassistant.commands.AddAction.AddActionsCommand;
import io.github.opendonationassistant.commands.AddAction.NewAction;
import io.github.opendonationassistant.repository.Action;
import io.github.opendonationassistant.repository.ActionData;
import io.github.opendonationassistant.repository.ActionRepository;
import io.micronaut.security.authentication.Authentication;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import org.instancio.junit.Given;
import org.instancio.junit.InstancioExtension;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.testcontainers.rabbitmq.RabbitMQContainer;
import org.testcontainers.utility.DockerImageName;

@ExtendWith(InstancioExtension.class)
public class AddActionTest {

  ActionRepository repository = mock(ActionRepository.class);

  RabbitMQContainer rabbit = new RabbitMQContainer(
    DockerImageName.parse("rabbitmq:3.7.25-management-alpine")
  );

  AddAction controller = new AddAction(repository);

  Authentication auth = mock(Authentication.class);

  @BeforeEach
  public void setup() {
    rabbit.start();
  }

  @Test
  public void testAddingAction(@Given NewAction newAction)
    throws InterruptedException, ExecutionException {
    var createdAction = mock(Action.class);
    when(createdAction.save()).thenReturn(
      CompletableFuture.completedFuture(createdAction)
    );
    when(createdAction.data()).thenReturn(
      new ActionData(
        "id",
        "recipient",
        newAction.category(),
        "DonationListener",
        newAction.name(),
        newAction.price(),
        newAction.game(),
        true,
        newAction.payload()
      )
    );
    when(auth.getAttributes()).thenReturn(
      Map.of("preferred_username", "recipient")
    );

    when(
      repository.create(any(), any(), any(), any(), any(), any(), any())
    ).thenReturn(CompletableFuture.completedFuture(createdAction));

    var command = new AddActionsCommand(List.of(newAction));
    var response = controller.addAction(command, auth).get();

    verify(repository).create(
      "recipient",
      newAction.category(),
      "DonationListener",
      newAction.name(),
      newAction.price(),
      newAction.game(),
      newAction.payload()
    );

    assertTrue(response.code() == 200);
    var responseBody = response.getBody();
    assertTrue(responseBody.isPresent());
    var actionResults = responseBody.get();
    assertEquals(1, actionResults.size());
    assertTrue(actionResults.getFirst().success());
    var action = actionResults.getFirst().action();
    assertEquals(newAction.name(), action.name());
    assertEquals(newAction.category(), action.category());
    assertEquals(newAction.price(), action.price());
    assertEquals(newAction.game(), action.game());
    assertEquals(newAction.payload(), action.payload());
    assertEquals("id", action.id());
  }
}
