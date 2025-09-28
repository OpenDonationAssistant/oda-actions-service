package io.github.opendonationassistant.commands;

import io.micronaut.http.HttpResponse;
import io.micronaut.http.annotation.Body;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Post;
import jakarta.annotation.Nullable;
import java.util.List;

@Controller
public class DeleteActions {

  @Post("/actions/commands/deleteActions")
  public HttpResponse<DeleteActionsResult> deleteActions(
    @Body DeleteActionsCommand command
  ) {
    return HttpResponse.ok(new DeleteActionsResult(true, "", command.ids()));
  }

  public static record DeleteActionsResult(
    Boolean success,
    String message,
    List<String> ids
  ) {}

  public static record DeleteActionsCommand(
    @Nullable String category,
    @Nullable String provider,
    @Nullable List<String> ids
  ) {}
}
