<template>
  <div class="rich-text-editor">
    <div v-if="editor" class="border border-slate-200 dark:border-slate-600 rounded-lg overflow-hidden bg-white dark:bg-slate-800">
      <!-- Toolbar -->
      <div class="bg-slate-50 dark:bg-slate-700 border-b border-slate-200 dark:border-slate-600 p-2 flex flex-wrap gap-1">
        <button
          @click="editor.chain().focus().toggleBold().run()"
          :class="{ 
            'bg-violet-100 text-violet-700 dark:bg-violet-900/30 dark:text-violet-300': editor.isActive('bold') 
          }"
          class="p-2 rounded hover:bg-slate-100 dark:hover:bg-slate-600 transition-colors text-slate-700 dark:text-slate-300"
          type="button"
        >
          <UIcon name="i-lucide-bold" class="w-4 h-4" />
        </button>
        <button
          @click="editor.chain().focus().toggleItalic().run()"
          :class="{ 
            'bg-violet-100 text-violet-700 dark:bg-violet-900/30 dark:text-violet-300': editor.isActive('italic') 
          }"
          class="p-2 rounded hover:bg-slate-100 dark:hover:bg-slate-600 transition-colors text-slate-700 dark:text-slate-300"
          type="button"
        >
          <UIcon name="i-lucide-italic" class="w-4 h-4" />
        </button>
        <button
          @click="editor.chain().focus().toggleStrike().run()"
          :class="{ 
            'bg-violet-100 text-violet-700 dark:bg-violet-900/30 dark:text-violet-300': editor.isActive('strike') 
          }"
          class="p-2 rounded hover:bg-slate-100 dark:hover:bg-slate-600 transition-colors text-slate-700 dark:text-slate-300"
          type="button"
        >
          <UIcon name="i-lucide-strikethrough" class="w-4 h-4" />
        </button>

        <div class="w-px bg-slate-300 dark:bg-slate-500 mx-1"></div>

        <button
          @click="editor.chain().focus().toggleHeading({ level: 2 }).run()"
          :class="{ 
            'bg-violet-100 text-violet-700 dark:bg-violet-900/30 dark:text-violet-300': editor.isActive('heading', { level: 2 }) 
          }"
          class="p-2 rounded hover:bg-slate-100 dark:hover:bg-slate-600 transition-colors text-slate-700 dark:text-slate-300"
          type="button"
        >
          <UIcon name="i-lucide-heading-2" class="w-4 h-4" />
        </button>
        <button
          @click="editor.chain().focus().toggleHeading({ level: 3 }).run()"
          :class="{ 
            'bg-violet-100 text-violet-700 dark:bg-violet-900/30 dark:text-violet-300': editor.isActive('heading', { level: 3 }) 
          }"
          class="p-2 rounded hover:bg-slate-100 dark:hover:bg-slate-600 transition-colors text-slate-700 dark:text-slate-300"
          type="button"
        >
          <UIcon name="i-lucide-heading-3" class="w-4 h-4" />
        </button>

        <div class="w-px bg-slate-300 dark:bg-slate-500 mx-1"></div>

        <button
          @click="editor.chain().focus().toggleBulletList().run()"
          :class="{ 
            'bg-violet-100 text-violet-700 dark:bg-violet-900/30 dark:text-violet-300': editor.isActive('bulletList') 
          }"
          class="p-2 rounded hover:bg-slate-100 dark:hover:bg-slate-600 transition-colors text-slate-700 dark:text-slate-300"
          type="button"
        >
          <UIcon name="i-lucide-list" class="w-4 h-4" />
        </button>
        <button
          @click="editor.chain().focus().toggleOrderedList().run()"
          :class="{ 
            'bg-violet-100 text-violet-700 dark:bg-violet-900/30 dark:text-violet-300': editor.isActive('orderedList') 
          }"
          class="p-2 rounded hover:bg-slate-100 dark:hover:bg-slate-600 transition-colors text-slate-700 dark:text-slate-300"
          type="button"
        >
          <UIcon name="i-lucide-list-ordered" class="w-4 h-4" />
        </button>

        <div class="w-px bg-slate-300 dark:bg-slate-500 mx-1"></div>

        <button
          @click="editor.chain().focus().toggleBlockquote().run()"
          :class="{ 
            'bg-violet-100 text-violet-700 dark:bg-violet-900/30 dark:text-violet-300': editor.isActive('blockquote') 
          }"
          class="p-2 rounded hover:bg-slate-100 dark:hover:bg-slate-600 transition-colors text-slate-700 dark:text-slate-300"
          type="button"
        >
          <UIcon name="i-lucide-quote" class="w-4 h-4" />
        </button>
        <button
          @click="editor.chain().focus().toggleCodeBlock().run()"
          :class="{ 
            'bg-violet-100 text-violet-700 dark:bg-violet-900/30 dark:text-violet-300': editor.isActive('codeBlock') 
          }"
          class="p-2 rounded hover:bg-slate-100 dark:hover:bg-slate-600 transition-colors text-slate-700 dark:text-slate-300"
          type="button"
        >
          <UIcon name="i-lucide-code" class="w-4 h-4" />
        </button>

        <div class="w-px bg-slate-300 dark:bg-slate-500 mx-1"></div>

        <button
          @click="editor.chain().focus().setHorizontalRule().run()"
          class="p-2 rounded hover:bg-slate-100 dark:hover:bg-slate-600 transition-colors text-slate-700 dark:text-slate-300"
          type="button"
        >
          <UIcon name="i-lucide-minus" class="w-4 h-4" />
        </button>

        <div class="w-px bg-slate-300 dark:bg-slate-500 mx-1"></div>

        <button
          @click="editor.chain().focus().undo().run()"
          :disabled="!editor.can().undo()"
          class="p-2 rounded hover:bg-slate-100 dark:hover:bg-slate-600 transition-colors disabled:opacity-50 disabled:cursor-not-allowed text-slate-700 dark:text-slate-300"
          type="button"
        >
          <UIcon name="i-lucide-undo" class="w-4 h-4" />
        </button>
        <button
          @click="editor.chain().focus().redo().run()"
          :disabled="!editor.can().redo()"
          class="p-2 rounded hover:bg-slate-100 dark:hover:bg-slate-600 transition-colors disabled:opacity-50 disabled:cursor-not-allowed text-slate-700 dark:text-slate-300"
          type="button"
        >
          <UIcon name="i-lucide-redo" class="w-4 h-4" />
        </button>
      </div>

      <!-- Editor Content -->
      <EditorContent
        :editor="editor"
        class="prose prose-slate dark:prose-invert max-w-none p-4 min-h-[300px] focus:outline-none bg-white dark:bg-slate-800"
      />
    </div>
  </div>
</template>

<script setup lang="ts">
import { useEditor, EditorContent } from '@tiptap/vue-3'
import StarterKit from '@tiptap/starter-kit'

const props = defineProps<{
  modelValue: string
}>()

const emit = defineEmits<{
  (e: 'update:modelValue', value: string): void
}>()

const editor = useEditor({
  content: props.modelValue,
  extensions: [
    StarterKit.configure({
      heading: {
        levels: [2, 3]
      }
    })
  ],
  editorProps: {
    attributes: {
      class: 'prose prose-slate dark:prose-invert max-w-none focus:outline-none'
    }
  },
  onUpdate: ({ editor }) => {
    emit('update:modelValue', editor.getHTML())
  }
})

watch(() => props.modelValue, (value) => {
  if (editor.value) {
    const isSame = editor.value.getHTML() === value
    if (!isSame) {
      editor.value.commands.setContent(value, false)
    }
  }
})

onBeforeUnmount(() => {
  if (editor.value) {
    editor.value.destroy()
  }
})
</script>

<style>
.ProseMirror {
  min-height: 300px;
  outline: none;
  color: #334155;
}

.dark .ProseMirror {
  color: #e2e8f0;
}

.ProseMirror p.is-editor-empty:first-child::before {
  color: #adb5bd;
  content: attr(data-placeholder);
  float: left;
  height: 0;
  pointer-events: none;
}

.dark .ProseMirror p.is-editor-empty:first-child::before {
  color: #64748b;
}

.ProseMirror:focus {
  outline: none;
}

/* Basic styling for the editor content */
.ProseMirror h2 {
  font-size: 1.5em;
  font-weight: bold;
  margin-top: 1em;
  margin-bottom: 0.5em;
  color: #1e293b;
}

.dark .ProseMirror h2 {
  color: #f1f5f9;
}

.ProseMirror h3 {
  font-size: 1.25em;
  font-weight: bold;
  margin-top: 1em;
  margin-bottom: 0.5em;
  color: #1e293b;
}

.dark .ProseMirror h3 {
  color: #f1f5f9;
}

.ProseMirror ul,
.ProseMirror ol {
  padding-left: 1.5em;
  margin: 1em 0;
}

.ProseMirror li {
  color: #334155;
}

.dark .ProseMirror li {
  color: #e2e8f0;
}

.ProseMirror p {
  color: #334155;
  margin: 0.5em 0;
}

.dark .ProseMirror p {
  color: #e2e8f0;
}

.ProseMirror blockquote {
  border-left: 3px solid #cbd5e1;
  padding-left: 1em;
  margin: 1em 0;
  color: #64748b;
}

.dark .ProseMirror blockquote {
  border-left: 3px solid #475569;
  color: #94a3b8;
}

.ProseMirror pre {
  background: #f1f5f9;
  border-radius: 0.5em;
  padding: 1em;
  margin: 1em 0;
  overflow-x: auto;
  color: #334155;
}

.dark .ProseMirror pre {
  background: #1e293b;
  color: #e2e8f0;
}

.ProseMirror code {
  background: #f1f5f9;
  padding: 0.2em 0.4em;
  border-radius: 0.25em;
  font-size: 0.9em;
  color: #334155;
}

.dark .ProseMirror code {
  background: #1e293b;
  color: #e2e8f0;
}

.ProseMirror pre code {
  background: none;
  padding: 0;
}

.ProseMirror hr {
  border: none;
  border-top: 2px solid #e2e8f0;
  margin: 2em 0;
}

.dark .ProseMirror hr {
  border-top: 2px solid #475569;
}

/* Ensure proper text selection visibility in dark mode */
.dark .ProseMirror ::selection {
  background: #3b82f6;
  color: white;
}

.ProseMirror ::selection {
  background: #dbeafe;
  color: #1e40af;
}

/* Strong and emphasis styling */
.ProseMirror strong {
  color: #1e293b;
  font-weight: 600;
}

.dark .ProseMirror strong {
  color: #f8fafc;
}

.ProseMirror em {
  color: #475569;
}

.dark .ProseMirror em {
  color: #cbd5e1;
}

/* Strike-through styling */
.ProseMirror s {
  color: #64748b;
}

.dark .ProseMirror s {
  color: #94a3b8;
}
</style>
