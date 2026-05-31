<script setup lang="ts">
import AppState from '../components/AppState.vue'
</script>

<template>
  <section class="view-layout" aria-labelledby="dashboard-title">
    <div class="view-heading">
      <p class="eyebrow">Dashboard</p>
      <h1 id="dashboard-title">Plant overview</h1>
      <p class="lede">
        Live device status, latest readings, and watering signals in one place.
      </p>
    </div>

    <div class="state-grid" aria-label="Reusable state examples">
      <AppState
        tone="loading"
        eyebrow="Syncing"
        title="Fetching readings"
        message="Waiting for the latest device update."
      />
      <AppState
        tone="empty"
        eyebrow="Empty"
        title="No readings yet"
        message="Connect a device to begin collecting measurements."
      />
      <AppState
        tone="error"
        eyebrow="Error"
        title="Backend unavailable"
        message="Check the backend connection and try again."
      />
    </div>
  </section>
</template>
