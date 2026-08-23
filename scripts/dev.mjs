#!/usr/bin/env node

/**
 * RecruitFlow local runner. It intentionally does not read .env files or persist credentials.
 * Database secrets are inherited only by the short-lived mysql/Tomcat child processes.
 */
import fs from 'node:fs';
import net from 'node:net';
import os from 'node:os';
import path from 'node:path';
import process from 'node:process';
import { fileURLToPath } from 'node:url';
import { spawn, spawnSync } from 'node:child_process';

const SCRIPT_DIRECTORY = path.dirname(fileURLToPath(import.meta.url));
const PROJECT_ROOT = path.resolve(SCRIPT_DIRECTORY, '..');
const SAFE_MIGRATION_MARKER = 'RECRUITFLOW_SAFE_ADDITIVE_MIGRATION';
const APPROVED_DEV_MIGRATION_MARKER = 'RECRUITFLOW_APPROVED_DEV_MIGRATION';
const argumentsSet = new Set(process.argv.slice(2));

function fail(message) {
    console.error(`\n[RecruitFlow dev] ${message}`);
    process.exitCode = 1;
    throw new Error(message);
}

function hasFlag(flag) {
    return argumentsSet.has(flag);
}

function printHelp() {
    console.log(`
RecruitFlow local developer runner

Usage:
  npm run dev                 Build, apply safe additive migrations, deploy and run Tomcat
  npm run dev:migrate         Apply safe additive migrations only
  npm run dev:check           Validate local tools/configuration without changing DB or Tomcat

Optional flags:
  --no-migrate                Do not apply migrations during npm run dev
  --skip-build                Deploy the existing target/recruitflow-1.0-SNAPSHOT.war

Configuration priority: environment variable > dev.config.json > default.
Secrets are never read from dev.config.json. Set RECRUITFLOW_DB_PASSWORD for non-interactive use,
or the runner securely prompts for it on an interactive terminal.

By default the runner creates .recruitflow/tomcat-base and deploys only RecruitFlow there.
This keeps old WAR backups in the Tomcat installation from slowing down each development start.
`);
}

function nonBlank(value) {
    return typeof value === 'string' && value.trim().length > 0;
}

function readLocalConfig() {
    const candidates = [
        path.join(PROJECT_ROOT, 'dev.config.json'),
        path.join(PROJECT_ROOT, '.recruitflow', 'dev.config.json')
    ];
    for (const candidate of candidates) {
        if (!fs.existsSync(candidate)) {
            continue;
        }
        try {
            const parsed = JSON.parse(fs.readFileSync(candidate, 'utf8'));
            if (parsed === null || Array.isArray(parsed) || typeof parsed !== 'object') {
                fail(`${path.basename(candidate)} phải là JSON object.`);
            }
            for (const key of Object.keys(parsed)) {
                if (/password|secret|token/i.test(key)) {
                    fail(`${path.basename(candidate)} không được chứa secret (${key}). Dùng biến môi trường hoặc prompt bảo mật.`);
                }
            }
            return parsed;
        } catch (error) {
            if (error instanceof SyntaxError) {
                fail(`Không đọc được ${candidate}: JSON không hợp lệ.`);
            }
            throw error;
        }
    }
    return {};
}

function setting(config, environmentName, configName, fallback = undefined) {
    if (nonBlank(process.env[environmentName])) {
        return process.env[environmentName].trim();
    }
    if (config[configName] !== undefined && config[configName] !== null && String(config[configName]).trim() !== '') {
        return String(config[configName]).trim();
    }
    return fallback;
}

function parseInteger(value, label) {
    const number = Number.parseInt(String(value), 10);
    if (!Number.isInteger(number) || number < 1 || number > 65535) {
        fail(`${label} phải là port hợp lệ.`);
    }
    return number;
}

function parseJdbcUrl(jdbcUrl) {
    if (!nonBlank(jdbcUrl)) {
        fail('Thiếu RECRUITFLOW_DB_URL.');
    }
    const match = /^jdbc:mysql:\/\/([^\/?#:]+|\[[^\]]+\])(?::(\d+))?\/([^?;\/]+)(?:[?;].*)?$/i.exec(jdbcUrl.trim());
    if (!match) {
        fail('RECRUITFLOW_DB_URL phải theo dạng jdbc:mysql://host:3306/database?... và không chứa credential.');
    }
    const host = match[1].replace(/^\[|\]$/g, '');
    const port = match[2] ? parseInteger(match[2], 'MySQL port') : 3306;
    const database = match[3];
    if (!/^[A-Za-z0-9_$-]+$/.test(database)) {
        fail('Tên database trong JDBC URL chứa ký tự không được dev runner hỗ trợ.');
    }
    return { host, port, database };
}

function isLocalHost(host) {
    const normalized = host.toLowerCase();
    return normalized === 'localhost' || normalized === '127.0.0.1' || normalized === '::1';
}

function commandResult(command, args, options = {}) {
    let executable = command;
    let executableArgs = args;
    // Windows cannot reliably execute a .cmd/.bat file through spawnSync without cmd.exe.
    // All arguments here are controlled by the runner; reject command metacharacters as a
    // defense-in-depth measure before composing the tiny command string.
    if (process.platform === 'win32' && /\.(?:cmd|bat)$/i.test(command)) {
        // PowerShell's call operator handles paths with spaces without putting arguments through
        // cmd.exe parsing. Pass both values as environment data, not interpolated script text.
        executable = 'powershell.exe';
        executableArgs = [
            '-NoLogo', '-NoProfile', '-Command',
            '$tool = $env:RECRUITFLOW_DEV_RUNNER_COMMAND; '
                + '$toolArgs = ConvertFrom-Json $env:RECRUITFLOW_DEV_RUNNER_ARGS; '
                + '& $tool @toolArgs; exit $LASTEXITCODE'
        ];
        options = {
            ...options,
            env: {
                ...(options.env ?? process.env),
                RECRUITFLOW_DEV_RUNNER_COMMAND: command,
                RECRUITFLOW_DEV_RUNNER_ARGS: JSON.stringify(args)
            }
        };
    }
    return spawnSync(executable, executableArgs, {
        cwd: options.cwd ?? PROJECT_ROOT,
        env: options.env ?? process.env,
        encoding: 'utf8',
        input: options.input,
        stdio: options.stdio ?? 'pipe',
        windowsHide: options.windowsHide ?? false
    });
}

function commandAvailable(command) {
    const result = commandResult(command, ['--version']);
    return !result.error && result.status === 0;
}

function locateMaven() {
    const executable = process.platform === 'win32' ? 'mvn.cmd' : 'mvn';
    const candidates = [
        process.env.RECRUITFLOW_MAVEN_BIN,
        executable,
        process.env.MAVEN_HOME ? path.join(process.env.MAVEN_HOME, 'bin', executable) : undefined,
        process.env.M2_HOME ? path.join(process.env.M2_HOME, 'bin', executable) : undefined,
        process.platform === 'win32'
            ? 'C:\\Program Files\\Apache\\apache-maven-3.9.16-bin\\apache-maven-3.9.16\\bin\\mvn.cmd'
            : undefined
    ].filter(Boolean);
    for (const candidate of candidates) {
        if (commandAvailable(candidate)) {
            return candidate;
        }
    }
    fail('Không tìm thấy Maven. Cài Maven 3.8+ hoặc đặt MAVEN_HOME/RECRUITFLOW_MAVEN_BIN.');
}

async function securePasswordPrompt() {
    if (Object.prototype.hasOwnProperty.call(process.env, 'RECRUITFLOW_DB_PASSWORD')) {
        return process.env.RECRUITFLOW_DB_PASSWORD;
    }
    if (!process.stdin.isTTY || typeof process.stdin.setRawMode !== 'function') {
        fail('Thiếu RECRUITFLOW_DB_PASSWORD và terminal không thể hiển thị prompt bảo mật.');
    }
    // Raw terminal input is intentionally handled in Node rather than by a nested PowerShell
    // process. The latter loses its console in several IDE/PTY environments. Raw mode prevents
    // echoing the password and works the same on Windows, macOS and Linux.
    process.stderr.write('MySQL password (không lưu): ');
    return new Promise((resolve, reject) => {
        let password = '';
        const stdin = process.stdin;
        const restore = () => {
            stdin.off('data', onData);
            stdin.setRawMode(false);
            stdin.pause();
        };
        const finish = value => {
            restore();
            process.stderr.write('\n');
            resolve(value);
        };
        const cancel = () => {
            restore();
            process.stderr.write('\n');
            reject(new Error('Đã hủy nhập mật khẩu MySQL.'));
        };
        const onData = chunk => {
            const characters = String(chunk);
            for (const character of characters) {
                if (character === '\r' || character === '\n') {
                    finish(password);
                    return;
                }
                if (character === '\u0003') {
                    cancel();
                    return;
                }
                if (character === '\b' || character === '\u007f') {
                    password = password.slice(0, -1);
                    continue;
                }
                // Ignore terminal escape sequences/control characters; credentials are text.
                if (character >= ' ') {
                    password += character;
                }
            }
        };
        stdin.setEncoding('utf8');
        stdin.setRawMode(true);
        stdin.resume();
        stdin.on('data', onData);
    });
}

function locateMysqlBinary(config) {
    const configured = setting(config, 'RECRUITFLOW_MYSQL_BIN', 'mysqlBin');
    const candidates = [
        configured,
        process.platform === 'win32' ? 'mysql.exe' : 'mysql',
        'mysql',
        process.platform === 'win32' ? 'C:\\Program Files\\MySQL\\MySQL Server 8.0\\bin\\mysql.exe' : undefined,
        process.platform === 'win32' ? 'C:\\Program Files\\MySQL\\MySQL Workbench 8.0\\mysql.exe' : undefined
    ].filter(Boolean);
    for (const candidate of candidates) {
        if (commandAvailable(candidate)) {
            return candidate;
        }
    }
    fail('Không tìm thấy mysql client. Cài MySQL client hoặc đặt RECRUITFLOW_MYSQL_BIN.');
}

async function sleep(milliseconds) {
    await new Promise(resolve => setTimeout(resolve, milliseconds));
}

function ensureLocalMysqlService(config, database) {
    if (process.platform !== 'win32' || !isLocalHost(database.host)
            || process.env.RECRUITFLOW_MYSQL_AUTOSTART === 'false') {
        return;
    }
    const serviceName = setting(config, 'RECRUITFLOW_MYSQL_SERVICE', 'mysqlService', 'MySQL80');
    const query = commandResult('sc.exe', ['query', serviceName]);
    if (query.error || query.status !== 0) {
        // MySQL may be managed by Docker, XAMPP, a different service name, or a remote host.
        return;
    }
    if (/\bRUNNING\b/i.test(query.stdout ?? '')) {
        return;
    }
    console.log(`[RecruitFlow dev] Khởi động MySQL service ${serviceName}…`);
    const started = commandResult('sc.exe', ['start', serviceName], { stdio: 'inherit' });
    if (started.error || started.status !== 0) {
        fail(`Không thể khởi động MySQL service ${serviceName}. Chạy service bằng quyền phù hợp hoặc đặt RECRUITFLOW_MYSQL_AUTOSTART=false.`);
    }
}

function mysqlArguments(database, user, includeDatabase = true) {
    const args = [
        '--protocol=TCP',
        `--host=${database.host}`,
        `--port=${database.port}`,
        `--user=${user}`,
        '--default-character-set=utf8mb4',
        '--connect-timeout=3'
    ];
    if (includeDatabase) {
        args.push(`--database=${database.database}`);
    }
    return args;
}

function mysqlEnvironment(password) {
    const environment = { ...process.env };
    if (password.length > 0) {
        environment.MYSQL_PWD = password;
    } else {
        delete environment.MYSQL_PWD;
    }
    return environment;
}

async function verifyMysql(mysqlBinary, database, user, password) {
    let latestOutput = '';
    for (let attempt = 0; attempt < 10; attempt += 1) {
        const result = commandResult(mysqlBinary, [...mysqlArguments(database, user), '--batch', '--skip-column-names', '-e', 'SELECT 1'], {
            env: mysqlEnvironment(password)
        });
        if (!result.error && result.status === 0) {
            return;
        }
        latestOutput = `${result.stderr ?? ''}${result.stdout ?? ''}`.trim();
        await sleep(1_000);
    }
    fail(`Không kết nối được MySQL database ${database.database} tại ${database.host}:${database.port}. ${latestOutput || 'Kiểm tra URL, user, password và service MySQL.'}`);
}

function migrationFiles() {
    const migrationDirectory = path.join(PROJECT_ROOT, 'db', 'migrations');
    if (!fs.existsSync(migrationDirectory)) {
        return [];
    }
    return fs.readdirSync(migrationDirectory, { withFileTypes: true })
        .filter(entry => entry.isFile() && entry.name.toLowerCase().endsWith('.sql'))
        .map(entry => path.join(migrationDirectory, entry.name))
        .sort((left, right) => path.basename(left).localeCompare(path.basename(right)));
}

function isSafeAdditiveMigration(content) {
    const safeAdditive = content.includes(SAFE_MIGRATION_MARKER);
    const approvedIdempotent = content.includes(APPROVED_DEV_MIGRATION_MARKER);
    if (!safeAdditive && !approvedIdempotent) {
        return false;
    }
    // Approved idempotent migrations are reviewed repository files. They may INSERT IGNORE starter
    // metadata or use a temporary stored procedure, but no migration run by this runner may delete
    // application data or drop a table.
    const destructive = /\b(?:DROP\s+TABLE|TRUNCATE(?:\s+TABLE)?|DELETE\s+FROM|REPLACE\s+INTO)\b/i.test(content);
    // Match an UPDATE statement, not column clauses such as `ON UPDATE CURRENT_TIMESTAMP`.
    const altersExistingData = /^\s*UPDATE\s+(?!jobs\b)/im.test(content);
    return !destructive
            && !(safeAdditive && /\b(?:UPDATE\s+\w+\s+SET|INSERT\s+(?:IGNORE\s+)?INTO)\b/i.test(content))
            && !(approvedIdempotent && altersExistingData)
            && !/\bUSE\s+[`A-Za-z0-9_$-]+\s*;/i.test(content);
}

function applySafeMigrations(mysqlBinary, database, user, password) {
    const skipped = [];
    for (const file of migrationFiles()) {
        const content = fs.readFileSync(file, 'utf8');
        if (!isSafeAdditiveMigration(content)) {
            skipped.push(path.basename(file));
            continue;
        }
        console.log(`[RecruitFlow dev] Áp dụng migration bổ sung: ${path.basename(file)}`);
        const result = commandResult(mysqlBinary, mysqlArguments(database, user), {
            env: mysqlEnvironment(password),
            input: content
        });
        if (result.error || result.status !== 0) {
            const detail = `${result.stderr ?? ''}${result.stdout ?? ''}`.trim();
            fail(`Migration ${path.basename(file)} thất bại. ${detail}`);
        }
    }
    if (skipped.length > 0) {
        console.log(`[RecruitFlow dev] Không tự chạy migration không được đánh dấu an toàn: ${skipped.join(', ')}`);
    }
}

function isTomcatHome(candidate) {
    if (!nonBlank(candidate)) {
        return false;
    }
    const catalina = process.platform === 'win32' ? 'catalina.bat' : 'catalina.sh';
    return fs.existsSync(path.join(candidate, 'bin', catalina))
            && fs.existsSync(path.join(candidate, 'webapps'));
}

function tomcatVersionIsSupported(candidate) {
    const releaseNotes = path.join(candidate, 'RELEASE-NOTES');
    if (!fs.existsSync(releaseNotes)) {
        return true;
    }
    const content = fs.readFileSync(releaseNotes, 'utf8');
    return /Apache Tomcat (?:Version )?9\./i.test(content);
}

function directTomcatCandidates(directory) {
    if (!directory || !fs.existsSync(directory)) {
        return [];
    }
    try {
        return fs.readdirSync(directory, { withFileTypes: true })
            .filter(entry => entry.isDirectory() && /(?:apache-)?tomcat-?9/i.test(entry.name))
            .map(entry => path.join(directory, entry.name));
    } catch {
        return [];
    }
}

function nestedTomcatCandidates(directory, remainingDepth) {
    if (!directory || !fs.existsSync(directory) || remainingDepth < 0) {
        return [];
    }
    let entries;
    try {
        entries = fs.readdirSync(directory, { withFileTypes: true });
    } catch {
        return [];
    }
    const found = [];
    for (const entry of entries) {
        if (!entry.isDirectory()) {
            continue;
        }
        const fullPath = path.join(directory, entry.name);
        if (/(?:apache-)?tomcat-?9/i.test(entry.name)) {
            found.push(fullPath);
        } else if (remainingDepth > 0) {
            found.push(...nestedTomcatCandidates(fullPath, remainingDepth - 1));
        }
    }
    return found;
}

function readTomcatHttpPort(tomcatHome) {
    const serverXml = path.join(tomcatHome, 'conf', 'server.xml');
    if (!fs.existsSync(serverXml)) {
        return undefined;
    }
    const content = fs.readFileSync(serverXml, 'utf8');
    const connectorTags = content.match(/<Connector\b[^>]*>/gi) ?? [];
    for (const tag of connectorTags) {
        if (!/HTTP|Http11/i.test(tag)) {
            continue;
        }
        const port = /\bport\s*=\s*"(\d+)"/i.exec(tag)?.[1];
        if (port) {
            return Number.parseInt(port, 10);
        }
    }
    return undefined;
}

/**
 * Build a private CATALINA_BASE for the development runner. A shared Tomcat installation often
 * contains old WAR backups, which Tomcat auto-deploys before RecruitFlow and can make a healthy
 * startup exceed the readiness window. CATALINA_HOME remains the installed Tomcat binary; the
 * private base contains only its copied configuration and the current RecruitFlow WAR.
 */
function ensureTomcatBase(tomcatHome, config) {
    const configured = setting(config, 'RECRUITFLOW_TOMCAT_BASE', 'tomcatBase')
        ?? process.env.CATALINA_BASE;
    const tomcatBase = path.resolve(configured || path.join(PROJECT_ROOT, '.recruitflow', 'tomcat-base'));
    if (tomcatBase === path.resolve(tomcatHome)) {
        return tomcatBase;
    }

    const sourceConf = path.join(tomcatHome, 'conf');
    const targetConf = path.join(tomcatBase, 'conf');
    if (!fs.existsSync(targetConf)) {
        fs.mkdirSync(tomcatBase, { recursive: true });
        fs.cpSync(sourceConf, targetConf, { recursive: true, errorOnExist: false });
    }
    for (const directory of ['bin', 'logs', 'temp', 'webapps', 'work']) {
        fs.mkdirSync(path.join(tomcatBase, directory), { recursive: true });
    }
    // catalina.bat loads tomcat-juli from CATALINA_BASE, not CATALINA_HOME.
    const juliSource = path.join(tomcatHome, 'bin', 'tomcat-juli.jar');
    const juliTarget = path.join(tomcatBase, 'bin', 'tomcat-juli.jar');
    if (fs.existsSync(juliSource) && !fs.existsSync(juliTarget)) {
        fs.copyFileSync(juliSource, juliTarget);
    }
    if (!fs.existsSync(path.join(targetConf, 'server.xml'))) {
        fail(`CATALINA_BASE thiếu conf/server.xml: ${tomcatBase}`);
    }
    return tomcatBase;
}

function startupTimeoutMilliseconds(config) {
    const configured = setting(config, 'RECRUITFLOW_STARTUP_TIMEOUT_SECONDS', 'startupTimeoutSeconds', 90);
    const seconds = Number.parseInt(String(configured), 10);
    if (!Number.isInteger(seconds) || seconds < 15 || seconds > 600) {
        fail('startupTimeoutSeconds phải là số giây từ 15 đến 600.');
    }
    return seconds * 1_000;
}

function locateTomcat(config) {
    const explicit = setting(config, 'RECRUITFLOW_TOMCAT_HOME', 'tomcatHome')
            ?? process.env.CATALINA_HOME;
    if (explicit) {
        if (!isTomcatHome(explicit)) {
            fail(`RECRUITFLOW_TOMCAT_HOME không phải Tomcat hợp lệ: ${explicit}`);
        }
        if (!tomcatVersionIsSupported(explicit)) {
            fail('RecruitFlow dùng javax.servlet nên yêu cầu Tomcat 9.x, không dùng Tomcat 10+.');
        }
        return path.resolve(explicit);
    }

    const home = os.homedir();
    const candidates = [
        ...directTomcatCandidates(path.join(PROJECT_ROOT, '.tomcat')),
        ...directTomcatCandidates(path.join(PROJECT_ROOT, 'tomcat')),
        ...directTomcatCandidates(path.join(home, 'Downloads')),
        ...directTomcatCandidates(path.join(home, 'Documents')),
        ...nestedTomcatCandidates(path.join(home, 'Documents', 'ChatGPT', 'AppNhanSu'), 3),
        ...directTomcatCandidates('C:\\tomcat'),
        ...directTomcatCandidates('C:\\tools')
    ];
    const valid = [...new Set(candidates.map(candidate => path.resolve(candidate)))]
        .filter(isTomcatHome)
        .filter(tomcatVersionIsSupported);
    if (valid.length === 1) {
        return valid[0];
    }
    if (valid.length > 1) {
        const configuredPort = setting(config, 'RECRUITFLOW_HTTP_PORT', 'httpPort');
        if (configuredPort) {
            const match = valid.find(candidate => readTomcatHttpPort(candidate) === parseInteger(configuredPort, 'HTTP port'));
            if (match) {
                return match;
            }
        }
        fail(`Tìm thấy nhiều Tomcat 9: ${valid.join(', ')}. Đặt RECRUITFLOW_TOMCAT_HOME hoặc dev.config.json.`);
    }
    fail('Không tìm thấy Tomcat 9. Đặt RECRUITFLOW_TOMCAT_HOME hoặc sao chép dev.config.example.json thành dev.config.json.');
}

function normalizeContextPath(value) {
    const raw = nonBlank(value) ? value.trim() : '/recruitflow';
    const normalized = `/${raw.replace(/^\/+|\/+$/g, '')}`;
    if (!/^\/[A-Za-z0-9._-]+$/.test(normalized)) {
        fail('contextPath chỉ được chứa một tên context hợp lệ, ví dụ /recruitflow.');
    }
    return normalized;
}

function runTomcatScript(tomcatHome, scriptName, environment, inheritedStdio = 'inherit') {
    const scriptPath = path.join(tomcatHome, 'bin', scriptName);
    if (!fs.existsSync(scriptPath)) {
        fail(`Không tìm thấy ${scriptName} trong ${tomcatHome}.`);
    }
    if (process.platform === 'win32') {
        return commandResult(scriptPath, [], { cwd: tomcatHome, env: environment, stdio: inheritedStdio });
    }
    return commandResult(scriptPath, [], { cwd: tomcatHome, env: environment, stdio: inheritedStdio });
}

function isPortOpen(port) {
    return new Promise(resolve => {
        const socket = net.createConnection({ host: '127.0.0.1', port });
        const done = value => {
            socket.removeAllListeners();
            socket.destroy();
            resolve(value);
        };
        socket.setTimeout(800);
        socket.once('connect', () => done(true));
        socket.once('timeout', () => done(false));
        socket.once('error', () => done(false));
    });
}

async function applicationReachable(applicationUrl) {
    const controller = new AbortController();
    const timeout = setTimeout(() => controller.abort(), 1_500);
    try {
        const response = await fetch(applicationUrl, { redirect: 'manual', signal: controller.signal });
        return response.status >= 200 && response.status < 500;
    } catch {
        return false;
    } finally {
        clearTimeout(timeout);
    }
}

async function waitForApplication(applicationUrl, timeoutMilliseconds) {
    const deadline = Date.now() + timeoutMilliseconds;
    while (Date.now() < deadline) {
        if (await applicationReachable(applicationUrl)) {
            return true;
        }
        await sleep(1_000);
    }
    return false;
}

async function waitForPortToClose(port, timeoutMilliseconds) {
    const deadline = Date.now() + timeoutMilliseconds;
    while (Date.now() < deadline) {
        if (!(await isPortOpen(port))) {
            return true;
        }
        await sleep(500);
    }
    return false;
}

function buildWar(skipBuild) {
    const war = path.join(PROJECT_ROOT, 'target', 'recruitflow-1.0-SNAPSHOT.war');
    if (skipBuild) {
        if (!fs.existsSync(war)) {
            fail('Không có WAR hiện có để deploy; bỏ --skip-build hoặc chạy Maven package.');
        }
        return war;
    }
    const maven = locateMaven();
    console.log('[RecruitFlow dev] Build Maven WAR…');
    // Some Windows launch contexts expose Java's user.home as the drive root even though the
    // interactive user profile is available. Pin Maven's cache to the real profile unless the
    // developer has already selected a repository (for example a corporate cache).
    const existingMavenOptions = process.env.MAVEN_OPTS ?? '';
    const mavenRepositoryOption = /(?:^|\s)-Dmaven\.repo\.local=/.test(existingMavenOptions)
        ? existingMavenOptions
        : `${existingMavenOptions} -Dmaven.repo.local="${path.join(os.homedir(), '.m2', 'repository')}"`.trim();
    const result = commandResult(maven, ['-DskipTests', 'package'], {
        env: {
            ...process.env,
            MAVEN_SKIP_RC: process.env.MAVEN_SKIP_RC ?? '1',
            MAVEN_OPTS: mavenRepositoryOption
        },
        stdio: 'inherit'
    });
    if (result.error || result.status !== 0 || !fs.existsSync(war)) {
        fail('Maven build thất bại. Kiểm tra Java 17, Maven và dependency cache/network.');
    }
    return war;
}

function deployWar(war, tomcatBase, contextPath) {
    const contextName = contextPath.substring(1);
    const destination = path.join(tomcatBase, 'webapps', `${contextName}.war`);
    console.log(`[RecruitFlow dev] Deploy ${path.basename(war)} → ${destination}`);
    fs.copyFileSync(war, destination);
}

function startTomcat(tomcatHome, environment) {
    const catalina = process.platform === 'win32' ? 'catalina.bat' : 'catalina.sh';
    const scriptPath = path.join(tomcatHome, 'bin', catalina);
    if (process.platform !== 'win32') {
        return spawn(scriptPath, ['run'], { cwd: tomcatHome, env: environment, stdio: 'inherit' });
    }
    return spawn('powershell.exe', [
        '-NoLogo', '-NoProfile', '-Command',
        '$tool = $env:RECRUITFLOW_DEV_RUNNER_COMMAND; & $tool run; exit $LASTEXITCODE'
    ], {
        cwd: tomcatHome,
        env: { ...environment, RECRUITFLOW_DEV_RUNNER_COMMAND: scriptPath },
        stdio: 'inherit',
        windowsHide: false
    });
}

function tomcatEnvironment(tomcatHome, tomcatBase, jdbcUrl, databaseUser, password) {
    return {
        ...process.env,
        CATALINA_HOME: tomcatHome,
        CATALINA_BASE: tomcatBase,
        RECRUITFLOW_DB_URL: jdbcUrl,
        RECRUITFLOW_DB_USER: databaseUser,
        RECRUITFLOW_DB_PASSWORD: password
    };
}

async function run() {
    if (hasFlag('--help') || hasFlag('-h')) {
        printHelp();
        return;
    }
    const config = readLocalConfig();
    const tomcatHome = locateTomcat(config);
    const tomcatBase = ensureTomcatBase(tomcatHome, config);
    const discoveredPort = readTomcatHttpPort(tomcatHome) ?? 8080;
    const httpPort = parseInteger(setting(config, 'RECRUITFLOW_HTTP_PORT', 'httpPort', discoveredPort), 'HTTP port');
    const contextPath = normalizeContextPath(setting(config, 'RECRUITFLOW_CONTEXT_PATH', 'contextPath', '/recruitflow'));
    const applicationUrl = `http://127.0.0.1:${httpPort}${contextPath}/home`;

    if (hasFlag('--check')) {
        locateMaven();
        console.log(`[RecruitFlow dev] OK: Tomcat 9 = ${tomcatHome}`);
        console.log(`[RecruitFlow dev] OK: CATALINA_BASE = ${tomcatBase}`);
        console.log(`[RecruitFlow dev] OK: URL = ${applicationUrl}`);
        console.log('[RecruitFlow dev] Check không động vào MySQL, migration hoặc Tomcat.');
        return;
    }

    const jdbcUrl = setting(config, 'RECRUITFLOW_DB_URL', 'databaseUrl',
        'jdbc:mysql://localhost:3306/recruitflow?useSSL=false&serverTimezone=Asia/Bangkok&allowPublicKeyRetrieval=true&characterEncoding=UTF-8');
    const databaseUser = setting(config, 'RECRUITFLOW_DB_USER', 'databaseUser', 'root');
    const database = parseJdbcUrl(jdbcUrl);
    const password = await securePasswordPrompt();
    const mysqlBinary = locateMysqlBinary(config);

    ensureLocalMysqlService(config, database);
    console.log(`[RecruitFlow dev] Kiểm tra MySQL ${database.host}:${database.port}/${database.database}…`);
    await verifyMysql(mysqlBinary, database, databaseUser, password);
    if (!hasFlag('--no-migrate')) {
        applySafeMigrations(mysqlBinary, database, databaseUser, password);
    }
    if (hasFlag('--migrate-only')) {
        console.log('[RecruitFlow dev] Hoàn tất migration.');
        return;
    }

    const tomcatEnv = tomcatEnvironment(tomcatHome, tomcatBase, jdbcUrl, databaseUser, password);
    const alreadyRunning = await applicationReachable(applicationUrl);
    if (alreadyRunning) {
        console.log('[RecruitFlow dev] Dừng Tomcat RecruitFlow hiện có để áp dụng cấu hình và WAR mới…');
        const stopResult = runTomcatScript(tomcatHome,
            process.platform === 'win32' ? 'shutdown.bat' : 'shutdown.sh', tomcatEnv);
        if (stopResult.error || stopResult.status !== 0 || !(await waitForPortToClose(httpPort, 20_000))) {
            fail('Tomcat hiện có không dừng an toàn. Không ghi đè process đang chạy; hãy kiểm tra CATALINA_HOME/port.');
        }
    } else if (await isPortOpen(httpPort)) {
        fail(`Port ${httpPort} đang do process khác sử dụng. Không tự dừng process không xác định.`);
    }

    const war = buildWar(hasFlag('--skip-build'));
    deployWar(war, tomcatBase, contextPath);
    console.log(`[RecruitFlow dev] Khởi động Tomcat tại ${applicationUrl}…`);
    const child = startTomcat(tomcatHome, tomcatEnv);
    const isReady = await waitForApplication(applicationUrl, startupTimeoutMilliseconds(config));
    if (!isReady) {
        runTomcatScript(tomcatHome, process.platform === 'win32' ? 'shutdown.bat' : 'shutdown.sh', tomcatEnv);
        fail('Tomcat không sẵn sàng trong thời gian chờ đã cấu hình. Xem log catalina để biết chi tiết.');
    }
    console.log(`\n[RecruitFlow dev] Đang chạy: ${applicationUrl}`);
    console.log('[RecruitFlow dev] Nhấn Ctrl+C để dừng Tomcat do runner này khởi động.');

    let stopping = false;
    const stopChild = () => {
        if (stopping) {
            return;
        }
        stopping = true;
        console.log('\n[RecruitFlow dev] Đang dừng Tomcat…');
        runTomcatScript(tomcatHome, process.platform === 'win32' ? 'shutdown.bat' : 'shutdown.sh', tomcatEnv);
    };
    process.once('SIGINT', stopChild);
    process.once('SIGTERM', stopChild);
    await new Promise(resolve => child.once('exit', resolve));
}

run().catch(error => {
    if (process.exitCode !== 1) {
        console.error(`\n[RecruitFlow dev] ${error.message}`);
        process.exitCode = 1;
    }
});
