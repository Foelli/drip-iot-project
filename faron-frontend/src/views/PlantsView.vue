<script setup lang="ts">
import AppState from '../components/AppState.vue'
</script>

<template>
  <section class="view-layout" aria-labelledby="plants-title">
    <div class="view-heading">
      <p class="eyebrow">Plants</p>
      <h1 id="plants-title">Plant library</h1>
      <p class="lede">
        Stored plants, catalog selections, and plant details.
      </p>
    </div>

    <AppState
      tone="empty"
      title="No plants added"
      message="Add a plant from the catalog to start monitoring it."
    />
  </section>
</template>
