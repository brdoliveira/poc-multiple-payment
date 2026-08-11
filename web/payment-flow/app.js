import {
  METHOD_DETAILS,
  METHOD_NAMES,
  createInitialState,
  createOperationId,
  formatAmount,
  nextStep,
  previousStep,
  selectMethodState,
  validateDetails as validateDetailsState,
} from './payment-flow-core.js';

const state = createInitialState();

const form = document.querySelector('#payment-form');
const alertBox = document.querySelector('#form-alert');
const amountInput = document.querySelector('#amount');
const descriptionInput = document.querySelector('#description');
const customerInput = document.querySelector('#customer');

function syncState() {
  state.description = descriptionInput.value.trim();
  state.amount = amountInput.value.trim();
  state.customer = customerInput.value.trim();
}

function updateSummary() {
  document.querySelector('[data-summary="description"]').textContent = state.description || 'Aguardando dados';
  document.querySelector('[data-summary="customer"]').textContent = state.customer || 'N\u00e3o informado';
  document.querySelector('[data-summary="method"]').textContent = METHOD_NAMES[state.method];
  document.querySelector('[data-summary="amount"]').textContent = formatAmount(state.amount);
  document.querySelector('[data-review="description"]').textContent = state.description || '\u2014';
  document.querySelector('[data-review="customer"]').textContent = state.customer || 'N\u00e3o informado';
  document.querySelector('[data-review="method"]').textContent = METHOD_NAMES[state.method];
  document.querySelector('[data-review="amount"]').textContent = formatAmount(state.amount);
}

function setAlert(message = '') {
  alertBox.textContent = message;
  alertBox.hidden = !message;
}

function renderStep() {
  document.querySelectorAll('[data-panel]').forEach((panel) => {
    const visible = panel.dataset.panel === String(state.step);
    panel.hidden = !visible;
    panel.classList.toggle('is-visible', visible);
  });
  document.querySelectorAll('.step').forEach((step) => {
    const number = Number(step.dataset.step);
    step.classList.toggle('is-active', number === state.step);
    step.classList.toggle('is-done', number < state.step);
    step.setAttribute('aria-current', number === state.step ? 'step' : 'false');
  });
  document.querySelector('#flow-kicker').textContent = `ETAPA 0${state.step}`;
  document.querySelector('#flow-title').textContent = ['Dados do pagamento', 'Meio de pagamento', 'Revis\u00e3o do pagamento'][state.step - 1];
  document.querySelector('[data-action="back"]').hidden = state.step === 1;
  updateSummary();
}

function validateDetails() {
  syncState();
  const validation = validateDetailsState(state);
  document.querySelector('#description').closest('.field').classList.toggle('is-invalid', Boolean(validation.errors.description));
  document.querySelector('#amount').closest('.field').classList.toggle('is-invalid', Boolean(validation.errors.amount));
  if (!validation.valid) {
    setAlert('Informe a descri\u00e7\u00e3o e um valor maior que zero para continuar.');
    return false;
  }
  setAlert();
  return true;
}

function selectMethod(method) {
  Object.assign(state, selectMethodState(state, method));
  document.querySelectorAll('.method-card').forEach((card) => {
    const selected = card.dataset.method === state.method;
    card.classList.toggle('is-selected', selected);
    card.setAttribute('aria-checked', String(selected));
  });
  const [title, copy] = METHOD_DETAILS[state.method];
  document.querySelector('#method-detail strong').textContent = title;
  document.querySelector('#method-detail small').textContent = copy;
  updateSummary();
}

function createPayment() {
  document.querySelector('#operation-id').textContent = createOperationId();
  document.querySelectorAll('[data-panel]').forEach((panel) => { panel.hidden = panel.dataset.panel !== 'success'; });
  document.querySelector('[data-actions]').hidden = true;
  document.querySelectorAll('.step').forEach((step) => step.classList.add('is-done'));
  setAlert();
}

function resetFlow() {
  Object.assign(state, createInitialState());
  form.reset();
  selectMethod('pix');
  document.querySelector('[data-actions]').hidden = false;
  document.querySelectorAll('.field').forEach((field) => field.classList.remove('is-invalid'));
  renderStep();
  setAlert();
}

document.querySelectorAll('.method-card').forEach((card) => card.addEventListener('click', () => selectMethod(card.dataset.method)));
document.querySelectorAll('.step').forEach((step) => step.addEventListener('click', () => {
  const target = Number(step.dataset.step);
  if (target < state.step || (target === 2 && validateDetails())) {
    state.step = target;
    renderStep();
  }
}));
document.querySelector('[data-action="back"]').addEventListener('click', () => {
  Object.assign(state, previousStep(state));
  renderStep();
});
document.querySelectorAll('[data-action="reset"]').forEach((button) => button.addEventListener('click', resetFlow));
form.addEventListener('submit', (event) => {
  event.preventDefault();
  if (state.step === 1 && validateDetails()) {
    Object.assign(state, nextStep(state));
    renderStep();
  } else if (state.step === 2) {
    Object.assign(state, nextStep(state));
    renderStep();
  } else if (state.step === 3) {
    createPayment();
  }
});

[descriptionInput, amountInput, customerInput].forEach((input) => input.addEventListener('input', () => {
  syncState();
  updateSummary();
}));

selectMethod('pix');
renderStep();
