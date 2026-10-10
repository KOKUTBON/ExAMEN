import { ChangeDetectionStrategy, Component, Input } from '@angular/core';

/**
 * Vespers icon set: 24px grid, 1.6 stroke, round caps. Every glyph is drawn for Examen,
 * so the five movements, practice areas and UI actions share one visual language.
 */
const ICONS: Record<string, string> = {
  // Brand + practice
  flame:
    '<path d="M12 2.8c.9 2.9 4.6 5 4.6 9.6a4.6 4.6 0 0 1-9.2 0c0-2 .9-3.4 2-4.5.2 1.5 1 2.5 2.1 2.9-.3-2.6-.5-5.2.5-8Z"/><path d="M12 14.2c.9 1 1.5 1.8 1.5 2.8a1.5 1.5 0 0 1-3 0c0-1 .6-1.8 1.5-2.8Z"/>',
  candle:
    '<path d="M12 2.6c1.3 1.6 2.2 2.8 2.2 4.1a2.2 2.2 0 0 1-4.4 0c0-1.3.9-2.5 2.2-4.1Z"/><rect x="9" y="11" width="6" height="9" rx="1"/><path d="M5.5 20.5h13"/>',
  arch: '<path d="M5.5 20.5V10a6.5 6.5 0 0 1 13 0v10.5"/><path d="M3.5 20.5h17"/><path d="M12 9.2c.9 1.1 1.6 2 1.6 3a1.6 1.6 0 0 1-3.2 0c0-1 .7-1.9 1.6-3Z"/>',
  compass:
    '<circle cx="12" cy="12" r="9"/><path d="m15.6 8.4-2.1 5.1-5.1 2.1 2.1-5.1 5.1-2.1Z"/><path d="M12 3v1.6M12 19.4V21M3 12h1.6M19.4 12H21"/>',
  quill:
    '<path d="M20 3.5C12.5 5 7.6 10 5.6 18.4"/><path d="M20 3.5c-.4 7.4-4.8 11.6-11.6 12.3"/><path d="M4.5 20.5 7 17.8"/><path d="M11.2 12.4h4.3"/>',
  checklist:
    '<path d="m3.8 6.6 1.6 1.6 2.8-2.9"/><path d="M11.5 7h8.7"/><path d="m3.8 12.6 1.6 1.6 2.8-2.9"/><path d="M11.5 13h8.7"/><circle cx="6" cy="18.6" r="1.4"/><path d="M11.5 19h8.7"/>',
  book: '<path d="M3 5.4c3.1-1.1 6.1-.8 9 1.1 2.9-1.9 5.9-2.2 9-1.1v13.2c-3.1-1.1-6.1-.8-9 1.1-2.9-1.9-5.9-2.2-9-1.1V5.4Z"/><path d="M12 6.5v13.2"/>',
  sunrise:
    '<path d="M2.5 17.5h19"/><path d="M6.5 17.5a5.5 5.5 0 0 1 11 0"/><path d="M12 4v4M4.6 9.3l1.9 1.3M19.4 9.3l-1.9 1.3"/><path d="m9.5 6.3 2.5-2.3 2.5 2.3"/><path d="M8 21h8"/>',
  sliders:
    '<path d="M4 7h9M18 7h2"/><circle cx="15.5" cy="7" r="2.3"/><path d="M4 17h3M12 17h8"/><circle cx="9.5" cy="17" r="2.3"/>',
  layers:
    '<path d="m12 3.5 9 4.8-9 4.8-9-4.8 9-4.8Z"/><path d="m3 12.4 9 4.8 9-4.8"/><path d="m3 16.4 9 4.8 9-4.8"/>',
  // Five movements
  heart:
    '<path d="M12 20s-7.5-4.6-7.5-10.3A4.2 4.2 0 0 1 12 7.2a4.2 4.2 0 0 1 7.5 2.5C19.5 15.4 12 20 12 20Z"/>',
  light:
    '<circle cx="12" cy="12" r="3.6"/><path d="M12 2.8v2.4M12 18.8v2.4M2.8 12h2.4M18.8 12h2.4M5.5 5.5l1.7 1.7M16.8 16.8l1.7 1.7M5.5 18.5l1.7-1.7M16.8 7.2l1.7-1.7"/>',
  eye: '<path d="M2.5 12S6 5.5 12 5.5 21.5 12 21.5 12 18 18.5 12 18.5 2.5 12 2.5 12Z"/><circle cx="12" cy="12" r="3"/>',
  droplet: '<path d="M12 3.2s6.2 6.6 6.2 11.1a6.2 6.2 0 0 1-12.4 0C5.8 9.8 12 3.2 12 3.2Z"/><path d="M9.2 14.6a2.9 2.9 0 0 0 2.6 2.7"/>',
  sprout:
    '<path d="M12 21v-8.6"/><path d="M12 12.4c0-4.3 3-7.4 8-7.4 0 5.1-3.2 7.4-8 7.4Z"/><path d="M12 14.8c0-3.3-2.5-5.6-7-5.6 0 3.9 2.7 5.6 7 5.6Z"/><path d="M8 21h8"/>',
  // Insight + growth
  sparkle:
    '<path d="m11 3 1.9 5.1L18 10l-5.1 1.9L11 17l-1.9-5.1L4 10l5.1-1.9L11 3Z"/><path d="m18.5 15 .8 2 2 .8-2 .8-.8 2-.8-2-2-.8 2-.8.8-2Z"/>',
  lightbulb:
    '<path d="M9 18h6M10 21h4"/><path d="M12 3a6 6 0 0 0-3.6 10.8c.7.6 1.1 1.3 1.1 2.2h5c0-.9.4-1.6 1.1-2.2A6 6 0 0 0 12 3Z"/>',
  'trend-up': '<path d="m3 17 6-6 4 4 8-8"/><path d="M15 7h6v6"/>',
  chart: '<path d="M3.5 20.5h17"/><rect x="5" y="11" width="3" height="7" rx="1"/><rect x="10.5" y="5" width="3" height="13" rx="1"/><rect x="16" y="8.5" width="3" height="9.5" rx="1"/>',
  pulse: '<path d="M2.5 12h4l2.2-6 4.6 12 2.2-6h6"/>',
  target: '<circle cx="12" cy="12" r="9"/><circle cx="12" cy="12" r="5"/><circle cx="12" cy="12" r="1.2"/>',
  star: '<path d="m12 3.6 2.6 5.3 5.9.9-4.3 4.1 1 5.8L12 17l-5.2 2.7 1-5.8-4.3-4.1 5.9-.9L12 3.6Z"/>',
  smile: '<circle cx="12" cy="12" r="9"/><path d="M8.5 14.3a4.5 4.5 0 0 0 7 0"/><path d="M9 9.5h.01M15 9.5h.01"/>',
  'moon-stars':
    '<path d="M19.5 14.6A7.8 7.8 0 0 1 9.4 4.5a7.8 7.8 0 1 0 10.1 10.1Z"/><path d="m17 3 .6 1.4L19 5l-1.4.6L17 7l-.6-1.4L15 5l1.4-.6L17 3Z"/>',
  quote:
    '<path d="M9.5 7.5H6.3A1.8 1.8 0 0 0 4.5 9.3v3.2h5V7.5Z"/><path d="M9.5 12.5c0 2.6-1.5 4.2-4 4.8"/><path d="M19.5 7.5h-3.2a1.8 1.8 0 0 0-1.8 1.8v3.2h5V7.5Z"/><path d="M19.5 12.5c0 2.6-1.5 4.2-4 4.8"/>',
  // Theme
  sun: '<circle cx="12" cy="12" r="4"/><path d="M12 2.5v2M12 19.5v2M2.5 12h2M19.5 12h2M5.3 5.3l1.4 1.4M17.3 17.3l1.4 1.4M5.3 18.7l1.4-1.4M17.3 6.7l1.4-1.4"/>',
  moon: '<path d="M20 14.5A8 8 0 0 1 9.5 4a8 8 0 1 0 10.5 10.5Z"/>',
  monitor: '<rect x="3" y="4" width="18" height="12" rx="2"/><path d="M8 20.5h8M12 16v4.5"/>',
  // Actions
  plus: '<path d="M12 5v14M5 12h14"/>',
  minus: '<path d="M5 12h14"/>',
  check: '<path d="m5 12.5 4.5 4.5L19 7.5"/>',
  'check-circle': '<circle cx="12" cy="12" r="9"/><path d="m8.3 12.3 2.5 2.5 4.9-5"/>',
  circle: '<circle cx="12" cy="12" r="9"/>',
  x: '<path d="M6 6l12 12M18 6 6 18"/>',
  trash:
    '<path d="M4 7h16"/><path d="M9.5 7V4.5h5V7"/><path d="m6 7 1 12.5a1.5 1.5 0 0 0 1.5 1.5h7a1.5 1.5 0 0 0 1.5-1.5L18 7"/><path d="M10 11v6M14 11v6"/>',
  pencil: '<path d="M4 20l1.1-4.2L15.8 5.1a2.1 2.1 0 0 1 3 3L8.2 18.9 4 20Z"/><path d="m13.8 7.1 3 3"/>',
  'chevron-right': '<path d="m9 5 7 7-7 7"/>',
  'chevron-left': '<path d="m15 5-7 7 7 7"/>',
  'chevron-down': '<path d="m5 9 7 7 7-7"/>',
  'chevron-up': '<path d="m5 15 7-7 7 7"/>',
  'arrow-right': '<path d="M4.5 12h15"/><path d="m13 5.5 6.5 6.5-6.5 6.5"/>',
  'arrow-left': '<path d="M19.5 12h-15"/><path d="M11 5.5 4.5 12l6.5 6.5"/>',
  'arrow-up-right': '<path d="M7 17 17 7"/><path d="M8 7h9v9"/>',
  logout: '<path d="M9.5 20.5H6a2 2 0 0 1-2-2v-13a2 2 0 0 1 2-2h3.5"/><path d="m15.5 16.5 4.5-4.5-4.5-4.5"/><path d="M20 12H9.5"/>',
  login: '<path d="M14.5 3.5H18a2 2 0 0 1 2 2v13a2 2 0 0 1-2 2h-3.5"/><path d="m9 16.5 4.5-4.5L9 7.5"/><path d="M13.5 12H3.5"/>',
  refresh:
    '<path d="M20 11a8 8 0 0 0-14.6-4.5"/><path d="M4.5 3v4h4"/><path d="M4 13a8 8 0 0 0 14.6 4.5"/><path d="M19.5 21v-4h-4"/>',
  download: '<path d="M12 3.5v11.5"/><path d="m7 10 5 5 5-5"/><path d="M4.5 20h15"/>',
  upload: '<path d="M12 15.5V4"/><path d="m7 9 5-5 5 5"/><path d="M4.5 20h15"/>',
  send: '<path d="m21 3-8.5 18-2.3-7.7L2.5 11 21 3Z"/><path d="m10.2 13.3 4.3-4.3"/>',
  search: '<circle cx="11" cy="11" r="7"/><path d="m20.5 20.5-4.5-4.5"/>',
  filter: '<path d="M4 5h16l-6.2 7.6v5.9l-3.6 1.8v-7.7L4 5Z"/>',
  menu: '<path d="M4 7h16M4 12h16M4 17h10"/>',
  more: '<circle cx="5.5" cy="12" r="1.2"/><circle cx="12" cy="12" r="1.2"/><circle cx="18.5" cy="12" r="1.2"/>',
  play: '<path d="M7.5 4.8v14.4a.8.8 0 0 0 1.2.7l11.3-7.2a.8.8 0 0 0 0-1.4L8.7 4.1a.8.8 0 0 0-1.2.7Z"/>',
  save: '<path d="M5 3.5h11l3.5 3.5v12a1.5 1.5 0 0 1-1.5 1.5H5A1.5 1.5 0 0 1 3.5 19V5A1.5 1.5 0 0 1 5 3.5Z"/><path d="M7.5 3.5v5h8v-5"/><rect x="7" y="13" width="10" height="7.5" rx="1"/>',
  bookmark: '<path d="M6.5 3.5h11v17l-5.5-4-5.5 4v-17Z"/>',
  'thumb-up':
    '<path d="M7 10.5v10H4.5a1 1 0 0 1-1-1v-8a1 1 0 0 1 1-1H7Z"/><path d="M7 10.5 11 3a2.6 2.6 0 0 1 2.6 3l-.6 3.5h5.6a2 2 0 0 1 2 2.4l-1.4 7a2 2 0 0 1-2 1.6H7"/>',
  'thumb-down':
    '<path d="M17 13.5v-10h2.5a1 1 0 0 1 1 1v8a1 1 0 0 1-1 1H17Z"/><path d="M17 13.5 13 21a2.6 2.6 0 0 1-2.6-3l.6-3.5H5.4a2 2 0 0 1-2-2.4l1.4-7a2 2 0 0 1 2-1.6H17"/>',
  // Objects + status
  user: '<circle cx="12" cy="8" r="4"/><path d="M4 21a8 8 0 0 1 16 0"/>',
  mail: '<rect x="3" y="5" width="18" height="14" rx="2"/><path d="m3.6 6.6 8.4 6.4 8.4-6.4"/>',
  lock: '<rect x="4.5" y="10.5" width="15" height="10.5" rx="2"/><path d="M8 10.5V7a4 4 0 0 1 8 0v3.5"/><path d="M12 14.5v2.5"/>',
  key: '<circle cx="8" cy="15" r="4.5"/><path d="m11.2 11.8 8.8-8.8"/><path d="m17 6 3 3M14.5 8.5l2 2"/>',
  bell: '<path d="M6 16.5V11a6 6 0 0 1 12 0v5.5l1.8 1.8H4.2L6 16.5Z"/><path d="M10 21h4"/>',
  calendar: '<rect x="3.5" y="5" width="17" height="16" rx="2"/><path d="M3.5 10h17M8 3v4M16 3v4"/>',
  clock: '<circle cx="12" cy="12" r="9"/><path d="M12 7v5l3.2 2"/>',
  hourglass: '<path d="M6.5 3.5h11M6.5 20.5h11"/><path d="M7.5 3.5c0 4.5 4.5 5.5 4.5 8.5s-4.5 4-4.5 8.5"/><path d="M16.5 3.5c0 4.5-4.5 5.5-4.5 8.5s4.5 4 4.5 8.5"/>',
  shield: '<path d="M12 3 19.5 6v6c0 4.6-3.2 8.2-7.5 9-4.3-.8-7.5-4.4-7.5-9V6L12 3Z"/><path d="m9 12 2.2 2.2 4-4.2"/>',
  database:
    '<ellipse cx="12" cy="5.5" rx="7.5" ry="2.8"/><path d="M4.5 5.5v13c0 1.5 3.4 2.8 7.5 2.8s7.5-1.3 7.5-2.8v-13"/><path d="M4.5 12c0 1.5 3.4 2.8 7.5 2.8s7.5-1.3 7.5-2.8"/>',
  code: '<path d="m8.5 7-5 5 5 5M15.5 7l5 5-5 5"/><path d="m13.5 4.5-3 15"/>',
  info: '<circle cx="12" cy="12" r="9"/><path d="M12 11v5.5M12 7.8h.01"/>',
  alert: '<path d="M12 3.5 21.5 20h-19L12 3.5Z"/><path d="M12 10v4.5M12 17.2h.01"/>',
  folder: '<path d="M3 7.2a2 2 0 0 1 2-2h4l2 2.2h8a2 2 0 0 1 2 2V17a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2V7.2Z"/>',
  list: '<path d="M9 6.5h11M9 12h11M9 17.5h11"/><path d="M4.5 6.5h.01M4.5 12h.01M4.5 17.5h.01"/>',
  scale: '<path d="M12 3.5v17M7.5 20.5h9"/><path d="M4 7.5h16"/><path d="m6.5 7.5-3 6.5a3 3 0 0 0 6 0l-3-6.5Z"/><path d="m17.5 7.5-3 6.5a3 3 0 0 0 6 0l-3-6.5Z"/>',
  toggle: '<rect x="2.5" y="7" width="19" height="10" rx="5"/><circle cx="16.5" cy="12" r="2.5"/>',
  hash: '<path d="M5 9h15M4 15h15M10 3.5 8 20.5M16 3.5l-2 17"/>',
  gift: '<rect x="3.5" y="8" width="17" height="4.5" rx="1"/><path d="M5 12.5V20a1 1 0 0 0 1 1h12a1 1 0 0 0 1-1v-7.5"/><path d="M12 8v13"/><path d="M12 8c-1.8-3.8-5.8-3.8-5.8-1.6S9.5 8 12 8Zm0 0c1.8-3.8 5.8-3.8 5.8-1.6S14.5 8 12 8Z"/>',
  wand: '<path d="m4 20 11-11"/><path d="m13.5 7.5 3 3"/><path d="M18 3v3M16.5 4.5h3M20 9.5v2M19 10.5h2M9 3v2M8 4h2"/>',
  history: '<path d="M3.5 12a8.5 8.5 0 1 0 2.5-6"/><path d="M3.5 3.5V8H8"/><path d="M12 7.5V12l3 2"/>',
  home: '<path d="M3.5 11 12 3.8l8.5 7.2"/><path d="M5.5 9.5V20h13V9.5"/><path d="M10 20v-5.5h4V20"/>',
  globe: '<circle cx="12" cy="12" r="9"/><path d="M3 12h18"/><path d="M12 3c2.5 2.6 3.7 5.6 3.7 9S14.5 18.4 12 21c-2.5-2.6-3.7-5.6-3.7-9S9.5 5.6 12 3Z"/>',
  'cloud-off': '<path d="M8 7.2A5.5 5.5 0 0 1 17.4 10H18a3.5 3.5 0 0 1 2 6.4"/><path d="M16 18.5H7a4 4 0 0 1-1.2-7.8"/><path d="m3.5 3.5 17 17"/>',
  github:
    '<path d="M9 19.5c-4.3 1.3-4.3-2.2-6-2.6m12 5.1v-3.4a3 3 0 0 0-.8-2.3c2.8-.3 5.8-1.4 5.8-6.2a4.8 4.8 0 0 0-1.3-3.3 4.5 4.5 0 0 0-.1-3.3s-1-.3-3.4 1.3a11.6 11.6 0 0 0-6 0C6.8 3.2 5.8 3.5 5.8 3.5a4.5 4.5 0 0 0-.1 3.3 4.8 4.8 0 0 0-1.3 3.3c0 4.8 3 5.9 5.8 6.2A3 3 0 0 0 9.4 18.7V22"/>'
};

export type IconName = keyof typeof ICONS | string;

type Shape = { tag: 'path' | 'circle' | 'rect' | 'ellipse'; a: Record<string, string> };

/** Parses each glyph once into plain shape data so it renders identically in SSR and the browser. */
const SHAPES = new Map<string, Shape[]>();
function shapesFor(name: string): Shape[] {
  const key = name in ICONS ? name : 'circle';
  let shapes = SHAPES.get(key);
  if (!shapes) {
    shapes = [...ICONS[key].matchAll(/<(path|circle|rect|ellipse)\s([^>]*?)\/>/g)].map(match => ({
      tag: match[1] as Shape['tag'],
      a: Object.fromEntries([...match[2].matchAll(/([\w-]+)="([^"]*)"/g)].map(attr => [attr[1], attr[2]]))
    }));
    SHAPES.set(key, shapes);
  }
  return shapes;
}

@Component({
  selector: 'app-icon',
  standalone: true,
  changeDetection: ChangeDetectionStrategy.OnPush,
  host: {
    class: 'ex-icon',
    '[attr.aria-hidden]': 'label ? null : "true"',
    '[attr.role]': 'label ? "img" : null',
    '[attr.aria-label]': 'label || null',
    '[style.width.px]': 'size',
    '[style.height.px]': 'size'
  },
  template: `<svg
    viewBox="0 0 24 24"
    fill="none"
    stroke="currentColor"
    [attr.stroke-width]="stroke"
    stroke-linecap="round"
    stroke-linejoin="round"
    focusable="false"
  >
    @for (s of shapes; track $index) {
      @switch (s.tag) {
        @case ('path') { <svg:path [attr.d]="s.a['d']" /> }
        @case ('circle') { <svg:circle [attr.cx]="s.a['cx']" [attr.cy]="s.a['cy']" [attr.r]="s.a['r']" /> }
        @case ('rect') {
          <svg:rect [attr.x]="s.a['x']" [attr.y]="s.a['y']" [attr.width]="s.a['width']" [attr.height]="s.a['height']" [attr.rx]="s.a['rx']" />
        }
        @case ('ellipse') { <svg:ellipse [attr.cx]="s.a['cx']" [attr.cy]="s.a['cy']" [attr.rx]="s.a['rx']" [attr.ry]="s.a['ry']" /> }
      }
    }
  </svg>`,
  styles: `
    :host {
      display: inline-flex;
      flex-shrink: 0;
      line-height: 0;
      vertical-align: middle;
    }
    svg {
      width: 100%;
      height: 100%;
      overflow: visible;
    }
  `
})
export class IconComponent {
  @Input() size = 20;
  @Input() stroke = 1.6;
  @Input() label = '';

  shapes: Shape[] = shapesFor('circle');

  @Input({ required: true })
  set name(value: IconName) {
    this.shapes = shapesFor(value);
  }
}
