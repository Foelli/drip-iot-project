<script setup lang="ts">
defineProps({
  eyebrow: {
    type: String,
    default: '',
  },
  title: {
    type: String,
    required: true,
  },
  message: {
    type: String,
    default: '',
  },
  tone: {
    type: String,
    default: 'neutral',
    validator: (value: string) => ['neutral', 'loading', 'empty', 'error'].includes(value),
  },
})
</script>

<template>
  <section class="app-state" :class="`app-state--${tone}`">
    <p v-if="eyebrow" class="app-state__eyebrow">{{ eyebrow }}</p>
    <h2>{{ title }}</h2>
    <p v-if="message">{{ message }}</p>
  </section>
</template>
