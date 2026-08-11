export const METHOD_NAMES = Object.freeze({
  pix: 'Pix',
  card: 'Cart\u00e3o',
  boleto: 'Boleto',
});

export const METHOD_DETAILS = Object.freeze({
  pix: ['Pix selecionado', 'Um QR Code ser\u00e1 criado ao confirmar o pagamento.'],
  card: ['Cart\u00e3o selecionado', 'A autoriza\u00e7\u00e3o ser\u00e1 simulada neste prot\u00f3tipo.'],
  boleto: ['Boleto selecionado', 'O c\u00f3digo de barras ser\u00e1 disponibilizado ap\u00f3s a confirma\u00e7\u00e3o.'],
});

export function createInitialState() {
  return {
    step: 1,
    method: 'pix',
    description: '',
    amount: '',
    customer: '',
  };
}

export function parseAmount(value) {
  const raw = String(value ?? '').trim().replace(/\s/g, '');
  if (!raw) return NaN;

  const normalized = raw.includes(',')
    ? raw.replace(/\./g, '').replace(',', '.')
    : raw.replace(/,/g, '');
  const numeric = Number(normalized.replace(/[^0-9.-]/g, ''));
  return Number.isFinite(numeric) ? numeric : NaN;
}

export function formatAmount(value) {
  const numeric = parseAmount(value);
  if (!Number.isFinite(numeric) || numeric <= 0) return 'R$ 0,00';
  return numeric
    .toLocaleString('pt-BR', { style: 'currency', currency: 'BRL' })
    .replace(/\u00a0/g, ' ');
}

export function validateDetails({ description = '', amount = '' } = {}) {
  const errors = {};
  const normalizedDescription = String(description).trim();
  const numericAmount = parseAmount(amount);

  if (!normalizedDescription) errors.description = 'Informe a descri\u00e7\u00e3o do pagamento.';
  if (!Number.isFinite(numericAmount) || numericAmount <= 0) {
    errors.amount = 'Informe um valor maior que zero.';
  }

  return {
    valid: Object.keys(errors).length === 0,
    errors,
  };
}

export function selectMethodState(state, method) {
  if (!Object.hasOwn(METHOD_NAMES, method)) return { ...state };
  return { ...state, method };
}

export function nextStep(state) {
  return { ...state, step: Math.min(3, state.step + 1) };
}

export function previousStep(state) {
  return { ...state, step: Math.max(1, state.step - 1) };
}

export function createOperationId(randomValue = Math.random()) {
  const boundedRandom = Math.min(0.999999, Math.max(0, Number(randomValue) || 0));
  return `PAY-${String(Math.floor(100000 + boundedRandom * 900000)).padStart(6, '0')}`;
}
