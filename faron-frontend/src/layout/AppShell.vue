<script setup lang="ts">
import { RouterView } from 'vue-router'
import AppSidebar from './Sidebar/AppSidebar.vue'
import AppTopBar from './TopBar/AppTopBar.vue'
</script>

<template>
  <n-layout has-sider class="shell">
    <AppSidebar />

    <n-layout>
      <AppTopBar />
      <main class="content">
        <RouterView />
      </main>
    </n-layout>
  </n-layout>
</template>

<style scoped>
.shell {
  min-height: 100vh;
}

.content {
  padding: 2rem;
}
</style>
