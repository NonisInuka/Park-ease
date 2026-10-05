(function () {
    'use strict';

    var STORAGE_KEY = 'vehicleparking-theme';
    var root = document.documentElement;

    function getPreferredTheme() {
        try {
            var saved = localStorage.getItem(STORAGE_KEY);
            if (saved === 'light' || saved === 'dark') {
                return saved;
            }
        } catch (e) {
            // Local storage may be unavailable in restricted browser modes.
        }

        if (window.matchMedia && window.matchMedia('(prefers-color-scheme: dark)').matches) {
            return 'dark';
        }
        return 'light';
    }

    function applyTheme(theme) {
        var safeTheme = theme === 'dark' ? 'dark' : 'light';
        root.setAttribute('data-theme', safeTheme);
        root.style.colorScheme = safeTheme;
        updateControls(safeTheme);
    }

    function updateControls(theme) {
        var isDark = theme === 'dark';
        document.querySelectorAll('[data-theme-toggle]').forEach(function (button) {
            button.setAttribute('aria-pressed', String(isDark));
            button.setAttribute('aria-label', isDark ? 'Switch to light mode' : 'Switch to dark mode');
            button.setAttribute('title', isDark ? 'Switch to light mode' : 'Switch to dark mode');

            var icon = button.querySelector('[data-theme-icon]');
            if (icon) {
                icon.textContent = isDark ? '☀' : '☾';
            }

            var label = button.querySelector('[data-theme-label]');
            if (label) {
                label.textContent = isDark ? 'Light' : 'Dark';
            }
        });
    }

    function saveTheme(theme) {
        try {
            localStorage.setItem(STORAGE_KEY, theme);
        } catch (e) {
            // The active theme still works for the current page if storage is blocked.
        }
    }

    // Apply the theme immediately when this file is loaded in <head>.
    applyTheme(getPreferredTheme());

    document.addEventListener('DOMContentLoaded', function () {
        updateControls(root.getAttribute('data-theme') || 'light');

        document.querySelectorAll('[data-theme-toggle]').forEach(function (button) {
            button.addEventListener('click', function () {
                var nextTheme = root.getAttribute('data-theme') === 'dark' ? 'light' : 'dark';
                saveTheme(nextTheme);
                applyTheme(nextTheme);
            });
        });
    });
})();
