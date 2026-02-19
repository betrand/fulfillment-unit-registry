# Case Study Scenarios to discuss

## Scenario 1: Cost Allocation and Tracking
**Situation**: The company needs to track and allocate costs accurately across different Warehouses and Stores. The costs include labor, inventory, transportation, and overhead expenses.

**Task**: Discuss the challenges in accurately tracking and allocating costs in a fulfillment environment. Think about what are important considerations for this, what are previous experiences that you have you could related to this problem and elaborate some questions and considerations

**Questions you may have and considerations:**
- What is the target allocation granularity: by warehouse, by store, by product, by order, or by shipment lane?
- Which cost drivers are accepted by Finance for each cost type?
- Labor: hours or touches.
- Transportation: distance, weight, volume, or stop count.
- Overhead: fixed split, activity-based, or revenue-based.
- How are shared services treated (IT, facilities, management), and what is the policy for indirect cost allocation?
- Which systems are the source of truth for each input (WMS, TMS, ERP, payroll), and what is the required data freshness?
- How will warehouse replacement be represented so history is preserved while BU code is reused?
- Need a stable internal warehouse identity and an effective date model.
- What reconciliation controls are required at month-end between operational cost views and the general ledger?
- What audit requirements exist for traceability (who changed allocation rules, when, and why)?
- What are the decision KPIs this allocation must support (unit cost per order, cost per line, cost per store)?

## Scenario 2: Cost Optimization Strategies
**Situation**: The company wants to identify and implement cost optimization strategies for its fulfillment operations. The goal is to reduce overall costs without compromising service quality.

**Task**: Discuss potential cost optimization strategies for fulfillment operations and expected outcomes from that. How would you identify, prioritize and implement these strategies?

**Questions you may have and considerations:**
- What are current baseline costs and service KPIs by warehouse/store/product segment?
- Which service metrics are non-negotiable constraints (fill rate, OTIF, lead time, stockout risk)?
- Which levers are in scope first: inventory positioning, replenishment policy, labor planning, carrier mix, route design, automation?
- How much savings potential and implementation effort does each lever have?
- Use an impact vs effort matrix and sequence quick wins before structural changes.
- What is the acceptable payback period for optimization investments?
- How will we run pilots safely (limited geography/store set, control group, rollback plan)?
- Which cross-functional owners approve trade-offs (Operations, Finance, Supply Chain, Commercial)?
- What change-management risks exist at store/warehouse level, and what training is needed?
- How will gains be measured and locked in after rollout to avoid regression?

## Scenario 3: Integration with Financial Systems
**Situation**: The Cost Control Tool needs to integrate with existing financial systems to ensure accurate and timely cost data. The integration should support real-time data synchronization and reporting.

**Task**: Discuss the importance of integrating the Cost Control Tool with financial systems. What benefits the company would have from that and how would you ensure seamless integration and data synchronization?

**Questions you may have and considerations:**
- Which financial systems must integrate (ERP, GL, AP, cost centers, budgeting tools), and which fields are mandatory?
- Is near-real-time truly required for all data, or only for selected cost events?
- Define event classes that need real-time vs batch.
- How will master data be harmonized (store ids, warehouse ids, product ids, cost center codes)?
- What integration pattern is preferred (event streaming, API sync, scheduled ETL), and what fallback is needed during outages?
- How will idempotency, retries, and replay be handled to avoid duplicate postings?
- What reconciliation process will ensure financial and operational totals match each close cycle?
- What security and compliance controls are required (least privilege, PII boundaries, audit logs, segregation of duties)?
- What monitoring and alerting are required for integration health and data latency SLA breaches?
- Who owns error triage and correction workflows when financial postings fail?

## Scenario 4: Budgeting and Forecasting
**Situation**: The company needs to develop budgeting and forecasting capabilities for its fulfillment operations. The goal is to predict future costs and allocate resources effectively.

**Task**: Discuss the importance of budgeting and forecasting in fulfillment operations and what would you take into account designing a system to support accurate budgeting and forecasting?

**Questions you may have and considerations:**
- What planning horizon is needed: monthly budget plus rolling 12/18 month forecast?
- Is the model top-down, bottom-up, or hybrid by business unit?
- Which demand and cost drivers should power forecasts (orders, lines, labor rates, fuel, seasonality, promotions, returns)?
- What level of scenario planning is needed (best/base/worst case, shock scenarios)?
- How frequently will forecasts be refreshed and who approves revisions?
- How are variances explained and fed back into the model (forecast vs actual loop)?
- What granularity is required for decision-making (network, warehouse, store, product family)?
- How should one-off events be handled (warehouse replacement, network redesign, supplier disruption)?
- What forecast accuracy target is acceptable for each decision layer?
- What governance is needed for model versioning, assumptions registry, and sign-off?

## Scenario 5: Cost Control in Warehouse Replacement
**Situation**: The company is planning to replace an existing Warehouse with a new one. The new Warehouse will reuse the Business Unit Code of the old Warehouse. The old Warehouse will be archived, but its cost history must be preserved.

**Task**: Discuss the cost control aspects of replacing a Warehouse. Why is it important to preserve cost history and how this relates to keeping the new Warehouse operation within budget?

**Questions you may have and considerations:**
- What is the accounting treatment for replacement costs: capex vs opex, and how is depreciation handled across old/new assets?
- What is the exact cutover date and what costs belong before vs after cutover?
- With BU code reuse, how will historical and new operations remain separable in reports?
- Use immutable warehouse internal ids and effective dates behind the reused BU code.
- Which transition costs must be planned (migration labor, dual-running period, decommissioning, write-offs)?
- How will replacement affect inventory carrying costs, transportation lanes, and labor productivity in the first 3-6 months?
- What budget guardrails and early-warning KPIs are required during ramp-up?
- Unit cost trend, overtime, expedited freight, service misses.
- What controls ensure historical cost records are not overwritten during replacement data migration?
- What reconciliation and audit checkpoints are required after cutover to validate cost continuity?
