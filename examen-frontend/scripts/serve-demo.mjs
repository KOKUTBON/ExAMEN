import { createServer } from 'node:http';
import { readFile } from 'node:fs/promises';
import { resolve, extname, sep } from 'node:path';
import { fileURLToPath } from 'node:url';
const root = fileURLToPath(new URL('../dist/examen-demo/browser/', import.meta.url));
const types = { '.html': 'text/html', '.js': 'text/javascript', '.css': 'text/css', '.svg': 'image/svg+xml', '.png': 'image/png', '.txt': 'text/plain' };
createServer(async (request, response) => {
  const url = new URL(request.url, 'http://localhost');
  if (url.pathname === '/' || url.pathname === '/ExAMEN') {
    response.writeHead(302, { Location: '/ExAMEN/' }); response.end(); return;
  }
  if (!url.pathname.startsWith('/ExAMEN/')) { response.writeHead(404); response.end(); return; }
  try {
    const path = resolve(root, decodeURIComponent(url.pathname.slice('/ExAMEN/'.length)) || 'index.html');
    if (!path.startsWith(root.endsWith(sep) ? root : root + sep)) throw new Error('Invalid path');
    const data = await readFile(path);
    response.writeHead(200, { 'Content-Type': types[extname(path)] || 'application/octet-stream' }); response.end(data);
  } catch { response.writeHead(404); response.end('Not found'); }
}).listen(4300, '127.0.0.1', () => console.log('Demo preview: http://127.0.0.1:4300/ExAMEN/'));
