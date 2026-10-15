import { ChangeDetectionStrategy, Component, Input } from '@angular/core';

/** Examen mark: a candle flame kept inside a chapel arch — the evening light of the examen. */
@Component({
  selector: 'app-logo',
  standalone: true,
  changeDetection: ChangeDetectionStrategy.OnPush,
  host: { class: 'ex-logo', '[style.width.px]': 'size', '[style.height.px]': 'size' },
  template: `
    <svg viewBox="0 0 48 48" role="img" aria-label="Examen" [class.animated]="animated">
      <defs>
        <linearGradient [attr.id]="uid + '-flame'" x1="24" y1="11" x2="24" y2="33" gradientUnits="userSpaceOnUse">
          <stop offset="0" stop-color="#FFE3A1" />
          <stop offset=".55" stop-color="#E9A93E" />
          <stop offset="1" stop-color="#C8612F" />
        </linearGradient>
        <radialGradient [attr.id]="uid + '-halo'" cx="24" cy="25" r="16" gradientUnits="userSpaceOnUse">
          <stop offset="0" stop-color="#F2B64F" stop-opacity=".45" />
          <stop offset="1" stop-color="#F2B64F" stop-opacity="0" />
        </radialGradient>
      </defs>
      <rect width="48" height="48" rx="14" class="tile" />
      <circle cx="24" cy="25" r="16" [attr.fill]="'url(#' + uid + '-halo)'" class="halo" />
      <path d="M12.5 38.5V22a11.5 11.5 0 0 1 23 0v16.5" class="arch" />
      <path d="M9.5 38.5h29" class="arch" />
      <path
        class="flame"
        [attr.fill]="'url(#' + uid + '-flame)'"
        d="M24 11.5c2.9 4.1 6.2 6.9 6.2 11.6a6.2 6.2 0 0 1-12.4 0c0-4.7 3.3-7.5 6.2-11.6Z"
      />
      <path d="M24 21.4c1.3 1.6 2.3 2.8 2.3 4.2a2.3 2.3 0 0 1-4.6 0c0-1.4 1-2.6 2.3-4.2Z" class="core" />
      <path d="M24 31v4.4" class="wick" />
    </svg>
  `,
  styles: `
    :host { display: inline-flex; flex-shrink: 0; }
    svg { width: 100%; height: 100%; display: block; }
    .tile { fill: var(--logo-tile, #17182B); }
    .arch { fill: none; stroke: #E6B959; stroke-width: 2.2; stroke-linecap: round; opacity: .9; }
    .core { fill: #FFF6DF; }
    .wick { stroke: #FFF6DF; stroke-width: 1.6; stroke-linecap: round; opacity: .7; }
    .flame { transform-origin: 24px 32px; }
    .animated .flame { animation: flicker 3.6s ease-in-out infinite; }
    .animated .halo { animation: breathe 3.6s ease-in-out infinite; transform-origin: 24px 25px; }
    @keyframes flicker {
      0%, 100% { transform: scale(1, 1) skewX(0deg); }
      30% { transform: scale(.97, 1.04) skewX(-1.5deg); }
      60% { transform: scale(1.02, .97) skewX(1deg); }
    }
    @keyframes breathe { 0%, 100% { opacity: .85; } 50% { opacity: 1; transform: scale(1.06); } }
    @media (prefers-reduced-motion: reduce) {
      .animated .flame, .animated .halo { animation: none; }
    }
  `
})
export class LogoComponent {
  private static seq = 0;
  @Input() size = 36;
  @Input() animated = false;
  readonly uid = `exlogo${++LogoComponent.seq}`;
}
