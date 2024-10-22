<template>
  <div>
    <router-view />
  </div>
</template>

<script setup>
import {useDark, useToggle} from "@vueuse/core";

useDark({
  selector:'html',
  attribute:'class',
  valueDark:'dark',
  valueLight:'light'
})

useDark({
  onChanged(dark){ useToggle(dark) }
})

</script>

<style scoped>

</style>
