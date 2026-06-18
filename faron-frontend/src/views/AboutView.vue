<script setup lang="ts">
import { useRouter } from 'vue-router'
import {
  BarChartOutline,
  GridOutline,
  InformationCircleOutline,
  LeafOutline,
  LogoGithub,
  SettingsOutline,
} from '@vicons/ionicons5'
import type { Component } from 'vue'

defineOptions({ name: 'AboutView' })

const REPO_URL = 'https://github.com/Foelli/drip-iot-project'

const router = useRouter()

interface TabGuide {
  name: string
  route: string
  icon: Component
  desc: string
}

// Mirror the sidebar order so this page reads top-to-bottom the way the app
// is structured. Icons come from @vicons/ionicons5 to match AppSidebar.vue.
const tabs: TabGuide[] = [
  {
    name: 'Overview',
    route: '/',
    icon: GridOutline,
    desc: 'Daily snapshot — care queue (what needs attention now), fleet status, recent activity, and watering this week. Each row links into the relevant detail view.',
  },
  {
    name: 'Plants',
    route: '/plants',
    icon: LeafOutline,
    desc: 'Inventory of every plant. Click a card to open the detail drawer, which has its own sub-tabs: Overview, Vitals, Journal, and Settings.',
  },
  {
    name: 'Vitals',
    route: '/vitals',
    icon: BarChartOutline,
    desc: 'Fleet-wide telemetry deep-dive: KPIs, attention queue, time-in-band heat map (moisture or temperature), trends across plants, and device status.',
  },
  {
    name: 'Settings',
    route: '/settings',
    icon: SettingsOutline,
    desc: 'App-level preferences — theme, network, notifications.',
  },
  {
    name: 'About',
    route: '/about',
    icon: InformationCircleOutline,
    desc: 'This page. Project description, app guide, and hardware + software setup notes.',
  },
]

const features: { title: string; body: string }[] = [
  {
    title: 'Soil moisture monitoring',
    body: 'Capacitive sensors report soil moisture per plant on a fixed telemetry cycle.',
  },
  {
    title: 'Automatic watering',
    body: 'A small pump fires when moisture drops below the per-plant threshold, with a configurable cooldown and quiet-hours window.',
  },
  {
    title: 'Self-hosted',
    body: 'Backend runs on a Raspberry Pi in any Docker-compatible environment. No cloud dependency.',
  },
  {
    title: 'Per-plant device',
    body: 'Each plant gets a Raspberry Pi Pico W with sensors, pump, and Wi-Fi. The Pi acts as the hub.',
  },
  {
    title: 'Journal & history',
    body: 'Every watering, note, and config change is logged so plant behaviour can be reviewed over time.',
  },
]

const hardware: { name: string; detail: string }[] = [
  {
    name: 'Raspberry Pi (3B+ or newer)',
    detail: 'Runs the Ktor backend and this frontend. Acts as the hub for all Pico nodes.',
  },
  {
    name: 'Raspberry Pi Pico W (one per plant)',
    detail:
      'Sensors + pump + Wi-Fi. Reports telemetry on a fixed interval and accepts water commands.',
  },
  {
    name: 'Capacitive soil moisture sensor',
    detail: 'Resistive sensors corrode quickly — capacitive is worth the small price bump.',
  },
  {
    name: 'Temperature sensor (DS18B20 or DHT22)',
    detail:
      'Ambient air temperature near the plant. DS18B20 is more accurate; DHT22 also gives humidity.',
  },
  {
    name: '3–5 V mini pump (peristaltic or submersible)',
    detail: 'Peristaltic is cleaner and more controllable. Submersible is cheaper.',
  },
  {
    name: 'N-channel MOSFET or motor driver',
    detail:
      "The Pico's GPIOs cannot switch the pump current directly — needs a logic-level MOSFET or a small motor driver.",
  },
  {
    name: 'Silicone tubing + drip emitter',
    detail:
      'Food-grade silicone tubing from the reservoir to the soil, ending in a simple emitter or t-junction.',
  },
  {
    name: 'Water reservoir',
    detail: 'Any clean container. A wide opening makes refilling easier.',
  },
  {
    name: 'Power supply',
    detail:
      'USB-C for the Pico, with optional LiPo + charge controller for mobility. Battery voltage on VSYS can be reported via ADC channel 3 — useful for device-health telemetry.',
  },
]

function open(path: string) {
  router.push(path)
}
</script>

<template>
  <section class="about-view">
    <!-- Hero ----------------------------------------------------------- -->
    <header class="about-hero">
      <div>
        <h1 class="about-hero__title">DRIP</h1>
        <p class="about-hero__tagline">
          Distributed Raspberry Irrigation Platform — a self-hosted plant monitoring and watering
          system.
        </p>
      </div>
      <n-button tag="a" :href="REPO_URL" target="_blank" rel="noreferrer noopener">
        <template #icon>
          <n-icon><LogoGithub /></n-icon>
        </template>
        View on GitHub
      </n-button>
    </header>

    <!-- About ---------------------------------------------------------- -->
    <n-divider title-placement="left">About the project</n-divider>
    <p class="about-paragraph">
      DRIP keeps an eye on a small collection of houseplants and waters them when they need it —
      without sending any data off-device. Each plant gets a Raspberry Pi Pico W with a soil sensor,
      a temperature sensor, and a small pump. A Raspberry Pi runs the Ktor backend and serves this
      dashboard.
    </p>
    <p class="about-paragraph">Everything is locally controllable and runs in Docker.</p>

    <ul class="feature-list">
      <li v-for="f in features" :key="f.title" class="feature-list__item">
        <div class="feature-list__title">{{ f.title }}</div>
        <div class="feature-list__body">{{ f.body }}</div>
      </li>
    </ul>

    <!-- Guide to the app ---------------------------------------------- -->
    <n-divider title-placement="left">Guide to the app</n-divider>
    <p class="about-paragraph">
      The sidebar is split into the three main areas (Overview, Plants, Vitals) and two utility
      entries (Settings, About). Click any row below to jump straight to that tab.
    </p>
    <ul class="tab-guide">
      <li v-for="t in tabs" :key="t.name" class="tab-guide__row" @click="open(t.route)">
        <n-icon :size="18" class="tab-guide__icon">
          <component :is="t.icon" />
        </n-icon>
        <div class="tab-guide__text">
          <div class="tab-guide__name">{{ t.name }}</div>
          <div class="tab-guide__desc">{{ t.desc }}</div>
        </div>
      </li>
    </ul>

    <!-- Hardware setup ------------------------------------------------ -->
    <n-divider title-placement="left">Hardware setup</n-divider>

    <h3 class="about-subheading">Components</h3>
    <ul class="hardware-list">
      <li v-for="h in hardware" :key="h.name" class="hardware-list__item">
        <div class="hardware-list__name">{{ h.name }}</div>
        <div class="hardware-list__detail">{{ h.detail }}</div>
      </li>
    </ul>

    <h3 class="about-subheading">Wiring overview</h3>
    <pre class="code-block code-block--ascii"><code>+-----------------------+
|     Soil Sensor       |
|   Temperature Sensor  |
+---------+-------------+
          |
          v
+-------------------+
| Raspberry Pi Pico |   ←  flashes pump via MOSFET, reads sensors
|  (one per plant)  |       sends telemetry over Wi-Fi
+---------+---------+
          |
          v
+-------------------+
|   Ktor Backend    |   ←  runs on the Raspberry Pi hub
|   (Raspberry Pi)  |
+---------+---------+
          |
          v
+-------------------+
| Dashboard (this)  |
+-------------------+</code></pre>

    <h3 class="about-subheading">Flashing the Pico</h3>
    <ol class="about-steps">
      <li>
        Wire the soil sensor and temperature sensor to the Pico's ADC and GPIO pins per the
        project's pinout doc.
      </li>
      <li>
        Connect the pump through an N-channel MOSFET driven from a GPIO. Add a flyback diode across
        the pump.
      </li>
      <li>
        Hold the <strong>BOOTSEL</strong> button while plugging the Pico into USB. It mounts as a
        mass-storage device.
      </li>
      <li>
        Drop the firmware <code>.uf2</code> from the repo's <code>pico/</code> directory onto the
        mounted volume. The Pico reboots and starts running.
      </li>
      <li>
        Configure Wi-Fi and the backend URL in the firmware's config block (or via the provisioning
        serial command, depending on which firmware variant you use).
      </li>
    </ol>

    <!-- Software setup ------------------------------------------------ -->
    <n-divider title-placement="left">Software setup</n-divider>

    <h3 class="about-subheading">Backend (on the Raspberry Pi)</h3>
    <ol class="about-steps">
      <li>
        Update the Pi and install Java (the Ktor backend uses the Gradle wrapper, which expects JDK
        17+).
        <pre class="code-block"><code>sudo apt update
sudo apt upgrade
sudo apt install openjdk-17-jdk</code></pre>
      </li>
      <li>
        Clone the repository.
        <pre class="code-block"><code>git clone git@github.com:Foelli/drip-iot-project.git
cd drip-iot-project</code></pre>
      </li>
      <li>
        Start the backend.
        <pre class="code-block"><code>./gradlew run</code></pre>
      </li>
      <li>
        The backend exposes its HTTP API on the Pi's LAN address. Point the Pico firmware and this
        frontend at that address.
      </li>
    </ol>

    <h3 class="about-subheading">Frontend (this dashboard)</h3>
    <ol class="about-steps">
      <li>
        Install dependencies.
        <pre class="code-block"><code>npm install</code></pre>
      </li>
      <li>
        Run it in dev mode.
        <pre class="code-block"><code>npm run dev</code></pre>
      </li>
      <li>
        Or build a static bundle to serve from the Pi alongside the backend.
        <pre class="code-block"><code>npm run build</code></pre>
      </li>
    </ol>
  </section>
</template>

<style scoped>
.about-view {
  /* Keep the column readable rather than stretching across a wide screen. */
  max-width: 56rem;
}

/* --- Hero -------------------------------------------------------------- */
.about-hero {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: 1rem;
  margin-bottom: 1rem;
}
.about-hero__title {
  margin: 0;
  font-size: 2rem;
  letter-spacing: -0.01em;
}
.about-hero__tagline {
  margin: 0.25rem 0 0;
  color: var(--color-text-muted);
  max-width: 36rem;
}

/* --- Shared typography ------------------------------------------------- */
.about-paragraph {
  line-height: 1.55;
  margin: 0 0 0.75rem;
}
.about-paragraph--muted {
  color: var(--color-text-muted);
}
.about-subheading {
  font-size: 1rem;
  font-weight: 600;
  margin: 1.25rem 0 0.5rem;
}

/* --- Feature list ------------------------------------------------------ */
.feature-list {
  list-style: none;
  margin: 1rem 0 0;
  padding: 0;
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(16rem, 1fr));
  gap: 0.75rem;
}
.feature-list__item {
  border: 1px solid var(--color-border-subtle);
  border-radius: var(--radius-md);
  padding: 0.75rem 1rem;
}
.feature-list__title {
  font-weight: 600;
  font-size: 0.9rem;
  margin-bottom: 0.25rem;
}
.feature-list__body {
  font-size: 0.8125rem;
  color: var(--color-text-muted);
  line-height: 1.45;
}

/* --- Tab guide --------------------------------------------------------- */
.tab-guide {
  list-style: none;
  margin: 0.5rem 0 0;
  padding: 0;
}
.tab-guide__row {
  display: flex;
  align-items: flex-start;
  gap: 0.75rem;
  padding: 0.75rem 0.5rem;
  border-bottom: 1px solid var(--color-border-subtle);
  cursor: pointer;
  transition: background-color 120ms ease;
  border-radius: var(--radius-md);
}
.tab-guide__row:last-child {
  border-bottom: none;
}
.tab-guide__row:hover {
  background-color: var(--color-surface-hover);
}
.tab-guide__icon {
  flex: 0 0 auto;
  margin-top: 0.15rem;
  color: var(--color-text-muted);
}
.tab-guide__text {
  flex: 1;
  min-width: 0;
}
.tab-guide__name {
  font-weight: 600;
  margin-bottom: 0.15rem;
}
.tab-guide__desc {
  font-size: 0.8125rem;
  color: var(--color-text-muted);
  line-height: 1.45;
}

/* --- Hardware list ----------------------------------------------------- */
.hardware-list {
  list-style: none;
  margin: 0;
  padding: 0;
}
.hardware-list__item {
  padding: 0.5rem 0;
  border-bottom: 1px solid var(--color-border-subtle);
}
.hardware-list__item:last-child {
  border-bottom: none;
}
.hardware-list__name {
  font-weight: 500;
  font-size: 0.9rem;
}
.hardware-list__detail {
  font-size: 0.8125rem;
  color: var(--color-text-muted);
  margin-top: 0.15rem;
  line-height: 1.45;
}

/* --- Steps ------------------------------------------------------------- */
.about-steps {
  margin: 0 0 0.5rem;
  padding-left: 1.25rem;
}
.about-steps li {
  margin-bottom: 0.5rem;
  line-height: 1.5;
}

/* --- Code -------------------------------------------------------------- */
.code-block {
  background: var(--color-surface, rgba(0, 0, 0, 0.04));
  border: 1px solid var(--color-border-subtle);
  border-radius: var(--radius-md);
  padding: 0.625rem 0.875rem;
  margin: 0.5rem 0;
  font-family: ui-monospace, SFMono-Regular, Menlo, Consolas, monospace;
  font-size: 0.8125rem;
  line-height: 1.5;
  overflow-x: auto;
}
.code-block--ascii {
  /* Tightened so the ASCII diagram reads as a single visual block. */
  line-height: 1.25;
  white-space: pre;
}
code {
  font-family: ui-monospace, SFMono-Regular, Menlo, Consolas, monospace;
  font-size: 0.85em;
}
</style>
