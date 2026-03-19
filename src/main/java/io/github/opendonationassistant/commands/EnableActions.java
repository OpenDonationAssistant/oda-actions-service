package io.github.opendonationassistant.commands;

import io.github.opendonationassistant.commons.logging.ODALogger;
import io.github.opendonationassistant.commons.micronaut.BaseController;
import io.github.opendonationassistant.repository.Action;
import io.github.opendonationassistant.repository.ActionData;
import io.github.opendonationassistant.repository.ActionRepository;
import io.micronaut.data.repository.jpa.criteria.PredicateSpecification;
import io.micronaut.http.HttpResponse;
import io.micronaut.security.authentication.Authentication;
import jakarta.inject.Inject;
import jakarta.persistence.criteria.Predicate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public class EnableActions extends BaseController implements EnableActionsApi {

  private final ODALogger log = new ODALogger(this);
  private final ActionRepository repository;

  @Inject
  public EnableActions(ActionRepository repository) {
    this.repository = repository;
  }

  public CompletableFuture<HttpResponse<EnableActionsResult>> execute(
    EnableActionsCommand command,
    Authentication auth
  ) {
    Optional<String> ownerIdOpt = getOwnerId(auth);
    if (ownerIdOpt.isEmpty()) {
      return CompletableFuture.completedFuture(HttpResponse.unauthorized());
    }
    String ownerId = ownerIdOpt.get();
    Optional<List<String>> idsOpt = Optional.ofNullable(command.ids());
    return getActions(ownerId, command)
      .stream()
      .map(Action::enable)
      .reduce(CompletableFuture.completedFuture(null), (f1, f2) ->
        f1.thenCombine(f2, (a, b) -> null)
      )
      .thenApply(unused -> {
        EnableActionsResult result = new EnableActionsResult(
          true,
          "",
          idsOpt.orElse(List.of())
        );
        return HttpResponse.ok(result);
      });
  }

  private List<Action> getActions(
    String ownerId,
    EnableActionsCommand command
  ) {
    final List<Action> actions = new ArrayList<>();
    if (command.ids() != null) {
      actions.addAll(
        command
          .ids()
          .stream()
          .flatMap(id -> {
            return repository.findByIdAndRecipientId(id, ownerId).stream();
          })
          .toList()
      );
    }
    PredicateSpecification<ActionData> spec = (root, builder) -> {
      final ArrayList<Predicate> conditions = new ArrayList<>();
      conditions.add(builder.equal(root.get("recipientId"), ownerId));
      if (command.game() != null) {
        conditions.add(builder.equal(root.get("game"), command.game()));
      }
      if (command.category() != null) {
        conditions.add(builder.equal(root.get("category"), command.category()));
      }
      if (command.provider() != null) {
        conditions.add(builder.equal(root.get("provider"), command.provider()));
      }
      return conditions.size() == 0
        ? builder.isTrue(builder.literal(true))
        : builder.and(conditions.toArray(new Predicate[conditions.size()]));
    };
    actions.addAll(repository.findAll(spec));
    return actions;
  }
}
