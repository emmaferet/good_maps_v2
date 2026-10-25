const { spawn } = require('child_process');
const path = require('path');

const isWin = process.platform === 'win32';
const executable = isWin ? 'mvnw.cmd' : './mvnw';
const backendDir = path.resolve(__dirname, '..', 'backend');
const args = process.argv.slice(2);

const child = spawn(executable, args, {
  cwd: backendDir,
  stdio: 'inherit',
  shell: true
});

child.on('exit', (code) => {
  process.exit(code || 0);
});

child.on('error', (err) => {
  console.error(`Erreur d'exécution de Maven Wrapper (${executable}):`, err);
  process.exit(1);
});
