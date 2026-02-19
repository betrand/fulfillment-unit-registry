# Questions

Here we have 3 questions related to the code base for you to answer. It is not about right or wrong, but more about what's the reasoning behind your decisions.

1. In this code base, we have some different implementation strategies when it comes to database access layer and manipulation. If you would maintain this code base, would you refactor any of those? Why?

**Answer:**
```txt
Yes, I would refactor to a single data-access style and a clearer layering model.

Today we mix:
- Active Record (`Store` extends `PanacheEntity` and is manipulated directly in the resource).
- Repository-only style (`ProductRepository`).
- Domain + ports/adapters (`Warehouse` use cases + `WarehouseStore`).

I would standardize on repository + application/use-case services for all modules.
Why:
1. Consistency lowers cognitive load and onboarding time.
2. It keeps HTTP, business logic, and persistence concerns separated.
3. It makes testing easier (mock repositories/use cases, fewer static Panache calls).
4. It prepares the codebase for growth (transactions, domain events, integrations, multiple data sources).

Concretely, I would move `Store` and `Product` business rules out of resources into use cases, keep resources thin, and use repositories/adapters everywhere.
```
----
2. When it comes to API spec and endpoints handlers, we have an Open API yaml file for the `Warehouse` API from which we generate code, but for the other endpoints - `Product` and `Store` - we just coded directly everything. What would be your thoughts about what are the pros and cons of each approach and what would be your choice?

**Answer:**
```txt
Both approaches are valid, but they optimize for different needs.

OpenAPI contract-first (Warehouse style):
Pros:
- Explicit API contract first (great for alignment with consumers).
- Safer refactors and easier client/server code generation.
- Better for cross-team collaboration and external/public APIs.
Cons:
- Extra tooling/process overhead.
- Spec/code drift risk if governance is weak.

Code-first (Product/Store style):
Pros:
- Fast iteration and lower initial friction.
- Simple for small internal endpoints.
Cons:
- Contract can become implicit and inconsistent.
- Harder for consumers to rely on stable schemas.

My choice:
- For this project long-term, I would adopt OpenAPI contract-first for all externally consumed APIs.
- For small internal endpoints, code-first is acceptable early, but I would still publish/validate generated OpenAPI in CI to avoid drift.
```
----
3. Given the need to balance thorough testing with time and resource constraints, how would you prioritize and implement tests for this project? Which types of tests would you focus on, and how would you ensure test coverage remains effective over time?

**Answer:**
```txt
I would use a risk-based test strategy with a test pyramid:

1) Highest priority: business-rule tests (fast unit/use-case tests)
- Cover warehouse create/replace/archive validations and fulfilment-association limits.
- These rules carry the most business risk and should run on every commit.

2) Second priority: API integration tests
- Validate status codes, payload shape, and persistence behavior.
- Include transactional/integration behavior (for example, store sync event after commit).

3) Third priority: end-to-end smoke tests
- A small number of flows to verify system wiring in CI/CD environments.

To keep coverage effective over time:
- Add regression tests for every bug fix and every new business rule.
- Keep tests deterministic and isolated (no cross-test data leakage).
- Track coverage on critical paths (not only global %), and enforce CI gates.
- Periodically review flaky/slow tests and prune or redesign them.
```
