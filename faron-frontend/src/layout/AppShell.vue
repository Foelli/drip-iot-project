<script setup lang="ts">
import { RouterView } from 'vue-router'
import AppSidebar from './AppSidebar.vue'
import AppTopBar from './AppTopBar.vue'
</script>

<template>
  <n-layout has-sider class="shell">
    <AppSidebar />

    <!-- plain div, not <n-layout>: n-layout wraps its slot in its own
         scrolling .n-layout-scroll-container, which becomes the real scroller
         and breaks position:sticky inside .content. A div makes .content the
         single, genuine scroll container. -->
    <div class="main-column">
      <AppTopBar />
      <main class="content">
        <RouterView />
      </main>
    </div>
  </n-layout>
</template>

<style scoped>
/* ------------------------------------------------------------------
   App-shell scroll model
   ------------------------------------------------------------------
   The shell is pinned to viewport height and clips overflow, so the
   window itself never scrolls. The right-hand column is a flex column
   (TopBar fixed-height + content fills the rest); only `.content`
   scrolls. Result: the sidebar stays put on every page because it
   lives outside the scroll container.
   ------------------------------------------------------------------ */

.shell {
  height: 100vh;
  overflow: hidden;
}

/* The inner n-layout (right of the sidebar) becomes the flex column
   that owns the scroll. `min-width: 0` prevents wide content (data
   tables, code blocks) from forcing horizontal overflow. */
.main-column {
  /* fill the width beside the sider (n-layout used to provide this growth) */
  flex: 1;
  display: flex;
  flex-direction: column;
  height: 100vh;
  min-width: 0;
}

.content {
  flex: 1;
  overflow-y: auto;
  padding: 2rem;
  /* `min-height: 0` is required for a flex child to actually shrink
     and let its own overflow take over. Without it, content can push
     the column taller than the viewport. */
  min-height: 0;
}
</style>
