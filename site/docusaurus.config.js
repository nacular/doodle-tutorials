// @ts-check
// Note: type annotations allow type checking and IDEs autocompletion

import {themes as prismThemes} from 'prism-react-renderer';

/** @type {import('@docusaurus/types').Config} */
const config = {
  title                : 'Doodle Tutorials',
  tagline              : 'A pure Kotlin, UI framework',
  url                  : 'https://nacular.github.io',
  baseUrl              : '/doodle-tutorials/',
  trailingSlash        : false,
  onBrokenLinks        : 'throw',
  onBrokenMarkdownLinks: 'warn',
  favicon              : 'img/favicon.png',
  organizationName     : 'Nacular',
  projectName          : 'doodle',

  presets: [
    [
      '@docusaurus/preset-classic',
      /** @type {import('@docusaurus/preset-classic').Options} */
      ({
        docs: {
          sidebarPath  : require.resolve('./sidebars.js'),
          remarkPlugins: [require('mdx-mermaid')],
          breadcrumbs  : false,
        },
        theme: {
          customCss: require.resolve('./src/css/custom.css'),
        },
        gtag: {
          trackingID: 'G-KN5YH7BJYG',
          anonymizeIP: true,
        },
      }),
    ],
  ],

  markdown: {
    mermaid: true,
  },
  themes: ['@docusaurus/theme-mermaid'],

  scripts: [
    {
      src: 'https://unpkg.com/kotlin-playground@1',
    }
  ],

  themeConfig:
    /** @type {import('@docusaurus/preset-classic').ThemeConfig} */
    ({
      colorMode: {
        defaultMode: 'light',
        respectPrefersColorScheme: true,
      },
      navbar: {
        logo: {
          alt: 'Doodle Logo',
          src: 'img/doodle.svg',
        },
        items: [
          {
            href: 'https://github.com/nacular/doodle-tutorials',
            position: 'right',
            className: 'header-github-link',
            'aria-label': 'GitHub repository',
          },
          {
            href: 'https://kotlinlang.slack.com/messages/doodle',
            position: 'right',
            className: 'header-slack-link',
            'aria-label': 'Slack channel'
          },
        ],
      },
      footer: {
        copyright: `Copyright © ${new Date().getFullYear()} Nacular`,
      },
      prism: {
        theme              : prismThemes.github,
        darkTheme          : prismThemes.dracula,
        additionalLanguages: ['kotlin', 'groovy'],
      },
      mermaid: {
        theme: {light: 'default', dark: 'dark'},
      },
      image: 'img/site_preview.png',
      metadata: [{name: 'og:image:width', content: '380'},{name: 'og:image:height', content: '78'}],
    }),
};

module.exports = config;