<script setup lang="ts">
import { RouterLink, RouterView } from 'vue-router'
</script>

<template>
  <div class="app-shell">
    <header class="app-header">
      <RouterLink class="app-logo" to="/" aria-label="Dashboard">DRIP</RouterLink>

      <nav class="app-nav" aria-label="Primary navigation">
        <RouterLink to="/">Dashboard</RouterLink>
        <RouterLink to="/plants">Plants</RouterLink>
        <RouterLink to="/settings">Settings</RouterLink>
      </nav>
    </header>

    <main class="app-main">
      <RouterView />
    </main>
  </div>
</template>
