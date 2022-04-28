// @ts-check
// Note: type annotations allow type checking and IDEs autocompletion

const darkCodeTheme  = require('prism-react-renderer/themes/dracula');

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
          remarkPlugins: [require('mdx-mermaid')]
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

  themeConfig:
    /** @type {import('@docusaurus/preset-classic').ThemeConfig} */
    ({
      colorMode: {
        defaultMode: 'light',
        respectPrefersColorScheme: true,
      },
      navbar: {
        hideOnScroll: true,
        logo: {
          alt: 'Doodle Logo',
          src: 'img/doodle.svg',
        },
        items: [
          {
            type    : 'doc',
            docId   : 'introduction',
            position: 'left',
            label   : 'Guide',
          },
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
        style: 'dark',
      },
      prism: {
        theme              : require('prism-react-renderer/themes/dracula'),
        darkTheme          : darkCodeTheme,
        additionalLanguages: ['kotlin', 'groovy'],
      },
      hideableSidebar: true,
      image: 'img/site_preview.png',
      metadata: [{name: 'og:image:width', content: '380'},{name: 'og:image:height', content: '78'}],
    }),
};

module.exports = config;