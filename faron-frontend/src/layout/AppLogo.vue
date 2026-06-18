<script setup lang="ts">
// Swap this single import for the final asset (e.g. `logo.svg`) when ready —
// the `<img :src>` markup below works identically for JPG, PNG, and SVG.
import logoUrl from '@/assets/placeholder_logo.jpg'

defineOptions({ name: 'AppLogo' })
</script>

<template>
  <router-link to="/" class="app-logo" aria-label="DRIP — go to overview">
    <img :src="logoUrl" alt="" class="app-logo__img" />
    <span class="app-logo__name">DRIP</span>
  </router-link>
</template>

<style scoped>
/* Brand row at the top of the sidebar. Height matches the AppTopBar so the
   sidebar header and top bar align across the layout. */
.app-logo {
  display: flex;
  align-items: center;
  gap: 0.625rem;
  height: 4.5rem;
  /* keep the full row height in the flex column so the logo + wordmark stay
     vertically centered and the row can't be squeezed shorter */
  flex-shrink: 0;
  /* hover highlight spans the full sidebar width; absorb the gutter into the
     padding so the logo + wordmark still line up with the menu item content
     below (menu items: margin gutter + padding x-padding). */
  padding: 0 calc(var(--sidebar-gutter) + var(--sidebar-x-padding));
  text-decoration: none;
  color: inherit;
  transition: background-color 120ms ease;
}
.app-logo:hover {
  background-color: var(--sidebar-item-bg-hover);
}

.app-logo__img {
  width: 42px;
  height: 42px;
  /* `contain` so non-square source images don't get stretched. */
  object-fit: contain;
  flex-shrink: 0;
  /* Mild rounding so a square JPG / PNG sits nicely; SVGs with their own
     shape will be unaffected by the corner radius. */
  border-radius: 6px;
}

.app-logo__name {
  font-size: 1.4625rem;
  font-weight: 600;
  letter-spacing: -0.01em;
  white-space: nowrap;
  overflow: hidden;
}

/* NOTE: when sidebar collapse is wired up, the rules to hide the wordmark
   and shrink the logo need to live in AppSidebar.vue — `:deep()` can only
   target descendants, and the `.n-layout-sider--collapsed` class is on a
   parent of this component. */
</style>
