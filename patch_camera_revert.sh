for file in app/src/main/java/com/example/ui/ExperimentScreen.kt app/src/main/java/com/example/ui/FocusExperimentScreen.kt app/src/main/java/com/example/ui/LensExperimentScreen.kt app/src/main/java/com/example/ui/V4ExperimentScreen.kt; do
  sed -i 's/try { provider.unbindAll() } catch(e: Exception) {}/try { imageAnalysisRef?.let { provider.unbind(it) }; previewRef?.let { provider.unbind(it) } } catch(e: Exception) {}/g' "$file"
done
