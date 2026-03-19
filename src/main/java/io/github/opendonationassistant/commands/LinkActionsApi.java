package io.github.opendonationassistant.commands;

import com.fasterxml.uuid.Generators;
import io.github.opendonationassistant.commons.Amount;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.annotation.Body;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Post;
import io.micronaut.serde.annotation.Serdeable;
import io.swagger.v3.oas.annotations.Hidden;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.annotation.Nullable;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@Controller
public interface LinkActionsApi {

  @Post("/actions/commands/link-actions")
  @Hidden
  @Operation(
    summary = "Link actions",
    description = "Links actions together for coordinated execution"
  )
  @ApiResponse(
    responseCode = "200",
    description = "Actions successfully linked",
    content = @Content(
      mediaType = "application/json",
      schema = @Schema(implementation = LinkActionsResponse.class)
    )
  )
  default CompletableFuture<HttpResponse<LinkActionsResponse>> linkActions(
    @Body LinkActionsRequest request
  ) {
    return execute(request);
  }

  CompletableFuture<HttpResponse<LinkActionsResponse>> execute(
    LinkActionsRequest request
  );

  @Serdeable
  record LinkActionsRequest(
    String source,
    String originId,
    List<ActionRequest> actions
  ) {}

  @Serdeable
  record ActionRequest(String actionId, Integer amount) {}

  @Serdeable
  record LinkActionsResponse(Amount amount) {}
}
