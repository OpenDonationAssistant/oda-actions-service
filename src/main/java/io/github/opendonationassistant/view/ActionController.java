package io.github.opendonationassistant.view;

import io.github.opendonationassistant.commons.Amount;
import io.github.opendonationassistant.repository.ActionData;
import io.micronaut.http.annotation.Controller;
import java.util.Map;

@Controller
public class ActionController {

  public static record ActionDto(
    String id,
    String name,
    Amount amount,
    String catogory,
    Map<String, Object> payload
  ) {
    public static ActionDto from(ActionData data) {
      return new ActionDto(
        data.id(),
        data.name(),
        data.amount(),
        data.category(),
        data.payload()
      );
    }
  }
}
