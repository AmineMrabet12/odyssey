/* ===== DASHBOARD ===== */
let currentUser = null;
let allQuests = [];

async function init() {
  currentUser = await refreshUser();
  if (!currentUser) return;
  await Promise.all([loadProfile(), loadQuests(), loadAchievements(), loadLeaderboard()]);
}

async function loadProfile() {
  document.getElementById('stat-xp').textContent = currentUser.xp.toLocaleString();
  document.getElementById('stat-level').textContent = currentUser.level;
  const { cur, next } = levelXpRange(currentUser.level);
  const pct = Math.min(100, ((currentUser.xp - cur) / (next - cur)) * 100);
  document.getElementById('xp-fill').style.width = pct + '%';
  document.getElementById('xp-label').textContent = `${currentUser.xp} XP`;
  document.getElementById('next-level').textContent = currentUser.level + 1;

  try {
    const profile = await api.get(`/profiles/user/${currentUser.id}`);
    const icon = classIcon(profile.avatarClass);
    document.getElementById('char-avatar-card').innerHTML = `${icon}<div class="char-level">${currentUser.level}</div>`;
    document.getElementById('char-avatar-card').className = `char-avatar ${profile.avatarClass}`;
    document.getElementById('char-name').textContent = profile.fullName || currentUser.username;
    document.getElementById('char-title').textContent = profile.title || '—';
    document.getElementById('char-class').textContent = profile.avatarClass;
    document.getElementById('char-bio').textContent = profile.bio || '';
    document.getElementById('nav-avatar-icon').textContent = icon;
  } catch {}

  if (currentUser.guild) {
    document.getElementById('guild-mini-content').innerHTML = `
      <div style="font-size:2rem;margin-bottom:0.25rem">${currentUser.guild.emblem || '⚔️'}</div>
      <div style="font-family:'Cinzel',serif;font-size:1rem;color:var(--text)">${currentUser.guild.name}</div>
      <div style="font-size:0.8rem;color:var(--gold);font-style:italic;margin-top:0.25rem">${currentUser.guild.motto || ''}</div>
      <a href="/guild.html" class="btn btn-ghost btn-sm" style="margin-top:0.75rem;display:inline-block">View Guild</a>
    `;
  }
}

async function loadQuests() {
  try {
    allQuests = await api.get(`/quests/user/${currentUser.id}`);
    const active = allQuests.filter(q => q.status === 'ACTIVE');
    const completed = allQuests.filter(q => q.status === 'COMPLETED');

    document.getElementById('stat-active').textContent = active.length;
    document.getElementById('stat-completed').textContent = completed.length;
    document.getElementById('stat-quests-total').textContent = allQuests.length;

    document.getElementById('active-quests').innerHTML = active.length
      ? active.slice(0, 4).map(q => renderQuestCard(q)).join('')
      : `<div class="empty-state"><div class="empty-icon">⚔️</div><div class="empty-title">No active quests</div><div class="empty-sub">Create your first quest!</div><a href="/quests.html" class="btn btn-primary">Go to Quests</a></div>`;

    document.getElementById('completed-quests').innerHTML = completed.length
      ? completed.slice(0, 2).map(q => renderQuestCard(q)).join('')
      : `<div class="empty-state" style="padding:1.5rem"><div class="empty-title" style="font-size:0.9rem">No completed quests yet</div></div>`;

    // Count completed missions
    let doneMissions = 0;
    for (const q of active.slice(0, 6)) {
      try {
        const missions = await api.get(`/missions/quest/${q.id}`);
        doneMissions += missions.filter(m => m.completed).length;
      } catch {}
    }
    document.getElementById('stat-missions').textContent = doneMissions;
  } catch (e) {
    document.getElementById('active-quests').innerHTML = `<div class="empty-state"><div class="empty-title">Failed to load quests</div></div>`;
  }
}

async function loadAchievements() {
  try {
    const [all, userAch] = await Promise.all([
      api.get('/achievements'),
      api.get(`/users/${currentUser.id}/achievements`)
    ]);
    const unlockedIds = new Set(userAch.map(a => a.id));
    document.getElementById('stat-achievements').textContent = `${unlockedIds.size}/${all.length}`;
    const unlocked = all.filter(a => unlockedIds.has(a.id)).slice(0, 5);
    document.getElementById('achievement-mini').innerHTML = unlocked.length
      ? unlocked.map(a => `
        <div class="member-row" style="padding:0.4rem 0">
          <span style="font-size:1.4rem">${a.icon}</span>
          <div class="member-info">
            <div class="member-name" style="font-size:0.85rem">${a.name}</div>
            <div>${rarityBadge(a.rarity)}</div>
          </div>
        </div>`).join('')
      : `<div style="text-align:center;padding:1rem;color:var(--text-dim);font-size:0.875rem">Complete quests to unlock achievements!</div>`;
  } catch {}
}

async function loadLeaderboard() {
  try {
    const users = await api.get('/users');
    const sorted = [...users].sort((a, b) => b.xp - a.xp).slice(0, 5);
    const rankEmojis = ['🥇','🥈','🥉','4️⃣','5️⃣'];
    document.getElementById('leaderboard').innerHTML = sorted.map((u, i) => `
      <div class="lb-row rank-${i+1}">
        <div class="lb-rank">${rankEmojis[i]}</div>
        <div style="flex:1">
          <div style="font-weight:600;font-size:0.9rem;color:${u.id === currentUser.id ? 'var(--purple-light)' : 'var(--text)'}">${u.username}${u.id === currentUser.id ? ' 👈' : ''}</div>
          <div style="font-size:0.75rem;color:var(--text-dim)">LVL ${u.level}</div>
        </div>
        <div class="member-xp">${u.xp.toLocaleString()} XP</div>
      </div>`).join('');
  } catch {}
}

function renderQuestCard(q) {
  const pct = q.progress || 0;
  const diffColor = { EASY:'var(--diff-easy)', MEDIUM:'var(--diff-medium)', HARD:'var(--diff-hard)', LEGENDARY:'var(--legendary)' };
  const catChips = (q.categories || []).map(c => `<span class="cat-chip">${c.icon||''} ${c.name}</span>`).join('');
  return `
    <div class="card quest-card" onclick="window.location='/quests.html'" style="border-left:3px solid ${diffColor[q.difficulty]}">
      <div class="quest-card-header">
        <div class="quest-title-text">${q.title}</div>
        <div>${diffBadge(q.difficulty)}</div>
      </div>
      <div class="quest-desc">${q.description || '—'}</div>
      <div class="progress-wrap">
        <div style="display:flex;justify-content:space-between;font-size:0.75rem;color:var(--text-dim);margin-bottom:0.3rem">
          <span>Progress</span><span>${pct}%</span>
        </div>
        <div class="progress-bar">
          <div class="progress-fill" style="width:${pct}%;background:${pct===100?'var(--green)':'linear-gradient(90deg,var(--purple),var(--gold))'}"></div>
        </div>
      </div>
      <div class="quest-meta">
        <div class="quest-categories">${catChips}</div>
        <div class="quest-xp">⭐ ${q.xpReward} XP</div>
      </div>
    </div>`;
}

async function quickCreateQuest() {
  const title = document.getElementById('quick-title').value.trim();
  const difficulty = document.getElementById('quick-diff').value;
  if (!title) return showToast('Enter a quest title', 'error');
  const xpMap = { EASY:100, MEDIUM:250, HARD:500, LEGENDARY:1000 };
  try {
    await api.post(`/quests?userId=${currentUser.id}`, { title, difficulty, xpReward: xpMap[difficulty] });
    document.getElementById('quick-title').value = '';
    showToast(`Quest "${title}" forged! 🗡️`, 'success');
    await loadQuests();
  } catch (e) {
    showToast(e.message, 'error');
  }
}

document.addEventListener('DOMContentLoaded', init);
