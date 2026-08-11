import { spawnSync } from 'node:child_process';
import {
  existsSync,
  mkdirSync,
  readdirSync,
  readFileSync,
  rmSync,
} from 'node:fs';
import path from 'node:path';
import { fileURLToPath } from 'node:url';

const specDir = path.dirname(fileURLToPath(import.meta.url));
const rootDir = path.resolve(specDir, '..');
const resultsDir = path.join(specDir, 'test-results');
const javaHome = findJavaHome();
const mavenBin = path.join(rootDir, '.local-tools', 'apache-maven-3.9.11', 'bin');
const gradleBin = path.join(rootDir, '.local-tools', 'gradle-8.8', 'bin');
const mavenCommand = localCommand(mavenBin, process.platform === 'win32' ? 'mvn.cmd' : 'mvn') || 'mvn';
const gradleCommand = localCommand(gradleBin, process.platform === 'win32' ? 'gradle.bat' : 'gradle') || 'gradle';
const dotnetCommand = localDotnetCommand();
const npmCommand = process.platform === 'win32' ? 'npm.cmd' : 'npm';
const npxCommand = process.platform === 'win32' ? 'npx.cmd' : 'npx';

rmSync(resultsDir, { recursive: true, force: true });
mkdirSync(resultsDir, { recursive: true });

const commands = [
  {
    name: 'payment-orchestrator-java',
    cwd: path.join(rootDir, 'services', 'payment-orchestrator-java'),
    command: mavenCommand,
    args: ['-Dmaven.repo.local=' + path.join(rootDir, '.local-tools', 'm2'), 'test'],
    env: toolEnv([mavenBin, javaHome && path.join(javaHome, 'bin')], {
      JAVA_HOME: javaHome,
    }),
    reports: [path.join(rootDir, 'services', 'payment-orchestrator-java', 'target', 'surefire-reports')],
    reportType: 'junit',
  },
  {
    name: 'pix-boleto-kotlin',
    cwd: path.join(rootDir, 'services', 'pix-boleto-kotlin'),
    command: gradleCommand,
    args: ['test', '--no-daemon'],
    env: toolEnv([gradleBin, javaHome && path.join(javaHome, 'bin')], {
      GRADLE_USER_HOME: path.join(rootDir, '.local-tools', 'gradle-home'),
      JAVA_HOME: javaHome,
    }),
    reports: [path.join(rootDir, 'services', 'pix-boleto-kotlin', 'build', 'test-results', 'test')],
    reportType: 'junit',
  },
  {
    name: 'card-payment-csharp-restore',
    cwd: path.join(rootDir, 'services', 'card-payment-csharp'),
    command: dotnetCommand,
    args: ['restore', '--configfile', 'NuGet.Config'],
    env: dotnetEnv(),
    reports: [],
  },
  {
    name: 'card-payment-csharp',
    cwd: path.join(rootDir, 'services', 'card-payment-csharp'),
    command: dotnetCommand,
    args: [
      'test',
      '--no-restore',
      '--logger',
      'trx;LogFileName=card-payment.trx',
      '--results-directory',
      path.join(resultsDir, 'dotnet'),
    ],
    env: dotnetEnv(),
    reports: [path.join(resultsDir, 'dotnet')],
    reportType: 'trx',
  },
  {
    name: 'payment-flow-dependencies',
    cwd: path.join(rootDir, 'web', 'payment-flow'),
    command: npmCommand,
    args: ['ci', '--ignore-scripts'],
    env: {},
    reports: [],
  },
  {
    name: 'payment-flow-browser-install',
    cwd: path.join(rootDir, 'web', 'payment-flow'),
    command: npxCommand,
    args: ['playwright', 'install', 'chromium'],
    env: {},
    reports: [],
  },
  {
    name: 'payment-flow-unit',
    cwd: path.join(rootDir, 'web', 'payment-flow'),
    command: process.execPath,
    args: ['--test', '--test-reporter=tap', path.join(rootDir, 'web', 'payment-flow', 'tests', 'payment-flow.unit.test.mjs')],
    env: {},
    reports: [],
    reportType: 'tap',
  },
  {
    name: 'payment-flow-integration',
    cwd: path.join(rootDir, 'web', 'payment-flow'),
    command: npmCommand,
    args: ['run', 'test:integration'],
    env: {},
    reports: [path.join(rootDir, 'web', 'payment-flow', 'test-results')],
    reportType: 'junit',
  },
  {
    name: 'aws-terraform-payment-poc-static',
    cwd: rootDir,
    command: process.execPath,
    args: ['--test', '--test-reporter=tap', path.join(rootDir, '.spec', 'static-tests', 'aws-terraform-payment-poc.test.mjs')],
    env: {},
    reports: [],
    reportType: 'tap',
  },
  {
    name: 'payment-flow-screen-static',
    cwd: rootDir,
    command: process.execPath,
    args: ['--test', '--test-reporter=tap', path.join(rootDir, '.spec', 'static-tests', 'payment-flow-screen.test.mjs')],
    env: {},
    reports: [],
    reportType: 'tap',
  },
  {
    name: 'reliability-observability-static',
    cwd: rootDir,
    command: process.execPath,
    args: ['--test', '--test-reporter=tap', path.join(rootDir, '.spec', 'static-tests', 'reliability-observability.test.mjs')],
    env: {},
    reports: [],
    reportType: 'tap',
  },
  {
    name: 'github-actions-terraform-ci-static',
    cwd: rootDir,
    command: process.execPath,
    args: ['--test', '--test-reporter=tap', path.join(rootDir, '.spec', 'static-tests', 'github-actions-terraform-ci.test.mjs')],
    env: {},
    reports: [],
    reportType: 'tap',
  },
];

const commandResults = [];
for (const step of commands) {
  if (!existsSync(step.cwd)) {
    commandResults.push({
      step,
      status: 1,
      output: `Missing working directory: ${step.cwd}`,
      tests: [],
    });
    continue;
  }

  for (const reportDir of step.reports) {
    rmSync(reportDir, { recursive: true, force: true });
  }

  const invocation = commandInvocation(step.command, step.args);
  const proc = spawnSync(invocation.command, invocation.args, {
    cwd: step.cwd,
    shell: false,
    encoding: 'utf-8',
    maxBuffer: 64 * 1024 * 1024,
    env: {
      ...process.env,
      ...(step.env || {}),
    },
  });

  const output = [proc.stdout, proc.stderr].filter(Boolean).join('\n');
  commandResults.push({
    step,
    status: proc.status ?? 1,
    error: proc.error,
    output,
    tests: collectReports(step, output),
  });
}

const tests = [];
for (const result of commandResults) {
  if (result.error) {
    tests.push({
      title: `${result.step.name} command failed to start: ${result.error.message}`,
      status: 'fail',
    });
    continue;
  }

  if (result.step.reportType && result.tests.length === 0) {
    tests.push({
      title: `${result.step.name} did not produce a readable test report`,
      status: result.status === 0 ? 'skip' : 'fail',
    });
    continue;
  }

  tests.push(...result.tests);
}

console.log('TAP version 13');
tests.forEach((test, index) => {
  const prefix = test.status === 'fail' ? 'not ok' : 'ok';
  const directive = test.status === 'skip' ? ' # SKIP' : '';
  console.log(`${prefix} ${index + 1} - ${sanitizeTapTitle(test.title)}${directive}`);
});
console.log(`1..${tests.length}`);

for (const result of commandResults) {
  if (result.status !== 0 || result.error) {
    console.log(`# ${result.step.name} exit=${result.status}`);
    firstLines(result.output).forEach((line) => console.log(`# ${line}`));
  }
}

const failedCommand = commandResults.some((result) => result.status !== 0 || result.error);
const failedTest = tests.some((test) => test.status === 'fail');
process.exit(failedCommand || failedTest ? 1 : 0);

function collectReports(step, output = '') {
  if (!step.reportType) return [];

  if (step.reportType === 'tap') return parseTap(output, step.name);

  const files = step.reports.flatMap((reportDir) => findXmlFiles(reportDir));
  if (step.reportType === 'junit') {
    return files.flatMap((file) => parseJUnit(file, step.name));
  }
  if (step.reportType === 'trx') {
    return files.flatMap((file) => parseTrx(file, step.name));
  }
  return [];
}

function parseTap(output, service) {
  const tests = [];
  const re = /^(ok|not ok)\s+\d+\s+-\s+(.+?)(?:\s+#\s+(SKIP|TODO)\b.*)?$/gm;

  for (const match of output.matchAll(re)) {
    const directive = (match[3] || '').toLowerCase();
    tests.push({
      title: `${service} ${match[2].trim()}`,
      status: match[1] === 'not ok' ? 'fail' : directive ? 'skip' : 'pass',
    });
  }

  return tests;
}

function findXmlFiles(dir) {
  if (!existsSync(dir)) return [];

  const files = [];
  for (const entry of readdirSync(dir, { withFileTypes: true })) {
    const fullPath = path.join(dir, entry.name);
    if (entry.isDirectory()) {
      files.push(...findXmlFiles(fullPath));
    } else if (entry.name.endsWith('.xml') || entry.name.endsWith('.trx')) {
      files.push(fullPath);
    }
  }
  return files;
}

function parseJUnit(file, service) {
  const xml = readFileSync(file, 'utf-8');
  const tests = [];
  const re = /<testcase\b([^>]*)>([\s\S]*?)<\/testcase>|<testcase\b([^/>]*)\/>/g;

  for (const match of xml.matchAll(re)) {
    const attrs = parseAttrs(match[1] || match[3] || '');
    const body = match[2] || '';
    const name = decodeXml(attrs.name || 'unnamed test');
    const className = decodeXml(attrs.classname || service);
    const status = /<(failure|error)\b/.test(body)
      ? 'fail'
      : /<skipped\b/.test(body)
        ? 'skip'
        : 'pass';

    tests.push({
      title: `${service} ${className} ${name}`,
      status,
    });
  }

  return tests;
}

function parseTrx(file, service) {
  const xml = readFileSync(file, 'utf-8');
  const tests = [];
  const re = /<UnitTestResult\b([^>]*)\/?>/g;

  for (const match of xml.matchAll(re)) {
    const attrs = parseAttrs(match[1] || '');
    const name = decodeXml(attrs.testName || attrs.testId || 'unnamed test');
    const outcome = (attrs.outcome || '').toLowerCase();
    const status = outcome === 'passed'
      ? 'pass'
      : outcome === 'notexecuted' || outcome === 'skipped'
        ? 'skip'
        : 'fail';

    tests.push({
      title: `${service} ${name}`,
      status,
    });
  }

  return tests;
}

function parseAttrs(text) {
  const attrs = {};
  const re = /([\w:.-]+)\s*=\s*"([^"]*)"/g;
  for (const match of text.matchAll(re)) {
    attrs[match[1]] = match[2];
  }
  return attrs;
}

function decodeXml(value) {
  return value
    .replace(/&quot;/g, '"')
    .replace(/&apos;/g, "'")
    .replace(/&lt;/g, '<')
    .replace(/&gt;/g, '>')
    .replace(/&amp;/g, '&');
}

function sanitizeTapTitle(title) {
  return title.replace(/\s+/g, ' ').replace(/#/g, '\\#').trim();
}

function firstLines(output) {
  return (output || '')
    .split(/\r?\n/)
    .map((line) => line.trim())
    .filter(Boolean)
    .slice(0, 30);
}

function localCommand(binDir, commandName) {
  const fullPath = path.join(binDir, commandName);
  return existsSync(fullPath) ? fullPath : null;
}

function localDotnetCommand() {
  const windowsDotnet = 'C:\\Program Files\\dotnet\\dotnet.exe';
  if (process.platform === 'win32' && existsSync(windowsDotnet)) return windowsDotnet;
  return 'dotnet';
}

function findJavaHome() {
  if (process.env.JAVA_HOME && existsSync(process.env.JAVA_HOME)) return process.env.JAVA_HOME;

  const candidates = [
    'C:\\Program Files\\Eclipse Adoptium\\jdk-17.0.19.10-hotspot',
    'C:\\Program Files\\Java\\jdk-17',
  ];

  return candidates.find((candidate) => existsSync(candidate)) || undefined;
}

function toolEnv(pathEntries, extra = {}) {
  const paths = pathEntries.filter(Boolean);
  return compactEnv({
    ...extra,
    PATH: [...paths, process.env.PATH || ''].join(path.delimiter),
  });
}

function dotnetEnv() {
  return compactEnv({
    DOTNET_CLI_HOME: path.join(rootDir, '.local-tools', 'dotnet-home'),
    NUGET_PACKAGES: path.join(rootDir, '.local-tools', 'nuget'),
    APPDATA: path.join(rootDir, '.local-tools', 'appdata'),
    LOCALAPPDATA: path.join(rootDir, '.local-tools', 'localappdata'),
  });
}

function compactEnv(env) {
  return Object.fromEntries(
    Object.entries(env).filter(([, value]) => value !== undefined && value !== null && value !== '')
  );
}

function commandInvocation(command, args) {
  if (process.platform !== 'win32' || !/\.(cmd|bat)$/i.test(command)) {
    return { command, args };
  }

  return {
    command: 'cmd.exe',
    args: ['/d', '/s', '/c', 'call', command, ...args],
  };
}
