import { create } from 'zustand';
import { persist } from 'zustand/middleware';

type Theme = 'light' | 'dark' | 'system';

interface ThemeStore {
    theme: Theme;
    resolvedTheme: 'light' | 'dark';
    setTheme: (theme: Theme) => void;
    toggleTheme: () => void;
}

const getSystemTheme = (): 'light' | 'dark' => {
    if (typeof window !== 'undefined') {
        return window.matchMedia('(prefers-color-scheme: dark)').matches ? 'dark' : 'light';
    }
    return 'light';
};

const useThemeStore = create<ThemeStore>()(
    persist(
        (set, get) => ({
            theme: 'system',
            resolvedTheme: getSystemTheme(),
            setTheme: (theme: Theme) => {
                const resolvedTheme = theme === 'system' ? getSystemTheme() : theme;
                set({ theme, resolvedTheme });
                applyTheme(resolvedTheme);
            },
            toggleTheme: () => {
                const currentTheme = get().resolvedTheme;
                const newTheme = currentTheme === 'light' ? 'dark' : 'light';
                set({ theme: newTheme, resolvedTheme: newTheme });
                applyTheme(newTheme);
            },
        }),
        {
            name: 'theme-storage',
            onRehydrateStorage: () => (state) => {
                if (state) {
                    const resolvedTheme = state.theme === 'system' ? getSystemTheme() : state.theme;
                    state.resolvedTheme = resolvedTheme;
                    applyTheme(resolvedTheme);
                }
            },
        }
    )
);

const applyTheme = (theme: 'light' | 'dark') => {
    const root = document.documentElement;
    root.setAttribute('data-theme', theme);

    if (theme === 'dark') {
        root.classList.add('dark');
        root.classList.remove('light');
    } else {
        root.classList.add('light');
        root.classList.remove('dark');
    }
};

// 시스템 테마 변경 감지
if (typeof window !== 'undefined') {
    window.matchMedia('(prefers-color-scheme: dark)').addEventListener('change', (e) => {
        const state = useThemeStore.getState();
        if (state.theme === 'system') {
            const newTheme = e.matches ? 'dark' : 'light';
            useThemeStore.setState({ resolvedTheme: newTheme });
            applyTheme(newTheme);
        }
    });
}

export default useThemeStore;
