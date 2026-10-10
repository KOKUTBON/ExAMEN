import { writeFileSync } from 'node:fs';
import { resolve } from 'node:path';

const baseUrl = (process.env.APP_PUBLIC_URL || 'http://localhost:4200').replace(/\/+$/, '');

const routes = [
  { path: '/', changefreq: 'daily', priority: '1.0' }
];

const now = new Date().toISOString();

const xml = `<?xml version="1.0" encoding="UTF-8"?>\n<urlset xmlns="http://www.sitemaps.org/schemas/sitemap/0.9">\n${routes
  .map(
    (route) =>
      `  <url>\n    <loc>${baseUrl}${route.path === '/' ? '' : route.path}</loc>\n    <lastmod>${now}</lastmod>\n    <changefreq>${route.changefreq}</changefreq>\n    <priority>${route.priority}</priority>\n  </url>`
  )
  .join('\n')}\n</urlset>\n`;

const robots = `User-agent: *\nAllow: /\n\nSitemap: ${baseUrl}/sitemap.xml\n`;

const publicDir = resolve(import.meta.dirname, '../public');
writeFileSync(resolve(publicDir, 'sitemap.xml'), xml, 'utf8');
writeFileSync(resolve(publicDir, 'robots.txt'), robots, 'utf8');

console.log(`Generated sitemap and robots for ${baseUrl}`);
