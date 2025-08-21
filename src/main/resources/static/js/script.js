// Mobile nav toggle
const toggle = document.querySelector('.nav-toggle');
const headerNav = document.querySelector('.nav');
if (toggle && headerNav) {
  toggle.addEventListener('click', () => headerNav.classList.toggle('show'));
}
// ============ Simple Carousel ============
(function () {
  const root = document.querySelector('.carousel');
  if (!root) return;
  const slidesWrap = root.querySelector('.slides');
  const slides = Array.from(root.querySelectorAll('.slide'));
  const dotsWrap = root.querySelector('.controls');
  let idx = 0, timer = null, isPaused = false;

  function go(n) {
    idx = (n + slides.length) % slides.length;
    slidesWrap.style.transform = `translateX(-${idx * 100}%)`;
    dotsWrap.querySelectorAll('button').forEach((b, i) => b.setAttribute('aria-current', i === idx));
  }
  function play() { timer = setInterval(() => { if (!isPaused) go(idx + 1); }, 6000); }
  function stop() { if (timer) { clearInterval(timer); timer = null; } }

  slides.forEach((_, i) => {
    const b = document.createElement('button');
    b.setAttribute('aria-label', `Go to slide ${i + 1}`);
    b.addEventListener('click', () => go(i));
    if (i === 0) b.setAttribute('aria-current', 'true');
    dotsWrap.appendChild(b);
  });

  root.querySelector('.prev')?.addEventListener('click', () => go(idx - 1));
  root.querySelector('.next')?.addEventListener('click', () => go(idx + 1));
  root.addEventListener('mouseenter', () => { isPaused = true; });
  root.addEventListener('mouseleave', () => { isPaused = false; });
  window.addEventListener('visibilitychange', () => { isPaused = document.hidden; });

  root.setAttribute('role', 'region');
  root.setAttribute('aria-label', 'Promotional banners');
  document.addEventListener('keydown', (e) => {
    if (document.activeElement.closest('.carousel')) {
      if (e.key === 'ArrowLeft') go(idx - 1);
      if (e.key === 'ArrowRight') go(idx + 1);
    }
  });

  go(0); play();
})();
