/** @type {import('tailwindcss').Config} */
export default {
  content: ['./index.html', './src/**/*.{js,jsx}'],
  theme: {
    extend: {
      colors: {
        navy: {
          50: '#e8eaf0',
          100: '#c5cad8',
          200: '#9ea6be',
          300: '#7783a4',
          400: '#576791',
          500: '#364c7e',
          600: '#2d4271',
          700: '#233562',
          800: '#1b2838',
          900: '#111b2b',
          DEFAULT: '#1B2838',
        },
        gold: {
          50: '#fdf8ec',
          100: '#faeece',
          200: '#f5deaf',
          300: '#f0cd8f',
          400: '#ebbf72',
          500: '#D4A843',
          600: '#c49230',
          700: '#a87c28',
          800: '#8c6620',
          900: '#6f5018',
          DEFAULT: '#D4A843',
        },
      },
      fontFamily: {
        sans: ['Inter', 'ui-sans-serif', 'system-ui'],
      },
    },
  },
  plugins: [],
};
