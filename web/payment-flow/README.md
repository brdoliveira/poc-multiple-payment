# Payment flow

Standalone local screen for the payment creation flow.

## Run

```powershell
npm ci
node test-server.mjs
```

Open `http://127.0.0.1:4173/index.html` in a browser.

## Test

Unit tests do not need a browser:

```powershell
npm run test:unit
```

Integration tests use Playwright and start `test-server.mjs` automatically:

```powershell
npx playwright install chromium
npm run test:integration
npm test
```

The screen is a local prototype. `payment-flow-core.js` owns pure validation and state logic, while `app.js` connects that logic to the DOM. No real payment, provider call or persistent storage is performed.
