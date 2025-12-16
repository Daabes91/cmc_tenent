<template>
  <ProductForm :product-id="Number(route.params.id)" mode="edit" />
</template>

<script setup lang="ts">
import ProductForm from "../new.vue";

definePageMeta({
  requiresAuth: true,
  ssr: false,
});

const route = useRoute();
</script>
