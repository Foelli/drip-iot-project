<script setup lang="ts">
import { API_BASE_URL } from '../api/config'
</script>

<template>
  <section class="view-layout" aria-labelledby="settings-title">
    <div class="view-heading">
      <p class="eyebrow">Settings</p>
      <h1 id="settings-title">Device connection</h1>
      <p class="lede">
        Backend and Pico connection values for local development.
      </p>
    </div>

    <dl class="settings-list">
      <div>
        <dt>Backend API</dt>
        <dd>{{ API_BASE_URL }}</dd>
      </div>
      <div>
        <dt>Device fields</dt>
        <dd>deviceId, nodeIp, nodePort</dd>
      </div>
    </dl>
  </section>
</template>
