/* ===== GUILD PAGE ===== */
let currentUser = null;
let allGuilds = [];

async function init() {
  currentUser = await refreshUser();
  if (!currentUser) return;
  await Promise.all([loadGuilds(), loadGuildLeaderboard()]);
  renderMyGuild();
}

async function loadGuilds() {
  try {
    allGuilds = await api.get('/guilds');
    renderGuildList();
  } catch {
    document.getElementById('guilds-list').innerHTML = '<div class="empty-state"><div class="empty-title">Failed to load guilds</div></div>';
  }
}

function renderMyGuild() {
  const section = document.getElementById('my-guild-section');
  if (currentUser.guild) {
    const g = currentUser.guild;
    section.innerHTML = `
      <div class="section-title">🛡️ My Guild</div>
      <div class="guild-banner">
        <div class="guild-emblem">${g.emblem || '⚔️'}</div>
        <div class="guild-name">${g.name}</div>
        <div class="guild-motto">"${g.motto || ''}"</div>
        <div style="color:var(--text-dim);font-size:0.875rem;margin-top:0.5rem">${g.description || ''}</div>
        <div class="guild-stats">
          <div class="stat-card" style="padding:0">
            <div class="stat-value" style="font-size:1.2rem" id="guild-member-count">—</div>
            <div class="stat-label">Members</div>
          </div>
          <div class="stat-card" style="padding:0">
            <div class="stat-value" style="font-size:1.2rem" id="guild-total-xp">—</div>
            <div class="stat-label">Total XP</div>
          </div>
        </div>
        <button class="btn btn-ghost btn-sm" style="margin-top:1rem" onclick="leaveGuild()">Leave Guild</button>
      </div>
      <div class="card" style="margin-top:1rem">
        <div class="section-title">👥 Members</div>
        <div id="guild-members-list"><div class="loading-spinner"><div class="spinner"></div></div></div>
      </div>`;
    loadGuildMembers(g.id);
  } else {
    section.innerHTML = `
      <div class="section-title">🛡️ My Guild</div>
      <div class="card" style="text-align:center;padding:2rem">
        <div style="font-size:3rem;margin-bottom:0.75rem">🏰</div>
        <div style="font-family:'Cinzel',serif;font-size:1.1rem;color:var(--text);margin-bottom:0.5rem">No Guild Yet</div>
        <div style="color:var(--text-dim);font-size:0.875rem;margin-bottom:1.5rem">Join an existing guild or found your own.</div>
        <button class="btn btn-primary" onclick="openCreateGuild()">⚔️ Found a Guild</button>
      </div>`;
  }
}

async function loadGuildMembers(guildId) {
  try {
    const members = await api.get(`/guilds/${guildId}/members`);
    const total = members.reduce((s, m) => s + m.xp, 0);
    document.getElementById('guild-member-count').textContent = members.length;
    document.getElementById('guild-total-xp').textContent = total.toLocaleString();
    const sorted = [...members].sort((a, b) => b.xp - a.xp);
    document.getElementById('guild-members-list').innerHTML = sorted.map((m, i) => `
      <div class="member-row">
        <div class="member-avatar-sm" style="background:linear-gradient(135deg,var(--purple),var(--gold));font-size:1rem">
          ${i === 0 ? '👑' : '⚔️'}
        </div>
        <div class="member-info">
          <div class="member-name">${m.username}${m.id === currentUser.id ? ' (you)' : ''}</div>
          <div class="member-sub">LVL ${m.level}</div>
        </div>
        <div class="member-xp">${m.xp.toLocaleString()} XP</div>
      </div>`).join('');
  } catch {}
}

function renderGuildList() {
  document.getElementById('guilds-list').innerHTML = allGuilds.map(g => `
    <div class="guild-list-item">
      <div class="guild-item-emblem">${g.emblem || '⚔️'}</div>
      <div class="guild-item-info">
        <div class="guild-item-name">${g.name}</div>
        <div class="guild-item-motto">"${g.motto || 'No motto'}"</div>
        <div class="guild-item-members">${g.memberCount || 0} members</div>
      </div>
      ${!currentUser.guild || currentUser.guild.id !== g.id
        ? `<button class="btn btn-ghost btn-sm" onclick="joinGuild(${g.id})">Join</button>`
        : `<span class="badge badge-active">Joined</span>`
      }
    </div>`).join('') || `<div class="empty-state"><div class="empty-icon">🏰</div><div class="empty-title">No guilds yet</div><div class="empty-sub">Be the first to found one!</div></div>`;
}

async function loadGuildLeaderboard() {
  try {
    const guilds = await api.get('/guilds');
    const sorted = [...guilds].sort((a, b) => (b.totalXp || 0) - (a.totalXp || 0)).slice(0, 5);
    const rankEmojis = ['🥇','🥈','🥉','4️⃣','5️⃣'];
    document.getElementById('guild-leaderboard').innerHTML = sorted.map((g, i) => `
      <div class="lb-row rank-${i+1}">
        <div class="lb-rank">${rankEmojis[i]}</div>
        <div style="font-size:1.4rem">${g.emblem || '⚔️'}</div>
        <div style="flex:1">
          <div style="font-weight:600;font-size:0.9rem;color:var(--text)">${g.name}</div>
          <div style="font-size:0.75rem;color:var(--text-dim)">${g.memberCount || 0} members</div>
        </div>
        <div class="member-xp">${(g.totalXp || 0).toLocaleString()}</div>
      </div>`).join('') || `<div style="color:var(--text-dim);text-align:center;padding:1rem;font-size:0.875rem">No guilds yet</div>`;
  } catch {}
}

async function joinGuild(guildId) {
  try {
    await api.post(`/guilds/${guildId}/members/${currentUser.id}`, null);
    showToast('Joined the guild! Welcome, hero! ⚔️', 'success');
    currentUser = await refreshUser();
    renderMyGuild();
    await loadGuilds();
  } catch (e) { showToast(e.message, 'error'); }
}

async function leaveGuild() {
  if (!confirm('Leave your guild?')) return;
  try {
    await api.delete(`/guilds/${currentUser.guild.id}/members/${currentUser.id}`);
    showToast('You left the guild.', 'success');
    currentUser = await refreshUser();
    renderMyGuild();
    await loadGuilds();
  } catch (e) { showToast(e.message, 'error'); }
}

function openCreateGuild() {
  document.getElementById('create-guild-modal').classList.add('open');
}

async function createGuild() {
  const name = document.getElementById('g-name').value.trim();
  const emblem = document.getElementById('g-emblem').value.trim() || '⚔️';
  const motto = document.getElementById('g-motto').value.trim();
  const description = document.getElementById('g-desc').value.trim();
  if (!name) return showToast('Enter a guild name', 'error');
  try {
    const guild = await api.post('/guilds', { name, emblem, motto, description });
    await api.post(`/guilds/${guild.id}/members/${currentUser.id}`, null);
    closeModal('create-guild-modal');
    showToast(`Guild "${name}" founded! Glory awaits! 🏰`, 'success');
    currentUser = await refreshUser();
    renderMyGuild();
    await Promise.all([loadGuilds(), loadGuildLeaderboard()]);
  } catch (e) { showToast(e.message, 'error'); }
}

document.addEventListener('DOMContentLoaded', init);
