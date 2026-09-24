package io.github.opendonationassistant.commands;

import io.github.opendonationassistant.api.DeleteActionsApi;
import io.github.opendonationassistant.commons.logging.ODALogger;
import io.github.opendonationassistant.commons.micronaut.BaseController;
import io.github.opendonationassistant.repository.Action;
import io.github.opendonationassistant.repository.ActionData;
import io.github.opendonationassistant.repository.ActionRepository;
import io.micronaut.data.repository.jpa.criteria.PredicateSpecification;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.annotation.Controller;
import io.micronaut.security.authentication.Authentication;
import io.micronaut.validation.Validated;
import jakarta.inject.Inject;
import jakarta.persistence.criteria.Predicate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Stream;

@Controller
@Validated
public class DeleteActions extends BaseController implements DeleteActionsApi {

  private final ODALogger log = new ODALogger(this);
  private final ActionRepository repository;

  @Inject
  public DeleteActions(ActionRepository repository) {
    this.repository = repository;
  }

  public CompletableFuture<HttpResponse<DeleteActionsResult>> execute(
    DeleteActionsCommand command,
    Authentication auth
  ) {
    Optional<String> ownerIdOpt = getOwnerId(auth);
    if (ownerIdOpt.isEmpty()) {
      return CompletableFuture.completedFuture(HttpResponse.unauthorized());
    }
    String ownerId = ownerIdOpt.get();
    log.info(
      "Received DeleteActionsCommand",
      Map.of("command", command, "recipientId", ownerId)
    );
    Optional<List<String>> idsOpt = Optional.ofNullable(command.ids());
    return collectActions(ownerId, command)
      .map(Action::delete)
      .reduce(CompletableFuture.completedFuture(null), (f1, f2) ->
        f1.thenCombine(f2, (a, b) -> null)
      )
      .thenApply(_ ->
        HttpResponse.ok(
          new DeleteActionsResult(true, "", idsOpt.orElse(List.of()))
        )
      );
  }

  /**
   * Collects the actions to delete, de-duplicated by id. Actions are selected by
   * their explicit ids and/or the provided category/provider filters; when only
   * ids are given the filter query is skipped so unrelated actions are never
   * deleted.
   */
  private Stream<Action> collectActions(
    String ownerId,
    DeleteActionsCommand command
  ) {
    if (command.ids() != null) {
      return command
        .ids()
        .stream()
        .flatMap(id -> repository.findByIdAndRecipientId(id, ownerId).stream());
    } else if (hasFilters(command)) {
      return repository.findAll(filterSpec(ownerId, command)).stream();
    }
    return Stream.empty();
  }

  private boolean hasFilters(DeleteActionsCommand command) {
    return command.category() != null || command.provider() != null;
  }

  private PredicateSpecification<ActionData> filterSpec(
    String ownerId,
    DeleteActionsCommand command
  ) {
    return (root, builder) -> {
      final var conditions = new ArrayList<Predicate>();
      conditions.add(builder.equal(root.get("recipientId"), ownerId));
      if (command.category() != null) {
        conditions.add(builder.equal(root.get("category"), command.category()));
      }
      if (command.provider() != null) {
        conditions.add(builder.equal(root.get("provider"), command.provider()));
      }
      return builder.and(conditions.toArray(new Predicate[conditions.size()]));
    };
  }
}
