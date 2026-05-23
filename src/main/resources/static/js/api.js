/* ===== ODYSSEY — API CLIENT ===== */
const BASE = '/api';

const api = {
  async get(path) {
    const r = await fetch(BASE + path);
    if (!r.ok) { const e = await r.json().catch(() => ({})); throw new Error(e.message || 'Request failed'); }
    return r.json();
  },
  async post(path, body) {
    const r = await fetch(BASE + path, { method: 'POST', headers: {'Content-Type':'application/json'}, body: JSON.stringify(body) });
    if (!r.ok) { const e = await r.json().catch(() => ({})); throw new Error(e.message || JSON.stringify(e.fields) || 'Request failed'); }
    return r.json();
  },
  async put(path, body) {
    const r = await fetch(BASE + path, { method: 'PUT', headers: {'Content-Type':'application/json'}, body: JSON.stringify(body) });
    if (!r.ok) { const e = await r.json().catch(() => ({})); throw new Error(e.message || 'Request failed'); }
    return r.json();
  },
  async patch(path, body) {
    const r = await fetch(BASE + path, { method: 'PATCH', headers: body ? {'Content-Type':'application/json'} : {}, body: body ? JSON.stringify(body) : undefined });
    if (!r.ok) { const e = await r.json().catch(() => ({})); throw new Error(e.message || 'Request failed'); }
    return r.json().catch(() => ({}));
  },
  async delete(path) {
    const r = await fetch(BASE + path, { method: 'DELETE' });
    if (!r.ok) { const e = await r.json().catch(() => ({})); throw new Error(e.message || 'Request failed'); }
  }
};

/* ===== CURRENT USER ===== */
function getCurrentUser() {
  const s = localStorage.getItem('odyssey_user');
  return s ? JSON.parse(s) : null;
}

async function refreshUser() {
  const u = getCurrentUser();
  if (!u) return null;
  try {
    const fresh = await api.get(`/users/${u.id}`);
    localStorage.setItem('odyssey_user', JSON.stringify(fresh));
    return fresh;
  } catch { return u; }
}

function logout() {
  localStorage.removeItem('odyssey_user');
  window.location.href = '/';
}

/* ===== TOAST NOTIFICATIONS ===== */
function showToast(msg, type = 'success') {
  const el = document.getElementById('toasts');
  if (!el) return;
  const icons = { success: '✅', error: '❌', xp: '⭐' };
  const t = document.createElement('div');
  t.className = `toast ${type}`;
  t.innerHTML = `<span>${icons[type] || '💬'}</span><span>${msg}</span>`;
  el.appendChild(t);
  setTimeout(() => t.remove(), 4000);
}

/* ===== XP POPUP ===== */
function showXpPopup(amount, x, y) {
  const el = document.createElement('div');
  el.className = 'xp-popup';
  el.textContent = `+${amount} XP ⭐`;
  el.style.left = `${x}px`;
  el.style.top = `${y}px`;
  document.body.appendChild(el);
  setTimeout(() => el.remove(), 2000);
}

/* ===== CLOSE MODAL ===== */
function closeModal(id) {
  document.getElementById(id).classList.remove('open');
}

/* ===== HELPERS ===== */
function diffBadge(diff) {
  const map = { EASY:'easy', MEDIUM:'medium', HARD:'hard', LEGENDARY:'legendary' };
  const icons = { EASY:'⚡', MEDIUM:'🔥', HARD:'💀', LEGENDARY:'👑' };
  return `<span class="badge badge-${map[diff]}">${icons[diff]} ${diff}</span>`;
}

function statusBadge(status) {
  const map = { ACTIVE:'active', COMPLETED:'completed', PAUSED:'paused', ABANDONED:'abandoned' };
  return `<span class="badge badge-${map[status]}">${status}</span>`;
}

function rarityBadge(rarity) {
  const map = { COMMON:'common', RARE:'rare', EPIC:'epic', LEGENDARY:'legendary-r' };
  return `<span class="badge badge-${map[rarity]}">${rarity}</span>`;
}

function classIcon(cls) {
  const map = { WARRIOR:'⚔️', MAGE:'🧙', ROGUE:'🗡️', PALADIN:'🛡️' };
  return map[cls] || '⚔️';
}

function xpToLevel(xp) { return Math.floor(Math.sqrt(xp / 100)) + 1; }
function levelXpRange(lvl) {
  const cur = (lvl - 1) * (lvl - 1) * 100;
  const next = lvl * lvl * 100;
  return { cur, next };
}

/* ===== NAV INIT ===== */
document.addEventListener('DOMContentLoaded', async () => {
  const u = await refreshUser();
  if (!u) {
    // Only redirect if not on index
    if (!window.location.pathname.endsWith('index.html') && window.location.pathname !== '/') {
      window.location.href = '/';
    }
    return;
  }
  const navUsername = document.getElementById('nav-username');
  const navXp = document.getElementById('nav-xp-mini');
  const navAvatar = document.getElementById('nav-avatar-icon');
  if (navUsername) navUsername.textContent = u.username;
  if (navXp) navXp.textContent = `LVL ${u.level} · ${u.xp} XP`;
  if (navAvatar) {
    try {
      const profile = await api.get(`/profiles/user/${u.id}`);
      navAvatar.textContent = classIcon(profile.avatarClass);
    } catch {}
  }
});
