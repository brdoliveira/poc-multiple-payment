# Payment flow screen

## Objetivo

`web/payment-flow/` contains the local functional prototype for creating a payment. It gives an operator a three-step flow to enter order data, choose Pix, card or boleto, review the draft and simulate a successful operation.

The screen is intentionally independent from the backend microsservices. It does not call an API, persist data, authenticate users or send card data to a provider.

## Local execution

From the repository root:

```powershell
cd web/payment-flow
npm ci
npm run test:unit
npx playwright install chromium
npm run test:integration
node test-server.mjs
```

Open `http://127.0.0.1:4173/index.html` after starting the server. The browser test starts and stops its own server automatically through Playwright.

## Responsibilities

- `index.html`: semantic structure, form fields, payment methods, review and success states.
- `styles.css`: responsive layout and visual states for desktop and narrow viewports.
- `payment-flow-core.js`: framework-free state transitions, amount parsing, Brazilian currency formatting, validation and operation ID generation.
- `app.js`: DOM event handlers and rendering of the core state.
- `tests/payment-flow.unit.test.mjs`: deterministic tests for the core module with `node:test`.
- `tests/payment-flow.integration.spec.mjs`: browser test for the complete operator journey with Playwright.
- `test-server.mjs`: minimal local static server used by the browser test.

## Test strategy

The unit layer protects validation, formatting, method selection and transitions without a browser. The integration layer opens the real HTML, fills the form, changes the method, reviews the values, confirms the operation and starts a new payment. The static spec tests verify that the expected UI and CI contracts remain present in the repository.

Run all local screen tests with:

```powershell
npm test
```

The `payment-flow` CI job runs `npm ci`, installs Chromium and executes both layers on every push and pull request. The `spec-static` job runs the repository-level static proofs. The broader `.spec/run-tests.mjs` command also executes these checks when the spec verification command is used.

## Limits of the prototype

- The success state is simulated in memory.
- The operation ID is generated locally and has no backend meaning.
- No persistence, authentication, authorization, provider integration or sensitive data handling is implemented.
- Backend contract and end-to-end service integration remain future work after the payment APIs are defined.
