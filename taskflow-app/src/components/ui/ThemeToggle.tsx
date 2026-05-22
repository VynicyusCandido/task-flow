"use client";

import { useEffect, useState } from "react";
import { Moon, Sun } from "lucide-react";
import { cn } from "@/lib/utils";

export function ThemeToggle() {
  const [isDark, setIsDark] = useState(false);
  const [mounted, setMounted] = useState(false);

  useEffect(() => {
    setMounted(true);
    const isDarkMode = 
      localStorage.theme === 'dark' || 
      (!('theme' in localStorage) && window.matchMedia('(prefers-color-scheme: dark)').matches);
    
    setIsDark(isDarkMode);
    if (isDarkMode) {
      document.documentElement.classList.add('dark');
    } else {
      document.documentElement.classList.remove('dark');
    }
  }, []);

  const toggleTheme = () => {
    setIsDark(!isDark);
    if (!isDark) {
      document.documentElement.classList.add('dark');
      localStorage.theme = 'dark';
    } else {
      document.documentElement.classList.remove('dark');
      localStorage.theme = 'light';
    }
  };

  if (!mounted) return null;

  return (
    <div className="fixed bottom-6 right-6 z-50">
      <button
        onClick={toggleTheme}
        className={cn(
          "w-14 h-8 rounded-full p-1 transition-colors duration-300 relative focus:outline-none shadow-sm border border-border",
          isDark ? "bg-primary" : "bg-muted"
        )}
        aria-label="Alternar tema"
      >
        <div
          className={cn(
            "w-6 h-6 rounded-full bg-card shadow-md flex items-center justify-center transform transition-transform duration-300",
            isDark ? "translate-x-6" : "translate-x-0"
          )}
        >
          {isDark ? (
            <Moon className="w-4 h-4 text-primary" />
          ) : (
            <Sun className="w-4 h-4 text-primary" />
          )}
        </div>
      </button>
    </div>
  );
}
