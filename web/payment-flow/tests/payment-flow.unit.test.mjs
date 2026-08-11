import test from 'node:test';
import assert from 'node:assert/strict';
import {
  METHOD_NAMES,
  createInitialState,
  createOperationId,
  formatAmount,
  nextStep,
  previousStep,
  selectMethodState,
  validateDetails,
} from '../payment-flow-core.js';

test('formats Brazilian currency consistently @spec:AC-014', () => {
  assert.equal(formatAmount('1234,56'), 'R$ 1.234,56');
  assert.equal(formatAmount('0'), 'R$ 0,00');
  assert.equal(formatAmount('invalid'), 'R$ 0,00');
});

test('validates required description and positive amount @spec:AC-014', () => {
  assert.deepEqual(validateDetails({}), {
    valid: false,
    errors: {
      description: 'Informe a descri\u00e7\u00e3o do pagamento.',
      amount: 'Informe um valor maior que zero.',
    },
  });
  assert.deepEqual(validateDetails({ description: 'Pedido 4821', amount: '100,00' }), {
    valid: true,
    errors: {},
  });
});

test('changes method without mutating the previous state @spec:AC-014', () => {
  const initial = createInitialState();
  const selected = selectMethodState(initial, 'card');
  assert.equal(initial.method, 'pix');
  assert.equal(selected.method, 'card');
  assert.equal(METHOD_NAMES[selected.method], 'Cart\u00e3o');
});

test('moves forward and backward within the three-step flow @spec:AC-014', () => {
  const initial = createInitialState();
  assert.equal(nextStep(initial).step, 2);
  assert.equal(nextStep(nextStep(initial)).step, 3);
  assert.equal(nextStep(nextStep(nextStep(initial))).step, 3);
  assert.equal(previousStep(initial).step, 1);
  assert.equal(previousStep({ ...initial, step: 3 }).step, 2);
});

test('creates a six-digit operation identifier @spec:AC-014', () => {
  assert.equal(createOperationId(0), 'PAY-100000');
  assert.equal(createOperationId(0.999999), 'PAY-999999');
});
