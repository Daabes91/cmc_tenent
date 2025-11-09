export default defineAppConfig({
  ui: {
    strategy: "override",
    primary: "blue",
    gray: "slate",
    formGroup: {
      label: {
        wrapper: "flex content-center items-center justify-between",
        base: "block font-semibold text-slate-900 dark:text-slate-200",
        required: "after:content-['*'] after:ms-0.5 after:text-red-500 dark:after:text-red-400"
      },
      description: "text-slate-500 dark:text-slate-300",
      hint: "text-slate-500 dark:text-slate-300",
      help: "mt-2 text-slate-500 dark:text-slate-300",
      error: "mt-2 text-red-600 dark:text-red-400",
      default: {
        size: "sm"
      }
    },
    button: {
      rounded: "rounded-xl",
      base: "inline-flex items-center justify-center gap-2 font-medium transition-all duration-200 focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-primary-500 focus-visible:ring-offset-2 dark:focus-visible:ring-offset-0 disabled:opacity-50 disabled:cursor-not-allowed disabled:pointer-events-none",
      size: {
        xs: "h-8 px-3 text-xs",
        sm: "h-9 px-3.5 text-sm",
        md: "h-10 px-4 text-sm",
        lg: "h-11 px-5 text-base"
      },
      variant: {
        solid: "bg-primary-600 text-white hover:bg-primary-700 shadow-sm hover:shadow-md active:scale-[0.98] dark:bg-primary-500 dark:hover:bg-primary-400",
        ghost: "border border-transparent text-primary-600 hover:bg-primary-50/80 active:bg-primary-100 dark:text-primary-200 dark:hover:bg-white/10 dark:active:bg-white/5",
        outline: "border border-slate-300 bg-white text-slate-700 hover:border-primary-400 hover:text-primary-700 hover:bg-primary-50/30 shadow-sm dark:border-slate-700 dark:bg-slate-900 dark:text-slate-100 dark:hover:border-primary-400 dark:hover:text-primary-100 dark:hover:bg-white/5 dark:shadow-none",
        soft: "bg-primary-50 text-primary-700 hover:bg-primary-100 active:bg-primary-200 dark:bg-primary-500/15 dark:text-primary-100 dark:hover:bg-primary-500/25"
      }
    },
    input: {
      rounded: "rounded-xl",
      base: "block w-full text-sm text-slate-900 transition-all duration-200 bg-white border border-slate-300 focus:border-primary-500 focus:ring-2 focus:ring-primary-200 placeholder:text-slate-400 disabled:bg-slate-50 disabled:cursor-not-allowed dark:bg-slate-900 dark:border-slate-700 dark:text-slate-100 dark:placeholder:text-slate-400 dark:focus:border-blue-400 dark:focus:ring-blue-500/30 dark:disabled:bg-slate-800",
      size: {
        sm: "h-9 pl-10 pr-3 text-sm",
        md: "h-10 pl-10 pr-3.5 text-sm",
        lg: "h-11 pl-12 pr-4 text-base"
      }
    },
    textarea: {
      rounded: "rounded-xl",
      base: "block w-full text-sm text-slate-900 transition-all duration-200 bg-white border border-slate-300 focus:border-primary-500 focus:ring-2 focus:ring-primary-200 placeholder:text-slate-400 disabled:bg-slate-50 disabled:cursor-not-allowed dark:bg-slate-900 dark:border-slate-700 dark:text-slate-100 dark:placeholder:text-slate-400 dark:focus:border-blue-400 dark:focus:ring-blue-500/30 dark:disabled:bg-slate-800",
      size: {
        sm: "px-3 py-2 text-sm",
        md: "px-3.5 py-2.5 text-sm",
        lg: "px-4 py-3 text-base"
      }
    },
    select: {
      rounded: "rounded-xl",
      base: "relative block w-full disabled:cursor-not-allowed disabled:opacity-75 focus:outline-none border-0 form-select text-sm shadow-sm bg-white dark:bg-gray-900 ring-1 ring-inset ring-gray-300 dark:ring-gray-700 focus:ring-2 focus:ring-primary-500 dark:focus:ring-primary-400 text-gray-900 dark:text-gray-100",
      size: {
        sm: "px-2.5 py-1.5 pe-10 h-9",
        md: "px-3 py-2 pe-10 h-10",
        lg: "px-3.5 py-2.5 pe-12 h-11"
      }
    },
    card: {
      rounded: "rounded-xl",
      shadow: "shadow-sm border border-slate-200/60 dark:border-slate-800",
      base: "bg-white overflow-hidden dark:bg-slate-900",
      header: {
        base: "px-6 py-4 border-b border-slate-200/60 bg-slate-50/30 dark:border-slate-800 dark:bg-slate-900/60"
      },
      body: {
        base: "px-6 py-5 text-slate-700 dark:text-slate-300"
      }
    },
    table: {
      rounded: "rounded-lg",
      divide: "divide-slate-100 dark:divide-slate-800",
      th: {
        base: "text-xs font-semibold uppercase tracking-wide text-slate-700 bg-slate-50 dark:text-slate-300 dark:bg-slate-900/70",
        padding: "px-4 py-3.5"
      },
      td: {
        base: "text-sm text-slate-700 dark:text-slate-300",
        padding: "px-4 py-4"
      },
      tr: {
        base: "hover:bg-slate-50/60 transition-colors duration-150 dark:hover:bg-slate-800/60"
      }
    },
    dropdown: {
      rounded: "rounded-lg",
      shadow: "shadow-lg border border-slate-200/80 dark:shadow-xl dark:border-slate-800",
      background: "bg-white dark:bg-slate-900"
    },
    selectMenu: {
      rounded: "rounded-xl",
      trigger: "h-10 pr-10 pl-3.5 text-sm bg-white border border-slate-300 text-slate-900 transition-all duration-200 focus:border-primary-500 focus:ring-2 focus:ring-primary-200 dark:bg-slate-900 dark:border-slate-700 dark:text-slate-100 dark:focus:border-blue-400 dark:focus:ring-blue-500/30",
      icon: {
        trailing: {
          wrapper: "absolute inset-y-0 end-3 flex items-center text-slate-400 dark:text-slate-500"
        }
      },
      option: {
        base: "px-3.5 py-2.5 text-sm transition-colors duration-150 dark:text-slate-200",
        active: "bg-primary-50 text-primary-700 dark:bg-white/10 dark:text-white",
        selected: "bg-primary-100 text-primary-800 font-medium dark:bg-primary-500/20 dark:text-primary-100"
      }
    },
    badge: {
      rounded: "rounded-md",
      size: {
        sm: "px-2 py-0.5 text-xs",
        md: "px-2.5 py-1 text-xs font-medium"
      }
    },
    avatar: {
      rounded: "rounded-lg"
    },
    notifications: {
      wrapper: "fixed top-0 inset-x-0 flex flex-col items-end justify-start z-[100] pointer-events-none p-4 sm:p-6",
      position: "top-0 end-0",
      width: "w-full sm:w-[420px]",
      container: "space-y-3 overflow-y-auto pointer-events-auto max-h-screen"
    },
    notification: {
      wrapper: "max-w-[420px] pointer-events-auto transform transition-all duration-300 ease-out animate-in fade-in slide-in-from-right-full",
      container: "relative overflow-hidden",
      inner: "w-full flex gap-3.5 items-start",
      background: "bg-white/95 dark:bg-slate-900/90 backdrop-blur-xl",
      shadow: "shadow-xl shadow-slate-900/15 dark:shadow-black/40",
      rounded: "rounded-2xl",
      padding: "p-4 sm:p-5",
      gap: "gap-3.5",
      ring: "ring-1 ring-slate-200/40 dark:ring-slate-700/50",
      title: "text-sm font-bold text-slate-900 dark:text-white leading-snug",
      description: "mt-1.5 text-sm leading-relaxed text-slate-600 dark:text-slate-300",
      icon: {
        base: "flex-shrink-0 w-5 h-5",
        color: "text-{color}-600 dark:text-{color}-500"
      },
      progress: {
        base: "absolute bottom-0 start-0 end-0 h-1.5",
        background: "bg-gradient-to-r from-blue-500 via-blue-600 to-blue-500 dark:from-blue-400 dark:via-blue-500 dark:to-blue-400"
      },
      default: {
        color: "blue",
        timeout: 5000,
        closeButton: {
          icon: "i-lucide-x",
          color: "gray",
          variant: "ghost",
          size: "sm",
          class: "!p-1.5 hover:bg-slate-100 dark:hover:bg-slate-800 rounded-lg transition-colors",
          padded: false
        },
        actionButton: {
          size: "sm",
          color: "blue",
          variant: "soft",
          class: "mt-3"
        }
      }
    }
  }
});
