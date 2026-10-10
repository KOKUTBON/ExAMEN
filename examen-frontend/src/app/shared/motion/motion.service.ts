import { Inject, Injectable, PLATFORM_ID } from '@angular/core';
import { isPlatformBrowser } from '@angular/common';

export type MotionType = 'reveal' | 'parallax';

interface MotionEntry {
  el: HTMLElement;
  type: MotionType;
  range: number;
  value: number;
}

@Injectable({
  providedIn: 'root'
})
export class MotionService {
  readonly enabled: boolean;
  private reduceMotion = false;
  private entries = new Set<MotionEntry>();
  private ticking = false;
  private lastTime = 0;

  constructor(@Inject(PLATFORM_ID) platformId: object) {
    this.enabled = isPlatformBrowser(platformId);
    if (this.enabled) {
      this.reduceMotion =
        typeof window.matchMedia === 'function'
          ? window.matchMedia('(prefers-reduced-motion: reduce)').matches
          : false;
      window.addEventListener('scroll', this.onScroll, { passive: true });
      window.addEventListener('resize', this.onScroll, { passive: true });
    }
  }

  register(el: HTMLElement, type: MotionType = 'reveal', range = 24): () => void {
    if (!this.enabled) {
      return () => {};
    }

    const entry: MotionEntry = {
      el,
      type,
      range,
      value: type === 'reveal' ? 1 : 0
    };

    this.entries.add(entry);

    if (this.reduceMotion) {
      this.apply(entry, type === 'reveal' ? 1 : 0);
      return () => this.entries.delete(entry);
    }

    this.updateEntry(entry, 0);
    this.apply(entry, entry.value);
    this.schedule();

    return () => this.entries.delete(entry);
  }

  private onScroll = (): void => {
    this.schedule();
  };

  private schedule(): void {
    if (!this.enabled || this.ticking || this.entries.size === 0) {
      return;
    }

    this.ticking = true;
    requestAnimationFrame((time) => {
      const shouldContinue = this.update(time);
      this.ticking = false;
      if (shouldContinue) {
        this.schedule();
      }
    });
  }

  private update(time: number): boolean {
    const delta = this.lastTime ? (time - this.lastTime) / 1000 : 0.016;
    const dt = Math.min(delta, 0.05);
    this.lastTime = time;
    let shouldContinue = false;

    this.entries.forEach(entry => {
      const target = this.updateEntry(entry, dt);
      this.apply(entry, entry.value);
      if (Math.abs(target - entry.value) > 0.001) {
        shouldContinue = true;
      }
    });

    return shouldContinue;
  }

  private updateEntry(entry: MotionEntry, dt: number): number {
    const rect = entry.el.getBoundingClientRect();
    const viewHeight = window.innerHeight || 1;
    const raw = (viewHeight - rect.top) / (viewHeight + rect.height);
    const progress = clamp(raw, 0, 1);
    const eased = smootherstep(progress);
    const target = entry.type === 'parallax'
      ? (0.5 - eased) * entry.range
      : eased;

    entry.value = dt === 0 ? target : damp(entry.value, target, 16, dt);
    return target;
  }

  private apply(entry: MotionEntry, value: number): void {
    if (entry.type === 'parallax') {
      entry.el.style.setProperty('--motion-parallax', `${value.toFixed(3)}px`);
    } else {
      entry.el.style.setProperty('--motion-progress', `${value.toFixed(3)}`);
    }
  }
}

const clamp = (value: number, min: number, max: number): number =>
  Math.min(Math.max(value, min), max);

const smootherstep = (t: number): number => {
  const x = clamp(t, 0, 1);
  return x * x * x * (x * (x * 6 - 15) + 10);
};

const damp = (current: number, target: number, lambda: number, dt: number): number =>
  current + (target - current) * (1 - Math.exp(-lambda * dt));
