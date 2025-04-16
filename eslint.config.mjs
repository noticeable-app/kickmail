import {configs as litLintConfigs} from 'eslint-plugin-lit';

import eslint from '@eslint/js';
import tseslint from 'typescript-eslint';

export default tseslint.config(
    { ignores: ['.prettierrc.js', 'dist/**', 'demo/**/', 'out/**', 'public/**', 'packages/**/*.js', 'sw.js'] },
    eslint.configs.recommended,
    ...tseslint.configs.recommended,
    litLintConfigs['flat/recommended'],
    {
        files: ['**/*.ts'],
        rules: {
            '@typescript-eslint/no-explicit-any': 'off',
            '@typescript-eslint/no-empty-object-type': 'off',
            '@typescript-eslint/no-unsafe-function-type': 'off',
            '@typescript-eslint/no-unused-vars': 'off',
            'no-control-regex': 'off',
            'no-misleading-character-class': 'off'
        },
    },
);