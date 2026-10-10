import { readFile, writeFile, rm } from 'node:fs/promises';
const output = new URL('../dist/examen-demo/browser/', import.meta.url);
let html = await readFile(new URL('index.html', output), 'utf8');
html = html.replaceAll('content="/social-preview.jpg"', 'content="https://kokutbon.github.io/ExAMEN/social-preview.jpg"');
await writeFile(new URL('index.html', output), html);
await writeFile(new URL('.nojekyll', output), '');
await rm(new URL('sitemap.xml', output), { force: true });
await writeFile(new URL('robots.txt', output), 'User-agent: *\nAllow: /\n');
