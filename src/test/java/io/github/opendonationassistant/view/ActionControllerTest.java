package io.github.opendonationassistant.view;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import io.github.opendonationassistant.repository.Action;
import io.github.opendonationassistant.repository.ActionRepository;
import io.github.opendonationassistant.view.ActionController.ActionDto;
import io.micronaut.http.HttpResponse;
import io.micronaut.security.authentication.Authentication;
import java.util.List;
import java.util.Map;
import org.instancio.junit.Given;
import org.instancio.junit.InstancioExtension;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(InstancioExtension.class)
public class ActionControllerTest {

  ActionRepository repository = mock(ActionRepository.class);
  ActionController controller = new ActionController(repository);
  Authentication auth = mock(Authentication.class);

  @Test
  public void testGettingActionList(@Given Action first, @Given Action second) {
    when(repository.list("recipient")).thenReturn(List.of(first, second));
    final HttpResponse<List<ActionDto>> response = controller.getActions("recipient");
    assertTrue(response.code() == 200);
    assertEquals(2, response.body().size());
    var createdFirst = response.body().getFirst();
    var createdSecond = response.body().getLast();
    assertEquals(first.data().id(), createdFirst.id());
    assertEquals(first.data().payload(), createdFirst.payload());
    assertEquals(first.data().name(), createdFirst.name());
    assertEquals(second.data().category(), createdSecond.category());
    assertEquals(second.data().payload(), createdSecond.payload());
    assertEquals(second.data().name(), createdSecond.name());
  }
}
