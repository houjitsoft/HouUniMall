export default defineAppConfig({
    ui: {
        colors: {
            primary: 'emerald',
            secondary: 'slate',
            neutral: 'slate'
        },
        icons: {
            arrowLeft: 'i-lucide-arrow-left',
            arrowRight: 'i-lucide-arrow-right',
            check: 'i-lucide-check',
            close: 'i-lucide-x',
            info: 'i-lucide-info',
            loading: 'i-lucide-loader-circle',
            menu: 'i-lucide-menu',
            search: 'i-lucide-search'
        },
        button: {
            slots: {
                base: 'rounded-[var(--radius-button)] font-semibold tracking-normal'
            },
            variants: {
                size: {
                    sm: {
                        base: 'min-h-[var(--control-height-sm)] px-3 text-[var(--font-size-sm)]'
                    },
                    md: {
                        base: 'min-h-[var(--control-height-md)] px-4 text-[var(--font-size-base)]'
                    },
                    lg: {
                        base: 'min-h-[var(--control-height-lg)] px-6 text-[var(--font-size-md)]'
                    }
                }
            },
            defaultVariants: {
                size: 'md'
            }
        },
        input: {
            slots: {
                base: 'rounded-[var(--radius-control)] tracking-normal'
            },
            variants: {
                size: {
                    sm: {
                        base: 'min-h-[var(--control-height-sm)] text-[var(--font-size-sm)]'
                    },
                    md: {
                        base: 'min-h-[var(--control-height-md)] text-[var(--font-size-base)]'
                    },
                    lg: {
                        base: 'min-h-[var(--control-height-lg)] text-[var(--font-size-md)]'
                    }
                }
            },
            defaultVariants: {
                size: 'md'
            }
        },
        textarea: {
            slots: {
                base: 'rounded-[var(--radius-control)] text-[var(--font-size-base)] leading-[var(--line-height-base)] tracking-normal'
            },
            defaultVariants: {
                size: 'md'
            }
        },
        select: {
            slots: {
                base: 'min-h-[var(--control-height-md)] rounded-[var(--radius-control)] text-[var(--font-size-base)] tracking-normal',
                content: 'rounded-[var(--radius-button)] shadow-[var(--shadow-popover)]'
            },
            defaultVariants: {
                size: 'md'
            }
        },
        dropdownMenu: {
            slots: {
                content: 'rounded-[var(--radius-button)] shadow-[var(--shadow-popover)]',
                item: 'min-h-[var(--control-height-sm)] rounded-[var(--radius-control)] text-[var(--font-size-base)]'
            },
            defaultVariants: {
                size: 'md'
            }
        },
        modal: {
            slots: {
                content: 'rounded-[var(--radius-dialog)] shadow-[var(--shadow-popover)]',
                title: 'text-[var(--font-size-md)] leading-[var(--line-height-md)] font-semibold',
                description: 'text-[var(--font-size-sm)] leading-[var(--line-height-sm)]'
            }
        },
        card: {
            slots: {
                root: 'rounded-[var(--radius-card)] shadow-[var(--shadow-sm)]',
                title: 'text-[var(--font-size-md)] leading-[var(--line-height-md)] font-semibold',
                description: 'text-[var(--font-size-sm)] leading-[var(--line-height-sm)]'
            }
        },
        pagination: {
            slots: {
                list: 'gap-1',
                label: 'min-w-[var(--control-height-sm)] text-center text-[var(--font-size-base)]'
            }
        }
    }
})
