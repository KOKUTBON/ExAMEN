import { AfterViewInit, Directive, ElementRef, Input, OnDestroy } from '@angular/core';
import { MotionService, MotionType } from './motion.service';

@Directive({
  selector: '[appMotion]',
  standalone: true
})
export class MotionDirective implements AfterViewInit, OnDestroy {
  @Input('appMotion') motionType: MotionType | '' = 'reveal';
  @Input() motionRange = 24;
  @Input() motionOffset = 24;
  @Input() motionScale = 0.02;

  private cleanup?: () => void;

  constructor(
    private elementRef: ElementRef<HTMLElement>,
    private motion: MotionService
  ) {}

  ngAfterViewInit(): void {
    const element = this.elementRef.nativeElement;
    const enabled = this.motion.enabled;
    const type: MotionType = this.motionType === 'parallax' ? 'parallax' : 'reveal';

    if (type === 'reveal') {
      element.classList.add('motion-reveal');
      element.style.setProperty('--motion-offset', `${this.motionOffset}px`);
      element.style.setProperty('--motion-scale', `${this.motionScale}`);
    } else {
      element.classList.add('motion-parallax');
    }

    if (enabled) {
      element.classList.add('motion-ready');
      this.cleanup = this.motion.register(element, type, this.motionRange);
    }
  }

  ngOnDestroy(): void {
    if (this.cleanup) {
      this.cleanup();
    }
  }
}
