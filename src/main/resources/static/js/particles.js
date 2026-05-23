/* ===== CANVAS PARTICLE SYSTEM ===== */
(function () {
  const canvas = document.getElementById('particles-bg');
  if (!canvas) return;
  const ctx = canvas.getContext('2d');
  let W, H, particles = [];
  const COLORS = ['rgba(130,80,255,', 'rgba(245,200,66,', 'rgba(6,182,212,', 'rgba(255,255,255,'];

  function resize() {
    W = canvas.width = window.innerWidth;
    H = canvas.height = window.innerHeight;
  }

  function createParticle() {
    const color = COLORS[Math.floor(Math.random() * COLORS.length)];
    return {
      x: Math.random() * W,
      y: H + Math.random() * 20,
      r: Math.random() * 2 + 0.5,
      speed: Math.random() * 0.6 + 0.2,
      opacity: Math.random() * 0.6 + 0.2,
      drift: (Math.random() - 0.5) * 0.3,
      color,
      flicker: Math.random() * Math.PI * 2
    };
  }

  function init() {
    resize();
    particles = Array.from({ length: 80 }, createParticle);
    particles.forEach(p => { p.y = Math.random() * H; });
  }

  function draw() {
    ctx.clearRect(0, 0, W, H);
    const now = Date.now() / 1000;
    particles.forEach((p, i) => {
      p.y -= p.speed;
      p.x += p.drift;
      p.flicker += 0.05;
      const alpha = p.opacity * (0.7 + 0.3 * Math.sin(p.flicker));
      ctx.beginPath();
      ctx.arc(p.x, p.y, p.r, 0, Math.PI * 2);
      ctx.fillStyle = p.color + alpha + ')';
      ctx.fill();
      if (p.y < -10 || p.x < -10 || p.x > W + 10) particles[i] = createParticle();
    });
    requestAnimationFrame(draw);
  }

  window.addEventListener('resize', resize);
  init();
  draw();
})();
