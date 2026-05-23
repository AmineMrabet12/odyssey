/* ===== ACHIEVEMENTS PAGE ===== */
let currentUser = null;
let allAchievements = [];
let unlockedIds = new Set();
let currentFilter = 'ALL';

async function init() {
  currentUser = await refreshUser();
  if (!currentUser) return;
  await loadAchievements();
}

async function loadAchievements() {
  try {
    const [all, userAch] = await Promise.all([
      api.get('/achievements'),
      api.get(`/users/${currentUser.id}/achievements`)
    ]);
    allAchievements = all;
    unlockedIds = new Set(userAch.map(a => a.id));

    const maxXp = Math.max(...all.map(a => a.xpRequired), 100);
    const pct = Math.min(100, (currentUser.xp / maxXp) * 100);
    document.getElementById('hero-xp-display').textContent = `${currentUser.xp.toLocaleString()} XP`;
    document.getElementById('hero-xp-bar').style.width = pct + '%';

    document.getElementById('ach-count').textContent = `${unlockedIds.size}/${all.length}`;
    renderAchievements();
  } catch (e) {
    document.getElementById('ach-grid').innerHTML = `<div class="empty-state"><div class="empty-title">Failed to load achievements</div></div>`;
  }
}

function renderAchievements() {
  let list = [...allAchievements];
  if (currentFilter !== 'ALL') list = list.filter(a => a.rarity === currentFilter);

  // Sort: unlocked first, then by XP required
  list.sort((a, b) => {
    const au = unlockedIds.has(a.id);
    const bu = unlockedIds.has(b.id);
    if (au !== bu) return bu - au;
    return a.xpRequired - b.xpRequired;
  });

  document.getElementById('ach-grid').innerHTML = list.map(a => {
    const unlocked = unlockedIds.has(a.id);
    return `
      <div class="card achievement-card ${unlocked ? '' : 'locked'}" title="${unlocked ? 'Unlocked!' : `Requires ${a.xpRequired} XP`}">
        <div class="achievement-icon">${a.icon || '🏅'}</div>
        <div class="achievement-name">${a.name}</div>
        <div class="achievement-desc">${a.description}</div>
        <div style="margin:0.4rem 0">${rarityBadge(a.rarity)}</div>
        <div class="achievement-xp">${unlocked ? '✅ Unlocked' : `🔒 ${a.xpRequired.toLocaleString()} XP needed`}</div>
        ${!unlocked ? `
          <div class="progress-wrap" style="margin-top:0.5rem">
            <div class="progress-bar">
              <div class="progress-fill" style="width:${Math.min(100,(currentUser.xp/Math.max(a.xpRequired,1))*100).toFixed(0)}%;background:${rarityColor(a.rarity)}"></div>
            </div>
          </div>` : ''}
      </div>`;
  }).join('');
}

function rarityColor(r) {
  return { COMMON:'#6b7280', RARE:'#3b82f6', EPIC:'#a855f7', LEGENDARY:'linear-gradient(90deg,#a855f7,#f59e0b)' }[r] || '#6b7280';
}

function filterAch(rarity, btn) {
  currentFilter = rarity;
  document.querySelectorAll('.filter-btn').forEach(b => b.classList.remove('active'));
  btn.classList.add('active');
  renderAchievements();
}

document.addEventListener('DOMContentLoaded', init);
