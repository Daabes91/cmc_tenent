<template>
  <div class="relative" style="height: 300px;">
    <Bar
      v-if="chartData"
      :data="chartData"
      :options="chartOptions"
    />
    <div v-else class="flex items-center justify-center h-full text-gray-500 dark:text-gray-400">
      No data available
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { Bar } from 'vue-chartjs'
import {
  Chart as ChartJS,
  CategoryScale,
  LinearScale,
  BarElement,
  Title,
  Tooltip,
  Legend
} from 'chart.js'

// Register Chart.js components
ChartJS.register(
  CategoryScale,
  LinearScale,
  BarElement,
  Title,
  Tooltip,
  Legend
)

interface UsageMetricsData {
  labels: string[]
  users: number[]
  appointments: number[]
  storage: number[]
}

const props = defineProps<{
  data: UsageMetricsData | null
}>()

const chartData = computed(() => {
  if (!props.data) return null

  return {
    labels: props.data.labels,
    datasets: [
      {
        label: 'Users',
        data: props.data.users,
        backgroundColor: '#3B82F6',
        borderRadius: 6,
        maxBarThickness: 60
      },
      {
        label: 'Appointments',
        data: props.data.appointments,
        backgroundColor: '#10B981',
        borderRadius: 6,
        maxBarThickness: 60
      },
      {
        label: 'Storage (MB)',
        data: props.data.storage,
        backgroundColor: '#F59E0B',
        borderRadius: 6,
        maxBarThickness: 60
      }
    ]
  }
})

const chartOptions = computed(() => ({
  responsive: true,
  maintainAspectRatio: false,
  plugins: {
    legend: {
      display: true,
      position: 'top' as const,
      labels: {
        usePointStyle: true,
        padding: 15,
        font: {
          size: 12
        }
      }
    },
    tooltip: {
      mode: 'index' as const,
      intersect: false,
      backgroundColor: 'rgba(0, 0, 0, 0.8)',
      padding: 12,
      cornerRadius: 8,
      titleFont: {
        size: 14,
        weight: 'bold' as const
      },
      bodyFont: {
        size: 13
      }
    }
  },
  scales: {
    x: {
      grid: {
        display: false
      },
      ticks: {
        font: {
          size: 11
        }
      }
    },
    y: {
      beginAtZero: true,
      grid: {
        color: 'rgba(0, 0, 0, 0.05)'
      },
      ticks: {
        font: {
          size: 11
        },
        precision: 0
      }
    }
  },
  interaction: {
    mode: 'nearest' as const,
    axis: 'x' as const,
    intersect: false
  }
}))
</script>
